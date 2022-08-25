import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';


import { environment } from '../../../../environments/environment';
@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }


  //get dashboard data
  getdashBoardInfo() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/dashboard', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }


  //candidate Databse Growth
  getcandidateDatabseGrowth(timePeriod: string, teamId: any) {
    var data = {
      "timePeriod": timePeriod
    }
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/report/candidate/pool?teamIds=' + teamId, data, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get team list
  getTeamList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/team/list', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get candidate channel source mix
  getCandidateSoucePool() {
    var data = {
      "timePeriod": 'Last_6_Months'
    }
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/report/candidate/pool/sourcing', data, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }


  //get recruitment status report
  getRecruitmentStatusReport(hrEmail: any, clientName: any, timeRange: string, startDate: any, endDate: any) {
    var data = {
      timePeriod: timeRange,
      startDate: startDate,
      endDate: endDate
    }
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/report/client/all/position/recruiters/all?clients=' + clientName + '&userEmails=' + hrEmail, data, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }


}