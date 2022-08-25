import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';


import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DropdownService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //get list of hrs
  getHrsList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/user/hr', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get all clients
  getClientList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/client/name', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //time range
  getTimeRangeList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/report/timeperiod', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // status list for position
  getCommanStatusList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/status/all', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // Employment Type List for position
  getEmploymentTypeList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/jobtype', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // close by date for position
  getCloseByDateList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/closebydate', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // get ReqPosition Status List for position
  getReqPositionStatusList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/status/position/request', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // get source list
  getSourceList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/source', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }
 
  // prospect status
  getProspectStatusList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/prospect/status', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }
}
