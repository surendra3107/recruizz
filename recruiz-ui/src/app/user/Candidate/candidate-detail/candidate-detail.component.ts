import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-candidate-detail',
  templateUrl: './candidate-detail.component.html',
  styleUrls: ['./candidate-detail.component.css']
})
export class CandidateDetailComponent implements OnInit {

  isActive:boolean=false;
  selectedTab:string='resume';

  isResumeEmpty:boolean=false;
  isDocsEmpty:boolean=false;
  isActivityEmpty:boolean=false;
  isFeedbackEmpty:boolean=false;
  isPositionsEmpty:boolean=false;
  isNotesEmpty:boolean=false;
  isAssessmentEmpty:boolean=true;

  constructor() { }

  ngOnInit() {
  }

  onTabMenu(type:string){
    this.selectedTab=type;
    this.isActive=true;
  }

}
