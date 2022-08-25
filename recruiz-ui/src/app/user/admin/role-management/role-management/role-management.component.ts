import { Component, OnInit } from '@angular/core';

import { Router, ActivatedRoute } from '@angular/router';
import { first } from "rxjs/operators";
import Swal from 'sweetalert2';
import { MatDialog } from '@angular/material/dialog';

//services
import { RoleService } from '../roleService/role.service';

@Component({
  selector: 'app-role-management',
  templateUrl: './role-management.component.html',
  styleUrls: ['./role-management.component.css']
})
export class RoleManagementComponent implements OnInit {

  constructor(
    private _role: RoleService,
    private router: Router,
    public dialog: MatDialog

  ) { }

  error: any = '';
  userRoles: any;
  globalData: any;
  roleName: string;

  ngOnInit() {
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    this.loadUsersRole();
  }

  //get role user list
  public loadUsersRole() {
    this._role.getAllusersRole().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.userRoles = [];
        var userRoleData = response.data;
        for (var key in userRoleData) {
          if (key !== 'Normal') {
            this.userRoles.push(userRoleData[key]);
          }
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //add roles
  public submitRole() {
    var found = false;
    var permissionArray = ['Add Edit Roles', 'Super Admin'];
    for (var i = 0; i < permissionArray.length; i++) {
      if (this.globalData.permissions.indexOf(permissionArray[i]) > -1) {
        found = true;
        break;
      }
    }
    if (found) {
      this._role.addNewRoles(this.roleName).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          Swal.fire({
            title: "Added",
            text: "User role added successfully",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
          this.loadUsersRole();
        }
      })
    } else {
      Swal.fire({
        title: "Warning",
        text: "You dont have permission to add roles.",
        type: "warning",
        showConfirmButton: true
      });
    }

  }
}
