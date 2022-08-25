import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';


import { environment } from '../../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TeamService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //get all team
  getTeams() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/team/list/all', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get team list by id
  getAllTeamLists(teamId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/team/' + teamId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get all team list
  fetchAllTeamLists() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/team/list/details/all', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get user list by team id
  getTeamUsers(team: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/team/list/user?teamId=' + team, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //create a new team
  createTeam(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/team', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //update a team
  updateTeam(formData: any, teamId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/team/' + teamId, formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  deleteTeamInfo(teamId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/team/' + teamId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get team details by id
  getTeamDetails(teamId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/team/' + teamId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //save members info
  saveMember(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/team/member', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //remove team member
  removeMember(formData: any, teamId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/team/remove/members/' + teamId, formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //add member to team
  addMember(formData: any, teamId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/team/add/members/' + teamId, formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //add member to team
  getTeamStructure(teamId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/team/structure/' + teamId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

}
