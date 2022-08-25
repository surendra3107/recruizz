import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
//services
import { GlobalService } from '../../../globalServices/global.service';
import { ClientService } from './../../clientService/client.service';
@Component({
    selector: 'app-my-dialog',
    templateUrl: './decision-maker-dialog.component.html',
    styleUrls: ['../../client-detail/client-detail.component.css']
})
export class ClientDecisionMakerlDialog implements OnInit {

    constructor(
        public thisDialogRef: MatDialogRef<ClientDecisionMakerlDialog>, @Inject(MAT_DIALOG_DATA)
        public dataOption: any,
        private global: GlobalService,
        private clientServ: ClientService
    ) { }

    error = '';
    allDesionMaker: any;
    allGlobalDecisionMakerList: Array<any> = [];
    selectedDescisionMakerLists: Array<any> = [];
    manuallyAddedDecisionMaker: Array<any> = [];

    errorEmail: any;
    errorIndex: any;
    isInterviewerEmailExist: any;

    ngOnInit() {
        this.loadDecisionMaker();
        if (this.dataOption.modalType === 'update') {
            this.manuallyAddedDecisionMaker = [this.dataOption.descisionMakerList];
        }
    }

    //get all decision maker
    public loadDecisionMaker() {
        this.global.getAllDecisionMaker().pipe(first()).subscribe(response => {
            if (response.success) {
                this.allDesionMaker = response.data;
                this.allDesionMaker.forEach((item: any) => {
                    item.selected = false;
                });

                if (this.dataOption.descisionMakerList === undefined || this.dataOption.descisionMakerList.length === 0) {
                    this.allGlobalDecisionMakerList = this.allDesionMaker;
                } else {
                    this.allDesionMaker.forEach((itemData: any) => {
                        var added = false;
                        this.dataOption.descisionMakerList.forEach((value: any) => {
                            if (itemData.email === value.email) {
                                added = true;
                                return;
                            }
                        });
                        if (!added) {
                            this.allGlobalDecisionMakerList.push(itemData);
                        }
                    });
                }
            }
        }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
        })
    }

    //check uncheck descion maker
    public selectDecisionMaker(data: any) {
        if (data.selected === true) {
            var intList = {
                'name': data.name,
                'mobile': data.mobile,
                'email': data.email,
                'interviewerTimeSlots': data.slotRows
            }
            this.selectedDescisionMakerLists.push(intList);
        } else {
            var removeList = {
                'name': data.name,
                'mobile': data.mobile,
                'email': data.email,
                'interviewerTimeSlots': data.slotRows
            }
            var index = this.selectedDescisionMakerLists.indexOf(removeList);
            this.selectedDescisionMakerLists.splice(index, 1);
        }
    }

    //add interviewer decision maker
    public addMoreDecisionMaker() {
        const item = {
            name: '',
            email: '',
            mobile: ''
        }
        this.manuallyAddedDecisionMaker.push(item);
    }

    //remove interviewer manually
    public removeAddedDecisionMaker(index: any) {
        this.manuallyAddedDecisionMaker.splice(index, 1);
    }

    //email validation
    public validateDecisionMakerEmail(index: any, email: any) {
        var isEmailDuplicate = this.isEmailDuplicate(this.manuallyAddedDecisionMaker);
        if (!isEmailDuplicate) {
            //check for existing email
            // this.clientServ.validateDecisionMakerEmail(email, this.dataOption.clientId).pipe(first()).subscribe(response => {
            //     if (response.success) {
            //         if (response.data === true) {
            //             this.errorEmail = "Email aleady exists in record.";
            //             this.errorEmail = '';
            //             this.errorIndex = index;
            //             this.isInterviewerEmailExist = true;
            //         } else {
            //             this.errorEmail = '';
            //             this.errorIndex = index;
            //             this.isInterviewerEmailExist = false;
            //         }
            //     }
            // }, error => {
            //     console.log('Error : ' + JSON.stringify(error));
            //     this.error = error;
            // });
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

    //add descision maker for client
    public submitDescisionMakerInfo() {
        let decisionMakerArray = this.selectedDescisionMakerLists.concat(this.manuallyAddedDecisionMaker);
        if (this.dataOption.modalType === 'add-client') {
            this.thisDialogRef.close(decisionMakerArray);
        } else {
            this.clientServ.addDecisionMaker(decisionMakerArray, this.dataOption.clientId).pipe(first()).subscribe(response => {
                if (response.success) {
                    this.thisDialogRef.close('decision-maker-added');
                }
            })
        }
    }

    //update decision maker info
    public updateDescisionMakerInfo() {
        this.manuallyAddedDecisionMaker.forEach((item: any) => {
            var formData = {
                "id": item.id,
                "name": item.name,
                "email": item.email,
                "mobile": item.mobile
            }
            this.clientServ.updateDecisionMaker(formData, item.email).pipe(first()).subscribe(response => {
                if (response.success) {
                    this.thisDialogRef.close('decision-maker-updated');
                }
            })
        })
    }

    onCloseConfirm() {
        this.thisDialogRef.close();
    }

    onCloseCancel() {
        this.thisDialogRef.close();
    }

    public validateInterviewerEmail(index: any, enmail: any) {

    }
}