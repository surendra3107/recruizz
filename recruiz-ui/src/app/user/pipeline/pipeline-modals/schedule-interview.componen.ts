import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from "sweetalert2";
import { cloneDeep } from 'lodash';
import { MatExpansionPanel } from '@angular/material/expansion';

//services
import { PipelineService } from "../pipelineService/pipeline.service";
import { InterviewService } from "./../interviewService/interview.service";

@Component({
  selector: 'app-my-dialog',
  templateUrl: './schedule-interview.component.html',
  styleUrls: ['../pipeline/pipeline.component.css']
})
export class ScheduleInterviewDialog implements OnInit {

  constructor(
    public thisDialogRef: MatDialogRef<ScheduleInterviewDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _pipeline: PipelineService,
    private _interview: InterviewService
  ) { }

  error = '';
  globalData: any;
  data: any;
  roundData: any;
  roundId: any;
  templateView: string = 'interviewerTemplate';
  category: any;
  positionCode: any;
  intType: string;
  interviewPanel: any;
  intervwFrom: any;
  intervwTo: any;
  currentDate: any;
  feedbackId: any;
  tabs: any = [];
  currentActiveTab: any = '1';
  roundDataList: any;
  interviewerName: string;
  interviewerEmail: string;
  interviewerNumber: string;

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    //data
    this.data = this.dataOption.data;
    this.roundData = this.dataOption.roundData;
    this.roundId = this.dataOption.roundData.roundId;
    this.category = this.dataOption.category;
    this.positionCode = this.dataOption.positionCode;
    this.intType = this.dataOption.intType;
    this.interviewPanel = this.dataOption.interviewPanel;

    this.loadRoundName();
    this.currentDate = new Date();
    //tabs values
    this.tabs = [
      {
        interviewPanelData: cloneDeep(this.interviewPanel),
        intervwFrom: this.intervwFrom,
        intervwTo: this.intervwTo,
        currentDate: this.currentDate,
        feedbackId: this.feedbackId,
        expectFeedback: true,
        roundName: '',
        manualAddedInterviewerList: [],
      }
    ];

  }

  //get all added stages
  public loadRoundName() {
    this._pipeline.getRoundName(this.positionCode).pipe(first()).subscribe(response => {
      if (response.success) {
        this.roundDataList = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    });
  };

  //manually add interviwers
  public manualAddedInterviewer(index: any, first: MatExpansionPanel) {
    this.tabs.forEach((item: any, key: any) => {
      if (key === index) {
        item.manualAddedInterviewerList.push(
          { name: this.interviewerName, email: this.interviewerEmail, mobile: this.interviewerNumber }
        )
      }
    })
    //empty field
    this.interviewerName = undefined;
    this.interviewerEmail = undefined;
    this.interviewerNumber = undefined;
    first.close();
  }

  //remove manual added interviewer
  public removeInterviewer(interviewerIndex: any, tabIndex: any) {
    this.tabs.forEach((item: any, key: any) => {
      if (key === tabIndex) {
        item.manualAddedInterviewerList.splice(interviewerIndex, 1)
      }
    })
  }

  // tab click events
  public getCurentTabValues(events: any) {

  }

  //add more tabs
  public addMoreTab() {
    this.currentActiveTab = this.tabs.length + 1;
    this.currentDate = new Date();
    this.tabs.push(
      {
        interviewPanelData: cloneDeep(this.interviewPanel),
        intervwFrom: this.intervwFrom,
        intervwTo: this.intervwTo,
        currentDate: this.currentDate,
        feedbackId: this.feedbackId,
        expectFeedback: true,
        roundName: '',
        manualAddedInterviewerList: []
      }
    );
  }

  //remove tabs
  public removeTab(index: any) {
    this.tabs.splice(index, 1);
  }

  onCloseConfirm() {
    this.thisDialogRef.close();
  }

  onCloseCancel() {
    this.thisDialogRef.close();
  }

}
