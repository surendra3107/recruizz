import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';


import { environment } from '../../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OnBoardService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //get activity list
  getMasterActivityList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/admin/onboarding/activity/list', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get sub category
  getSubCategory() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/onboarding/sub/category/all', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get template list
  getTemplateList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/onboarding/templates/all', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get list of categories
  getCategory() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/on/boarding/category', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //create task
  postTask(formData: any, tpl: string) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/admin/onboarding?templateName=' + tpl, formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete task
  deleteTask(taskId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/onboarding/admin/details/delete/' + taskId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete sub category
  deleteSubCategories(subcategoryId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/onboarding/sub/category/delete/' + subcategoryId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete template
  deleteCurrentTemplate(template: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/onboarding/templates/delete?templateName=' + template, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //create template
  createTemplates(templateName: any, taskId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/onboarding/templates/add?templateName=' + templateName + '&taskIds=' + taskId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

}
