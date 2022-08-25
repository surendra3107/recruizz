import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
@Injectable({
  providedIn: 'root'
})
export class ClientService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //check if client name exists
  validateClientName(clientname: any) {
    let headers: HttpHeaders = new HttpHeaders();

    let params = new HttpParams();
    params = params.append('clientName', clientname);
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/client/check', { params: params, headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //create client
  postClientInfo(formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/client', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get all clients
  getAllClients(pageNo: any, sortKey: any, sortOrder: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/client?pageNo=' + pageNo + '&sortField=' + sortKey + '&sortOrder=' + sortOrder, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get all clients
  getClientList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '//client/name', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get client detail by id
  getClientDetailsById(clientId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/client/' + clientId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //change client status
  changeClientStatus(input: any, id: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/client/' + id + '/status?status=' + input, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //validate descision maker email
  validateDecisionMakerEmail(email: any, id: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/client/decisionmaker/check?email=' + email + '&id=' + id, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //add descision maker to client
  addDecisionMaker(formData: any, cid: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/generic/dm/client/' + cid + '/add', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //update descision maker to client
  updateDecisionMaker(formData: any, decisionEmail: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/generic/dm/update/' + decisionEmail, formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete decision maker
  deleteDesicionMaker(cid: any, did: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/client/' + cid + '/decisionmaker/delete/' + did, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //add interviewer to client
  addSelectedInterviewerToClient(arrayData: any, clientId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/generic/interviewer/client/' + clientId + '/add', arrayData, { headers: headers }).pipe(map(user => {
      return user;
    }));
  }

  //delete iny=terviewer
  deleteClientInterviewer(cid: any, intId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/client/' + cid + '/interviewer/delete/' + intId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get client activity
  getClientActivity(clientId: any, pageNo: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/activity/client/' + clientId + '?pageNo=' + pageNo, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get client notes
  getClientNotes(clientId: any, pageNo: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/client/' + clientId + '/notes?pageNo=' + pageNo, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //add client notes
  postNotesToClient(clientId: any, formData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/client/' + clientId + '/notes', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //update notes
  public updateNotesForClient(clientId: any, formData: any, noteId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/client/' + clientId + '/notes/' + noteId, formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete notes
  public removeNotesFromClient(noteId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/client/notes/' + noteId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get client rates
  public getAllRates(clientId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/client/' + clientId + '/invoice/info/get', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //add rates client
  public addUpdateClientRates(clientId: any, dataValue: any) {
    var formData = {
      'info': dataValue
    }
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/client/' + clientId + '/invoice/info', formData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

}
