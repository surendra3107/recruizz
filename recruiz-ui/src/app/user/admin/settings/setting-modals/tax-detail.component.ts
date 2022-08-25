import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';

//services
import { SettingService } from '../settingService/setting.service';

@Component({
  selector: 'app-my-dialog',
  templateUrl: './tax-detail-modal.component.html',
  styleUrls: ['../setting-management/setting-management.component.css']
})
export class TaxDetailDialog implements OnInit {
  constructor(
    public thisDialogRef: MatDialogRef<TaxDetailDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _setting: SettingService,
  ) { }

  error = '';
  globalData: any;
  taxName: string;
  taxNumber: string;

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

  }

  //add bank details
  public addBankDetails() {
    if (this.taxName && this.taxNumber) {
      var FormData = {
        "taxName": this.taxName,
        "taxValue": this.taxNumber
      }
      this._setting.postTaxDetails(FormData).pipe(first()).subscribe((response: any) => {
        if (response.success) {
          this.thisDialogRef.close(true);
         }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    } else {
      Swal.fire({
        title: "Warning",
        text: "All fields are mandatory.",
        type: "warning",
        showConfirmButton: true
      });
    }
  }

  onCloseConfirm() {
    this.thisDialogRef.close();
  }

  onCloseCancel() {
    this.thisDialogRef.close();
  }

}
