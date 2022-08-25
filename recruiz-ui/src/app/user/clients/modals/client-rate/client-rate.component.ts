import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';

//services
import { ClientService } from '../../clientService/client.service';

@Component({
    selector: 'app-my-dialog',
    templateUrl: './client-rate-modal.component.html',
    styleUrls: ['../../client-detail/client-detail.component.css']
})
export class ClientRateslDialog implements OnInit {
    constructor(
        public thisDialogRef: MatDialogRef<ClientRateslDialog>, @Inject(MAT_DIALOG_DATA)
        public dataOption: any,
        private clientServ: ClientService,
    ) { }

    error = '';
    globalData: any;
    percetRows: Array<any> = [];

    ngOnInit() {
        //local storage data
        this.globalData = JSON.parse(localStorage.getItem('userInfo'));

        //field array list
        if (this.dataOption.modalType === 'add-rates') {
            this.percetRows.push({ name: '', value: '', type: 'percentage' });
        }
        if (this.dataOption.modalType === 'update') {
            this.percetRows = [this.dataOption.rateData];
        }
    }

    //add more input field
    public addPercentRows() {
        this.percetRows.push({ name: '', value: '', type: 'percentage' });
    }

    //remove input field
    public removePercentRows(index: any) {
        this.percetRows.splice(index, 1);
    };

    //add rates
    public addClientRates() {
        for (var i = 0; i < this.percetRows.length; ++i) {
            var item = this.percetRows[i];
            if (item.name === '' || item.value === '') {
                Swal.fire({
                    title: "Alert",
                    text: "Please fill all mandatory fields.",
                    type: "warning",
                    showConfirmButton: true
                });
                return false;
            }
        }
        this.clientServ.addUpdateClientRates(this.dataOption.clientId, this.percetRows).pipe(first()).subscribe(response => {
            if (response.success) {
                if (this.dataOption.modalType === 'add-rates') {
                    this.thisDialogRef.close('rates-added');
                } else {
                    this.thisDialogRef.close('rates-updated');
                }

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