import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';


import { environment } from '../../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class IntegrationService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //get job portal info
  getJobPortalInformation() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/sixthsense/profile', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get sources
  getsixthSenseSources() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/sixthsense/sources', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // get job portal credentials
  getListPortalSourceCredentials(jobPortal: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/sixthsense/portal/credential/get?sources=' + jobPortal, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //connect to job portal
  addJobPortalIntegration(jobPortalUrl: any, clientId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/sixthsense/profile/add?sixthSenseUrl=' + jobPortalUrl + '&clientId=' + clientId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //disconnect job portal
  disconnectJobPortalIntegration() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/sixthsense/profile/disconnect', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //update sixth sense user
  public updatePortalSourceCredentials(portalData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/sixthsense/portal/credential/update', portalData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete sixth sense user
  public deleteSixthSenseUsers(input: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/sixthsense/portal/user/delete', input, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //resolve otp
  resolveOTPSixthSense(inputData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/sixthsense/resolve/otp', inputData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //check otp
  checkOTPSixthSense(inputData: any, source: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/sixthsense/check/otp?source=' + source, inputData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //This API is used to add mail gun integration details
  addMailGunIntegration(mailGunDomain: any, mailGunApiKey: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/mailgun/bulk/email/account/settings?domain=' + mailGunDomain + '&apiKey=' + mailGunApiKey, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //This API is used to disconnect
  disconnectMailGunIntegration() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/mailgun/bulk/email/account/settings', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //This API is test mail
  getMailGunInfo() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/mailgun/bulk/email/account/settings/test', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // get ivr details
  getIvrDetails(userEmail: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/knowlarity/integration/getByTenantName?tenantName=' + userEmail, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // post ivr details
  postIvrDetails(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/knowlarity/setting/addIntegration', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // delete ivr details
  deleteIvrDetails(ivrId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/knowlarity/integration/deleteKnowlarityIntegration?id=' + ivrId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get sixth sense profile
  getJobPortalInfo() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/sixthsense/profile', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get sixth usage type
  getUsageType() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/sixthsense/view/usage', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //enable  SixthSense Users
  enableSixthSenseUsers(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/sixthsense/admin/user/create', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //disable SixthSense Users
  disableSixthSenseUsers(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/sixthsense/admin/user/delete', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //update User Usage
  updateUserUsage(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/sixthsense/admin/user/viewusage/update', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }
}
