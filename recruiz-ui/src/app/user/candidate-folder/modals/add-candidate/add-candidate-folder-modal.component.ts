import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { UntypedFormControl } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';

//services
import { FolderService } from '../../folderService/folder.service';

@Component({
    selector: 'app-my-dialog',
    templateUrl: './add-candidate-folder-modal.component.html',
    styleUrls: ['../../candidate-folder.component.css']
})
export class AddCandidateFolderlDialog implements OnInit {
    public textControl = new UntypedFormControl('');
    public descriptionLength = new BehaviorSubject(0);

    constructor(
        public thisDialogRef: MatDialogRef<AddCandidateFolderlDialog>, @Inject(MAT_DIALOG_DATA)
        public dataOption: any,
        private folder: FolderService
    ) {
        this.textControl.valueChanges.subscribe((v) => {
            if (v) {
                this.descriptionLength.next(v.length)
            }
        });
    }

    error = '';
    globalData: any;
    folderName: any;
    candidateList: any;
    pageSize: any;
    totalElements: any;
    totalPageCount: any;
    onPage: any;
    currentPage: any;
    candidateIdsList: Array<any> = [];

    ngOnInit() {
        //local storage data
        this.globalData = JSON.parse(localStorage.getItem('userInfo'));

        this.loadCandidate(undefined);
    }

    //get list of candidate
    public loadCandidate(pageNo: any) {
        if (pageNo) {
            this.currentPage = pageNo - 1;
        } else {
            this.currentPage = 0;
        }
        this.folder.getCandidateList(this.dataOption.folderName, this.currentPage).pipe(first()).subscribe(response => {
            if (response.success) {
                this.candidateList = response.data.content;
                this.pageSize = response.data.size;
                this.totalElements = response.data.totalElements;
                this.totalPageCount = response.data.totalPages;
                this.onPage = response.data.number + 1;
                if (this.candidateIdsList.length > 0) {
                    this.candidateList.forEach((item: any) => {
                        this.candidateIdsList.forEach((value : any) => { 
                            if (item.cid === value) {
                                item.selected = true;
                            }
                        })
                    })
                }
               
            }
        }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
        })
    }

    //push selected candidate id
    public selectedCandidate(candidateData: any, indexId: any) {
        if (candidateData.selected === true) {
            this.candidateIdsList.push(candidateData.cid);
        } else {
            var index = this.candidateIdsList.indexOf(candidateData.cid);
            if (index > -1) {
                this.candidateIdsList.splice(index, 1);
            }
        }
    }

    //add candidate to folder
    public addCandidateToFolder() {
        if (this.candidateIdsList.length === 0) {
            Swal.fire({
                title: "Alert...",
                text: "You have not selected any candidate to add to the folder.",
                type: "warning",
                showConfirmButton: true
            });
            return true;
        }
        var inputData = {
            folderName: this.dataOption.folderName,
            candidateIds: this.candidateIdsList
        }
        this.folder.addCandidateToFolder(inputData).pipe(first()).subscribe(response => {
            if (response.success) {
                this.thisDialogRef.close(true);
            }
        })
    }

    onCloseConfirm() {
        this.thisDialogRef.close();
    }

    onCloseCancel() {
        this.thisDialogRef.close();
    }

}