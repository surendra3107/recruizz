import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-common-header',
  templateUrl: './common-header-component.html',
  styleUrls: ['./common-header.component.css']
})
export class CommonHeaderComponent implements OnInit {

  currentPath: any;
  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
  ) {
    this.currentPath = activatedRoute.snapshot.url[1].path;
  }

  activeTab: string = 'organizations'

  ngOnInit() {
    if (this.currentPath === 'setting-management') {
      this.activeTab = 'organizations';
    } else if (this.currentPath === 'user-management') {
      this.activeTab = 'user';
    } else if (this.currentPath === 'role-management') {
      this.activeTab = 'roles';
    } else if (this.currentPath === 'permission-management') {
      this.activeTab = 'permission';
    } else if (this.currentPath === 'integration-management') {
      this.activeTab = 'intergation';
    } else if (this.currentPath === 'onboard-management') {
      this.activeTab = 'onboarding';
    } else if (this.currentPath === 'team-management') {
      this.activeTab = 'team';
    }
  }

  // switch beetween tabs
  public switchTabs(tabName: any) {
    if (tabName === 'organizations') {
      this.router.navigate(['user/admin/setting-management']);
    } else if ( tabName === 'user') {
      this.router.navigate(['user/admin/user-management']);
    } else if (tabName === 'roles') {
      this.router.navigate(['user/admin/role-management']);
    } else if (tabName === 'permission') {
      this.router.navigate(['user/admin/permission-management']);
    } else if (tabName === 'intergation') {
      this.router.navigate(['user/admin/integration-management']);
    } else if (tabName === 'onboarding') {
      this.router.navigate(['user/admin/onboard-management']);
    } else if (tabName === 'team') {
      this.router.navigate(['user/admin/team-management']);
    }
  }
}
