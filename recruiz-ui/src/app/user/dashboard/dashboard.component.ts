import { Component, OnInit, ViewChild } from '@angular/core';
import { UntypedFormControl, Validators } from '@angular/forms';
import { Chart } from 'chart.js';
import { recruitmentStatusData } from './../../data/recruiz-data';
import { Router } from '@angular/router';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';

//cloud word
import { CloudData, CloudOptions, TagCloudComponent } from 'angular-tag-cloud-module';
//service
import { DashboardService } from './dashboardService/dashboard.service';
import { DropdownService } from './../dropdownService/dropdown.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  @ViewChild(TagCloudComponent, { static: true }) tagCloudComponent: TagCloudComponent;

  toppings = new UntypedFormControl();
  
  orgControl = new UntypedFormControl('', [Validators.required]);
  selectFormControl = new UntypedFormControl('', Validators.required);
  show = true;

  toppingList: any;
  lineChart = [];
  DoughnutChart = [];
  recruitmentStatus: any = recruitmentStatusData;
  selectedIndex: number = -1;
  isInnerRowShow: boolean = false;
  multiselectData: string[] = [];

  selectedHr: string = '';
  selectedClient: string = '';
  error = '';
  selected: any;

  dataResponse: any;
  clients: any;
  entityCounts: any;
  hrData: any;
  scheduleDetails: any;
  dataCandidateGrowth: any;
  dataTeamList: any;
  teamIds: Array<any> = [];

  customTimePeriodDropDown: Array<any>;
  candidateIntervalPeriod: string = 'Last_3_Months';

  chart: any;
  graphLabels: Array<any>;
  graphData: Array<any>;
  isCandidateGrowthData: boolean = true;
  charts: Chart;

  dataCandidateSourceMix: any;
  graphLabelsCandidateMix: Array<any> = [];
  graphDataCandidateMix: Array<any> = [];

  allHrsList: any;
  allClientList: any;
  timeInterval: any;
  selectedTime: string = 'Last_3_Months';
  recruitmentData: any;
  constructor(
    private dashboard: DashboardService,
    private dropdown: DropdownService
  ) { }

  ngOnInit() {
    // time period drop down
    this.customTimePeriodDropDown = [{
      "id": "Last_Month",
      "value": "1 Month"
    },
    {
      "id": "Last_3_Months",
      "value": "3 Month"
    },
    {
      "id": "Last_6_Months",
      "value": "6 Month"
    },
    {
      "id": "Last_12_Months",
      "value": "1 Year"
    }
    ];

    this.intityCount();
    this.loadLeamList();

    this.createDoughnutChart();

    //load hr
    this.loadHrsList();
    //load client name
    this.loadClientList();
    //time range
    this.loadTimeRange();

    //load recruitment status
    this.getRecruitmentStatus('all', 'all', this.selectedTime, null, null);
  }

  //intity count
  public intityCount() {
    this.dashboard.getdashBoardInfo().pipe(first()).subscribe(data => {
      if (data.success === true) {
        this.dataResponse = data.data;
        this.clients = this.dataResponse.clients;
        this.entityCounts = this.dataResponse.entityCounts;
        this.hrData = this.dataResponse.hrData;
        this.scheduleDetails = this.dataResponse.scheduleDetails;
      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
  }

  //get list of teams
  public loadLeamList() {
    this.dashboard.getTeamList().pipe(first()).subscribe(data => {
      if (data.success === true) {
        this.dataTeamList = data.data;
        this.dataTeamList.forEach((item: any) => {
          if (this.teamIds.length === 0) {
            this.teamIds.push(item.teamId);
          }
        })
        this.candidateDatabseGrowth(this.candidateIntervalPeriod, this.teamIds);
      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
  }


  //Candidate Databse Growth
  public candidateDatabseGrowth(candidateIntervalPeriod: string, teamIds: any) {
    this.dashboard.getcandidateDatabseGrowth(candidateIntervalPeriod, teamIds.join('')).pipe(first()).subscribe(data => {
      if (data.success === true) {
        this.graphLabels = [];
        this.graphData = [];
        this.dataCandidateGrowth = data.data;
        let candidateGraphData = this.dataCandidateGrowth.candidate.reportData;
        if (candidateGraphData) {
          this.isCandidateGrowthData = true;
          candidateGraphData.forEach((item: any) => {
            this.graphLabels.push(item[0]);
            this.graphData.push(item[1]);
          });
        }

        //graph 
        if (candidateGraphData !== null) {
          this.lineChart = new Chart('LineChart', {
            type: 'line',
            data: {
              labels: this.graphLabels,
              datasets: [
                {
                  data: this.graphData,
                  borderColor: '',
                  backgroundColor: "rgba(182,246,242,1)",
                  lineTension: 0,
                  pointRadius: 2
                }
              ]
            },
            options: {
              legend: {
                display: false
              },
              scales: {
                xAxes: [{
                  display: true,
                  gridLines: {
                    display: false
                  }
                }],
                yAxes: [{
                  display: true,

                }],
              }
            }

          });
        } else {
          this.isCandidateGrowthData = false;
          this.graphLabels = [];
          this.graphData = [];
        }

      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
  }

  // on chnage time period get candidate databse growth report
  public changeCandidateGrowth(timePeriod: string) {
    this.candidateDatabseGrowth(timePeriod, this.teamIds);
  }

  //pie chart for candidate channel source mix
  public createDoughnutChart() {
    this.dashboard.getCandidateSoucePool().pipe(first()).subscribe(data => {
      if (data.success === true) {
        this.dataCandidateSourceMix = data.data.reportData;
        if (this.dataCandidateSourceMix) {
          this.isCandidateGrowthData = true;
          this.dataCandidateSourceMix.forEach((item: any) => {
            this.graphLabelsCandidateMix.push(item[0]);
            this.graphDataCandidateMix.push(item[1]);
          });
        }
      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })

    this.DoughnutChart = new Chart('doughnutChart', {
      type: 'doughnut',
      data: {
        labels: this.graphLabelsCandidateMix,
        datasets: [
          {
            data: this.graphDataCandidateMix,
            borderColor: '#fff',
            backgroundColor: [
              "#ffbf00", "#00ffbf", "#0080ff", "#ff00ff", "#bf00ff", "#8000ff", "#800080", "#602020", "#5c5c3d", "#ff6666",
              "#40bf80", "#b3b300", "#b32400", "#ac3973",
            ],
            fill: true
          }
        ]
      },
      options: {
        legend: {
          display: true,
          position: 'bottom',
          fullWidth: true,
          boxWidth: 10
        },
        scales: {
          xAxes: [{
            display: false
          }],
          yAxes: [{
            display: false
          }],
        }
      }
    });
  }

  //get list of all users
  public loadHrsList() {
    this.dropdown.getHrsList().pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.allHrsList = response.data;
      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      });
  }

  //on selct hrs
  public onSelectHrs(hrEmailList: any) {

  }

  //get list of all client name
  public loadClientList() {
    this.dropdown.getClientList().pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.allClientList = response.data;
      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      });
  }

  //on change client name
  public onSelectClient(clientName: any) {
    
  }

  //get time range
  public loadTimeRange() {
    this.dropdown.getTimeRangeList().pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.timeInterval = response.data;
      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      });
  }

  //on change time period
  public onSelectTimePeriod(clientName: any) {

  }

  //load recruitment status
  public getRecruitmentStatus(hrEmail: any, clientName: any, timeRange: string, startTme: any, endTime: any) {
    this.dashboard.getRecruitmentStatusReport(hrEmail, clientName, timeRange, startTme, endTime).pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.recruitmentData = response.data;
      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      });
  }


  toggleInnerRow(index: number) {
    this.isInnerRowShow = !this.isInnerRowShow;
    this.isInnerRowShow == true ? (this.selectedIndex = index) : (this.selectedIndex = -1);
  }

  onSelect(obj:any){
    let index=this.multiselectData.findIndex(o => o == obj);
    if(index!==-1) this.multiselectData.splice(index,1);
    else this.multiselectData.push(obj);
  }

  removeFromSelection(index: number) {
    this.multiselectData.splice(index, 1);
    if (this.multiselectData.length == 0) {
      this.selectedHr = '';
      this.selectedClient = '';
    }
  }


}