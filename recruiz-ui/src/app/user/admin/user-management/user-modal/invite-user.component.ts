import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators'
import Swal from 'sweetalert2';

//services
import { UserService } from "../userService/user.service";;
@Component({
  selector: 'app-my-dialog',
  templateUrl: './invite-user.component.html',
  styleUrls: ['../user-management/user-management.component.css']
})
export class InviteUserDialog implements OnInit {


  constructor(
    public thisDialogRef: MatDialogRef<InviteUserDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _user: UserService
  ) { }

  error = '';
  globalData: any;
  data: any = {
    inviteUsers: []
  }
  user: any;
  joinedCount: any;
  pendingCount: any;
  allusers: any;
  allroles: any;
  defaultSelectedRoles: any;
  isSubmitted: any;
  emailTags: any;

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    this.data = {
      inviteUsers: [{ userName: "", email: "", roleId: "" }]
    };

    //load user
    this.loadUsers();

  }

  //load users
  public loadUsers() {
    this._user.getAllusers(this.dataOption.addType).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.user = response.data.length;
        this.joinedCount = response.data.All_User.joined_count;
        this.pendingCount = response.data.All_User.pending_count;
        this.allusers = response.data.All_User.gridData;
        this.allroles = response.data.All_Roles.gridData;
        this.defaultSelectedRoles = this.allroles[2].id;
        for (const [key, value] of Object.entries(this.data.inviteUsers)) {
          value['roleId'] = this.defaultSelectedRoles
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  };

  //push more user input fields
  public addMoreUser(index: any) {
    var userName = { userName: "", email: "", roleId: "" };
    if (this.data.inviteUsers.length + 1) {
      this.data.inviteUsers.splice(index + 1, 0, userName);
      for (const [key, value] of Object.entries(this.data.inviteUsers)) {
        value['roleId'] = this.defaultSelectedRoles
      }
    }
  }

  //delete input fileds
  public deleteRow(event: any, userName: any) {
    var index = this.data.inviteUsers.indexOf(userName);
    if (event.which == 1) {
      this.data.inviteUsers.splice(index, 1);
    }
  }

  //invite users
  public getInviteUser() {
    var emailUser: boolean = true;
    var found: boolean = false;
    var permissionArray = ['Add Edit USER', 'Super Admin', 'Admin Setting'];
    for (var i = 0; i < permissionArray.length; i++) {
      if (this.globalData.permissions.indexOf(permissionArray[i]) > -1) {
        found = true;
        break;
      }
    }
    if (found) {
      if (emailUser === true) {
        this._user.inviteUser(this.data, this.dataOption.addType).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            var existingUserList = response.data.ExistingEmail;
            var invalidEmailList = response.data.InvalidEmails;

            var invalidEmailMessage = ('Specified email-id domain is not allowed');
            var existingEmailMessage = ('User is already part of your organization');

            var responseMessage = "";
            if (response.data.ExistingEmail.length !== 0) {
              responseMessage = existingEmailMessage;
            }
            if (response.data.InvalidEmails.length !== 0) {
              responseMessage = responseMessage.concat(invalidEmailMessage);
            }

            if (responseMessage !== "") {
              Swal.fire({
                title: "Warning",
                text: responseMessage,
                type: "warning",
                showConfirmButton: true
              });
              this.isSubmitted = true;
              this.emailTags = [];
              this.emailTags = invalidEmailList;
            } else {
              Swal.fire({
                title: "Success",
                text: 'User invite successful. Activation link has been sent to registered email.',
                type: "success",
                timer: 2000,
                showConfirmButton: false
              });
              this.isSubmitted = true;
              this.data = {
                inviteUsers: [{ userName: "", email: "" }]
              };
              this.thisDialogRef.close(response.success);
            }
          } else if (response.data === 'User is already part of your organization') {
            Swal.fire({
              title: "Warning",
              text: responseMessage,
              type: "warning",
              showConfirmButton: true
            });
          } else if (response.success === true && response.data.ExistingEmail !== 0) {
            Swal.fire({
              title: "Warning",
              text: "User is already part of your organization",
              type: "warning",
              showConfirmButton: true
            });
            this.isSubmitted = true;
            this.emailTags = [];
          } else {
            Swal.fire({
              title: "Warning",
              text: responseMessage,
              type: "warning",
              showConfirmButton: true
            });
            Swal.fire({
              title: "Warning",
              text: "Email, username and role can not be empty.",
              type: "warning",
              showConfirmButton: true
            });
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    }
  }

  onCloseConfirm() {
    this.thisDialogRef.close(true);
  }

  onCloseCancel() {
    this.thisDialogRef.close();
  }

}
