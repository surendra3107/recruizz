import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';


import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class GlobalService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //get all interviewer pannel
  getAllInterviewer() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/generic/interviewer/get/all', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //generic interviewer add global
  addInterviewerGlobal(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/generic/interviewer/add', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //generic interviewer delete global
  deleteInterviewerGlobal(id: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/generic/interviewer/delete/' + id, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //generic interviewer validate duplicate email
  validateDuplicateEmail(email: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/generic/interviewer/email/check?emailId=' + email, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //add interviewer pannel to position
  addSelectedInterviewerToPosition(bodyData: any, positionId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/generic/interviewer/position/' + positionId + '/add', bodyData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //update interviewer pannel to position
  updateSelectedInterviewerToPosition(bodyData: any, email: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/generic/interviewer/update/' + email, bodyData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get team list
  getAllTeam() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/team/list/details/all', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get all HRS
  getAllHrs() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/user/hr', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get all descision maker
  getAllDecisionMaker() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/generic/dm/get/all', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get candidate resume in pdf file
  getCandidateResume(resumePath: any) {
    let headers: HttpHeaders = new HttpHeaders();
    let params = new HttpParams();
    params = params.append('fileName', resumePath);
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/candidate/files/download', { params: params, headers: headers, responseType: 'arraybuffer' as 'json' })
      .pipe(map(user => {
        return user;
      }));
  }

  //bulk upload xls file user management
  importUserData(dataFile: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/user/bulkupload', dataFile, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //validate user email id
  validateGlobalUserEmail(email: any, type: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/user/exists?email=' + email + '&userType=' + type, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

}
