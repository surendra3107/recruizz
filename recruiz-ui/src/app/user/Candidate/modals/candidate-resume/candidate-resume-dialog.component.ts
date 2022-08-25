import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { DomSanitizer } from '@angular/platform-browser';

//services
import { GlobalService } from '../../../globalServices/global.service';

@Component({
    selector: 'app-my-dialog',
    templateUrl: './candidate-resume-dialog.component.html',
    styleUrls: ['../../candidate-detail/candidate-detail.component.css']
})
export class CandidateResumelDialog implements OnInit {

    constructor(
        public thisDialogRef: MatDialogRef<CandidateResumelDialog>, @Inject(MAT_DIALOG_DATA)
        public dataOption: any,
        private _global: GlobalService,
        private sanitizer: DomSanitizer

    ) { }

    error = '';
    contentPdfUrl: any;
    ngOnInit() {
        this.loadResume();
    }

    //load resume
    public loadResume() {
        this._global.getCandidateResume(this.dataOption.linkUrl).pipe(first()).subscribe(response => {
            if (response) {
                var file = new Blob([response], {
                    type: 'application/pdf'
                });
                var fileURL = URL.createObjectURL(file);
                this.contentPdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(fileURL);
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