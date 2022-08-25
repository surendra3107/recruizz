import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';

import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
//services
import { ClientService } from './../../clientService/client.service';

@Component({
    selector: 'app-my-dialog',
    templateUrl: './client-notes-modal.component.html',
    styleUrls: ['../../client-detail/client-detail.component.css']
})
export class ClientNoteslDialog implements OnInit {
    public Editor = ClassicEditor;
    constructor(
        public thisDialogRef: MatDialogRef<ClientNoteslDialog>, @Inject(MAT_DIALOG_DATA)
        public dataOption: any,
        private clientServ: ClientService,
    ) { }

    error = '';
    globalData: any;
    bodyData: any = '';
    noteId: any;
    addedBy: any;

    ngOnInit() {
        //local storage data
        this.globalData = JSON.parse(localStorage.getItem('userInfo'));

        //on edit
        if (this.dataOption.notesData) {
            this.bodyData = this.dataOption.notesData.notes;
            this.noteId = this.dataOption.notesData.id;
            this.addedBy = this.dataOption.notesData.addedBy;
        }
    }

    //add notes
    public addNotesToClient() {
        if (this.bodyData === '' || !this.bodyData) {
            Swal.fire({
                title: "Alert...",
                text: "Notes does not have any content to add",
                type: "warning",
                showConfirmButton: true
            });
            return true;
        }
        var clientDataNotes = {
            notes: this.bodyData,
            addedBy: this.globalData.email
        };
        this.clientServ.postNotesToClient(this.dataOption.clientId, clientDataNotes).pipe(first()).subscribe(response => {
            if (response.success) {
                this.thisDialogRef.close('notes-added');
            }
        }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
        })
    }

    //update notes
    public updateNotesToPosition() {
        var clientDataNotes = {
            notes: this.bodyData,
            addedBy: this.addedBy
        };
        this.clientServ.updateNotesForClient(this.dataOption.clientId, clientDataNotes, this.noteId).pipe(first()).subscribe(response => {
            if (response.success) {
                this.thisDialogRef.close('notes-updated');
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