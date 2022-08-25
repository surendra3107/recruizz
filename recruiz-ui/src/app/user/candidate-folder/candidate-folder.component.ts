import { Component, OnInit } from '@angular/core';

import { Router, ActivatedRoute } from '@angular/router';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { MatDialog } from '@angular/material/dialog';
import { CandidateFolderlDialog } from './modals/candidate-folder/candidate-folder-modal.component';
import { CandidateShareFolderlDialog } from './modals/candidate-share-folder/candidate-share-folder-modal.component';
import { AddCandidateFolderlDialog } from './modals/add-candidate/add-candidate-folder-modal.component';
//services
import { FolderService } from './folderService/folder.service';
import { from } from 'rxjs';

@Component({
  selector: 'app-candidate-folder',
  templateUrl: './candidate-folder.component.html',
  styleUrls: ['./candidate-folder.component.css']
})
export class CandidateFolderComponent implements OnInit {

  constructor(
    public dialog: MatDialog,
    private folder: FolderService
  ) { }

  //declairation
  error: any = '';
  candidateFolder: any;
  ngOnInit() {
    this.loadFolderList();
  }

  //get all folders
  public loadFolderList() {
    this.folder.getAllCandidateFolders().pipe(first()).subscribe(response => {
      if (response.success) {
        this.candidateFolder = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //open modals to create folder
  public openModalToCreateFolder(folderData: any) {
    let dialogRef = this.dialog.open(CandidateFolderlDialog, {
      width: '800px',
      data: { folderData: folderData },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'added') {
        Swal.fire({
          title: "Added",
          text: "Folder created successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadFolderList();
      } else if (result === 'update') {
        Swal.fire({
          title: "Updated",
          text: "Folder updated successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadFolderList();
      }
    })
  }

  //open modal to update folder
  public updateCandidateFolder(folderData: any) {
    this.openModalToCreateFolder(folderData);
  }

  //delete candidate folder
  public deleteCandidateFolder(folderName: any) {
    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete this folder?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then(result => {
      if (result.value) {
        this.folder.deleteCandidateFolders(folderName).pipe(first()).subscribe(response => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Folder deleted successfully.",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.loadFolderList();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //open modals to view user list
  public openModalToViewUserList(folderName: any) {
    let dialogRef = this.dialog.open(CandidateShareFolderlDialog, {
      width: '900px',
      data: { folderName: folderName },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        Swal.fire({
          title: "Shared",
          text: "Folder shared successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadFolderList();
      }
    })
  }

  //open modal to unshare folder
  public openModalToUnshareFolder(folderName: any, modalType: string) {
    let dialogRef = this.dialog.open(CandidateShareFolderlDialog, {
      width: '900px',
      data: { folderName: folderName, modalType: modalType },
      autoFocus: false,
      disableClose: true 
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        Swal.fire({
          title: "Removed",
          text: "User(s) remove successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadFolderList();
      }
    })
  }

  //open modal to add candidate 
  public openModalToAddCandidates(folderName: any) {
    let dialogRef = this.dialog.open(AddCandidateFolderlDialog, {
      width: '900px',
      data: { folderName: folderName },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        Swal.fire({
          title: "Added",
          text: "Candidate added to folder successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadFolderList();
      }
    })
  }

}
