import { Injectable } from '@angular/core';
import { ErrorDialogService } from '../error-dialog/errordialog.service';
import { Router } from '@angular/router';
import {
  HttpInterceptor,
  HttpRequest,
  HttpResponse,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';

import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { first } from 'rxjs/operators';
import { AuthenticationService } from '../auth_service/authenticationService';



@Injectable()
export class HttpConfigInterceptor implements HttpInterceptor {
  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(
    public errorDialogService: ErrorDialogService,
    private http: HttpClient,
    private auth: AuthenticationService,
    private router: Router
  ) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token: any = JSON.parse(localStorage.getItem('userInfo'));

    if (token) {
      request = request.clone({
        headers: request.headers.set('x-auth-token', token.authToken)
      });
    }

    if (!request.reportProgress) {
      request = request.clone({ headers: request.headers.set('Content-Type', 'application/json') });
    } else {
      request = request.clone({ headers: request.headers.set('Accept', 'application/json') });
    }

    return next.handle(request).pipe(
      map((event: HttpEvent<any>) => {
        if (event instanceof HttpResponse) {
          console.log('event--->>>', event);
          // this.errorDialogService.openDialog(event);
        }
        return event;
      }),
      catchError((error: HttpErrorResponse) => {
        //duplicate session
        switch (error.error.reason) {

        }
        if (error.error.reason === 'authentication_failure' && error.error.data !== 'Auth Token not valid or expired or tampered') {
          Swal.fire({
            title: "Your Session is active else where!",
            text: "Do you want to activate here?",
            type: 'warning',
            showConfirmButton: true,
            showCancelButton: true,
            reverseButtons: true,
            allowOutsideClick: false
          }).then((result) => {
            if (result.value) {
              this.auth.reActivateAccount().pipe(first()).subscribe(data => {
                if (data) {
                  Swal.fire({
                    title: "Activated",
                    text: "Your account has been reactivated",
                    type: "success",
                    timer: 2000,
                    showConfirmButton: false
                  });
                  let currentUrl = this.router.url;
                  this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
                    this.router.navigate([currentUrl]));
                }
              });
            }
          })
        } else if (error.error.reason === 'authentication_failure' && error.error.data === 'Auth Token not valid or expired or tampered') {
          Swal.fire({
            title: "Your session has been expired",
            text: "Please login again",
            type: 'warning',
            showConfirmButton: true,
            showCancelButton: true,
            reverseButtons: true,
            allowOutsideClick: false
          }).then((result) => {
            if (result.value) {
              localStorage.removeItem('userInfo');
              sessionStorage.removeItem('currentUser');
              this.router.navigate(['web/login']);
            }
          })
        } else {
          let data = {};
          data = {
            reason: error && error.error && error.error.reason ? error.error.reason : '',
            status: error.status
          };
          this.errorDialogService.openDialog(data);
        }
        return throwError(error);
      }));
  }
}
