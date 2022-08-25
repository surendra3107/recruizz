import { Component, OnInit } from '@angular/core';

import { Router, ActivatedRoute } from '@angular/router';
import { first } from "rxjs/operators";
import Swal from 'sweetalert2';
import { MatDialog } from '@angular/material/dialog';
import { UserBulkUploadDialog } from '../user-modal/user-upload.component';
import { InviteUserDialog } from '../user-modal/invite-user.component';
import { AssignUserDialog } from '../user-modal/assign-user.component';
import { TeamHierarchyDialog } from '../user-modal/team-hierarchy.component';
import { AddEditInterviewerDialog } from '../user-modal/add-edit-interviewer.component';
import { AddEditVendorDialog } from '../user-modal/add-edit-vendor.component';
import { AddVendorUserDialog } from '../user-modal/add-vendor-user.component';
import { AddDepartmetHeadUserDialog } from '../user-modal/add-department-user.component';

//service
import { UserService } from '../userService/user.service';
import { IntegrationService } from '../../integration/integrationService/integration.service';
import { GlobalService } from '../../../globalServices/global.service';
@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {

  constructor(
    private router: Router,
    public dialog: MatDialog,
    private _user: UserService,
    private _integration: IntegrationService,
    private _global: GlobalService
  ) { }

  error: any = '';
  globalData: any;
  fileDetails: any;
  user: any;
  joinedCount: any;
  pendingCount: any;
  allusers: any;
  allroles: any;
  portalType: any;
  naukriMosterCount: any;
  firstView: any = '1';
  secondView: any;
  activeJobPortalount: any;
  jobPortalFlag: boolean = false;
  sourceList: any;
  usageTypes: any;
  hrUsers: any;
  userName: string;
  page: any = '1';
  intPage: any = '1';
  InterviewerName: string;
  allInterviwerList: any;
  allVendorsList: any;
  venderUser: any;
  isUserVendor: boolean = false;
  vendUserPage: any = '1';
  vendorPage: any = '1';
  vendorId: any;
  descisionMakerList: any;
  descisionPage: any = '1';
  departmentHeadList: any;

  ngOnInit() {
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    this.loadUsers();
    this.initJobPortalIntegration();
    this.loadUsageType();
  }

  public onSelectedFilesChanged(event: any) {
    if (event) {
      this.fileDetails = event[0];
      var fd = new FormData();
      fd.append('file', this.fileDetails);
      this._user.bulkuploadFile(fd).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          if (response.data.fileHeaders) {
            var headerConstants = response.data.headerConstants;
            var filePath = response.data.filePath;
            var importType = response.data.importType;
            var fileHeaders = response.data.fileHeaders;

            //open modal
            let dialogRef = this.dialog.open(UserBulkUploadDialog, {
              width: '700px',
              data: { headerConstants: headerConstants, filePath: filePath, importType: importType, fileHeaders: fileHeaders },
              autoFocus: false,
              disableClose: true
            });

            dialogRef.afterClosed().subscribe(result => {
              if (result) {
                this.loadUsers();
              }
            })
          }
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    }
  }

  //download sample file
  public sampleBulkUploadDownload() {
    this._user.downloadSampleBulkUploadFile().pipe(first()).subscribe((response: any) => {
      if (response) {
        var fileName = 'sample_user_bulk_format.xlsx';
        var blob = new Blob([response], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" });
        var objectUrl = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = objectUrl;
        a.target = '_blank';
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //add user manually
  public loadUsers() {
    this._user.getAllusers('app').pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.portalType = [];
        this.naukriMosterCount = [];
        this.user = response.data.length;
        this.joinedCount = response.data.All_User.joined_count;
        this.pendingCount = response.data.All_User.pending_count;
        this.allusers = response.data.All_User.gridData;
        this.allroles = response.data.All_Roles.gridData;

        this.allusers.forEach((item: any) => {
          item.selectedSources.forEach((value: any) => {
            this.portalType.push(value);
          })
        })

        if (this.allusers.length > 10) {
          this.secondView = '10';
        } else {
          this.secondView = this.allusers.length;
        };

        //active portal user count
        this.activeJobPortalount = this.allusers.filter(function (s) {
          return s.jobPortalEnable;
        }).length;

        //count nuakri and monstor
        var sortedArr: any = [],
          count = 1;
        sortedArr = this.portalType.sort();
        for (var i = 0; i < sortedArr.length; i = i + count) {
          count = 1;
          for (var j = i + 1; j < sortedArr.length; j++) {
            if (sortedArr[i] === sortedArr[j])
              count++;
          }
          this.naukriMosterCount.push(sortedArr[i] + " : " + count);
        }

      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //open modals to invite users
  public inviteUsers(type: any) {
    let dialogRef = this.dialog.open(InviteUserDialog, {
      width: '900px',
      data: { addType: 'app' },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers();
      }
    })
  }

  //sixth sense integration details
  public initJobPortalIntegration() {
    this._integration.getJobPortalInfo().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        var responseData = response.data;
        if (responseData) {
          this.jobPortalFlag = true;
          this.getsixthSenseSources();
        } else {
          this.jobPortalFlag = false;
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //get sources
  public getsixthSenseSources() {
    this._integration.getsixthSenseSources().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.sourceList = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //load sixth sense usage Type
  public loadUsageType() {
    this._integration.getUsageType().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.usageTypes = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //change user role
  public changedUserRole(selected: string, email: string) {
    this._user.updateUserRole(selected, email).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Success",
          text: "User role changed successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadUsers();
      }
      if (response.reason === 'no_role_change_for_inactive_user') {
        Swal.fire({
          title: "Failure",
          text: "Unable to change user role.",
          type: "warning",
          showConfirmButton: true
        });
        this.loadUsers();
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //disable / enable user
  public disableUser(userEmail: any, index: any, state: any, type: any) {
    var title: any;
    if (state) {
      title = "Are you sure you want to enable user?";
    } else {
      title = "Are you sure you want to disable user?";
    }

    Swal.fire({
      title: "Alert",
      text: title,
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this._user.disableUser(userEmail, state).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            var updatedUser = response.data;
            this.allusers.splice(index, 1);
            this.allusers.unshift(updatedUser);
            if (updatedUser.accountStatus) {
              var responseTitle = 'Enabled';
              var responseText = "User has been successfully enabled";
            } else {
              var responseTitle = 'Disabled';
              var responseText = "User has been successfully disabled";
            }
            Swal.fire({
              title: responseTitle,
              text: responseText,
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            if (type === 'dept_head') {
              this.loadDepartmentHead();
            } else {
              this.loadUsers();
            }

          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //remove user
  public replaceUser(email: any, index: any) {
    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete ?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this._user.getHrUsers().pipe(first()).subscribe((response: any) => {
          if (response.success) {
            this.hrUsers = [];
            this.hrUsers = response.data;
            this.hrUsers.forEach((item: any, index: any) => {
              if (item.email === email) {
                this.hrUsers.splice(index, 1);
              }
            })

            if (this.hrUsers.length === 0) {
              Swal.fire({
                title: "Failure",
                text: "User can not be deleted. No other user there to assign.",
                type: "warning",
                showConfirmButton: true
              });
            } else {
              //open modal to assign user
              let dialogRef = this.dialog.open(AssignUserDialog, {
                width: '700px',
                data: { email: email, index: index },
                autoFocus: false,
                disableClose: true
              });

              dialogRef.afterClosed().subscribe(result => {
                if (result) {
                  this.loadUsers();
                }
              })
            }
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //re send invite
  public resend(email: string) {
    this._user.reInviteUser(email).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Sent",
          text: "Invitation sent to user successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //reset password
  public getClickedUserEmail(email: string) {
    Swal.fire({
      title: "Reset Password",
      html:
        '<input id="swal-input1" class="swal2-input" placeholder="New Password" type="password">' +
        '<input id="swal-input2" class="swal2-input" placeholder="Confirm Password" type="password">',
      inputAttributes: {
        autocapitalize: 'off'
      },
      showCancelButton: true,
      reverseButtons: true,
      confirmButtonText: 'Procees',
      showLoaderOnConfirm: true,
      preConfirm: () => {
        var password: any = document.getElementById('swal-input1');
        var confirmPassword: any = document.getElementById('swal-input2');
        //return [resolution, text2];
        if (password.value !== confirmPassword.value) {
          Swal.showValidationMessage("Password did not match.");
          return false
        } else {
          this._user.resetUserPasswordByAdmin(email, password.value).pipe(first()).subscribe((response: any) => {
            if (response.success) {
              Swal.fire({
                title: "Success",
                text: "Reset password successfully",
                type: "success",
                timer: 2000,
                showConfirmButton: false
              });
            }
          }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
          })
        }
      }

    })

  }

  // turn on / off portal
  public portalEnableDisable(userData: any) {
    var input: any = [];
    var portalInput = {
      'email': userData.email,
      'usageType': userData.usageType,
      'viewCount': userData.viewCount
    };
    input.push(portalInput);

    if (userData.jobPortalEnable) {
      this._integration.enableSixthSenseUsers(input).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          Swal.fire({
            title: "Portal Enabled",
            text: "Job Portal has been enabled successfully.",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
          this.loadUsers();
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    } else {
      this._integration.disableSixthSenseUsers(input).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          Swal.fire({
            title: "Portal Disabled",
            text: "Job Portal has been disabled successfully.",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
          this.loadUsers();
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    }
  }


  //update usage / switch usage type
  public updateUserUsage(userData: any, sorceType: any) {
    var prtalDataName: any = [];
    if (userData.usageType !== 'UNLIMITED_VIEW' && sorceType === 'usage') {
      userData.viewCount = 0;
      return true;
    } else if (userData.usageType === 'UNLIMITED_VIEW' && sorceType === 'usage') {
      userData.viewCount = -1;
      prtalDataName = userData.selectedSources;
    } else if (userData.usageType !== 'UNLIMITED_VIEW' && sorceType === 'count') {
      prtalDataName = userData.selectedSources;
    } else if (sorceType !== 'count' && sorceType !== 'usage') {
      if (sorceType) {
        let addedSource = userData.selectedSources;
        var index = addedSource.indexOf(sorceType.id);
        if (index > -1) {
          prtalDataName = addedSource.splice(index, 1);
          if (addedSource.length === 0) {
            prtalDataName = [];
          }
        } else {
          prtalDataName = addedSource.concat(sorceType.id);
        }
      }
    }

    var input = [];
    var portalInput = {
      'email': userData.email,
      'usageType': userData.usageType,
      'viewCount': userData.viewCount,
      'sources': prtalDataName
    };
    input.push(portalInput);

    this._integration.updateUserUsage(input).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Portal Updated",
          text: "Job Portal information has been updated successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadUsers();
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })

  }

  //checked check box if portal added
  public checkPortalAdded(portalList: any) {
    if (portalList) {
      this.sourceList.forEach((item: any) => {
        item.isChecked = false;
        portalList.forEach((value: any) => {
          if (item.id === value) {
            item.isChecked = true;
          }
        })
      })
    }
  }

  //open team herarachy modal
  public getTeamStructure(keyName: any) {
    let dialogRef = this.dialog.open(TeamHierarchyDialog, {
      width: '900px',
      data: { teamId: keyName },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {

      }
    })
  }

  // tab change event
  public userManagementTabSwitch(event: any) {
    if (event.tab.textLabel === 'Interviewers') {
      this.loadAllInterviewer();
    } else if (event.tab.textLabel === 'Vendors') {
      this.loadVendors();
    } else if (event.tab.textLabel === 'Decision Makers') {
      this.loadDescisionMaker();
    } else if (event.tab.textLabel === 'Department Head') {
      this.loadDepartmentHead();
    }
  }

  //Interviewer details
  public loadAllInterviewer() {
    this._global.getAllInterviewer().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.allInterviwerList = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //add edit interviewer
  public addEditInterviewer(intData: any) {
    let dialogRef = this.dialog.open(AddEditInterviewerDialog, {
      width: '500px',
      data: { intData: intData },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadAllInterviewer();
      }
    })
  }

  //delete interviwer
  public deleteInterviwer(id: any) {
    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete ?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this._global.deleteInterviewerGlobal(id).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Interviewer deleted successfully.",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.loadAllInterviewer();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //load vendors list
  public loadVendors() {
    this._user.getAllVendors().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.allVendorsList = response.data;
        this.allVendorsList.forEach((item: any) => {
          if (item.isInterviewSchedule === 'true') {
            item.isInterviewSchedule = true;
          } else {
            item.isInterviewSchedule = false;
          }
        })
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //add edit vendor
  public addEditVendor(vendorData: any) {
    let dialogRef = this.dialog.open(AddEditVendorDialog, {
      width: '500px',
      data: { vendorData: vendorData },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadVendors();
      }
    })
  }

  //enable disable user
  public disableVendor(vendorId: any, index: any, state: any) {
    if (state) {
      var states = 'disable'
    } else {
      var states = 'enable'
    }
    Swal.fire({
      title: "Alert on " + states,
      text: "Are you sure you want to " + states + " vendor ?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this._user.enableDisableVendor(vendorId, state).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            if (!state) {
              var states = 'disable'
            } else {
              var states = 'enable'
            }
            Swal.fire({
              title: states,
              text: "Vendor " + states + " successfully.",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.loadVendors();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //delete vendor
  public deleteVendor(vendorId: any, index: any) {
    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete ?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this._user.deleteVendor(vendorId).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Success",
              text: "Vendor deleted successfully.",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.loadVendors();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //permission for vendor to schedule interviews
  public permissionToScheduleInterview(permissionValue: any, vendorId: any) {
    this._user.vendorInterviewShedulePermission(permissionValue, vendorId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Success...",
          text: "Permission assigned successfully",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadVendors();
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  // manage user vendors
  public vendorUsersManagement(vendorId: any) {
    this.vendorId = vendorId;
    this._user.getVendorUser(this.vendorId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.isUserVendor = true;
        this.venderUser = response.data.All_User.gridData
      } else {
        this.isUserVendor = false;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
      this.isUserVendor = false;
    })
  }
  //back to vendor list
  public loadVendorList() {
    this.isUserVendor = false;
  }

  //add vendor user
  public addVendorUser() {
    let dialogRef = this.dialog.open(AddVendorUserDialog, {
      width: '900px',
      data: { vendorId: this.vendorId },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.vendorUsersManagement(this.vendorId);
      }
    })
  }

  //load descision maker
  public loadDescisionMaker() {
    this._global.getAllDecisionMaker().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.descisionMakerList = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //load department head
  public loadDepartmentHead() {
    this._user.getAllusers('department_head').pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.departmentHeadList = response.data.All_User.gridData;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //delete department head
  public deleteDeptHead(email: any, index: any, deptType: any) {
    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete ?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this._user.getDeptHeadUsers(email).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            this.hrUsers = [];
            this.hrUsers = response.data;
            this.hrUsers.forEach((item: any, index: any) => {
              if (item.email === email) {
                this.hrUsers.splice(index, 1);
              }
            })

            if (this.hrUsers.length === 0) {
              Swal.fire({
                title: "Failure",
                text: "User can not be deleted. No other user there to assign.",
                type: "warning",
                showConfirmButton: true
              });
            } else {
              //open modal to assign user
              let dialogRef = this.dialog.open(AssignUserDialog, {
                width: '700px',
                data: { email: email, index: index, deptType: deptType },
                autoFocus: false,
                disableClose: true
              });

              dialogRef.afterClosed().subscribe(result => {
                if (result) {
                  this.loadDepartmentHead();
                }
              })
            }
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //invite department head
  public inviteDepartHeadUSer() {
    let dialogRef = this.dialog.open(AddDepartmetHeadUserDialog, {
      width: '900px',
      data: { deptType: 'department_head'},
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadDepartmentHead();
      }
    })
  }
}
