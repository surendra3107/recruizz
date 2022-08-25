import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';


import { environment } from '../../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PermissionService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //get dashboard data
  getAllPermissionwithRole() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/userRoles/roles/permissions/all', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //update permission
  changePermission(input: any, individualAssign: any, allAssign: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/userRoles/changeRolePermissions?assignRole=' + individualAssign + '&allAssign=' + allAssign, input, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }


}
