import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';


import { environment } from '../../../../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class UserService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //upload file
  bulkuploadFile(file: any) {
    let headers = new HttpHeaders();
    headers.set('Content-Type', undefined);
    headers.set('Accept', "multipart/form-data");
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/user/bulkupload/import', file, {
      reportProgress: true, headers
    }).pipe(map(user => {
      return user;
    }));
  }

  //downoad sample xl file
  downloadSampleBulkUploadFile() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/bulkupload/sample/download', { headers: headers, responseType: 'arraybuffer' as 'json' })
      .pipe(map(user => {
        return user;
      }));
  }

  //get all users
  getAllusers(name: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/user/role?userType=' + name, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //invite users
  inviteUser(formData: any, name: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/user/inviteUserList?userType=' + name, formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //change users role
  updateUserRole(id: any, email: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/user/changeUserRole?email=' + email + '&id=' + id, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //this method is used to enable/disable user
  disableUser(email: any, status: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/user/account/status?email=' + email + '&status=' + status, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get user hr
  getHrUsers() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/user/hr', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  deleteUser(email: any, newOwner: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/user/' + email + '?newOwner=' + newOwner, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //re send invitation to user
  reInviteUser(email: string) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/user/reInviteUser?email=' + email, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //reset password
  resetUserPasswordByAdmin(email: any, pwd: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/user/resetUserPasswordByAdmin?email=' + email + '&password=' + pwd, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //load all vendors
  getAllVendors() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/vendor/all', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //add vendors
  addVendors(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/vendor/new', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //update vendor
  updateVendor(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/vendor/update', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // enable disable vendor
  enableDisableVendor(id: any, status: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/vendor/status?vendorId=' + id + '&status=' + status, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // delete vendor
  deleteVendor(id: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/vendor/remove/' + id, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //permission to schedule interviews
  vendorInterviewShedulePermission(isPermission: any, id: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/vendor/permission/scheduleInterview?permission=' + isPermission + '&vendorId=' + id, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //manage vendor user
  getVendorUser(vendorId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/vendor/user/' + vendorId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //invite vendor user
  inviteVendorUser(formData: any, vendorId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/vendor/user/invite?id=' + vendorId, formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get all department head user
  getDeptHeadUsers(email: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/user/dept/head/get?userEmailToRemove=' + email, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete department head user
  deleteDeptHead(email: any, ownerEmail: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/user/dept/head/delete?userEmailToRemove=' + email + '&newOwnerEmail=' + ownerEmail, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }
}
