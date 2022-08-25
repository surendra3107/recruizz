import { Component, OnInit } from '@angular/core';
import { ModalService } from 'src/app/modal-service/modal.service';

@Component({
  selector: 'app-employee',
  templateUrl: './employee.component.html',
  styleUrls: ['./employee.component.css']
})
export class EmployeeComponent implements OnInit {

  isActive:boolean=false;
  selectedTab:string='onboarding';

  isDecisionMakerEmpty:boolean=true;
  isInterviewPanelEmpty:boolean=false;
  isActivityEmpty:boolean=false;
  isNotesEmpty:boolean=false;
  isRatesEmpty:boolean=true;
  isDocsEmpty:boolean=false;

  isFilterOpen:boolean=false;
  isStatusDivOpen = true;
  statusDivIcon:string='fa-minus';
  currentDivIcon:string='fa-minus';
  isCurrentLocationDivOpen:boolean=true;
  clientNameDivIcon:string='fa-minus';
  isClientNameDivOpen:boolean=true;
  isLocationSearch:boolean=false;
  isNameSearch:boolean=false;

  pageTitle:string='EMPLOYEES';
  finalBreadcrumb:string='Employees';
  isBreadcrumbShow:boolean=true;


  panelOpenState = false;
  data: number[] = [2,5,6,7,8,8,9,9,9,0,4,5,42,24,5,4,3,4];

  isOnboardingEmpty:boolean=false;

  constructor(private _modalService : ModalService) { }

  ngOnInit() {
  }

  openModal(id:string){
    this._modalService.open(id);
  }

  closeModal(id:string){
    this._modalService.close(id);
  }


  onTabMenu(type:string){
    this.selectedTab=type;
    this.isActive=true;
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
