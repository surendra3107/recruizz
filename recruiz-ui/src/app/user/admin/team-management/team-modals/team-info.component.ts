import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators'
import Swal from 'sweetalert2';

//services
import { TeamService } from "../teamService/team.service";

@Component({
  selector: 'app-my-dialog',
  templateUrl: './team-info.component.html',
  styleUrls: ['../team-management/team-management.component.css']
})
export class TeamInfoDialog implements OnInit {


  constructor(
    public thisDialogRef: MatDialogRef<TeamInfoDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _team: TeamService,
  ) { }

  error = '';
  globalData: any;
  allUsers: any;
  teamName: string;
  totalRevenueTarget: any;
  totalClosureTarget: any;
  teamStructure: any;
  teamHierarchyList: any;
  teamHierarchyChildTeamList: any;
  selectedTeamForView: any;
  teamId: any;

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
    this.loadTeamUser();
    this.loadTeamStructure();

    this.teamName = this.dataOption.teamDetails.teamName;
    this.totalRevenueTarget = this.dataOption.teamDetails.teamTargetAmount;
    this.totalClosureTarget = this.dataOption.teamDetails.teamTargetPositionOpeningClosure;
    this.teamId = this.dataOption.teamDetails.teamId;
  }

  //get all teams
  public loadTeamUser() {
    this._team.getTeamDetails(this.dataOption.teamDetails.teamId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        response.data.members.forEach((item: any) => {
          item.revenueTarget = item.teamMemberTargetAmount;
          item.closureTarget = item.teamTargetPositionOpeningClosure
        })
        this.allUsers = response.data.members;

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

  //load team structure
  public loadTeamStructure() {
    this._team.getTeamStructure(this.dataOption.teamDetails.teamId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.teamStructure = response.data.members;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }


  //save member info on edit
  public saveMemberInfo(member: any) {
    var data = {
      "teamId": this.dataOption.teamDetails.teamId,
      "email": member.email,
      "role": member.teamRole || 'member',
      "targetAmount": member.revenueTarget,
      "targetPositionOpeningClosure": member.closureTarget
    };

    this._team.saveMember(data).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        Swal.fire({
          title: "Saved",
          text: "Member info undated successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //delete memner
  public deleteMemberInfo(member: any) {
    var data = [];

    data.push({
      'email': member.email,
      'role': member.teamRole
    });

    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete ?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this._team.removeMember(data, this.dataOption.teamDetails.teamId).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Removed",
              text: 'Member info been been removed successfully.',
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.allUsers = response.data.members;
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //delete team
  public deleteTeam() {
    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete ?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this._team.deleteTeamInfo(this.teamId).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: 'Team has been deleted successfully.',
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.thisDialogRef.close(true);
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //edit team
  public editTeamInfo() {
    this.thisDialogRef.close(this.teamId);
  }

  onCloseConfirm() {
    this.thisDialogRef.close(true);
  }

  onCloseCancel() {
    this.thisDialogRef.close();
  }

}
