import { Component, OnInit } from '@angular/core';

import { Router, ActivatedRoute } from '@angular/router';
import { first } from "rxjs/operators";
import Swal from 'sweetalert2';
import { MatDialog } from '@angular/material/dialog';
import { AddEditTeamDialog } from '../team-modals/add-edit-team.component';
import { AddMemberDialog } from '../team-modals/add-member.component';
import { TeamInfoDialog } from '../team-modals/team-info.component';
//service
import { TeamService } from '../teamService/team.service';
@Component({
  selector: 'app-team-management',
  templateUrl: './team-management.component.html',
  styleUrls: ['./team-management.component.css']
})
export class TeamManagementComponent implements OnInit {

  constructor(
    private router: Router,
    public dialog: MatDialog,
    private _team: TeamService
  ) { }

  error: any = '';
  globalData: any;
  teamList: any;

  ngOnInit() {
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    this.loadTeamList();
  }

  //get all teams
  public loadTeamList() {
    this._team.getTeams().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.teamList = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //add edit team modals
  public addUpdateTeam(teamId: string) {
    let dialogRef = this.dialog.open(AddEditTeamDialog, {
      width: '950px',
      data: { teamId: teamId },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        Swal.fire({
          title: "Success",
          text: "Team created successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadTeamList();
      }
    })
  }

  //add members to team
  public addMember(teamData: any) {
    let dialogRef = this.dialog.open(AddMemberDialog, {
      width: '950px',
      data: { teamId: teamData.teamId },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        Swal.fire({
          title: "Success",
          text: "Team created successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadTeamList();
      }
    })
  }

  //view Team Info
  public viewTeamInfo(teamData:any) {
    let dialogRef = this.dialog.open(TeamInfoDialog, {
      width: '950px',
      data: { teamDetails: teamData },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if(result === true) {
          this.loadTeamList();
        } else {
          this.addUpdateTeam(result);
        }
      }
    })
  }

  //delete team
  public deleteTeam(teamData: any) {
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
        this._team.deleteTeamInfo(teamData.teamId).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: 'Team has been deleted successfully.',
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.teamList.splice(this.teamList.indexOf(teamData), 1);
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

}
