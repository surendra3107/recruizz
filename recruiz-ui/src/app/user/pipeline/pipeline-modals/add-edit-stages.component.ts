import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from "sweetalert2";
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';

//services
import { PipelineService } from "./../pipelineService/pipeline.service";

@Component({
  selector: 'app-my-dialog',
  templateUrl: './add-edit-stages.component.html',
  styleUrls: ['../pipeline/pipeline.component.css']
})
export class AddEditStagesDialog implements OnInit {

  constructor(
    public thisDialogRef: MatDialogRef<AddEditStagesDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _pipeline: PipelineService
  ) { }

  error = '';
  globalData: any;
  stageName: string;
  boardData: any;
  rounds: any = [];
  preDeleteRoundArray: any = [];

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
    this.loadAllStages();
  }

  //get all added stages
  public loadAllStages() {
    this._pipeline.getBoardDataInfo(this.dataOption.positionCode, undefined, undefined, undefined, undefined, undefined).pipe(first()).subscribe(response => {
      if (response.success) {
        this.boardData = response.data;
        this.rounds = response.data.rounds;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    });
  };

  //drag drop stages
  public drop(event: CdkDragDrop<string[]>) {
    if (event.currentIndex === 0 || event.previousIndex === 0) {
      Swal.fire({
        title: "Warning",
        text: "Not allowed to drag drop 'Sourcing'.",
        type: "warning",
        showConfirmButton: true
      });
    } else {
      moveItemInArray(this.rounds, event.previousIndex, event.currentIndex);
    }

  }

  //add stages
  public addRounds() {
    this.rounds.push({ name: this.stageName, type: '' });
    this.stageName = '';
  }

  //delete stages
  public preDeleteRound(roundId: any, index: any, isStrike: any) {
    if (isStrike === true) {
      if (roundId) {
        this.preDeleteRoundArray.splice(this.preDeleteRoundArray.indexOf(roundId));
      }
      this.rounds[index].isStrike = false;
    } else {
      if (roundId) {
        this.preDeleteRoundArray.push(roundId);
      }
      this.rounds[index].isStrike = true;
    }
  }

  //save rounds
  public saveRounds() {
    var roundData: any = [];
    this.rounds.forEach((item: any) => {
      if (item.name && !item.isStrike) {
        roundData.push({ roundName: item.name, roundType: item.type, roundId: item.roundId });
      }
    })

    var nameArray = roundData.map((roundObject: any) => {
      return roundObject.roundName;
    });

    var isNameExist = nameArray.some((nameObject: any, index: any) => {
      return nameArray.indexOf(nameObject) !== index;
    });

    if (!isNameExist) {
      this._pipeline.saveRound(this.boardData.boardId, roundData).pipe(first()).subscribe(response => {
        if (response.success) {
          if (this.preDeleteRoundArray.length > 0) {
            this._pipeline.deleteRound(this.boardData.boardId, this.preDeleteRoundArray).pipe(first()).subscribe(response => {
              if (response.success) {
                Swal.fire({
                  title: "Success",
                  text: "Stages are saved into pipeline.",
                  type: "success",
                  timer:2000,
                  showConfirmButton: false
                });
                this.thisDialogRef.close(true);
               }
            }, error => {
              console.log('Error : ' + JSON.stringify(error));
              this.error = error;
            })
          } else {
            this.thisDialogRef.close(true);
           }
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      });
    }

  }

  onCloseConfirm() {
    this.thisDialogRef.close();
  }

  onCloseCancel() {
    this.thisDialogRef.close();
  }

}
