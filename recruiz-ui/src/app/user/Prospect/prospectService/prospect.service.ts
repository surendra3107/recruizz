import { throwError as observableThrowError, Observable } from "rxjs";
import { Injectable } from "@angular/core";
import { catchError, map } from "rxjs/operators";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { environment } from "../../../../environments/environment";

@Injectable({
  providedIn: "root",
})
export class ProspectService {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) {}

  //get all prospect
  getAllProspect(pageNo: any, sortKey: any, sortOrder: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/prospect?pageNo=' + pageNo + '&sortField=' + sortKey + '&sortOrder=' + sortOrder, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

}
