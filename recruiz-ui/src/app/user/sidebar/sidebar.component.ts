import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';
import { RecruizHelperService } from './../../toggle_service/recruiz-helper.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {

  toggleSidebar: string = 'true';
  disabledTooltip: boolean = false;
  toggleLabel: boolean = true;
  multiTenantList: any;
  globalData: any;
  orgName: any;
  userTenants: any;
  loggedInUserType: any;
  translateName: any;
  activeRoute: string = '';
  
  constructor(
    private _recruizHelperService : RecruizHelperService,
    private router: Router
    ) { }

  ngOnInit() {
    this.multiTenantList = JSON.parse(sessionStorage.getItem("currentUser"));
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
    this.orgName = this.globalData?.orgName;
    this.userTenants = this.globalData?.userTenants;
    this.loggedInUserType = this.globalData?.type;
    if (this.globalData.orgType === "Corporate") {
      this.translateName = 'DEPARTMENTS';
    } else {
      this.translateName = 'CLIENTS';
    }

    this._recruizHelperService.sidebarToggleEvent.subscribe((response)=>{
      this.toggleLabel=response;
      this.toggleSidebar=response.toString();
    });
  }

 public toggleTooltip() {
    if(this.toggleLabel) this.disabledTooltip=false;
    else this.disabledTooltip=true;
  }

  // go to pages
  public GoToRoute(url: any, permissionType: any) {
      //check for permission
    let found: boolean = false;
    var permissionArray = [permissionType];
    for (var i = 0; i < permissionArray.length; i++) {
      if (this.globalData.permissions.indexOf(permissionArray[i]) > -1) {
        found = true;
        break;
      }
    }

    if (!found) {
      if (url.includes('prospects')) {
        this.router.navigate([url], { queryParams: { page: '1', sort: 'modificationDate|desc' } });
      }
      else this.activeRoute = '';
      //this.router.navigate([url]);
    } else {
      Swal.fire({
        title: "Permission denied. Request your admin to grant permission to view this page",
        type: "warning",
        showConfirmButton: true
      });
    }
  }

}