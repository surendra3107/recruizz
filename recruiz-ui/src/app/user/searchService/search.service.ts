import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
@Injectable({
  providedIn: 'root'
})
  
export class SearchService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //get all positions list on saerch
  getPositionSearchResults(input: any, clientName: any, number: any, sortKey: any, sortOrder: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/search/position?pageNo=' + number + '&clientName=' + clientName + '&sortField=' + sortKey + '&sortOrder=' + sortOrder, input, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get all clients list on saerch
  getClientSearchResults(input: any, number: any, sortKey: any, sortOrder: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/search/client?pageNo=' + number + '&sortField=' + sortKey + '&sortOrder=' + sortOrder, input, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //search by current location
  getFilteredDataLocation(value:any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/search/client/location?location=' + value, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

   //search by current location prospect
   getFilteredDataLocationProspect(value:any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/search/prospect/location?location=' + value, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //search by current client name
  getFilteredDataNameProspect(value:any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/search/prospect/name?name=' + value, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //search by current location
  getFilteredDataClientName(value: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/search/client/name?name=' + value, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

}
