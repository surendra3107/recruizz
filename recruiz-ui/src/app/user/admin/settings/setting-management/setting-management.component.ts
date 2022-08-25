import { Component, OnInit } from '@angular/core';

import { Router, ActivatedRoute } from '@angular/router';
import { first } from "rxjs/operators";
import Swal from 'sweetalert2';
import { MatChipInputEvent } from '@angular/material/chips';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { MatDialog } from '@angular/material/dialog';
import { BankDetailDialog } from '../setting-modals/bank-detail.component';
import { TaxDetailDialog } from '../setting-modals/tax-detail.component';

//service
import { SettingService } from '../settingService/setting.service';
@Component({
  selector: 'app-setting-management',
  templateUrl: './setting-management.component.html',
  styleUrls: ['./setting-management.component.css']
})
export class SettingManagementComponent implements OnInit {

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private _setting: SettingService,
    public dialog: MatDialog
  ) { }

  globalData: any;
  activeMenu: string = 'basic-details';
  showMenuItem: string = 'basic-details';
  error: string = '';
  orgSettingData: any = {};
  fileDetails: any;
  localImageLogo: any;
  reportStatus: any;
  logoFile: any;
  markForDelete: any
  isDuplicate: any;
  candidateModificationDays: any;
  timePriods: any;
  reportTypeDropdown: Array<any> = [];
  customReportList: Array<any> = [];
  addedCustomReportName: any;
  timeRange: string = 'One_week';
  scheduledReport: any;
  isReportReady: any;
  isScheduledReport: boolean = true;
  translateNameSingle: string;
  clientCustomFields: any;
  positionCustomFields: any
  candidateCustomFields: any
  prospectsCustomFields: any
  employeesCustomFields: any
  dropList: any;
  optionList: any = [];
  stageName: string;
  customRoundData: any;
  allCustomStatus: any;
  customStatusName: string;
  customStatusId: any;
  templateList: any;
  bankDetailList: any;
  taxListDetails: any;
  panelOpenState: any;

  ngOnInit() {
    this.loadOrgDetails();
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    if (this.globalData.orgType === "Corporate") {
      this.translateNameSingle = 'Department';
    } else {
      this.translateNameSingle = 'Client';
    }

    // custom report drop down
    this.reportTypeDropdown = [
      {
        "id": "Pipeline",
        "value": "Pipeline"
      },
      {
        "id": "BizAnalysis",
        "value": "Biz Analysis"
      },
      {
        "id": "RecruiterPerformance",
        "value": "Recruiter Performance"
      },
      {
        "id": "ClientAnalysis",
        "value": "Client Analysis"
      },
      {
        "id": "PerformanceTrends",
        "value": "Performance Trends"
      },
      {
        "id": "ResumeSubmission",
        "value": "Resume Submission"
      },
      {
        "id": "RecruitersReport",
        "value": "Recruiter Productivity"
      },
      {
        "id": "clientProductivity",
        "value": "Client Productivity"
      }
    ];

  }

  //basic details
  public basicDetails() {
    this.showMenuItem = 'basic-details';
    this.activeMenu = 'basic-details';
    this.loadOrgDetails();
  }

  //address details
  public addressDetails() {
    this.showMenuItem = 'address-details';
    this.activeMenu = 'address-details';
    this.loadOrgDetails();
  }

  //social links
  public socialLinks() {
    this.showMenuItem = 'social-links';
    this.activeMenu = 'social-links';
    this.loadOrgDetails();
  }

  //accounts detais
  public accountsDetails() {
    this.showMenuItem = 'accounts';
    this.activeMenu = 'accounts';
  }

  //preference detais
  public preferences() {
    this.showMenuItem = 'preference';
    this.activeMenu = 'preference';
    this.candidateUpdateTimeRange();
  }

  //custom report
  public customReport() {
    this.showMenuItem = 'custom-report';
    this.activeMenu = 'custom-report';
    this.loadCustomReport();
    this.getReportStatus();
    this.getReportStatusDefault();
  }

  //custom fileds
  public customFileds() {
    this.showMenuItem = 'custom-fields';
    this.activeMenu = 'custom-fields';
  }

  //custom pipeline
  public customPipeline() {
    this.showMenuItem = 'custom-pipeline';
    this.activeMenu = 'custom-pipeline';
    this.initBoardForCustomRound();
  }

  //custom status
  public customStatus() {
    this.showMenuItem = 'custom-status';
    this.activeMenu = 'custom-status';
    this.loadCustomStatus();
  }

  //offer letter
  public offerLetter() {
    this.showMenuItem = 'offer-letter';
    this.activeMenu = 'offer-letter';
    this.getOfferLetterTemplates();
  }

  //bank details
  public bankDetails() {
    this.showMenuItem = 'bank-detail';
    this.activeMenu = 'bank-detail';
    this.getBankDetails();
  }

  //tax details
  public taxDetails() {
    this.showMenuItem = 'tax-detail';
    this.activeMenu = 'tax-detail';
    this.getTaxDetails();
  }

  //get org details
  public loadOrgDetails() {
    this._setting.getOrganizationInformation().pipe(first()).subscribe(response => {
      if (response.success) {
        this.orgSettingData = response.data;
        this.reportStatus = this.orgSettingData.organizationConfiguration.customReportEnabled;
        this.logoFile = response.data.logoUrlPath;
        this.markForDelete = response.data.markForDelete;
        if (response.data.duplicateCheck === 'yes') {
          this.isDuplicate = true;
        } else if (response.data.duplicateCheck === 'no') {
          this.isDuplicate = false;
        }
        if (this.orgSettingData.candidateModificationDays === null || this.orgSettingData.candidateModificationDays === undefined) {
          this.candidateModificationDays = '90';
        } else {
          this.candidateModificationDays = this.orgSettingData.candidateModificationDays.toString();
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //choose logo
  public onSelectedFilesChanged(event: any) {
    if (event) {
      this.fileDetails = event[0];
      if (this.fileDetails.size < 102400) {
        this.localImageLogo = this.fileDetails;
      } else {
        Swal.fire({
          title: "File size exceeded.",
          text: "Max width should be 240X 100 and max size of 100kb.",
          type: 'warning',
          showConfirmButton: true,
          showCancelButton: false,
          allowOutsideClick: false
        })
        event = null;
      }

    }
  }
  //save edited org setting
  public saveOrgSettings() {
    if (this.localImageLogo !== undefined && this.localImageLogo !== null) {
      var fd = new FormData();
      fd.append('file', this.localImageLogo._file);
      var fileName = this.localImageLogo.name;
    } else {
      var fd = new FormData();
      fd.append('file', null);
      if (fileName !== "") {
        fileName = this.logoFile; // give here existing file path
      } else {
        fileName = "";
      }
    }

    var input = {
      "orgName": this.orgSettingData.orgName,
      "websiteUrl": this.orgSettingData.websiteUrl,
      "facebookUrl": this.orgSettingData.facebookUrl,
      "twitterUrl": this.orgSettingData.twitterUrl,
      "linkedInUrl": this.orgSettingData.linkedInUrl,

      "panNo": this.orgSettingData.panNo,
      "gstNo": this.orgSettingData.gstId,
      "addressL1": this.orgSettingData.addressL1,
      "addressL2": this.orgSettingData.addressL2,
      "city": this.orgSettingData.city,
      "pincode": this.orgSettingData.pincode,
      "country": this.orgSettingData.country,
      "state": this.orgSettingData.state,
      "phone": this.orgSettingData.phone,

      "fileName": fileName,
      "candidateNodificationDate": this.candidateModificationDays
    };
    fd.append('file', new Blob([JSON.stringify(input)], {
      type: "application/json"
    }));


    this._setting.submitOrganizationInformation(fd, input).pipe(first()).subscribe((response: any) => {
      if (response.success) {

      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //delete account
  public deleteAccount(state: any) {
    var days = 1;
    if (state) {
      var successHeader = "Marked as delete!";
      var successMsg = "Your organization account has been successfully marked for delete. The delete action will automatically trigger after" + " " + days + " days";
      this.deleteAccountSweetAlert(state, days, "Delete Account", "All data (including that of all users) will be deleted and lost permenantly if you go ahead and delete! Are your sure?", successHeader, successMsg);
    } else {
      var successHeader = "Revoked Account!";
      var successMsg = "Your organization account has been successfully revoked";
      this.deleteAccountSweetAlert(state, days, "Revoke Account", "All data (including that of all users) will be restored", successHeader, successMsg);
    }
  }

  public deleteAccountSweetAlert(state: any, days: any, title: any, text: any, successHeader: any, successMsg: any) {
    Swal.fire({
      title: title,
      text: text,
      input: 'password',
      inputAttributes: {
        autocapitalize: 'off'
      },
      showCancelButton: true,
      reverseButtons: true,
      confirmButtonText: 'Procees',
      showLoaderOnConfirm: true,
      preConfirm: (password) => {
        this._setting.deleteOrgAccount(state, days, password).pipe(first()).subscribe((response: any) => {
          if (!response.success) {
            Swal.fire({
              title: "Warning",
              text: "Password is invalid",
              type: "warning",
              showConfirmButton: true
            });
          } else {
            Swal.fire({
              title: successHeader,
              text: successMsg,
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
          }
        })
      }
    })
  }

  // preference
  public candidateUpdateTimeRange() {
    this._setting.loadTimePeriod().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.timePriods = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //enable/disabled cinadidate duolicate while searching'
  public checkDuplicatesTrueFalse(status: any) {
    if (status === true) {
      var isStatusActive = 'yes';
    } else {
      var isStatusActive = 'no'
    }

    this._setting.changeStatusForDuplicate(isStatusActive).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Success",
          text: "Status changed successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
      } else {
        Swal.fire({
          title: "Failure",
          text: "Error while changing status",
          type: "error",
          showConfirmButton: true
        });
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //enable/disabled cinadidate duolicate while searching'
  public reportStatusTrueFalse(status: any) {
    this._setting.postReportStatusTrueFalse(status).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        if (status === true) {
          Swal.fire({
            title: "Success",
            text: "Custom report turned on successfully.",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
        } else {
          Swal.fire({
            title: "Success",
            text: "Custom report turned off successfully.",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
        }

      } else {
        Swal.fire({
          title: "Failure",
          text: "Error while changing status",
          type: "error",
          showConfirmButton: true
        });
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //check uncheck custom report name
  public addReportListName(data: any) {
    if (data.status === true) {
      this.customReportList.push(data.id);
    } else {
      var index = this.customReportList.indexOf(data.id);
      if (index !== -1) {
        this.customReportList.splice(index, 1);
      };
    };
  };

  //load custom report list
  public loadCustomReport() {
    this._setting.getCustomReportList().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.addedCustomReportName = response.data;
        this.reportTypeDropdown.forEach((item: any) => {
          this.addedCustomReportName.forEach((val: any) => {
            if (item.id === val) {
              item.status = true;
              this.customReportList.push(item.id);
            }
          })
        })
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //call function to save report name
  public saveCustomReport() {
    if (this.customReportList.length === 0) {
      Swal.fire({
        title: "Alert",
        text: "Custom report name list is empty.",
        type: "warning",
        showConfirmButton: true,
        showLoaderOnConfirm: true
      });
      return true;
    }
    this._setting.saveCustomReportList(this.customReportList).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Success",
          text: "Custom report list name saved successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
      } else {
        Swal.fire({
          title: "Failure",
          text: "Error while changing status",
          type: "error",
          showConfirmButton: true
        });
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  };

  //download custom report
  public getOverallReport() {
    this._setting.downloadOverallReport().pipe(first()).subscribe((response: any) => {
      if (response) {
        var today = new Date();
        var month = today.getMonth() + 1;
        var d = today.getDate() + '/' + month + '/' + today.getFullYear();
        var fileName = 'OverallRecruitermentStats_' + d + '.xlsx';
        var blob = new Blob([response], {
          type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        });
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

  //download custom report client position
  public getOverallReportClientPostion() {
    this._setting.downloadOverallReportClientPostion().pipe(first()).subscribe((response: any) => {
      if (response) {
        var today = new Date();
        var month = today.getMonth() + 1;
        var d = today.getDate() + '/' + month + '/' + today.getFullYear();
        var fileName = 'AllPosition_StageReport_' + d + '.xlsx';
        var blob = new Blob([response], {
          type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        });
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

  //schedule report
  public changeTimePeriod(timeRange: any) {
    this._setting.changeTimePeriodSchedule(timeRange).pipe(first()).subscribe((response: any) => {
      if (response === null) {
        Swal.fire({
          title: "All Status and Stages Report",
          text: "Report has been scheduled. Please check back tomorrow.",
          type: "success",
          showConfirmButton: true
        });
        this.getReportStatusDefault();
      } else {
        Swal.fire({
          title: "Failure",
          text: "Error while changing status",
          type: "error",
          showConfirmButton: true
        });
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //get schedule report
  public getReportStatus() {
    this._setting.checkAllStageAllStatusReportStatus().pipe(first()).subscribe((response: any) => {
      if (response.success === true) {
        this.scheduledReport = true;
      } else {
        this.isReportReady = false;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //get schedule report degault
  public getReportStatusDefault() {
    this._setting.getReportTimePeriodByDefault().pipe(first()).subscribe((response: any) => {
      if (response.success === true) {
        this.timeRange = response.data;
        this.scheduledReport = "You have Scheduled " + response.data + " report."
      } else {
        this.isScheduledReport = false;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //download all stages report
  public getOverallStageStatusReport(timeRange: any) {
    this._setting.checkAllStageAllStatusReportStatus().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this._setting.downloadOverallStageStatusReport().pipe(first()).subscribe((dataResponse: any) => {
          var blob = new Blob([dataResponse], {
            type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
          });
          var objectUrl = URL.createObjectURL(blob);
          var a = document.createElement('a');
          a.href = objectUrl;
          a.target = '_blank';
          a.download = response.data;
          document.body.appendChild(a);
          a.click();
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      } else {
        if (response.data === "null") {
          Swal.fire({
            title: "Report Status",
            text: "No report available for download. Please schedule one.",
            type: "success",
            showConfirmButton: true
          });
        } else {
          Swal.fire({
            title: "Report Status",
            text: "Your " + timeRange + " Report is not ready, Please try later !",
            type: "success",
            showConfirmButton: true
          });
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //custom fileds
  // get custom fields
  public getCustomFileds(entityType: any) {
    this._setting.fetchCustomFields(entityType).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        if (entityType === 'Client') {
          this.clientCustomFields = response.data;
        };
        if (entityType === 'Position') {
          this.positionCustomFields = response.data;
        };
        if (entityType === 'Candidate') {
          this.candidateCustomFields = response.data;
        };
        if (entityType === 'Prospects') {
          this.prospectsCustomFields = response.data;
        };
        if (entityType === 'Employees') {
          this.employeesCustomFields = response.data;
        };
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  };

  //show hide custom fields
  input: any = {};
  clickType: any
  public activeCustomFields(fieldType: any) {
    this.clickType = fieldType;
    this.input = {}
    this.optionList = [];
    this.input.dataType = 'text';
  };

  //delete custom field
  public deleteCustomField(filedId: any, entityType: any) {
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
        this._setting.deleteCustomField(filedId, entityType).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Custom field has been deleted successfully",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.getCustomFileds(entityType);
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //add custom fileds
  public addCustomFileds(customFeilds: any, entityType: any, fieldId: any) {
    this.dropList = [];
    if (customFeilds.dataType === 'text') {
      var stringValues = customFeilds.feildString;
    } else {
      this.optionList.forEach((item: any) => {
        this.dropList.push(item.name);
      })
      var stringValues = this.dropList.join(', ')
    };
    var formData = [{
      "name": customFeilds.feildName,
      "entityType": entityType,
      "dataType": customFeilds.dataType,
      "dropDownValues": stringValues
    }];

    this._setting.postCustomFields(formData).pipe(first()).subscribe((response: any) => {
      Swal.fire({
        title: "Added",
        text: "Custom field has been added successfully",
        type: "success",
        timer: 2000,
        showConfirmButton: false
      });
      this.clickType = false;
      this.input = {};
      this.optionList = [];
      this.getCustomFileds(entityType);
    })
  }

  //edit custom field
  public updateCustomFields(customFeilds: any) {
    this.clickType = true;
    if (customFeilds.entityType === 'Client' || customFeilds.entityType === 'Position' || customFeilds.entityType === 'Candidate' || customFeilds.entityType === 'Prospects' || customFeilds.entityType === 'Employees') {
      this.input.feildName = customFeilds.name;
      this.input.dataType = customFeilds.dataType;
      if (this.input.dataType === 'text') {
        this.input.feildString = customFeilds.dropDownValues;
      } else {
        this.input.dropdown = customFeilds.dropDownValueList;
        this.input.dropdown.forEach((item: any) => {
          var list = {
            name: item
          }
          this.optionList.push(list);
        })
      };
      this.input.feildId = customFeilds.id;
    }
  }

  //update custom fileds
  public updateCustomFiledsData(customFeilds: any, entityType: any, customFeildId: any) {
    this.dropList = [];
    if (customFeilds.dataType === 'text') {
      var stringValues = customFeilds.feildString;
    } else {
      this.optionList.forEach((item: any) => {
        this.dropList.push(item.name);
      })
      var stringValues = this.dropList.join(', ')
    };
    var formData = {
      "name": customFeilds.feildName,
      "entityType": entityType,
      "dataType": customFeilds.dataType,
      "dropDownValues": stringValues
    };

    this._setting.updateCustomFields(formData, customFeildId).pipe(first()).subscribe((response: any) => {
      Swal.fire({
        title: "Added",
        text: "Custom field has been updated successfully.",
        type: "success",
        timer: 2000,
        showConfirmButton: false
      });
      this.clickType = false;
      this.input = {};
      this.optionList = [];
      this.getCustomFileds(entityType);
    })
  }

  visible = true;
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];
  add(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    // Add our fruit
    if ((value || '').trim()) {
      this.optionList.push({ name: value.trim() });
    }

    // Reset the input value
    if (input) {
      input.value = '';
    }
  }

  remove(fruit: any): void {
    const index = this.optionList.indexOf(fruit);

    if (index >= 0) {
      this.optionList.splice(index, 1);
    }
  }

  //get custom pipeline stages
  public initBoardForCustomRound() {
    this._setting.getBoardInfoCustom().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.customRoundData = response.data;
        this.stageName = "";
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //add custom pipeline stage
  public addBoardForCustomRound(stage: any) {
    var inputData = [{
      'name': stage,
      'orderNo': this.customRoundData.length + 1
    }];

    this._setting.postBoardInfoCustom(inputData).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Added",
          text: "Custom pipeline stages has been added successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.initBoardForCustomRound();
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //drag drop
  public drop(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.customRoundData, event.previousIndex, event.currentIndex);
  }

  //update custom pipeline stages
  newCustomList: any;
  public updateCustomStages(stageName: any, index: any) {
    this.newCustomList = [];
    this.customRoundData.forEach((item: any, key: any) => {
      var inputData = {
        'name': item.name,
        'orderNo': key + 1,
        'id': item.id
      };
      this.newCustomList.push(inputData);
    })

    this._setting.postBoardInfoCustom(this.newCustomList).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Added",
          text: "Stages updated successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.initBoardForCustomRound();
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //delete stage
  public deleteCustomStage(customStageId: any) {
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
        this._setting.deleteCustomStages(customStageId).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Stages deleted successfully",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.initBoardForCustomRound();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //get custom status
  public loadCustomStatus() {
    this._setting.getCustomStatus().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.allCustomStatus = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  };

  //add custom status
  public addCustomStatus(statusName: string) {
    var statusList = [statusName];
    this._setting.postCustomStatus(statusList).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Added",
          text: "Custom status added successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadCustomStatus();
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //update custom status
  public updateCustomStatus(customStat: any, updateStatus: any) {
    if (updateStatus === 'update') {
      this.customStatusName = customStat.statusName;
      this.customStatusId = customStat.id;
    };
  };
  //update custom status on api call
  public updateCurrentCustomStatus(statusName: any) {
    var statusList = [statusName];
    this._setting.updateCurrentCustomStatus(statusList, this.customStatusId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Added",
          text: "Custom status updated successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.customStatusId = null;
        this.customStatusName = null;
        this.loadCustomStatus();
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //delete custom status
  public deleteCustomStatus(statusId: any) {
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
        this._setting.deleteCurrentCustomStatus(statusId).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Status deleted successfully",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.loadCustomStatus();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  // get offer letter templates
  public getOfferLetterTemplates() {
    this._setting.getOfferLetterTemplateList().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.templateList = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //select offer letter template
  public selectTemplate(tempId: any) {
    this._setting.selectOfferLeterTemp(tempId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Success",
          text: "Template has been selected successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.getOfferLetterTemplates();
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //view added template
  public viewOfferLetter(tempId: any) {

  }

  //edit template
  public editOfferLetter(offerLetterId: any, templateData: any) {

  }

  //delete template
  public deleteOfferLetter(offerLetterId: any) {
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
        this._setting.deleteAddedOfferLeter(offerLetterId).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Template deleted successfully",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.getOfferLetterTemplates();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //get bank details
  public getBankDetails() {
    this._setting.loadBankDetails().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.bankDetailList = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //add bank details modal
  public addBankDetails(modalType: string, bankData: any) {
    let dialogRef = this.dialog.open(BankDetailDialog, {
      width: '500px',
      data: { modalType: modalType, bank: bankData },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        Swal.fire({
          title: "Added",
          text: "Bank details added successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.getBankDetails();
      }
    })
  }

  //change default bank
  public handleRadioClick(defaultBank: any, bankDetailsId: any) {
    this._setting.setBankAsDefault(defaultBank, bankDetailsId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Changed",
          text: "Bank set as default successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.getBankDetails();
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //delete bank details
  public deleteBankDetails(bankId: any) {
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
        this._setting.deleteBankInfo(bankId).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Bank details deleted successfully",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.getBankDetails();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //get tax list
  public getTaxDetails() {
    this._setting.loadTaxDetails().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.taxListDetails = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //add bank details modal
  public addTaxDetails(modalType: string, bankData: any) {
    let dialogRef = this.dialog.open(TaxDetailDialog, {
      width: '500px',
      data: { modalType: modalType, bank: bankData },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        Swal.fire({
          title: "Added",
          text: "Tax details added successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.getTaxDetails();
      }
    })
  }

  //delete tax
  public deleteTaxes(taxId: any) {
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
        this._setting.deleteOtherTaxes(taxId).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Tax details deleted successfully",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.getTaxDetails();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

}
