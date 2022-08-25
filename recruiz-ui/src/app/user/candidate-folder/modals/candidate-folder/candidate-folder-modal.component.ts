import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { UntypedFormControl } from '@angular/forms';
import { BehaviorSubject } from 'rxjs';

//services
import { FolderService } from './../../folderService/folder.service';

@Component({
    selector: 'app-my-dialog',
    templateUrl: './candidate-folder-modal.component.html',
    styleUrls: ['../../candidate-folder.component.css']
})
export class CandidateFolderlDialog implements OnInit {
    public textControl = new UntypedFormControl('');
    public descriptionLength = new BehaviorSubject(0);

    constructor(
        public thisDialogRef: MatDialogRef<CandidateFolderlDialog>, @Inject(MAT_DIALOG_DATA)
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
    folderDescription: any;

    ngOnInit() {
        //local storage data
        this.globalData = JSON.parse(localStorage.getItem('userInfo'));
        if (this.dataOption.folderData) {
            this.folderName = this.dataOption.folderData.folderName;
            this.folderDescription = this.dataOption.folderData.folderDesc;
        }
    }

    //create folder
    public createCandidateFolder(folderName: any, folderDescription: any) {
        let formData = {
            'folderName': folderName,
            'folderDesc': folderDescription
        }

        this.folder.createCandidateFolders(formData).pipe(first()).subscribe(response => {
            if (response.success) {
                this.thisDialogRef.close('added');
            }
        }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
        })
    }

    //update candidate folder
    public updateCandidateFolder(folderName: any, folderDescription: any) {
        var formData = {
            'folderName': folderName,
            'folderDesc': folderDescription,
            'id': this.dataOption.folderData.id
        }
        this.folder.updateCandidateFolders(formData).pipe(first()).subscribe(response => {
            if (response.success) {
                this.thisDialogRef.close('update');
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