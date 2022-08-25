import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CandidateService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //fetch all candidates
  getAllCandidates(number: any, sortKey: any, sortOrder: any) {
    let headers: HttpHeaders = new HttpHeaders();
    let params = new HttpParams();
    params = params.append('pageNo', number);
    params = params.append('sortField', sortKey);
    params = params.append('sortOrder', sortOrder);
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/candidate', { params: params, headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //change candidate status
  switchCandidateStatus(status: any, id: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/candidate/' + id + '/status?status=' + status, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete candidate
  checkForCandidateInvoice(id: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/agency/check/candidate/invoice?ids=' + id, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete candidate with invoice
  deleteCandidateWithInvoice(candidateId: any, invoiceFlag: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/candidate/' + candidateId + '?removeInvoiceFlag=' + invoiceFlag, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete candidate
  deleteCandidate(candidateId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/candidate/' + candidateId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

}
