import { Component, OnInit } from '@angular/core';

import { Router, ActivatedRoute } from '@angular/router';
import { first } from "rxjs/operators";
import Swal from 'sweetalert2';
import { MatDialog } from '@angular/material/dialog';

//services
import { PermissionService } from '../permissionService/permission.service';

@Component({
  selector: 'app-permission-management',
  templateUrl: './permission-management.component.html',
  styleUrls: ['./permission-management.component.css']
})
export class PermissionManagementComponent implements OnInit {

  constructor(
    private _permission: PermissionService,
    private router: Router,
    public dialog: MatDialog
  ) { }

  error: any = '';
  globalData: any;
  permission: any;
  allroles: any;
  allpermission: any;
  loggedInOrgType: any;
  status: any;

  ngOnInit() {
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
    this.loggedInOrgType = this.globalData.orgType;
    this.loadPermission();
  }

  //load permission list
  public loadPermission() {
    this._permission.getAllPermissionwithRole().pipe(first()).subscribe((response: any) => {
      this.allroles = [];
      this.allpermission = [];
      if (response.success) {
        this.permission = response.data.length;
        //hide role normal
        var roleList = response.data.roles;
        roleList.forEach((item: any) => {
          if (item !== 'Normal') {
            this.allroles.push(item);
          }
        })

        //show hide permission based on org
        var allpermissionList = response.data.permissions;
        allpermissionList.forEach((item: any) => {
          if (item.id !== 'Super Admin' && item.id !== 'Normal' && item.id !== 'IT Admin' && item.id !== 'Email Client' && item.id !== 'Campaign Function') {
            this.allpermission.push(item);
          }
        });

      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //change permission
  public changePermission(permissionName: any, roleName: any, chkBox: any, allAssign: any, permission: any) {
    if (chkBox === true) {
      var isPermissionAllowed = false;
    } else {
      var isPermissionAllowed = true;
    }

    if (permissionName === 'View Reports' && isPermissionAllowed === true) {
      for (var i = 0, len = permission.permissionList.length; i < len; i++) {
        if (permission.permissionList[i] === 'Manager Setting') {
          var selected = isPermissionAllowed === true ? "YES" : "NO";
          permissionName = [permissionName];
          var input = {
            "roleName": roleName,
            "permissions": permissionName
          };
          this._permission.changePermission(input, selected, allAssign).pipe(first()).subscribe((response: any) => {
            if (response.success) {
              Swal.fire({
                title: "Updated",
                text: "Permission updated successfully",
                type: "success",
                timer: 2000,
                showConfirmButton: false
              });
              this.loadPermission();
            }
          }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
          })
          break;
        } else {
          Swal.fire({
            title: "Manager Setting not included.",
            text: "On proceed manager setting will be added automatically.",
            type: "warning",
            showConfirmButton: true,
            showCancelButton: true,
            allowOutsideClick: false,
          }).then((result) => {
            if (result.value) {
              var selected = isPermissionAllowed === true ? "YES" : "NO";
              permissionName = [permissionName, 'Manager Setting'];
              var input = {
                "roleName": roleName,
                "permissions": permissionName
              };

              this._permission.changePermission(input, selected, allAssign).pipe(first()).subscribe((response: any) => {
                if (response.success) {
                  Swal.fire({
                    title: "Updated",
                    text: "Permission updated successfully",
                    type: "success",
                    timer: 2000,
                    showConfirmButton: false
                  });
                  this.loadPermission();
                }
              }, error => {
                console.log('Error : ' + JSON.stringify(error));
                this.error = error;
              })
            } else {
              this.loadPermission();
            }
          })
        }
      };

    } else {
      if (permissionName === 'Manager Setting' && isPermissionAllowed === true) {
        permissionName = [permissionName];
      } else if (permissionName === 'Manager Setting' && isPermissionAllowed === false || permissionName === 'Manager Setting' && isPermissionAllowed === undefined) {
        permissionName = [permissionName, 'View Reports'];
      } else {
        permissionName = [permissionName];
      }
      var selected = isPermissionAllowed === true ? "YES" : "NO";
      var input = {
        "roleName": roleName,
        "permissions": permissionName
      };
      this._permission.changePermission(input, selected, allAssign).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          Swal.fire({
            title: "Updated",
            text: "Permission updated successfully",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
          this.loadPermission();
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    };

  }
}
