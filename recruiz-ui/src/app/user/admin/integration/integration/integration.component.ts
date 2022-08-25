import { Component, OnInit } from '@angular/core';

import { Router, ActivatedRoute } from '@angular/router';
import { first } from "rxjs/operators";
import Swal from 'sweetalert2';
import { MatDialog } from '@angular/material/dialog';

// services
import { IntegrationService } from '../integrationService/integration.service';

@Component({
  selector: 'app-integration',
  templateUrl: './integration.component.html',
  styleUrls: ['./integration.component.css']
})
export class IntegrationComponent implements OnInit {

  constructor(
    private router: Router,
    public dialog: MatDialog,
    private _integration: IntegrationService
  ) { }

  error: any = '';
  globalData: any;
  jobPoratTab: boolean = true;
  activeTab: string = 'jobportal';
  ivrPortalTab: boolean;
  jobPortalUrl: string = null;
  clientId: string = null;
  jobPortalFlag: any = false;
  portalButtonText: any = "Connect";
  sourceList: any;
  portalDataList: any;
  filterJobSourceChange: any = undefined;
  filterJobSource: any = '';
  showLoginStatusCol: any;
  filterOTPSource: any;
  isAllSelected: any;
  otpSubmitBtn: string;
  submitBtnHide: any;
  otpSourceDataList: any;
  resolvedCount: any;
  checkOtpSourceList: any;
  sourceIdList: any;
  checkOTPResponse: any;
  ivrDetails: any;
  isIVR: any;
  showIvrDetails: any;
  isIntergrationFormField: any;
  ivr: any = {};
  portalMailGunButtonText: string = 'Connect';
  mailGunDomain: string;
  mailGunApiKey: string;
  mailGunFlag: any = false;
  mailGunEmail: string;

  ngOnInit() {
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
    this.initJobPortalIntegration();
  }

  //switch between intergration tabs
  public switchPortal(tabType: any) {
    if (tabType === 'Portal') {
      this.jobPoratTab = true;
      this.ivrPortalTab = false;
      this.activeTab = 'jobportal';
      this.initJobPortalIntegration();
    } else {
      this.ivrPortalTab = true;
      this.jobPoratTab = false;
      this.activeTab = 'ivrInt';
      this.loadIvrIntergeration();
    }
  }
  //load portal integration
  public initJobPortalIntegration() {
    this._integration.getJobPortalInformation().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        var responseData = response.data;
        if (responseData) {
          this.jobPortalFlag = true;
          this.portalButtonText = "Disconnect";
          this.getsixthSenseSources();
          this.jobPortalUrl = responseData.integrationDetails.sixthSenseBaseUrl;
          this.clientId = responseData.integrationDetails.clientId;
        } else {
          this.portalButtonText = "Connect";
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
        var sourceArray = [];
        this.sourceList = response.data;
        this.sourceList.forEach((source: any) => {
          sourceArray.push(source.id);
        })
        var jobPortal = sourceArray.join();
        this.getListPortalSourceCredentials(jobPortal);
      } else {
        if (response.reason == 'portal_server_down') {
          Swal.fire({
            title: "Warning",
            text: "Unable to connect. Portal Server Down",
            type: "warning",
            showConfirmButton: true
          });
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //job portal credentials
  public getListPortalSourceCredentials(prtalData: any) {
    this._integration.getListPortalSourceCredentials(prtalData).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.portalDataList = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //connect to portal
  public jobPortalConnectDisconnect(jobPortalUrl: any, clientId: any, isConnected: boolean) {
    if (isConnected) {
      this._integration.disconnectJobPortalIntegration().pipe(first()).subscribe((response: any) => {
        if (response.success) {
          Swal.fire({
            title: "Success",
            text: "Job portal disconnected successfully.",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
          this.jobPortalFlag = false;
          this.clientId = null;
          this.jobPortalUrl = null;
          this.portalButtonText = "Connect";
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    } else {
      if (!jobPortalUrl && !clientId) {
        Swal.fire({
          title: "Warning",
          text: "Please enter job portal url and client Id",
          type: "warning",
          showConfirmButton: true
        });
        return false;
      }

      this._integration.addJobPortalIntegration(jobPortalUrl, clientId).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          Swal.fire({
            title: "Success",
            text: "Job portal connected successfully.",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
          var responseData = response.data;
          if (responseData) {
            this.jobPortalUrl = responseData.integrationDetails.sixthSenseBaseUrl;
            this.clientId = responseData.integrationDetails.clientId;
            this.jobPortalFlag = true;
            this.getsixthSenseSources();
            this.portalButtonText = "Disconnect";
          }
        } else {
          if (response.reason == 'portal_server_down' || response.reason == 'invalid_job_portal_url') {
            Swal.fire({
              title: "Warning",
              text: "Unable to connect to the specified URL. Please check and retry.",
              type: "warning",
              showConfirmButton: true
            });
          }
          if (response.reason == 'ss_licence_expired') {
            Swal.fire({
              title: "Warning",
              text: "Job Portal Search license is expired",
              type: "warning",
              showConfirmButton: true
            });
          }
          if (response.reason == 'ss_client_id') {
            Swal.fire({
              title: "Warning",
              text: "Unable to Connect due to invalid client id",
              type: "warning",
              showConfirmButton: true
            });
          }
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    }
  }

  // active tab
  public activeTabEvent(event: any) {
    if (event.tab.textLabel === 'OTP Solver') {
      this.filterOTPSource = this.sourceList[0].id;
      this.changeOTPFilter(this.filterOTPSource);
    }
  }

  // change sources
  public changeOTPFilter(otpSource: any) {
    this.isAllSelected = false;
    this.showLoginStatusCol = false;
    this.otpSubmitBtn = "Solve Source OTP";
    this.submitBtnHide = false;
    this.otpSourceDataList = [];
    this.portalDataList.forEach((item: any) => {
      if (item.source == otpSource) {
        this.otpSourceDataList.push(item);
      }
    })
  }

  //add souce fields
  public addSourceTab() {
    var selectedSource: any;
    if (this.filterJobSourceChange) {
      selectedSource = this.filterJobSourceChange;
    } else {
      selectedSource = "";
    }
    var newTab = {
      source: selectedSource,
      sourceUserId: "",
      password: "",
      otpEmailID: "",
      otpEmailPwd: ""
    };
    this.portalDataList.push(newTab);
  }

  //save settings
  public updatePortalSourceCredentials() {
    this._integration.updatePortalSourceCredentials(this.portalDataList).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Success",
          text: "Job Portal Source Setting has been saved successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.getsixthSenseSources();
      } else {
        Swal.fire({
          title: "Warning",
          text: "Job Portal Source Setting did not save successfully",
          type: "warning",
          showConfirmButton: true
        });
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //remove source fields
  public removeSourceTab(index: any, userEmail: any) {
    if (userEmail) {
      var bodyData = {
        "users": [userEmail]
      }
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
          this._integration.deleteSixthSenseUsers(bodyData).pipe(first()).subscribe((response: any) => {
            if (response.success) {
              Swal.fire({
                title: "Success",
                text: "User has been removed successfully.",
                type: "success",
                timer: 2000,
                showConfirmButton: false
              });
              this.portalDataList.splice(index, 1);
            }
          }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
          })
        }
      })
    } else {
      this.portalDataList.splice(index, 1);
    }
  }

  //select all
  public toggleAll(isAllChecked: any) {
    this.otpSourceDataList.forEach((item: any) => {
      item.selected = isAllChecked;
    })
  }

  //select one by one
  public otpSourceToggled(isChecked: any, optData: any) {
    if (this.isAllSelected) {
      this.isAllSelected = false;
    }
    this.otpSourceDataList.forEach((item: any) => {
      if (optData.sourceUserId === item.sourceUserId) {
        item.selected = isChecked;
      }
    })
  }

  //solve otp
  public resolveOTP() {
    var blankOPTCheck = false;
    var resolveOTPMap = {};
    this.otpSourceDataList.forEach((itm: any) => {
      if (!itm.submitOtp && !itm.resolved) {
        blankOPTCheck = true;
        return false;
      }
      itm.hiddenParameterMap["otpMobilePassword"] = itm.submitOtp;
      resolveOTPMap[itm.sourceUserId] = itm.hiddenParameterMap;
    })

    if (!blankOPTCheck) {
      var inputData = {
        'source': this.filterOTPSource,
        'resolveOtpMap': resolveOTPMap
      };

      this._integration.resolveOTPSixthSense(inputData).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          this.resolvedCount = response.data.resolvedCount;
          if (this.otpSourceDataList.length == this.resolvedCount) {
            this.submitBtnHide = true;
          }
          var resolveOTPResponse = response.data.gridData;
          this.otpSourceDataList = resolveOTPResponse;
          this.otpSubmitBtn = "Submit OTP";
          this.showLoginStatusCol = true;
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    } else {
      Swal.fire({
        title: "Warning",
        text: "OTP Field should not be empty",
        type: "warning",
        showConfirmButton: true
      });
    }


  }

  //check otp
  public checkOTP() {
    this.checkOtpSourceList = [];
    this.sourceIdList = [];

    this.otpSourceDataList.forEach((itm: any) => {
      if (itm.selected == true) {
        this.sourceIdList.push(itm.sourceUserId);
      }
    })

    if (this.sourceIdList <= 0) {
      Swal.fire({
        title: "Warning",
        text: "'Please select at least one source'",
        type: "warning",
        showConfirmButton: true
      });
      return false;
    }

    this._integration.checkOTPSixthSense(this.sourceIdList, this.filterOTPSource).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.resolvedCount = response.data.resolvedCount;
        this.checkOTPResponse = response.data.gridData;
        this.otpSourceDataList = this.checkOTPResponse;
        this.isAllSelected = true;
        this.otpSubmitBtn = "Submit OTP";
        this.showLoginStatusCol = true;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //mail gun connect
  public mailGunConnect() {
    if (!this.mailGunDomain) {
      Swal.fire({
        title: "Please enter Mailgun url.",
        type: "warning",
        showConfirmButton: true,
        showCancelButton: false,
      });
      return false;
    }
    if (!this.mailGunApiKey) {
      Swal.fire({
        title: "Please enter Mailgun api key",
        type: "warning",
        showConfirmButton: true,
        showCancelButton: false,
      });
      return false;
    }

    this._integration.addMailGunIntegration(this.mailGunDomain, this.mailGunApiKey).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        var responseData = response.data;
        if (responseData) {
          this.mailGunFlag = true;
          this.portalMailGunButtonText = "Disconnect";
          this.mailGunEmail = response.data.userEmail;
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //disconnect mail gun
  public mailGunDisconnect() {
    this._integration.disconnectMailGunIntegration().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.mailGunFlag = false;
        this.mailGunDomain = null;
        this.mailGunApiKey = null;
        this.portalMailGunButtonText = "Connect";
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //testing maail
  public initMailGunIntegration() {
    this._integration.getMailGunInfo().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        var responseData = response.data;
        if (responseData) {
          this.mailGunFlag = true;
          Swal.fire({
            title: "Success",
            text: response.data,
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
          this.portalMailGunButtonText = "Disconnect";
        } else {
          this.portalMailGunButtonText = "Connect";
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }


  //ivr details strats
  public loadIvrIntergeration() {
    this._integration.getIvrDetails(this.globalData.orgId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.ivrDetails = response.data;
        this.showIvrDetails = true;
        this.isIVR = true;
      } else {
        this.isIVR = false;
        this.isIntergrationFormField = false;
        this.showIvrDetails = false;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  // show fields
  public addIVRDetails() {
    this.isIVR = true;
    this.showIvrDetails = false;
    this.isIntergrationFormField = true;
  }
  //back to form
  public backToDetails() {
    this.isIVR = false;
    this.isIntergrationFormField = false;
  }

  //add details
  public submitIVRDetails() {
    if (!this.ivr.SerialNumber || !this.ivr.CallerId || !this.ivr.AuthorizationKey || !this.ivr.APIkey) {
      Swal.fire({
        title: "Please fill all (*) mandatory fields.",
        type: "warning",
        showConfirmButton: true,
        showCancelButton: false,
      });
      return true;
    }
    var formData = {
      srNumber: this.ivr.SerialNumber,
      callerId: this.ivr.CallerId,
      authorizationKey: this.ivr.AuthorizationKey,
      xApikey: this.ivr.APIkey
    }
    this._integration.postIvrDetails(formData).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Success",
          text: "IVR details were intergrated successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.isIntergrationFormField = false;
        this.isIVR = true;
        this.loadIvrIntergeration();
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //delete ivr details
  public deleteIVRDetails(ivrId: any) {
    Swal.fire({
      title: "Are you sure you want to delete ?",
      text: "All setting for this IVR will be deleted!",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this._integration.deleteIvrDetails(ivrId).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "IVR setting has beeen deleted successfully.",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.loadIvrIntergeration();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }


}
