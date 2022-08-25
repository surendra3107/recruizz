import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
//services
import { PositionService } from '../../positionService/position.service';

@Component({
    selector: 'app-my-dialog',
    templateUrl: './vendor-modal.component.html',
    styleUrls: ['../../positions-detail/positions-detail.component.css']
})
export class VendorPannelDialog implements OnInit {

    constructor(
        public thisDialogRef: MatDialogRef<VendorPannelDialog>, @Inject(MAT_DIALOG_DATA)
        public dataOption: any,
        private position: PositionService,
    ) { }

    error = '';

    vendorsList: any;
    allGlobalVendorList: any;
    selectedVendors: Array<any> = [];
    isSelected: boolean;

    ngOnInit() {
        this.getVendors();
    }

    //get all vendors
    public getVendors() {
        this.position.fetchVendors().pipe(first()).subscribe(response => {
            if (response.success) {
                this.vendorsList = response.data;

                if (this.dataOption.addedVendor === undefined || this.dataOption.addedVendor.length === 0) {
                    this.allGlobalVendorList = this.vendorsList;
                } else {
                    this.vendorsList.forEach((itemData: any) => {
                        var added = false;
                        this.dataOption.addedVendor.forEach((value: any) => {
                            if (itemData.email === value.email) {
                                added = true;
                                return;
                            }
                        });
                        if (!added) {
                            this.allGlobalVendorList.push(itemData);
                        }
                    });
                }

            }
        }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
        })
    }

    //select all vendors
    public selectAllVendor(isSelcted: boolean) {
        if (isSelcted) {
            this.vendorsList.forEach((items: any) => {
                items.selectStatus = true;
                this.selectedVendors.push(items.id);
            })
        } else {
            this.vendorsList.forEach((items: any) => {
                items.selectStatus = false;
                var index = this.selectedVendors.indexOf(items.id);
                this.selectedVendors.splice(index, 1);
            })
        }
    }

    //select vendors individual
    public selectIndividualVendor(vendorData: any) {
        if (vendorData.selectStatus === true) {
            this.selectedVendors.push(vendorData.id);
        } else {
            var index = this.selectedVendors.indexOf(vendorData.id);
            this.selectedVendors.splice(index, 1);
            if (this.selectedVendors.length === 0) {
                this.isSelected = false;
            }
        }
    }

    //add vendor to position
    public vendorsToPosition() {
        if (this.selectedVendors.length === 0) {
            Swal.fire({
                title: "Warning",
                text: "Please select vendors to add them",
                type: "warning",
                showConfirmButton: true
            })
            return true;
        }
        this.position.addVendorsToPosition(this.selectedVendors, this.dataOption.positionId).pipe(first()).subscribe(response => {
            if (response.success) {
                this.thisDialogRef.close('vendor-added');
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