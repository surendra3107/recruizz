import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators'
import Swal from 'sweetalert2';

//services
import { GlobalService } from "./../../../globalServices/global.service";

@Component({
  selector: 'app-my-dialog',
  templateUrl: './add-edit-interviewer.component.html',
  styleUrls: ['../user-management/user-management.component.css']
})
export class AddEditInterviewerDialog implements OnInit {

  constructor(
    public thisDialogRef: MatDialogRef<AddEditInterviewerDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _global: GlobalService
  ) { }

  error = '';
  globalData: any;
  InterviewerName: string;
  InterviewerEmail: string;
  InterviewerPhone: string;
  btnText: string = 'Add Interviewer';
  headerText: string = 'Add interviewer';
  isEmailDisabled: boolean = false;
  emailExists: boolean = false;

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    if (this.dataOption.intData) {
      this.InterviewerName = this.dataOption.intData.name;
      this.InterviewerEmail = this.dataOption.intData.email;
      this.InterviewerPhone = this.dataOption.intData.mobile;
      this.headerText = 'Update interviewer';
      this.btnText = 'Update Interviewer';
      this.isEmailDisabled = true;
    }
  }

  //add update interviwer
  public addUpdateInterviewer() {
    if (!this.InterviewerName || !this.InterviewerEmail) {
      Swal.fire({
        title: "Alert",
        text: "Please make sure interviewer name and interviewer email has valid input.",
        type: "warning",
        showConfirmButton: true
      });
      return true;
    }
    //validate email
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    var Isvalid = re.test(String(this.InterviewerEmail).toLowerCase());
    if (!Isvalid) {
      Swal.fire({
        title: "Alert",
        text: "Email id is not in proper format.",
        type: "warning",
        showConfirmButton: true
      });
      return true;
    }

    var formData = {
      "name": this.InterviewerName,
      "email": this.InterviewerEmail,
      "mobile": this.InterviewerPhone
    }

    if (this.isEmailDisabled) {
      this._global.updateSelectedInterviewerToPosition(formData, this.InterviewerEmail).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          Swal.fire({
            title: "Success",
            text: "Interviewer has been added / updated successfully.",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
          this.thisDialogRef.close(true);
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    } else {
      this._global.addInterviewerGlobal([formData]).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          Swal.fire({
            title: "Success",
            text: "Interviewer has been added / updated successfully.",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
          this.thisDialogRef.close(true);
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    }

  }

  //validate duplicate email
  public validateExistngEmail() {
    this._global.validateDuplicateEmail(this.InterviewerEmail).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        if (response.data) {
          Swal.fire({
            title: "Alert",
            text: "Interviewer email already exists.",
            type: "warning",
            showConfirmButton: true
          });
          this.emailExists = true;
        } else {
          this.emailExists = false;
        }

      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  onCloseConfirm() {
    this.thisDialogRef.close();
  }

  onCloseCancel() {
    this.thisDialogRef.close();
  }

}
