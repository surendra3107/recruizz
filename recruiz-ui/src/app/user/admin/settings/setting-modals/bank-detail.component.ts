import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';

//services
import { SettingService } from '../settingService/setting.service';

@Component({
  selector: 'app-my-dialog',
  templateUrl: './bank-detail-modal.component.html',
  styleUrls: ['../setting-management/setting-management.component.css']
})
export class BankDetailDialog implements OnInit {
  constructor(
    public thisDialogRef: MatDialogRef<BankDetailDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _setting: SettingService,
  ) { }

  error = '';
  globalData: any;
  bank: any = {};


  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    //on update auto fill
    if (this.dataOption.modalType) {
      this.bank = this.dataOption.bank;
    }
  }

  //add bank details
  public addBankDetails() {
    if (this.bank.accountName && this.bank.accountNumber && this.bank.bankName && this.bank.branch && this.bank.ifscCode) {
      var FormData = {
        "accountName": this.bank.accountName,
        "bankName": this.bank.bankName,
        "branch": this.bank.branch,
        "accountNumber": this.bank.accountNumber,
        "ifscCode": this.bank.ifscCode,
      }
      this._setting.postBankDetails(FormData).pipe(first()).subscribe((response: any) => {
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

  //update bank details
  public updateBankDetails() {
    if (this.bank.accountName && this.bank.accountNumber && this.bank.bankName && this.bank.branch && this.bank.ifscCode) {
      var FormData = {
        "accountName": this.bank.accountName,
        "bankName": this.bank.bankName,
        "branch": this.bank.branch,
        "accountNumber": this.bank.accountNumber,
        "ifscCode": this.bank.ifscCode,
      }
      this._setting.updateBankDetails(FormData, this.bank.id).pipe(first()).subscribe((response: any) => {
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
