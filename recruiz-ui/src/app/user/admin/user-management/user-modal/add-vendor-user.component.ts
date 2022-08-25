import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators'
import Swal from 'sweetalert2';

//services
import { UserService } from '../userService/user.service';

@Component({
  selector: 'app-my-dialog',
  templateUrl: './add-vendor-user.component.html',
  styleUrls: ['../user-management/user-management.component.css']
})
export class AddVendorUserDialog implements OnInit {

  constructor(
    public thisDialogRef: MatDialogRef<AddVendorUserDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _user: UserService
  ) { }

  error = '';
  globalData: any;
  data: any = {
    inviteUsers: []
  }

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
    this.data = {
      inviteUsers: [{ userName: "", email: "", roleId: "" }]
    };
  }

  //add more user
  public addUserRows() {
    var userName = { userName: "", email: "", roleId: "" };
    this.data.inviteUsers.push(userName);
  }

  //delete input fileds
  public deleteRow(event: any, userName: any) {
    var index = this.data.inviteUsers.indexOf(userName);
    if (event.which == 1) {
      this.data.inviteUsers.splice(index, 1);
    }
  }

  //add vendor user
  public inviteVendorUser() {
    this._user.inviteVendorUser(this.data, this.dataOption.vendorId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        var existingEmails = response.data.ExistingEmail;
        if (existingEmails.length > 0) {
          existingEmails.forEach((item: any) => {
            Swal.fire({
              title: "Warning",
              html: "Email already exists <b>" + item + "</b>",
              type: "warning",
              showConfirmButton: true
            }).then((results) =>{
              if (results.value) {
                this.thisDialogRef.close(true);
                }
            })
          })
        } else {
          Swal.fire({
            title: "Success",
            text: "Invite sent to vandor user",
            type: "warning",
            timer: 2000,
            showConfirmButton: false
          });
          this.thisDialogRef.close(true);
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
