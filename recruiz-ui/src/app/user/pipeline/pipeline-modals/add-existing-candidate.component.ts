import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from "sweetalert2";
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';

//services
import { PipelineService } from "../pipelineService/pipeline.service";

@Component({
  selector: 'app-my-dialog',
  templateUrl: './add-existing-candidate.component.html',
  styleUrls: ['../pipeline/pipeline.component.css']
})
export class AddExistingCandidateDialog implements OnInit {

  constructor(
    public thisDialogRef: MatDialogRef<AddExistingCandidateDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _pipeline: PipelineService
  ) { }

  error = '';
  globalData: any;
  candidateName: string;
  candidateList: any;

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
    this.loadCandidateList();
  }

  //get all added stages
  public loadCandidateList() {
    this._pipeline.getCandidatesToSource(this.dataOption.boardId).pipe(first()).subscribe(response => {
      if (response.success) {
        this.candidateList = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    });
  };

  //count selected
  public calculateChecked() {
    if (this.candidateList) {
      var count = 0;
      this.candidateList.forEach((value: any) => {
        if (value.selected)
          count++;
      })
      return count;
    }

  }

  //add candidate to pipeline
  public addCandidates() {
    var candidateArray: any = [];
    this.candidateList.forEach((candidate: any) => {
      if (candidate.selected) {
        candidateArray.push(candidate.email);
      }
    })
    var candidateData = {
      candidateEmailList: candidateArray,
      positionCode: this.dataOption.positionCode
    };
    this._pipeline.addCandidatesToSource(candidateData, undefined).pipe(first()).subscribe(response => {
      if (response.success) {
        Swal.fire({
          title: "Added",
          text: "Candidate added to pipeline Successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.thisDialogRef.close(true);
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    });
  }

  onCloseConfirm() {
    this.thisDialogRef.close();
  }

  onCloseCancel() {
    this.thisDialogRef.close();
  }

}
