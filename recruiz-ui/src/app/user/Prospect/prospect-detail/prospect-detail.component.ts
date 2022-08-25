import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-prospect-detail',
  templateUrl: './prospect-detail.component.html',
  styleUrls: ['./prospect-detail.component.css']
})
export class ProspectDetailComponent implements OnInit {

  loop: number[] = [];

  isActive:boolean=false;
  selectedTab:string='contacts';

  isContactsEmpty:boolean=false;
  isPositionsEmpty:boolean=false;
  isActivityEmpty:boolean=false;
  isNotesEmpty:boolean=false;
  isDocsEmpty:boolean=false;

  constructor() { }

  ngOnInit() {
   for(let i =0; i < 2 ; i++){
     this.loop.push(i);
   }
  }

  onTabMenu(type:string){
    this.selectedTab=type;
    this.isActive=true;
  }
  
}
