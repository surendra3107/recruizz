import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators'
import Swal from 'sweetalert2';

//services
import { TeamService } from "../teamService/team.service";

@Component({
  selector: 'app-my-dialog',
  templateUrl: './add-member.component.html',
  styleUrls: ['../team-management/team-management.component.css']
})
export class AddMemberDialog implements OnInit {


  constructor(
    public thisDialogRef: MatDialogRef<AddMemberDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _team: TeamService,
  ) { }

  error = '';
  globalData: any;
  allUsers: any;
  selectedUsers: any = [];
  memberName: string;
  totalRevenue: any = 0;
  totalClosure: any = 0;

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
    this.loadTeamUser();
  }

  //get all teams
  public loadTeamUser() {
    this._team.getTeamUsers(0).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.allUsers = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //on change add revenue target
  public onChangeRevenue() {
    var totalRevenueTarget: any = 0;
    this.allUsers.forEach((num: any) => {
      if (!isNaN(num.revenueTarget)) {
        totalRevenueTarget += parseInt(num.revenueTarget ? num.revenueTarget : 0);
        this.totalRevenue = totalRevenueTarget;
      }
    })
  }

  //on change add closure target
  public onChangeClosure() {
    var totalClosureTarget: any = 0;
    this.allUsers.forEach((num: any) => {
      if (!isNaN(num.closureTarget)) {
        totalClosureTarget += parseInt(num.closureTarget ? num.closureTarget : 0);
        this.totalClosure = totalClosureTarget;
      }
    })
  }
  //select user to add to team
  public selectUser(user: any, selected: any) {
    if (selected) {
      this.selectedUsers.push(user);
    } else {
      this.selectedUsers.splice(this.selectedUsers.indexOf(user), 1);
    }
  }

  //add members
  public addMemeber() {

    if (this.selectedUsers.length === 0) {
      Swal.fire({
        title: "Warning",
        text: "Please select one or more team members",
        type: "warning",
        showConfirmButton: true
      });
      return;
    }

    var data = [];
    this.selectedUsers.forEach((usr: any) => {
      data.push({
        'email': usr.email,
        'role': usr.teamRole || 'member',
        "targetAmount": this.totalRevenue,
        "targetPositionOpeningClosure": this.totalClosure,
      });
    })

    this._team.addMember(data, this.dataOption.teamId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.thisDialogRef.close(true);
      } else if (response.reason === 'team_add_users_failed') {
        Swal.fire({
          title: "Warning",
          text: "User already added to team",
          type: "warning",
          showConfirmButton: true
        });
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
