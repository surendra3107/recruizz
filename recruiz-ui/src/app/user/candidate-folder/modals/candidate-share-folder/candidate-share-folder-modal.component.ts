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
    templateUrl: './candidate-share-folder-modal.component.html',
    styleUrls: ['../../candidate-folder.component.css']
})
export class CandidateShareFolderlDialog implements OnInit {
    public textControl = new UntypedFormControl('');
    public descriptionLength = new BehaviorSubject(0);

    constructor(
        public thisDialogRef: MatDialogRef<CandidateShareFolderlDialog>, @Inject(MAT_DIALOG_DATA)
        public dataOption: any,
        private folder: FolderService
    ) { }

    error = '';
    globalData: any;
    userList: any;
    selectedUsers: Array<any> = [];
    modalType: string;
    addedUserList: any;
    countSelected: Array<boolean> = [];

    ngOnInit() {
        //local storage data
        this.globalData = JSON.parse(localStorage.getItem('userInfo'));
        this.modalType = this.dataOption.modalType;
        this.loadUserList();
        this.loadAddedUserList();
    }


    //update candidate folder
    public loadUserList() {
        this.folder.getUserListToShareFolders(this.dataOption.folderName).pipe(first()).subscribe(response => {
            if (response.success) {
                this.userList = response.data;
            }
        }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
        })
    }

    //share folder details
    public shareCandidateFolder() {
        this.userList.forEach((item: any) => {
            if (item.selected === true) {
                this.selectedUsers.push(item.email);
            }
        })
        let formData = {
            'folderName': this.dataOption.folderName,
            'userEmails': this.selectedUsers
        }
        this.folder.shareFoldersToUsers(formData).pipe(first()).subscribe(response => {
            if (response.success) {
                this.thisDialogRef.close(true);
            }
        }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
        })
    }

    //get list of users to whom the folder has been shared
    public loadAddedUserList() {
        if (this.modalType) {
            this.folder.sharedFolderUserList(this.dataOption.folderName).pipe(first()).subscribe(response => {
                if (response.success) {
                    this.addedUserList = response.data;
                }
            })
        }

    }

    //unshare folder
    public unshareCandidateFolder() {
        this.addedUserList.forEach((item: any) => {
            if (item.selected === true) {
                this.selectedUsers.push(item.email);
            }
        })
        let formData = {
            'folderName': this.dataOption.folderName,
            'userEmails': this.selectedUsers
        }

        this.folder.unshareFoldersToUsers(formData).pipe(first()).subscribe(response => {
            if (response.success) {
                this.thisDialogRef.close(true);
            }
        }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
        })
    }

    //selected count
    public selectedItem(selectBoolean: any, index: any) {
        if (selectBoolean) {
            this.countSelected.push(selectBoolean);
        } else {
            this.countSelected.splice(index, 1);
        }
    }

    onCloseConfirm() {
        this.thisDialogRef.close();
    }

    onCloseCancel() {
        this.thisDialogRef.close();
    }

}