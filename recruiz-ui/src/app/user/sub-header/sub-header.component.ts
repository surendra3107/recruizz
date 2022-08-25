import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-sub-header',
  templateUrl: './sub-header.component.html',
  styleUrls: ['./sub-header.component.css']
})
export class SubHeaderComponent implements OnInit {

  sortDropdown: Array<any> = [];
  currentPage: string;
  sortBy: string;
  currentTab: string;

  data: any[] = [];
  isFilterOpen:boolean=false;
  isStatusDivOpen = true;
  statusDivIcon:string='fa-minus';
  currentDivIcon:string='fa-minus';
  isCurrentLocationDivOpen:boolean=true;
  clientNameDivIcon:string='fa-minus';
  isClientNameDivOpen:boolean=true;
  isLocationSearch:boolean=false;
  isNameSearch:boolean=false;

  pageTitle:string='PROSPECTS LIST';
  mainBreacrumb:string='Home';
  mainRoute:string='/user/client-list';
  finalBreadcrumb:string='Prospect';
  isBreadcrumbShow:boolean=false;

  isClient:boolean=false;
  isPosition:boolean=false;

  constructor(private router: Router, private activatedRoute: ActivatedRoute){
  }

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      this.currentPage = params.page;
      this.sortBy = params.sort;
      this.currentTab = params.tab;
    });
    /*
     * Providing sorting option in dropdown list
     */
    this.sortDropdown = [{
      key: 'Name A-Z',
      value: 'title|asc'
    },
    {
      key: 'Name Z-A',
      value: 'title|desc'
    },
    {
      key: 'Modification Date',
      value: 'modificationDate|desc'
    },
    {
      key: 'Close by Date',
      value: 'closeByDate|desc'
    }
    ];

    this.setBreadcrumb();
  }

  setBreadcrumb(){
    this.isBreadcrumbShow=false;
    if(this.router.url=='/user/client-list') {
      this.isClient=true;
    }else if(this.router.url=='/user/positions-list'){
      this.pageTitle='POSITIONS';
      this.isClient=false;
      this.isPosition=true;
    }else {
      this.isBreadcrumbShow=true;
      if(this.router.url=='/user/client-details') {
        this.pageTitle='CLIENT DETAILS';
        this.isClient=true;
        this.isPosition=false;
      }else if(this.router.url=='/user/positions-detail'){
        this.pageTitle='POSITIONS';
        this.finalBreadcrumb='Recruiz-Sales';
        this.mainBreacrumb='Position';
        this.mainRoute='/user/positions-list';
        this.isPosition=true;
        this.isClient=false;
      }
    }
  }

  onOpenMenu(menu: any): void {
  }

  openFilter(){
    this.isFilterOpen=!this.isFilterOpen;
  }

  toggleFilterDiv(type:string) {
    if(type=='status'){
      this.isStatusDivOpen = !this.isStatusDivOpen;
      if(this.isStatusDivOpen) this.statusDivIcon='fa-minus';
      else this.statusDivIcon='fa-plus';
    }else if(type=='location'){
      this.isCurrentLocationDivOpen=!this.isCurrentLocationDivOpen;
      if(this.isCurrentLocationDivOpen) this.currentDivIcon='fa-minus';
      else this.currentDivIcon='fa-plus';
    }else{
      this.isClientNameDivOpen=!this.isClientNameDivOpen;
      if(this.isClientNameDivOpen) this.clientNameDivIcon='fa-minus';
      else this.clientNameDivIcon='fa-plus';
    }
  }

  search(event:any,type:string){
    let value=event.target.value;
    if(value==''){
      this.isNameSearch=false;
      this.isLocationSearch=false;
    }else{
      if(type=='location') this.isLocationSearch=true;
      else this.isNameSearch=true;
    }
  }


}