import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';

import { MatSnackBar } from '@angular/material/snack-bar';
import { InterviwerPannelDialog } from '../modals/interviewer/interviwer-pannel.component';
import { HrPannelDialog } from '../modals/hr-pannel/hr-pannel.component';
import { VendorPannelDialog } from '../modals/vendors/vendor.component';
import { PositionNoteslDialog } from '../modals/notes/notes.component';
import { MatDialog } from '@angular/material/dialog';

import { environment } from '../../../../environments/environment';
//services
import { PositionService } from './../positionService/position.service';
import { DropdownService } from './../../dropdownService/dropdown.service';
import { TeamService } from '../../../user/admin/team-management/teamService/team.service';

@Component({
  selector: 'app-positions-detail',
  templateUrl: './positions-detail.component.html',
  styleUrls: ['./positions-detail.component.css']
})
export class PositionsDetailComponent implements OnInit {
  //base url
  baseRoot: any = environment.baseUrl;

  error: '';
  positionEmail: any;
  positionId: any;
  clientName: any;
  closures: any;;
  pending_openings: any;;
  clientNotes: any;
  positionData: any;
  teamList: any;;
  goodSkillSetList: any;
  reqSkillSetList: any;
  qualificationsList: any;
  positionCode: any;
  hrSelected: any;
  interviewerList: any;
  positionName: any;
  allVendors: any;
  owner: any;
  isFilterOpen: any;
  selectedSource: any

  allStatusList: any;
  activePosition: any;
  onHoldPosition: any;
  closedPosition: any;
  stopSourcingPosition: any;

  globalData: any;
  sourceList: any;
  positionDetailsExternalURL: any

  selectedHrPanel: Array<any>;

  isActive: boolean = false;
  selectedTab: string = 'decisionMaker';

  isDecisionMakerEmpty: boolean = true;
  isActivityEmpty: boolean = false;
  isNotesEmpty: boolean = false;
  isRatesEmpty: boolean = false;
  isDocsEmpty: boolean = false;


  pageTitle: string = 'POSITION';
  mainBreacrumb: string = 'Position';
  mainRoute: string = '/user/positions-list';
  finalBreadcrumb: string = 'Recruiz-Sales';
  isBreadcrumbShow: boolean = true;

  allActivity: any;
  nextActivities: any;
  numberOfElements: any;
  totalElements: any;
  totalPages: any;
  currentActivityPage: number = 0;

  allNotes: any;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private position: PositionService,
    private dropdown: DropdownService,
    private team: TeamService,
    private _snackBar: MatSnackBar,
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    //read url params query
    this.activatedRoute.queryParams.subscribe(params => {
      this.positionId = params.pId;
    });

    //get position by Id
    this.initPosition();
    this.loadStatusList();
    this.loadSourceList();
  }

  onTabMenu(type: string) {
    this.selectedTab = type;
    this.isActive = true;
  }

  onOpenMenu(menu: any): void {
  }

  //load position
  public initPosition() {
    if (!this.positionId) {
      Swal.fire({
        title: "Opps...!",
        text: "Oops something went wrong. Please try again!",
        type: "warning",
        showConfirmButton: true
      });
      this.router.navigateByUrl('/user/dashboard');
      return false;
    }

    this.position.getPositionsById(this.positionId).pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.positionEmail = response.data.position_email;
        this.clientName = response.data.client;
        this.closures = response.data.closures;
        this.pending_openings = response.data.pending_openings;
        this.clientNotes = response.data.clientNotes;
        this.positionData = response.data.position;
        this.teamList = response.data.position.team;
        this.goodSkillSetList = this.positionData.goodSkillSet;
        this.reqSkillSetList = this.positionData.reqSkillSet;
        this.qualificationsList = this.positionData.educationalQualification;
        this.positionCode = this.positionData.positionCode;
        this.hrSelected = response.data.hrList;
        this.interviewerList = response.data.interviewerList;
        this.positionName = this.positionData.title;
        this.allVendors = this.positionData.vendors;
        this.owner = this.positionData.owner;

        //get the list of hrs that are part of the team
        if (this.teamList !== null) {
          this.team.getAllTeamLists(this.teamList.id).pipe(first()).subscribe(response => {
            if (response.success) {
              this.selectedHrPanel = [];
              const hrListsMembers = response.data.members;
              hrListsMembers.forEach((items: any) => {
                this.hrSelected.forEach((values: any) => {
                  if (items.email === values.email) {
                    var selectHrList = {
                      'email': values.email,
                      'id': values.id || values.userId,
                      'mobile': values.mobile,
                      'name': values.name,
                      'selectedStatus': true
                    }
                    this.selectedHrPanel.push(selectHrList);
                  }
                })
              })
            }
          }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
          })
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  // get all status list
  public loadStatusList() {
    this.dropdown.getCommanStatusList().pipe(first()).subscribe(data => {
      if (data.success === true) {
        this.allStatusList = data.data;
        this.activePosition = this.allStatusList[0].value;
        this.onHoldPosition = this.allStatusList[1].value;
        this.closedPosition = this.allStatusList[2].value;
        this.stopSourcingPosition = this.allStatusList[3].value;
      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
  }

  //change status
  public changeStatusFromDetails(status: any, id: any, clientStatus: any) {
    if (clientStatus === "OnHold") {
      Swal.fire({
        title: "Not allowed...",
        text: "Client's status is 'On Hold'",
        type: 'warning',
        showConfirmButton: true,
        showCancelButton: false,
        allowOutsideClick: false
      })
    } else if (clientStatus === "StopSourcing") {
      Swal.fire({
        title: "Not allowed...",
        text: "Client's status is 'Stop Sourcing'",
        type: 'warning',
        showConfirmButton: true,
        showCancelButton: false,
        allowOutsideClick: false
      })
    } else {
      this.position.changePosStatus(status, id).pipe(first()).subscribe(response => {
        if (response.success === true) {
          this.initPosition();
          Swal.fire({
            title: "Status Changed",
            text: "Status has been successfully updated",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
        }
      },
        error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
    }
  }

  //publish position
  public publishPositionFromDetails(isPublish: boolean, id: any, position: any) {
    if (position.clientStatus === "OnHold") {
      Swal.fire({
        title: "Not allowed...",
        text: "Client's status is 'On Hold'",
        type: 'warning',
        showConfirmButton: true,
        showCancelButton: false,
        allowOutsideClick: false
      })
    } else if (position.clientStatus === "StopSourcing") {
      Swal.fire({
        title: "Not allowed...",
        text: "Client's status is 'Stop Sourcing'",
        type: 'warning',
        showConfirmButton: true,
        showCancelButton: false,
        allowOutsideClick: false
      })
    } else {
      this.position.publishingPosition(isPublish, id).pipe(first()).subscribe(response => {
        if (isPublish) {
          var pubInfo = 'published';
        } else {
          var pubInfo = 'unpublished';
        }
        if (response.success === true) {
          this.initPosition();
          Swal.fire({
            title: "Position " + pubInfo,
            text: "Position is successfully " + pubInfo,
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
        }
      },
        error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
    }
  }

  //delete position
  public deletePositionFromDetail(positionId: any) {
    //check for permission
    var found = false;
    var permissionArray = ['Delete Position', 'Super Admin', 'Global Delete'];
    for (var i = 0; i < permissionArray.length; i++) {
      if (this.globalData.permissions.indexOf(permissionArray[i]) > -1) {
        found = true;
        break;
      }
    }
    if (found) {
      Swal.fire({
        title: "Alert on delete",
        text: "Are you sure you want to delete this position ?",
        type: 'warning',
        confirmButtonText: 'Yes',
        showConfirmButton: true,
        showCancelButton: true,
        allowOutsideClick: false,
        reverseButtons: true
      }).then((result) => {
        if (result.value) {
          this.position.deletePositionList(positionId).pipe(first()).subscribe(response => {
            if (response.success === true) {
              Swal.fire({
                title: "Deleted",
                text: "Position has been successfully deleted.",
                type: "success",
                timer: 2000,
                showConfirmButton: false
              });
              let currentUrl = '/user/positions-list';
              let urls = currentUrl.split('?')[0];
              this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
                this.router.navigate([urls], { queryParams: { page: '1', sort: 'modificationDate|desc', tab: 'position' } })
              );
            }
          },
            error => {
              console.log('Error : ' + JSON.stringify(error));
              this.error = error;
            })
        }
      })

    } else {
      Swal.fire({
        title: "Permission denied. Request your admin to grant permission to delete the positions.",
        type: "warning",
        showConfirmButton: true
      });
    }
  }

  // load all source list
  public loadSourceList() {
    this.dropdown.getSourceList().pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.sourceList = response.data;
      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
  };

  //generate url
  public generateExtUrl(pCode: string, source: string) {
    this.position.getPositionDetailsURL(pCode, source).pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.positionDetailsExternalURL = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //copy to clip board
  public copied(event: any) {
    if (event.isSuccess === true) {
      this._snackBar.open('Copied to clipboard', 'Copied', {
        duration: 2000,
      });
    }
  }

  //open modal to add interviewer
  public openDialogToAddInterviewer(addedInterviewerList: any, positionId: any) {
    let dialogRef = this.dialog.open(InterviwerPannelDialog, {
      width: '900px',
      data: { interviwerLists: addedInterviewerList, positionId: positionId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'interviwer-added') {
        Swal.fire({
          title: "Added",
          text: "Interviewer added to position successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.initPosition();
      }
    })
  }

  //open modal to add hrs
  public openDialogToAddHr(positionId: any) {
    let dialogRef = this.dialog.open(HrPannelDialog, {
      width: '900px',
      data: { selectedTeamId: this.teamList, addedHrs: this.selectedHrPanel, positionId: positionId },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'hr-added') {
        Swal.fire({
          title: "Added",
          text: "Team and HR's added to position successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.initPosition();
      }
    })
  }

  //remove hr from position
  public removeHrFromPosition(pId: any, hrId: any) {
    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete ?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then(result => {
      if (result.value) {
        this.position.removeHrFromPosition(pId, hrId).pipe(first()).subscribe(response => {
          if (response.success) {
            Swal.fire({
              title: "Removed",
              text: "User removed from position successfully.",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.initPosition();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //open modal to add vendors
  public openDialogToAddVendors(pId: any) {
    let dialogRef = this.dialog.open(VendorPannelDialog, {
      width: '900px',
      data: { positionId: pId, addedVendor: this.allVendors },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'vendor-added') {
        Swal.fire({
          title: "Added",
          text: "Vendor(s) added to position successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.initPosition();
      }
    })
  }

  //delete vrndor
  public deleteVendorFromPosition(pId: any, vendorId: any) {
    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete this vendor?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then(result => {
      if (result.value) {
        this.position.removeVendorFromPosition(pId, vendorId).pipe(first()).subscribe(response => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Vendor removed from position successfully.",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.initPosition();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //call function on tab switch
  public tabClick(tab: any) {
    if (tab.tab.textLabel === 'ACTIVITY') {
      this.getAllActivity();
    } else if (tab.tab.textLabel === 'NOTES') {
      this.getAllNotes();
    }
  }

  //function call to get posiion activity
  public getAllActivity() {
    this.position.getPositionActivity(this.positionCode, this.currentActivityPage).pipe(first()).subscribe(response => {
      this.allActivity = [];
      if (response.success) {
        this.allActivity = this.manipulateActivityForClientAndPositionLink(response.data.content);
        this.numberOfElements = response.data.numberOfElements;
        this.totalElements = response.data.totalElements;
        this.totalPages = response.data.totalPages;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //get next count activity
  public getNextActivity() {
    this.currentActivityPage = this.currentActivityPage + 1;
    this.position.getPositionActivity(this.positionCode, this.currentActivityPage).pipe(first()).subscribe(response => {
      this.nextActivities = response.data.content;
      this.numberOfElements = response.data.numberOfElements;
      this.totalElements = response.data.totalElements;
      this.totalPages = response.data.totalPages;
      this.manipulateActivityForClientAndPositionLink(this.allActivity.concat(this.nextActivities));
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }
  //find # and $ and replace with link
  public manipulateActivityForClientAndPositionLink(responseData: any) {
    responseData.forEach((activity: any) => {
      var indexClient = activity.message.indexOf("$@");
      var indexPosition = activity.message.indexOf("$#");
      var clientString = activity.message.substring(indexClient, indexPosition);

      var sepraterIndex = clientString.indexOf("_");
      var clientId = clientString.substring(2, sepraterIndex);
      // var clientId = clientId.replace(clientId,"$@");
      var clientName = clientString.substring(sepraterIndex + 1, clientString.length);
      var link = this.baseRoot + "user/client-details?cid=" + clientId;
      link = "<a href=" + link + " class='link-cursor' target='_blank'>" + clientName + "</a> , ";

      var positionIndexLast = this.nth_occurrence(activity.message, "$#", 2);
      var positionString = activity.message.substring(indexPosition, positionIndexLast);
      sepraterIndex = positionString.indexOf("_");
      var positionId = positionString.substring(2, sepraterIndex);
      var positionName = positionString.substring(sepraterIndex + 1, positionString.length);

      var positionLink = this.baseRoot + "user/position-details?pid=" + positionId;
      positionLink = "<a href=" + positionLink + " class='link-cursor' target='_blank'>" + positionName + "</a> ";
      if (indexClient > 0 && indexPosition > 0) {
        activity.message = activity.message.replace(clientString, link);
        activity.message = activity.message.replace(positionString, positionLink);
        activity.message = activity.message.replace("$#", "");
        activity.message = activity.message.replace("$@", "");
      }
      this.allActivity.push(activity)
    })
    return this.allActivity;
  }

  public nth_occurrence(string: any, char: any, nth: any) {
    var first_index = string.indexOf(char);
    var length_up_to_first_index = first_index + 1;
    if (nth == 1) {
      return first_index;
    } else {
      var string_after_first_occurrence = string.slice(length_up_to_first_index);
      var next_occurrence = this.nth_occurrence(string_after_first_occurrence, char, nth - 1);
      if (next_occurrence === -1) {
        return -1;
      } else {
        return length_up_to_first_index + next_occurrence;
      }
    }
  }

  //get all notes for position
  public getAllNotes() {
    this.currentActivityPage = 0;
    this.position.getPositionNotes(this.positionId, this.currentActivityPage).pipe(first()).subscribe(response => {
      if (response.success) {
        this.allNotes = response.data.content;
        this.numberOfElements = response.data.numberOfElements;
        this.totalElements = response.data.totalElements;
        this.totalPages = response.data.totalPages;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //open modal to add notes
  public openDialogNotes() {
    let dialogRef = this.dialog.open(PositionNoteslDialog, {
      width: '900px',
      data: { positionId: this.positionId },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'notes-added') {
        Swal.fire({
          title: "Added",
          text: "Notes added to position successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.getAllNotes();
      }
    })
  }

  //delete position notes
  public deletePositionNotes(noteId: any) {
    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete this note?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then(result => {
      if (result.value) {
        this.position.removeNotesFromPosition(noteId).pipe(first()).subscribe(response => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Notes deleted from position successfully.",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.getAllNotes();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //update notes
  public updatePositionNotes(notesData: any) {
    let dialogRef = this.dialog.open(PositionNoteslDialog, {
      width: '900px',
      data: { positionId: this.positionId, notesData: notesData },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'notes-updated') {
        Swal.fire({
          title: "Updated",
          text: "Notes updated successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.getAllNotes();
      }
    })
  }

  public updateInterviwerFromPosition(interviewer: any, titile: any){

  }

  public deleteInterviewerFromPosition(positionId: any, interviewerId: any){

  }

}
