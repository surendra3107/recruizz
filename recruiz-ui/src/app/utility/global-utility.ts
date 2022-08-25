import { Injectable } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class GlobalUtility {

  constructor(private router: Router) { }

  logout() {
    // remove user from local storage to log user out
    localStorage.removeItem('userInfo');
    sessionStorage.removeItem('currentUser');
    this.router.navigate(['']);
  }


}