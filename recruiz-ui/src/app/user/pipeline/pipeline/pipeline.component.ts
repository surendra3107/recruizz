import { Component, OnInit } from "@angular/core";
import { Router, ActivatedRoute, Params } from "@angular/router";
import { first } from "rxjs/operators";
import Swal from "sweetalert2";
import { MatDialog } from '@angular/material/dialog';
import { AddEditStagesDialog } from '../pipeline-modals/add-edit-stages.component';
import { AddExistingCandidateDialog } from '../pipeline-modals/add-existing-candidate.component';
import { ScheduleInterviewDialog } from '../pipeline-modals/schedule-interview.componen';

import { PipelineService } from "./../pipelineService/pipeline.service";
import { UtilService } from "./../../utilService/util.service";
import { DropdownService } from "./../../dropdownService/dropdown.service";
import { SearchService } from "./../../searchService/search.service";
import { PositionService } from "../../positions/positionService/position.service";
import { InterviewService } from "../interviewService/interview.service";

@Component({
  selector: "app-pipeline",
  templateUrl: "./pipeline.component.html",
  styleUrls: ["./pipeline.component.css", "../pipeline-sub-header/pipeline-sub-header.component.css"]
})
export class PipelineComponent implements OnInit {
  selectedTabIndex: number = 0;
  constructor(
    private router: Router,
    public dialog: MatDialog,
    private activatedRoute: ActivatedRoute,
    private utilService: UtilService,
    private dropdown: DropdownService,
    private searchFilter: SearchService,
    private _pipeline: PipelineService,
    private _position: PositionService,
    private _interview: InterviewService
  ) { }


  error: any = '';
  globalData: any;
  positionCode: any;
  showMyCandidates: boolean;
  statusFilerValue: any;
  timePeriod: any;
  endDate: any;
  recReport: any;
  startDate: any;
  showCandidateOnborad: any = 'view_mine';

  responseData: any;
  positionId: any;
  clientStatus: any;
  positionStatus: any;
  rounds: any;
  clientNameForSchecdule: any;
  visible: boolean = true;
  candidateListData: any;
  multiStatusList: Array<any>;
  statusList: any;
  roundName: any;
  activeStageName: any;
  dragContainerOrderId: any;
  dragRoundId: any;
  orderNumber: any;
  isCandidateCardMoved: boolean = false;
  selectedEmails: any;
  isDivLoader: boolean = false;
  sortFilter: string;
  interviewPanelList: any;
  interviewDetails: any;
  category: any;
  
  ngOnInit(): void {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem("userInfo"));
    //read url params query
    this.activatedRoute.queryParams.subscribe(params => {
      this.positionCode = params.pId;
    });

    //pipeline function
    this.loadPipeline(this.showCandidateOnborad);
    //load status
    this.loadStatusList();
    //load rounds
    this.loadRoundNameListView();
  }

  //load pipe line
  public loadPipeline(showCandidate: any) {
    if (this.showCandidateOnborad === 'view_all') {
      var sourcedByLoggedinUser = null;
      this.showMyCandidates = true;
      this.statusFilerValue = null;
      this.timePeriod = null;
      this.endDate = null;
      this.timePeriod = null;
      this.recReport = undefined;
    } else {
      var sourcedByLoggedinUser = this.globalData.email;
      this.showMyCandidates = false;
    }

    this._pipeline.getBoardDataInfo(this.positionCode, this.statusFilerValue, sourcedByLoggedinUser, this.timePeriod, this.startDate, this.endDate).pipe(first()).subscribe(response => {
      if (response.success) {
        this.responseData = response.data;
        this.positionId = this.responseData.positionId;
        this.clientStatus = this.responseData.clientStatus;
        this.positionStatus = this.responseData.positionStatus;
        this.rounds = this.responseData.rounds;
        this.clientNameForSchecdule = this.responseData.clientName;
        this.visible = true;
        this.toggleRounds({ index: 0 });
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  // switch between rounds with candidate info
  public toggleRounds(event: any) {
    this.isDivLoader = true;
    this.selectedTabIndex = event.index;
    this.candidateListData = this.rounds[this.selectedTabIndex].candidateList;
    this.rounds.forEach((item: any, key: any) => {
      if (this.selectedTabIndex === key) {
        this.activeStageName = item.name;
      }
    })
  }
  isLoader: any;
  public onLoad(event: any) {
    this.isLoader = true;
  }

  //moving cards from one stage to another
  public moveCandidateCardSuccessViewList(cardIndx: any, cardData: any, roundId: any) {
    this.dragContainerOrderId = this.rounds[this.selectedTabIndex].orderNo;
    this.dragRoundId = this.rounds[this.selectedTabIndex].roundId;

    //get order number
    this.rounds.forEach((item: any) => {
      if (item.roundId === roundId) {
        this.orderNumber = item.orderNo;
      };
    })

    //match order number and drag order number
    if (this.orderNumber === this.dragContainerOrderId) {
      this.isCandidateCardMoved = true;
      return cardData;
    }

    //check the status
    if (this.utilService.isClientOrPositionActive(this.clientStatus, this.positionStatus)) {
      var moveCandidateList: any = {};
      moveCandidateList.candidateEmailList = [];
      moveCandidateList.candidateEmailList.push(cardData.email);
      var dropRoundId = this.rounds[this.selectedTabIndex].roundId;
      var candidateData = this.rounds[this.selectedTabIndex].candidateList;
      var cardIndex = this.findArrayIndex(candidateData, cardData);

      if (this.orderNumber > this.dragContainerOrderId) {
        if (cardData.status === "Approved" || cardData.status === "Offered") {
          // // calling this method because update is not happening for drag and drop library after pop button click
          if (cardIndex !== -1) {
            this._pipeline.moveCandidates(roundId, dropRoundId, moveCandidateList, cardIndex).pipe(first()).subscribe((response: any) => {
              if (response.success) {
                Swal.fire({
                  title: "Success",
                  text: "Candidate was moved successfully.",
                  type: "warning",
                  timer: 2000,
                  showConfirmButton: false
                });

                this.rounds[this.selectedTabIndex].candidateList[cardIndex] = response.data;
                //$scope.initBoard($scope.showMyCandidates);
                this.isCandidateCardMoved = false;
                // $scope.slideChecked = false;
                this.selectedEmails = [];
                this.rounds[this.selectedTabIndex].candidateList.splice(cardIndx, 1);
                this.loadPipeline(this.showCandidateOnborad);
              }
            }, error => {
              console.log('Error : ' + JSON.stringify(error));
              this.error = error;
            });
          }
          return cardData;
        } else {
          Swal.fire({
            title: "Alert on moving",
            text: "Only 'Approved' and 'Offered' candidate can move forward.",
            type: 'warning',
            confirmButtonText: 'Approve and move',
            showConfirmButton: true,
            showCancelButton: true,
            allowOutsideClick: false,
            reverseButtons: true
          }).then((result) => {
            if (result.value) {
              var approvedCheck = 'Approved';
              var input = {
                "candidateEmailList": [cardData.email],
                "status": 'Approved',
                "positionCode": this.positionCode
              }

              this._pipeline.changecandidateStatusforMove(input).pipe(first()).subscribe((response: any) => {
                if (response.success) {
                  if (this.isCandidateCardMoved || approvedCheck === 'Approved') {
                    if (cardIndex !== -1) {
                      this._pipeline.moveCandidatesWithStatus(roundId, dropRoundId, moveCandidateList, cardIndex, response.data).pipe(first()).subscribe((response: any) => {
                        if (response.success) {
                          Swal.fire({
                            title: "Success",
                            text: "Candidate moved to next stage.",
                            type: "warning",
                            timer: 2000,
                            showConfirmButton: false
                          });

                          this.rounds[this.selectedTabIndex].candidateList[cardIndex] = response.data;
                          //$scope.initBoard($scope.showMyCandidates);
                          this.isCandidateCardMoved = false;
                          // $scope.slideChecked = false;
                          this.selectedEmails = [];
                          this.rounds[this.selectedTabIndex].candidateList.splice(cardIndx, 1);
                          this.loadPipeline(this.showCandidateOnborad);
                        }
                      }, error => {
                        console.log('Error : ' + JSON.stringify(error));
                        this.error = error;
                      });
                    }
                  } else if (approvedCheck === 'Cancel') {
                    //$scope.slideChecked = false;
                    // $scope.isCandidateCardMoved = true;
                    this.selectedEmails = [];
                  }
                }
              }, error => {
                console.log('Error : ' + JSON.stringify(error));
                this.error = error;
              });
            }
          })
          return cardData;
        }
      } else if (this.orderNumber < this.dragContainerOrderId) {
        if (cardData.status !== "Approved" && cardData.status !== "Offered") {
          if (cardIndex !== -1) {
            this._pipeline.moveCandidates(roundId, dropRoundId, moveCandidateList, cardIndex).pipe(first()).subscribe((response: any) => {
              if (response.success) {
                Swal.fire({
                  title: "Success",
                  text: "Candidate was moved successfully.",
                  type: "warning",
                  timer: 2000,
                  showConfirmButton: false
                });

                this.rounds[this.selectedTabIndex].candidateList[cardIndex] = response.data;
                //$scope.initBoard($scope.showMyCandidates);
                this.isCandidateCardMoved = false;
                // $scope.slideChecked = false;
                this.selectedEmails = [];
                this.rounds[this.selectedTabIndex].candidateList.splice(cardIndx, 1);
                this.loadPipeline(this.showCandidateOnborad);
              }
            }, error => {
              console.log('Error : ' + JSON.stringify(error));
              this.error = error;
            });
          }
        }
      }
    }

  }

  public findArrayIndex(array: any, value: any) {
    for (var i = 0; i < array.length; i += 1) {
      if (array[i].email === value.email) {

        var startIndex: any, endIndex: any;
        if (i == 0) {
          startIndex = 0;
        } else {
          startIndex = array[i - 1].cardIndex;
        }
        if (array.length > 1) {
          if ((array.length - 1) == i) {
            endIndex = array[i].cardIndex + 1;
          } else {
            endIndex = array[i + 1].cardIndex;
          }
        } else
          endIndex = array[i].cardIndex;
        return this.getMovedCandidateCardIndex(startIndex, endIndex);
        break;
      }
    }
    return -1;
  }

  public getMovedCandidateCardIndex(startIndex: any, endIndex: any) {
    return (startIndex + endIndex) / 2
  }

  // Get all statusList
  public loadStatusList() {
    this._pipeline.getBoardStatusList().pipe(first()).subscribe(response => {
      if (response.success) {
        this.statusList = response.data;

        // this array useful for hover  multiselect status list
        this.multiStatusList = [];
        this.statusList.forEach((item: any) => {
          if (item.id != 'Joined' && item.id != 'Employee' && item.id != 'OfferAccepted') {
            this.multiStatusList.push(item);
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }

    });
  };

  // change status
  public changeSingleCardStatus(email: string, status: string, positionCode: any, dataList: any, category: string, roundCandidateId: any) {
    if (status === 'Joined') {

    } else if (status === 'Employee') {

    } else {
      var input = {
        "candidateEmailList": [email],
        "status": status,
        "positionCode": positionCode,
        "joiningDate": null
      };
      var forceChange = false;
      this._pipeline.changecandidateStatus(input, forceChange).pipe(first()).subscribe(response => {
        if (response.success) {
          if (status === 'Rejected') {
            Swal.fire({
              title: "Status changed successfully.",
              text: "Do you want to send a mail to the candidate ?",
              type: 'success',
              confirmButtonText: 'Yes',
              showConfirmButton: true,
              showCancelButton: true,
              allowOutsideClick: false,
              reverseButtons: true
            }).then((result) => {
              if (result.value) {
                this.openModalToSendMail(email, category, roundCandidateId);
                Swal.fire({
                  title: "Request taken.",
                  text: "Wait... processing your request.",
                  type: "success",
                  timer: 2000,
                  showConfirmButton: false
                });
              }
            })
          } else {
            Swal.fire({
              title: "Changed",
              text: "Status Chnage Successfully.",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
          }
        }
        if (response.reason === 'candidate_exists_with_employee_status') {
          Swal.fire({
            title: response.data,
            showCancelButton: true,
            showConfirmButton: false,
            showLoaderOnConfirm: true
          }).then((result) => {
            if (result.value) {
              var forceChange = true;
              this._pipeline.changecandidateStatus(input, forceChange).pipe(first()).subscribe(response => {
                if (response.success) {
                  Swal.fire({
                    title: "Changed",
                    text: "Status Chnage Successfully.",
                    type: "success",
                    timer: 2000,
                    showConfirmButton: false
                  });
                }
              })
            }

          })
        }
      })
    }
  }

  //Loading round name with id
  public loadRoundNameListView() {
    this._pipeline.getRoundName(this.positionCode).pipe(first()).subscribe(response => {
      if (response.success) {
        this.roundName = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    });
  };

  //open modal to add vendors
  public openModalToSendMail(email: any, category: any, roundCandidateId: any) {
    // let dialogRef = this.dialog.open(VendorPannelDialog, {
    //   width: '900px',
    //   data: { email: email, category: category, roundCandidateId: roundCandidateId },
    //   autoFocus: false
    // });

    // dialogRef.afterClosed().subscribe(result => {
    //   if (result === 'vendor-added') {
    //     Swal.fire({
    //       title: "Added",
    //       text: "Vendor(s) added to position successfully.",
    //       type: "success",
    //       timer: 2000,
    //       showConfirmButton: false
    //     });
    //   }
    // })
  }

  //open modal add edit stages
  public addEditStage() {
    let dialogRef = this.dialog.open(AddEditStagesDialog, {
      width: '700px',
      data: { positionCode: this.positionCode },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadPipeline(this.showCandidateOnborad);
      }
    })
  }

  //oopen modals to add existing candidates
  public addExistingCandidates() {
    let dialogRef = this.dialog.open(AddExistingCandidateDialog, {
      width: '800px',
      data: { boardId: this.responseData.boardId, positionCode: this.positionCode },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadPipeline(this.showCandidateOnborad);
      }
    })
  }

  //get interviewer pannels before scheduling interviews
  public scheduleInterview(data: any, category: any, intType: any) {
    this._position.getInterviewPanel(this.clientNameForSchecdule).pipe(first()).subscribe(response => {
      if (response.success) {
        this.interviewPanelList = response.data;
        this.loadInterviewPanelCandidate(data, category, intType);
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    });

  }

  //function to check if interview has alread been scheduled or not
  public loadInterviewPanelCandidate(data: any, category: any, intType: any) {
    if (this.utilService.isClientOrPositionActive(this.clientStatus, this.positionStatus)) {
      var isSchedule = data.activeScheduleCount > 0;
      var candidateEmail = [data.email];
      var positionCode = [this.positionCode];
      if (isSchedule) {
        this._interview.getInterviewcSchedule(positionCode, candidateEmail).pipe(first()).subscribe(response => {
          if (response.success) {
            this.interviewDetails = response.data.schedules;
            if (response.data.interviewerData) {
              this.interviewScheduleDetailModal(this.interviewDetails, this.positionCode);
            } else {
              this.interviewScheduleDetailModalCandidate(this.interviewDetails, this.positionCode);
            }
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        });
      } else {
        this.onOpenInterviewScheduleModalCandidate(data, category, intType);
      }
    }
  }

  //open modal to schedule an interview
  public onOpenInterviewScheduleModalCandidate(data: any, category: any, intType: any) {
    var roundData: any;
    this.responseData.rounds.forEach((value: any, key: any) => {
      if (this.selectedTabIndex === key) {
        roundData = value;
      }
    })

    let dialogRef = this.dialog.open(ScheduleInterviewDialog, {
      width: '95%',
      data: {
        "data": data,
        "roundData": roundData,
        "positionCode": this.positionCode,
        "interviewPanel": this.interviewPanelList,
        "category": category,
        "intType": intType
      },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadPipeline(this.showCandidateOnborad);
      }
    })

  }

  //this is used to show the popup of interview details if it already scheduled
  public interviewScheduleDetailModal(interviewDetails: any, positionCode: any) {

  }

  public interviewScheduleDetailModalCandidate(interviewDetails: any, positionCode: any) {

  }
}
