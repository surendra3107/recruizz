import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
@Injectable({
  providedIn: 'root'
})
export class PositionService {

  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //get all positions
  getAllPositions(pageNo: any, sortKey: any, sortOrder: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/position?pageNo=' + pageNo + '&sortField=' + sortKey + '&sortOrder=' + sortOrder, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get position by Id
  getPositionsById(positionId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/position/' + positionId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete position
  deletePositionList(positionId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/position/' + positionId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // change Position Status
  changePosStatus(status: any, id: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/position/' + id + '/status?status=' + status, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // publish/unpublish Position
  publishingPosition(isPublish: any, id: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/position/' + id + '/publish?publish=' + isPublish, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // Get Position details URL for External apply
  getPositionDetailsURL(positionCode: any, source: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/position/url?positionCode=' + positionCode + '&sourceMode=' + source, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete interviewer from position
  removeInterviewerFromPosition(positionId: any, intId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/position/' + positionId + '/interviewer/delete/' + intId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // validate interviewer email id
  validateInterviewerEmail(email: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/generic/interviewer/email/check?emailId=' + email, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //add hrs and team to position-absolute
  addTeamWithHrs(hrsId: any, teamId: any, pId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/position/' + pId + '/hr/add?hrids=' + hrsId + '&teamId=' + teamId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete hrs and team to position-absolute
  removeHrFromPosition(pId: any, hrsId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/position/' + pId + '/hr/delete/' + hrsId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // get vendors for position
  fetchVendors() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/vendor/position/all', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // get vendors for position
  addVendorsToPosition(selectedVendors: any, positionId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/position/' + positionId + '/vendor/add?vids=' + selectedVendors, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  // delete vendors from position
  removeVendorFromPosition(positionId: any, vendorId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/position/' + positionId + '/vendor/delete/' + vendorId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get all position activity
  getPositionActivity(positionCode: any, count:any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/activity/position/' + positionCode + '?pageNo=' + count, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get all notes
  getPositionNotes(positionId: any, count: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/position/' + positionId + '/notes?pageNo=' + count, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //add notes to position
  postNotesToPosition(positionId: any, positionDataNotes: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/position/' + positionId + '/notes', positionDataNotes, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //add notes to position
  updateNotesForPosition(positionId: any, positionDataNotes: any, noteId:any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/position/' + positionId + '/notes/'+ noteId, positionDataNotes, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete position noted
  removeNotesFromPosition(notesId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/position/notes/' + notesId , { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }


  //get interviewer list based on client name
  getInterviewPanel(clientName: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/client/interviewer?clientName=' + clientName, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

}
