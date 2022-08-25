import { Component, OnInit } from '@angular/core';
import { RecruizHelperService } from './../../toggle_service/recruiz-helper.service';
import { Router } from '@angular/router';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
//service
import { AuthenticationService } from '../../auth_service/authenticationService';
import { Parser } from '@angular/compiler';
import * as moment from 'moment/moment.js';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  isSidebarToggle: boolean = true;
  activeRoute: string = '';

  constructor(
    private _recruizHelperService: RecruizHelperService,
    private router: Router,
    private auth: AuthenticationService
  ) {
    // this.goToRoute(this.router.url);
  }

  error = '';
  globalData: any;
  translateName: any;
  userTenants: any;
  panelOpenState: boolean = false;
  orgName: any;
  loggedInUserType: any;
  multiTenantList: any;
  bodyData: any;
  authToken: string;
  loggedInUserPermissionList: any;
  loggedInUserPermissions: Array<any>;
  adminPermission: boolean;
  executive: any;
  manager: any;
  isSwitch: boolean = false;

  ngOnInit() {
    this.multiTenantList = JSON.parse(sessionStorage.getItem("currentUser"));
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
    this.orgName = this.globalData?.orgName;
    this.userTenants = this.globalData?.userTenants;
    this.loggedInUserType = this.globalData?.type;
    if (this.globalData.orgType === "Corporate") {
      this.translateName = 'DEPARTMENTS';
    } else {
      this.translateName = 'CLIENTS';
    }
  }

  toggleSidebar() {
    this.isSidebarToggle = !this.isSidebarToggle;
    this._recruizHelperService.publishEvent('toggleSidebar', this.isSidebarToggle);
  }

  goToRoute(url: string) {
    //check for permission
    var found = false;
    var permissionArray = ['Normal'];
    for (var i = 0; i < permissionArray.length; i++) {
      if (this.globalData.permissions.indexOf(permissionArray[i]) > -1) {
        found = true;
        break;
      }
    }
    if (!found) {
      if (url.includes('client')) {
        this.router.navigate([url], { queryParams: { page: '1', sort: 'modificationDate|desc' } });
      } else if (url.includes('positions')) {
        this.router.navigate([url], { queryParams: { page: '1', sort: 'modificationDate|desc', tab: 'position' } });
      } else if (url.includes('candidate')) {
        this.router.navigate([url], { queryParams: { page: '1', sort: 'modificationDate|desc' } });
      }
      else this.activeRoute = '';
      //this.router.navigate([url]);
    } else {
      Swal.fire({
        title: "Permission denied. Request your admin to grant permission to view this page",
        type: "warning",
        showConfirmButton: true
      });
    }


  }

  //logout
  public logoutSession() {
    this.auth.logout().pipe(first()).subscribe(data => {
      if (data) {
        localStorage.removeItem('userInfo');
        sessionStorage.removeItem('currentUser');
        this.router.navigate(['web/login']);
        Swal.fire({
          title: "Success...",
          text: "You have been logged out successfully.",
          type: "success",
          timer: 5000,
          showConfirmButton: false
        });
      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
  }

  //go to admin settings
  public goToAdminSettings() {
    this.router.navigate(['user/admin/setting-management']);
  }

  // selected tenants
  public switchAccount(tenantName: string) {
    if (this.multiTenantList.tpk) {
      this.auth.loginAccount(tenantName, this.multiTenantList.email, this.multiTenantList.tpk).pipe(first()).subscribe(data => {
        if (data.body.success === true) {
          this.bodyData = data.body.data;
          this.authToken = data.headers.get('x-auth-token');
          this.loggedInUserPermissions = [];
          if (this.bodyData && this.authToken) {
            this.loggedInUserPermissionList = this.bodyData.userDetails.role.permissions;
            for (var i = 0; i < this.loggedInUserPermissionList.length; i++) {
              this.loggedInUserPermissions.push(this.bodyData.userDetails.role.permissions[i].permissionName);
            };

            this.adminPermission = false;
            if (this.loggedInUserPermissions !== undefined || this.loggedInUserPermissions !== null) {
              for (var j = 0; j < this.loggedInUserPermissions.length; j++) {
                if (this.loggedInUserPermissions[j].match('Super Admin')) {
                  this.adminPermission = true;
                  this.executive = false;
                  this.manager = true;
                  break;
                } else if (this.loggedInUserPermissions[j].match('Manager Setting')) {
                  this.executive = false;
                  this.manager = true;
                  if (this.loggedInUserPermissions.indexOf('Admin Setting') > -1) {
                    this.adminPermission = true;
                  }
                  break;
                } else if (this.loggedInUserPermissions[j].match('Normal')) {
                  this.adminPermission = false;
                  break;
                } else if (this.loggedInUserPermissions[j].match('IT Admin')) {
                  if (this.loggedInUserPermissions.indexOf('Admin Setting') > -1) {
                    this.adminPermission = true;
                    this.executive = false;
                    this.manager = false;
                    break;
                  }
                }
              };

              if (this.loggedInUserPermissions.indexOf('IT Admin') < 0
                && this.loggedInUserPermissions.indexOf('Normal') < 0
                && this.loggedInUserPermissions.indexOf('Super Admin') < 0) {
                if (this.loggedInUserPermissions.indexOf('Manager Setting') < 0) {
                  this.executive = true;
                  this.manager = false;
                  if (this.loggedInUserPermissions.indexOf('Admin Setting') > -1) {
                    this.adminPermission = true;
                  }
                }
              }


            }

            var userInfo = {
              authToken: this.authToken,
              id: this.bodyData.userDetails.id,
              userName: this.bodyData.userDetails.userName,
              userRole: this.bodyData.userDetails.userRole.value,
              email: this.bodyData.userDetails.email,
              designation: this.bodyData.userDetails.designation,
              mobile: this.bodyData.userDetails.mobile,
              timeZone: this.bodyData.userDetails.timeZone,
              permissionList: this.bodyData.userDetails.role.permissions,
              permissions: this.loggedInUserPermissions,
              type: this.bodyData.userDetails.type,
              orgId: this.bodyData.userDetails.orgId,
              orgName: this.bodyData.userDetails.orgName,
              orgType: this.bodyData.userDetails.orgType,
              userTenants: this.bodyData.tenantList,
              notificationStatus: this.bodyData.userDetails.isNotificationEnabled,
              profileSignature: this.bodyData.userDetails.profileSignature,
              teamIds: this.bodyData.userDetails.teamIds
            };

            // if mark for delete date exist then showing sweet alert to user for account delete action
            if (this.bodyData.userDetails.orgMarkDeleteDate) {
              var markDeleteDate = moment(new Date(this.bodyData.userDetails.orgMarkDeleteDate));
              var formattedDate = markDeleteDate.format('DD MMM YYYY');
              // showing sweet alert message after 5 seconds after user login
              setTimeout(function () {
                Swal.fire({
                  title: "Your account has been marked for delete on",
                  text: "Please contact the admin to revoke the delete action",
                  showConfirmButton: true
                });
              }, 5000);
            }

            if (this.bodyData.userDetails.type === 'app') {
              if (this.loggedInUserPermissions.indexOf('Normal') > -1) {
                this.router.navigate(['/user/settings']);
                Swal.fire({
                  title: "Alert...",
                  text: "No role has been assigned. Please request your admin to assign role",
                  type: 'warning',
                  showConfirmButton: true
                });
              } else if (this.loggedInUserPermissions.indexOf('IT Admin') > -1) {
                this.router.navigate(['/admin/user-management']);
              } else if (this.loggedInUserPermissions.indexOf('IT Admin') < 0 && this.loggedInUserPermissions.indexOf('Normal') < 0) {
                this.router.navigate(['/user/dashboard']);
              }
            } else if (this.bodyData.userDetails.orgType === 'vendor') {
              this.router.navigate(['/user/vendor-position-list']);
            } else if (this.bodyData.userDetails.orgType === 'department_head') {
              this.router.navigate(['/user/department-head-position-list']);
            }

            //set local storage
            localStorage.setItem('userInfo', JSON.stringify(userInfo));
          }
        } else {
          if (this.isSwitch) {
            Swal.fire({
              title: "Account",
              text: data.body.data,
              type: 'warning',
              showConfirmButton: true
            }).then((result) => {
              if (result.value) {
                Swal.fire(
                  
                )
              }
            })
          } else {
            Swal.fire({
              title: "Login Alert",
              text: data.body.data,
              customClass: 'swal-wide',
              showConfirmButton: true
            });
          }
        }
      })
    } else {
     // this.router.navigate(['/web/login']);
    }
  }

}
