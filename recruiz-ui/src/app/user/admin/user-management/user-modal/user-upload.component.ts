import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators'
import Swal from 'sweetalert2';

//services
import { UserService } from "../userService/user.service";
import { GlobalService } from '../../../globalServices/global.service';
@Component({
  selector: 'app-my-dialog',
  templateUrl: './user-upload.component.html',
  styleUrls: ['../user-management/user-management.component.css']
})
export class UserBulkUploadDialog implements OnInit {


  constructor(
    public thisDialogRef: MatDialogRef<UserBulkUploadDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _user: UserService,
    private _global: GlobalService
  ) { }

  error = '';
  globalData: any;
  headerConstants: any;
  filePath: any;
  importType: any;
  fileHeaders: any;
  isFormSubmitted: boolean;
  headerMapList: any = [];

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    this.headerConstants = this.dataOption.headerConstants;
    this.filePath = this.dataOption.filePath;
    this.importType = this.dataOption.importType;
    this.fileHeaders = this.dataOption.fileHeaders;
    this.isFormSubmitted = false;
    this.initImport();
  }

  public initImport = function () {
    for (var i = 0; i < this.fileHeaders.length; i++) {
      var headerMap = {};
      headerMap[this.fileHeaders[i].value] = this.fileHeaders[i].id;
      this.headerMapList.splice(i, 1, headerMap);
      if (this.headerConstants.indexOf(this.fileHeaders[i].value) == -1) {
        this.isFormSubmitted = true;
      }
    }
  }

  //start import file
  public startImportData() {
    var importData = {
      headerMapList: this.headerMapList,
      filePath: this.filePath,
      importType: this.importType
    };

    this._global.importUserData(importData).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        var len = response.data.length;
        if (len > 0) {
          Swal.fire({
            title: "Uploaded",
            text: "Activation link has been sent to<b> " + len + "</b> users",
            type: "success",
            showConfirmButton: true
          });
          this.thisDialogRef.close(true);
        } else {
          Swal.fire({
            title: "Warning",
            text: "Users already exist. Please check and try again!",
            type: "warning",
            showConfirmButton: true
          });
          this.thisDialogRef.close(true);
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  onCloseConfirm() {
    this.thisDialogRef.close(true);
  }

  onCloseCancel() {
    this.thisDialogRef.close();
  }

}
