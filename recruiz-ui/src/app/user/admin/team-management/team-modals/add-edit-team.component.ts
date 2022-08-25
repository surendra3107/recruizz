import { Component, OnInit, Inject, ElementRef, ViewChild } from '@angular/core';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { UntypedFormControl } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import { MatAutocompleteSelectedEvent, MatAutocomplete } from '@angular/material/autocomplete';
import { MatChipInputEvent } from '@angular/material/chips';
import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import Swal from 'sweetalert2';

//services
import { TeamService } from "../teamService/team.service";

@Component({
  selector: 'app-my-dialog',
  templateUrl: './add-edit-team.component.html',
  styleUrls: ['../team-management/team-management.component.css']
})
export class AddEditTeamDialog implements OnInit {

  visible = true;
  selectable = true;
  removable = true;
  separatorKeysCodes: number[] = [ENTER, COMMA];

  selectableTo = true;
  removableTo = true;
  separatorKeysCodesTo: number[] = [ENTER, COMMA];

  reportingCtrl = new UntypedFormControl();
  reportingToCtrl = new UntypedFormControl();
  filteredReportingTeam: Observable<any[]>;
  filteredReportingTeamTo: Observable<any[]>;
  reportingTeam: string[] = [];
  allTeamList: string[] = [];
  reportingToTeam: string[] = [];


  @ViewChild('reportingInput') reportingInput: ElementRef<HTMLInputElement>;
  @ViewChild('auto') matAutocomplete: MatAutocomplete;

  @ViewChild('reportingToInput') reportingToInput: ElementRef<HTMLInputElement>;
  @ViewChild('autoTo') matAutocompleteTo: MatAutocomplete;

  constructor(
    public thisDialogRef: MatDialogRef<AddEditTeamDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _team: TeamService,
  ) { }

  error = '';
  globalData: any;
  allUsers: any;
  rootTeam: boolean = false;
  memberName: string;
  teamTitle: string;
  totalRevenue: any = 0;
  totalClosure: any = 0;
  selectedUsers: any = [];
  selectedTeamForView: any;

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    //on edit
    if (this.dataOption.teamId) {
      this.editTeamInfo();
      this.getAllTeam();
    } else {
      this.loadTeamUser();
      this.getAllTeam();
    }
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

  //get all teams
  public getAllTeam() {
    this._team.getTeams().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.allTeamList = response.data;
        this.filteredReportingTeam = this.reportingCtrl.valueChanges.pipe(
          startWith(null),
          map((name: string | null) => name ? this._filter(name) : this.allTeamList.slice()));

        this.filteredReportingTeamTo = this.reportingToCtrl.valueChanges.pipe(
          startWith(null),
          map((name: string | null) => name ? this._filterMe(name) : this.allTeamList.slice()));
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  // on select add reportint to me
  public addToMe(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;
    // Add our reporting to me
    if ((value || '').trim()) {
      this.reportingTeam.push(value.trim());
    }
    // Reset the input value
    if (input) {
      input.value = '';
    }
    this.reportingCtrl.setValue(null);
  }

  //remove reporting to me
  public removeToMe(fruit: string): void {
    const index = this.reportingTeam.indexOf(fruit);

    if (index >= 0) {
      this.reportingTeam.splice(index, 1);
    }
  }

  //select team name to add
  public selected(event: MatAutocompleteSelectedEvent): void {
    let arr = this.reportingToTeam.concat(this.reportingTeam);
    if (arr.indexOf(event.option.viewValue) === -1) {
      this.reportingTeam.push(event.option.viewValue);
      this.reportingInput.nativeElement.value = '';
      this.reportingCtrl.setValue(null);
    } else {
      Swal.fire({
        title: "Warning",
        text: "Name already added",
        type: "warning",
        showConfirmButton: true
      });
      this.reportingInput.nativeElement.value = '';
      this.reportingCtrl.setValue(null);
    }
  }

  // filter report to me
  private _filter(value: string): string[] {
    const filterValue = value.toLowerCase();
    return this.allTeamList.filter((name: any, key: any) => {
      name.teamName.toLowerCase().indexOf(filterValue) === 0
    })
  }


  //on select add reportint me
  public addTo(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;
    // Add our reporting to me
    if ((value || '').trim()) {
      this.reportingToTeam.push(value.trim());
    }
    // Reset the input value
    if (input) {
      input.value = '';
    }
    this.reportingToCtrl.setValue(null);
  }

  //remove reporting to me
  public removeTo(fruit: string): void {
    const index = this.reportingToTeam.indexOf(fruit);

    if (index >= 0) {
      this.reportingToTeam.splice(index, 1);
    }
  }

  //select team name to add
  public selectedReportingTo(event: MatAutocompleteSelectedEvent): void {
    let arr = this.reportingToTeam.concat(this.reportingTeam);
    if (arr.indexOf(event.option.viewValue) === -1) {
      if (this.reportingToTeam.length === 1) {
        Swal.fire({
          title: "Warning",
          text: "Only one repoting name allowed.",
          type: "warning",
          showConfirmButton: true
        });
        return;
      }
      this.reportingToTeam.push(event.option.viewValue);
      this.reportingToInput.nativeElement.value = '';
      this.reportingToCtrl.setValue(null);
    } else {
      Swal.fire({
        title: "Warning",
        text: "Name already added",
        type: "warning",
        showConfirmButton: true
      });
      this.reportingToInput.nativeElement.value = '';
      this.reportingToCtrl.setValue(null);
    }
  }

  // filter report to me
  private _filterMe(value: string): string[] {
    const filterValue = value.toLowerCase();
    return this.allTeamList.filter((name: any, key: any) => {
      name.teamName.toLowerCase().indexOf(filterValue) === 0
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

  //creat a team
  public saveTeam() {
    if (this.teamTitle === '' || this.teamTitle === undefined) {
      Swal.fire({
        title: "Warning",
        text: "Please enter team name.",
        type: "warning",
        showConfirmButton: true
      });
      return;
    }

    var data = {
      "teamName": this.teamTitle,
      "members": [],
      "teamTargetAmount": this.totalRevenue,
      "teamTargetPositionOpeningClosure": this.totalClosure,
      "childrenTeamIds": [],
      "rootTeam": this.rootTeam,
      "parentTeamId": undefined
    };

    //reporing to me team ids
    if (this.reportingTeam.length !== 0) {
      this.allTeamList.forEach((item: any) => {
        this.reportingTeam.forEach((value: any) => {
          if (value === item.teamName) {
            data.childrenTeamIds.push(item.teamId);
          }
        })
      })
    }

    //reporting to team ids
    if (this.reportingToTeam.length !== 0) {
      this.allTeamList.forEach((item: any) => {
        this.reportingToTeam.forEach((value: any) => {
          if (value === item.teamName) {
            data.parentTeamId = item.teamId;
          }
        })
      })
    }

    //selected user array data
    this.selectedUsers.forEach((usr: any) => {
      data.members.push({
        'email': usr.email,
        'role': usr.teamRole,
        "targetAmount": parseInt(usr.revenueTarget) || 0,
        "targetPositionOpeningClosure": parseInt(usr.closureTarget) || 0,

      });
    })

    this._team.createTeam(data).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.thisDialogRef.close(true);
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //get team data on edit
  public editTeamInfo() {
    this._team.getTeamDetails(this.dataOption.teamId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.reportingTeam = [];
        this.reportingToTeam = [];
        this.selectedTeamForView = response.data;
        this.teamTitle = this.selectedTeamForView.teamName;
        this.rootTeam = this.selectedTeamForView.rootTeam;
        this.selectedTeamForView.members.forEach((item: any) => {
          item.revenueTarget = item.teamMemberTargetAmount;
          item.closureTarget = item.teamTargetPositionOpeningClosure
        })
        this.allUsers = this.selectedTeamForView.members;

        this.totalRevenue = this.selectedTeamForView.teamTargetPositionOpeningClosure || 0;
        this.totalClosure = this.selectedTeamForView.teamTargetAmount || 0;
        // reporting to me
        var reportingData = this.selectedTeamForView.childrenTeams;
        reportingData.forEach((item: any) => {
          this.reportingTeam.push(item.teamName);
        })

        var reportingToData = this.selectedTeamForView.parentTeam;
        this.reportingToTeam.push(reportingToData.teamName);
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //save member info on edit
  public saveMemberInfo(member: any) {
    var data = {
      "teamId": this.dataOption.teamId,
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
        this._team.removeMember(data, this.dataOption.teamId).pipe(first()).subscribe((response: any) => {
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

  //update team info
  public updateTeam() {
    if (this.teamTitle === '' || this.teamTitle === undefined) {
      Swal.fire({
        title: "Warning",
        text: "Please enter team name.",
        type: "warning",
        showConfirmButton: true
      });
      return;
    }

    if (this.teamTitle === '' || this.teamTitle === undefined) {
      Swal.fire({
        title: "Warning",
        text: "Please enter team name.",
        type: "warning",
        showConfirmButton: true
      });
      return;
    }

    var data = {
      "teamName": this.teamTitle,
      "members": [],
      "teamTargetAmount": this.totalRevenue,
      "teamTargetPositionOpeningClosure": this.totalClosure,
      "childrenTeamIds": [],
      "rootTeam": this.rootTeam,
      "parentTeamId": undefined
    };

    //reporing to me team ids
    if (this.reportingTeam.length !== 0) {
      this.allTeamList.forEach((item: any) => {
        this.reportingTeam.forEach((value: any) => {
          if (value === item.teamName) {
            data.childrenTeamIds.push(item.teamId);
          }
        })
      })
    }

    //reporting to team ids
    if (this.reportingToTeam.length !== 0) {
      this.allTeamList.forEach((item: any) => {
        this.reportingToTeam.forEach((value: any) => {
          if (value === item.teamName) {
            data.parentTeamId = item.teamId;
          }
        })
      })
    }

    //selected user array data
    this.allUsers.forEach((usr: any) => {
      data.members.push({
        'email': usr.email,
        'role': usr.teamRole,
        "targetAmount": parseInt(usr.revenueTarget) || 0,
        "targetPositionOpeningClosure": parseInt(usr.closureTarget) || 0,

      });
    })
    this._team.updateTeam(data, this.dataOption.teamId).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.thisDialogRef.close(true);
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
