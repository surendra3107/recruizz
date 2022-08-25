import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators'
import Swal from 'sweetalert2';

//services
import { GlobalService } from "../../../globalServices/global.service";
import { UserService } from '../userService/user.service';

@Component({
  selector: 'app-my-dialog',
  templateUrl: './add-edit-vendor.component.html',
  styleUrls: ['../user-management/user-management.component.css']
})
export class AddEditVendorDialog implements OnInit {

  constructor(
    public thisDialogRef: MatDialogRef<AddEditVendorDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _user: UserService,
    private _global :GlobalService
  ) { }

  error = '';
  globalData: any;
  vendorName: string;
  vendorEmail: string;
  vendorPhone: string;
  recruitmentFirm: string;
  btnText: string = 'Add Vendor';
  headerText: string = 'Add Vendor';
  isEmailDisabled: boolean = false;
  emailExists: boolean = false;

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    if (this.dataOption.vendorData) {
      this.vendorName = this.dataOption.vendorData.name;
      this.vendorEmail = this.dataOption.vendorData.email;
      this.vendorPhone = this.dataOption.vendorData.phone;
      this.recruitmentFirm = this.dataOption.vendorData.type;
      this.headerText = 'Update Vendor';
      this.btnText = 'Update Vendor';
      this.isEmailDisabled = true;
    }
  }

  //add update interviwer
  public addUpdateVendor() {
    if (!this.vendorName || !this.vendorEmail || !this.recruitmentFirm) {
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
    var Isvalid = re.test(String(this.vendorEmail).toLowerCase());
    if (!Isvalid) {
      Swal.fire({
        title: "Alert",
        text: "Email id is not in proper format.",
        type: "warning",
        showConfirmButton: true
      });
      return true;
    }

    if (this.isEmailDisabled) {
      var formDataEdit = {
        "id": this.dataOption.vendorData.id,
        "name": this.vendorName,
        "phone": this.vendorPhone,
        "email": this.vendorEmail,
        "type": this.recruitmentFirm,
      }
      this._user.updateVendor(formDataEdit).pipe(first()).subscribe((response: any) => {
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
      var formData = {
        "name": this.vendorName,
        "phone": this.vendorPhone,
        "email": this.vendorEmail,
        "type": this.recruitmentFirm,
      }
      this._user.addVendors(formData).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          Swal.fire({
            title: "Success",
            text: "Vendor has been added / updated successfully.",
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
    this._global.validateGlobalUserEmail(this.vendorEmail, 'vendor').pipe(first()).subscribe((response: any) => {
      if (response.success) {
        if (response.data.userExits === "true") {
          Swal.fire({
            title: "Alert",
            text: "Email already exists.",
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
