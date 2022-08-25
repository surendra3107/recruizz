import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
//services
import { GlobalService } from '../../../globalServices/global.service';
import { PositionService } from './../../positionService/position.service';
@Component({
    selector: 'app-my-dialog',
    templateUrl: './interviewer-dialog.component.html',
    styleUrls: ['../../positions-detail/positions-detail.component.css']
})
export class InterviwerPannelDialog implements OnInit {

    constructor(
        public thisDialogRef: MatDialogRef<InterviwerPannelDialog>, @Inject(MAT_DIALOG_DATA)
        public dataOption: any,
        private global: GlobalService,
        private position: PositionService
    ) { }

    error = '';
    allInterviwerList: any;
    selectedInterviewerLists: Array<any>;
    allGlobalInterviwerList: Array<any> = [];
    manuallyAddedInterviewer: Array<any> = [];

    errorEmail: any;
    errorIndex: any;
    isInterviewerEmailExist: any;
    formData: any;
    ngOnInit() {

        this.loadInterviewer();

        if (this.dataOption.mode === 'edit-mode') {
            this.manuallyAddedInterviewer = [this.dataOption.interviwerLists];
        }

    }

    // load all interviewer
    public loadInterviewer() {
        if (this.dataOption.mode !== 'edit-mode') {
            this.global.getAllInterviewer().pipe(first()).subscribe(response => {
                if (response.success === true) {
                    this.allInterviwerList = response.data;
                    this.allInterviwerList.forEach((item: any) => {
                        item.selected = false;
                    });

                    if (this.dataOption.interviwerLists === undefined || this.dataOption.interviwerLists.length === 0) {
                        this.allGlobalInterviwerList = this.allInterviwerList;
                    } else {
                        this.allInterviwerList.forEach((itemData: any) => {
                            var added = false;
                            this.dataOption.interviwerLists.forEach((value: any) => {
                                if (itemData.email === value.email) {
                                    added = true;
                                    return;
                                }
                            });
                            if (!added) {
                                this.allGlobalInterviwerList.push(itemData);
                            }
                        });
                    }

                }
            }, error => {
                console.log('Error : ' + JSON.stringify(error));
                this.error = error;
            });
        }
    }

    //add interviewer to position
    public addInterviewerToPosition() {
        this.selectedInterviewerLists = [];
        this.allInterviwerList.forEach((item: any) => {
            if (item.selected === true) {
                var intList = {
                    'name': item.name,
                    'mobile': item.mobile,
                    'email': item.email
                }
                this.selectedInterviewerLists.push(intList);
            }
        });
        if (this.selectedInterviewerLists.length === 0) {
            Swal.fire({
                title: "Alert...!",
                text: "No interviewer selected, please select to add to the position.",
                type: "warning",
                showConfirmButton: true
            });
            return true;
        }
        let interviewerListArray = this.selectedInterviewerLists.concat(this.manuallyAddedInterviewer);
        //calling api to add
        this.global.addSelectedInterviewerToPosition(interviewerListArray, this.dataOption.positionId).pipe(first()).subscribe(response => {
            if (response.success === true) {
                this.thisDialogRef.close('interviwer-added');
            }
        }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
        });
    }

    //add interviewer manually
    public addMoreInterviewer() {
        const item = {
            name: '',
            email: '',
            mobile: ''
        }
        this.manuallyAddedInterviewer.push(item);
    }

    //update interviewer
    public updateInterviewerToPosition() {
        this.manuallyAddedInterviewer.forEach(item => {
            this.formData = {
                "id": item.id,
                "name": item.name,
                "email": item.email,
                "mobile": item.mobile
            }
        })

        //calling api to add
        this.global.updateSelectedInterviewerToPosition(this.formData, this.formData.email).pipe(first()).subscribe(response => {
            if (response.success === true) {
                this.thisDialogRef.close('interviwer-updated');
            }
        }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
        });
    }

    //remove interviewer manually
    public removeAddedInterviewer(index: any) {
        this.manuallyAddedInterviewer.splice(index, 1);
    }

    //email validation
    public validateInterviewerEmail(index: any, email: any) {
        var isEmailDuplicate = this.isEmailDuplicate(this.manuallyAddedInterviewer);
        if (!isEmailDuplicate) {
            //check for existing email
            this.position.validateInterviewerEmail(email).pipe(first()).subscribe(response => {
                if (response.success) {
                    if (response.data === true) {
                        this.errorEmail = "Email aleady exists in record.";
                        this.errorEmail = '';
                        this.errorIndex = index;
                        this.isInterviewerEmailExist = true;
                    } else {
                        this.errorEmail = '';
                        this.errorIndex = index;
                        this.isInterviewerEmailExist = false;
                    }
                }
            }, error => {
                console.log('Error : ' + JSON.stringify(error));
                this.error = error;
            });
        } else {
            this.errorEmail = "Duplicate email entry.";
            this.errorIndex = index;
            this.isInterviewerEmailExist = true;
        }
    }

    //check for duplicate
    public isEmailDuplicate(rowsMap: any) {
        var emailArray = rowsMap.map(function (emailObject: any) {
            return emailObject.email;
        });
        var isEmailDuplicate = emailArray.some(function (emailObject: any, index: any) {
            if (emailObject !== '')
                return emailArray.indexOf(emailObject) !== index;
        });
        return isEmailDuplicate;
    }

    //validate id email already exits or not


    onCloseConfirm() {
        this.thisDialogRef.close();
    }

    onCloseCancel() {
        this.thisDialogRef.close();
    }

}