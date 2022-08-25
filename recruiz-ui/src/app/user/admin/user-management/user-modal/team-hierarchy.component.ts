import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators'
import Swal from 'sweetalert2';

//services
import { TeamService } from "../../team-management/teamService/team.service";
@Component({
  selector: 'app-my-dialog',
  templateUrl: './team-hierarchy.component.html',
  styleUrls: ['../user-management/user-management.component.css', '../../team-management/team-management/team-management.component.css']
})
export class TeamHierarchyDialog implements OnInit {

  constructor(
    public thisDialogRef: MatDialogRef<TeamHierarchyDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _team: TeamService,
  ) { }

  error = '';
  globalData: any;
  selectedTeamForView: any;
  teamHierarchyList: any;
  teamHierarchyChildTeamList: any;

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
   this.viewTeamInformation();
  }

  //start import file
  public viewTeamInformation() {
    this._team.getTeamDetails(this.dataOption.teamId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        // team teamHierarchyList
        this.selectedTeamForView = response.data;
        this.teamHierarchyList = [];
        this.teamHierarchyList.push(response.data);
        this.teamHierarchyChildTeamList = response.data.childrenTeams;
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
