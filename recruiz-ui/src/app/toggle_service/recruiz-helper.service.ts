import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RecruizHelperService {

  private _sidebarToggleSub = new Subject<any>();
  sidebarToggleEvent = this._sidebarToggleSub.asObservable();

  constructor() { }

  publishEvent(action:string,data?:any){
    if(action=='toggleSidebar') this._sidebarToggleSub.next(data);
  }


}