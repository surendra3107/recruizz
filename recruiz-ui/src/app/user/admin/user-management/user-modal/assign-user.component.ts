import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators'
import Swal from 'sweetalert2';

//services
import { UserService } from "../userService/user.service";;
@Component({
  selector: 'app-my-dialog',
  templateUrl: './assign-user.component.html',
  styleUrls: ['../user-management/user-management.component.css']
})
export class AssignUserDialog implements OnInit {


  constructor(
    public thisDialogRef: MatDialogRef<AssignUserDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _user: UserService
  ) { }

  error = '';
  globalData: any;
  hrUsers: any;

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    // for department head
    if (this.dataOption.deptType) {
      this.loadDepartmentHeadUsers();
    } else {
      //load user
      this.loadUsers();
    }

  }

  //load users
  public loadUsers() {
    this._user.getHrUsers().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.hrUsers = [];
        this.hrUsers = response.data;
        this.hrUsers.forEach((item: any, index: any) => {
          if (item.email === this.dataOption.email) {
            this.hrUsers.splice(index, 1);
          }
        })
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  };

  //delete user
  public deleteUser(userEmail: string) {
    var found = false;
    var permissionArray = ['Delete User', 'Super Admin', 'Global Delete'];
    for (var i = 0; i < permissionArray.length; i++) {
      if (this.globalData.permissions.indexOf(permissionArray[i]) > -1) {
        found = true;
        break;
      }
    }

    if (found) {
      this._user.deleteUser(this.dataOption.email, userEmail).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          Swal.fire({
            title: "Deleted",
            text: 'User has been deleted successfully.',
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
          this.hrUsers.splice(this.dataOption.index, 1);
          this.thisDialogRef.close(true);
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    } else {
      Swal.fire({
        title: 'Failure',
        text: "You don't have permission to delete this user.",
        type: "warning",
        showConfirmButton: true
      });
    }
  }

  //load department head user
  public loadDepartmentHeadUsers() {
    this._user.getDeptHeadUsers(this.dataOption.email).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.hrUsers = [];
        this.hrUsers = response.data;
        this.hrUsers.forEach((item: any, index: any) => {
          if (item.email === this.dataOption.email) {
            this.hrUsers.splice(index, 1);
          }
        })
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //delete department head
  public deleteDeptHead(replaceUser: any) {
    var found = false;
    var permissionArray = ['Delete User', 'Super Admin', 'Global Delete'];
    for (var i = 0; i < permissionArray.length; i++) {
      if (this.globalData.permissions.indexOf(permissionArray[i]) > -1) {
        found = true;
        break;
      }
    }
    if (found) {
      this._user.deleteDeptHead(this.dataOption.email, replaceUser).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          Swal.fire({
            title: "Deleted",
            text: 'User has been deleted successfully.',
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
          this.hrUsers.splice(this.dataOption.index, 1);
          this.thisDialogRef.close(true);
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    }

  }

  onCloseConfirm() {
    this.thisDialogRef.close(true);
  }

  onCloseCancel() {
    this.thisDialogRef.close();
  }

}
