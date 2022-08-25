import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FolderService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //get all folders
  getAllCandidateFolders() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/folder/candidate/folderlist', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //create folders
  createCandidateFolders(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/folder', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //create folders
  updateCandidateFolders(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/folder', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete folders
  deleteCandidateFolders(folderName: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/folder/candidate?folderName=' + folderName, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get user list to share folder
  getUserListToShareFolders(folderName: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/folder/list/user?folderName=' + folderName, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get user list to share folder
  shareFoldersToUsers(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/folder/candidate/user/share', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get list of users to whom the folder has been shared
  sharedFolderUserList(folderName: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/folder/candidate/userlist?folderName=' + folderName, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // unshare folder
  unshareFoldersToUsers(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/folder/candidate/user/unshare', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get list of candidate to add to folders
  getCandidateList(folderName: any, pageNumber: any) {
    let headers: HttpHeaders = new HttpHeaders();
    let params = new HttpParams();
    params = params.append('folderName', folderName);
    params = params.append('pageNo', pageNumber);
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/folder/list/candidate', { params: params, headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // add candidate folder
  addCandidateToFolder(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/folder/candidate/add', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

}
