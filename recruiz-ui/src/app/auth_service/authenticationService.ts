import { Injectable } from '@angular/core';
import { HttpClient,HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '../../environments/environment';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })

export class AuthenticationService {

    //base url
    baseRoot: any = environment.baseUrl;
    apiUrl: any = environment.apiUrl;

    constructor(private http: HttpClient, private router: Router) { }

    //login user
    login(username: string, password: string) {
        let headers = new Headers({ 'Content-Type': 'application/json' });
        return this.http.post<any>(this.baseRoot + '/auth/login?username=' + username + '&password=' + password, { headers: headers, observe: 'response' })
            .pipe(map(resp => {
                return resp;
            }));
    }

    //login user if multi tenants
    loginAccount(tenantName: string, loggedEmail: string, tempToken: string) {
        let headers = new Headers({ 'Content-Type': 'application/json' });
        //let options = new RequestOptions({ headers: headers });
        const options =  ({headers: headers});
        return this.http.post<any>(this.baseRoot + '/auth/tenant/selected?tenant=' + tenantName + '&email=' + loggedEmail + '&tpk=' + tempToken, options, { observe: 'response' })
            .pipe(map(resp => {
                console.log("RESPONSE HEADER X-AUTH-TOKEN :  " + resp.headers.get('X-AUTH-TOKEN'));
                resp.body.data = resp.body.data;
                resp.body.success = resp.body.success;
                return resp;
            }));
    }

    //re activate session
    reActivateAccount() {
        let headers = new Headers({ 'Content-Type': 'application/json' });
        return this.http.get<any>(this.baseRoot + '/api/v1/update/auth/token', { observe: 'response' })
            .pipe(map(resp => {
                return resp;
            }));
    }

    logout() {
        // remove user from local storage to log user out
        return this.http.get(this.baseRoot + this.apiUrl  + '/logout', { observe: 'response' })
            .pipe(map(resp => {
                return resp;
            }));

    }
}