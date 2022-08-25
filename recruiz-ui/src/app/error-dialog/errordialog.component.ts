import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
@Component({
    selector: 'app-root',
    templateUrl: './errordialog.component.html'
})
export class ErrorDialogComponent {
    title = 'Angular-Interceptor';
  constructor(
    public thisDialogRef: MatDialogRef<ErrorDialogComponent>, @Inject(MAT_DIALOG_DATA)
    public data: any
  ) { }

  onCloseConfirm() {
    this.thisDialogRef.close();
  }

  onCloseCancel() {
    this.thisDialogRef.close();
  }

}
