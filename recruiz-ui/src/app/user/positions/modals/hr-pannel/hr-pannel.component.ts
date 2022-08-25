import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
//services
import { GlobalService } from '../../../globalServices/global.service';
import { PositionService } from '../../positionService/position.service';
import { TeamService } from '../../../../user/admin/team-management/teamService/team.service';

@Component({
  selector: 'app-my-dialog',
  templateUrl: './hr-dialog.component.html',
  styleUrls: ['../../positions-detail/positions-detail.component.css']
})
export class HrPannelDialog implements OnInit {

  constructor(
    public thisDialogRef: MatDialogRef<HrPannelDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private global: GlobalService,
    private position: PositionService,
    private team: TeamService
  ) { }

  error = '';

  teamList: any;
  allTeamMembers: any;
  teamNotSelected: string;
  allHrsList: any;
  teamMembersList: any;
  selectedHrLists: Array<any> = [];
  selectedTeamId: any;
  selectedTeam: any
  ngOnInit() {
    this.loadTeamList();
    this.loadHrLists();
  }

  //load team
  public loadTeamList() {
    this.team.fetchAllTeamLists().pipe(first()).subscribe(response => {
      if (response.success) {
        this.teamList = response.data;
        //loop get the team members
        if (this.dataOption.selectedTeamId) {
          this.teamList.forEach((items: any) => {
            if (items.teamId === this.dataOption.selectedTeamId.id) {
              this.allTeamMembers = items.members;
              items.currentSelectedTeam = true;
            }
          })

          //loop to mark selected members
          this.allTeamMembers.forEach((items: any) => {
            this.dataOption.addedHrs.forEach((values: any) => {
              if (items.email === values.email) {
                items.selectStatus = true;
              }
            })
          })
        } else {
          this.teamNotSelected = 'Please choose a team to view members.';
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //get all hrs
  public loadHrLists() {
    this.global.getAllHrs().pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.allHrsList = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }
  // get members adding ehi adding to  position
  public getListOfHrs(team: any) {
    this.allTeamMembers = [];
    this.selectedTeamId = team.teamId;
    this.team.getAllTeamLists(team.teamId).pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.teamMembersList = response.data.members;
        this.allHrsList.forEach((items: any) => {
          this.teamMembersList.forEach((values: any) => {
            if (items.email === values.email) {
              var list = {
                userName: items.name,
                email: items.email,
                mobile: items.mobile,
                selectStatus: false,
                userId: items.userId
              }
              this.allTeamMembers.push(list);
            }
          })
        })
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //select and deselect hr
  public selectIndividualHr(hrData: any) {
    if (hrData.selectStatus === true) {
      this.selectedHrLists.push(hrData.userId || hrData.id);
    } else {
      var index = this.selectedHrLists.indexOf(hrData.userId || hrData.id);
      this.selectedHrLists.splice(index, 1);
    }
  }

  //adding hr to position
  public submitHrInfo() {
    this.position.addTeamWithHrs(this.selectedHrLists, this.selectedTeamId, this.dataOption.positionId).pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.thisDialogRef.close('hr-added');
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  onCloseConfirm() {
    this.thisDialogRef.close();
  }

  onCloseCancel() {
    this.thisDialogRef.close();
  }

}
