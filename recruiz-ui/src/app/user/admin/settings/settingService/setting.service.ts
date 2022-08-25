import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';


import { environment } from '../../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SettingService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //get dashboard data
  getOrganizationInformation() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/organization', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //post dashboard data
  submitOrganizationInformation(file: any, settingData: any) {
    let headers = new HttpHeaders();
    headers.set('Content-Type', undefined);
    headers.set('Accept', "multipart/form-data");
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/org/info/', file, {
      reportProgress: true, headers
    }).pipe(map(user => {
      return user;
    }));
  }

  //delete account
  deleteOrgAccount(state: any, days: any, password: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/organization/markdelete?markForDeleteState=' + state + '&days=' + days + '&password=' + password, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get candidate time range
  loadTimePeriod() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/candidate/modification/range', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //enable/disabled cinadidate duolicate while searching
  changeStatusForDuplicate(value: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/duplicate/candidate/check/changeDuplicateCheckStatus?status=' + value, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  // post report status true false
  postReportStatusTrueFalse(value: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/organization/settings/enable/custom/report?enabled=' + value, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //load custom report name list
  getCustomReportList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/custom/Reports/getSelectedCustomReportList', { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //save custom report name list
  saveCustomReportList(nameList: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/custom/Reports/selectedCustomReports?textArray=' + nameList, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  // get getOverallReport
  downloadOverallReport() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/report/pipeline/all/excel', { headers: headers, responseType: 'arraybuffer' as 'json' })
      .pipe(map(user => {
        return user;
      }));
  }

  // get getOverallReport client Position
  downloadOverallReportClientPostion() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/report/client/position/excel', { headers: headers, responseType: 'arraybuffer' as 'json' })
      .pipe(map(user => {
        return user;
      }));
  }

  //schedule time period
  changeTimePeriodSchedule(time: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/report/client/position/saveCustomReportTimePeriod?reportTimePeriod=' + time, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //get scheduled status
  checkAllStageAllStatusReportStatus() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/report/client/position/checkReportStatus', { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //get scheduled status default
  getReportTimePeriodByDefault() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/report/client/position/getReportTimePeriodByDefault', { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  // get download Overall Stage Status Report
  downloadOverallStageStatusReport() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/report/client/position/allstagestatus', { headers: headers, responseType: 'arraybuffer' as 'json' })
      .pipe(map(user => {
        return user;
      }));
  }

  // get custom fields
  fetchCustomFields(entityType: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/fields/custom/all/' + entityType, { headers: headers }).pipe(map(user => {
      return user;
    }));
  };

  // add custom fields
  postCustomFields(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/fields/custom/add', formData, { headers: headers }).pipe(map(user => {
      return user;
    }));
  };

  // update custom fields
  updateCustomFields(formData: any, customFieldId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/fields/custom/edit/' + customFieldId, formData, { headers: headers }).pipe(map(user => {
      return user;
    }));
  };

  // delete custom fields
  deleteCustomField(filedId: any, entityType: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/fields/custom/delete/' + filedId + '/' + entityType, { headers: headers }).pipe(map(user => {
      return user;
    }));
  };

  getBoardInfoCustom() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/round/custom/all', { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  // add custom pipeline stages
  postBoardInfoCustom(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/round/custom/add', formData, { headers: headers }).pipe(map(user => {
      return user;
    }));
  };

  //delete stages
  deleteCustomStages(stageId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/round/custom/delete/' + stageId, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //get custom status
  getCustomStatus() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/board/custom/status/all', { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //add custom staus
  postCustomStatus(customStatus: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/board/custom/status/add?customStatus=' + customStatus, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  // update custom status
  updateCurrentCustomStatus(customStatus: any, statusId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/board/custom/status/edit/' + statusId + '?newStatusStatus=' + customStatus, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //delete custom status
  deleteCurrentCustomStatus(statusId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/board/custom/status/delete/' + statusId, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //get offer letter templates
  getOfferLetterTemplateList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/offer/getOfferTemplateList', { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //select offer letter templates
  selectOfferLeterTemp(tempId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/offer/selectOfferTemplateById?offerTemplateId=' + tempId, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //delete offer letter templates
  deleteAddedOfferLeter(offerId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/offer/deleteOfferTemplateById?offerTemplateId=' + offerId, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  // get bank account dtails
  loadBankDetails() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/org/bank', { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  // add bank account dtails
  postBankDetails(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/org/bank/add', formData, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //set bank as default
  setBankAsDefault(defaultBank: any, bankDetailsId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/org/bank/set/default/' + bankDetailsId + '/?flag=' + defaultBank,
      { headers: headers }).pipe(map(user => {
        return user;
      }));
  }

  //update bank details
  updateBankDetails(formData: any, bankId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/org/bank/' + bankId, formData, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //delete bank info
  deleteBankInfo(bankId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/org/bank/' + bankId, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  // get tax dtails
  loadTaxDetails() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/org/tax', { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //add tax info
  postTaxDetails(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/org/tax/add', formData, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //delete tax
  deleteOtherTaxes(taxId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/org/tax/' + taxId, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

}
