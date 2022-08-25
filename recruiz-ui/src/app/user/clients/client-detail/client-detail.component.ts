import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { environment } from '../../../../environments/environment';

import { MatDialog } from '@angular/material/dialog';
import { ClientDecisionMakerlDialog } from '../modals/decision-maker/decision-maker.component';
import { ClientInterviwerPannelDialog } from '../modals/client-interviewer/client-interviwer-pannel.component';
import { ClientNoteslDialog } from './../modals/client-notes/client-notes.component';
import { ClientRateslDialog } from './../modals/client-rate/client-rate.component';
//services
import { ClientService } from './../clientService/client.service';

@Component({
  selector: 'app-client-detail',
  templateUrl: './client-detail.component.html',
  styleUrls: ['./client-detail.component.css']
})
export class ClientDetailComponent implements OnInit {
  baseRoot: any = environment.baseUrl;

  isActive: boolean = false;
  selectedTab: string = 'decisionMaker';

  mainBreacrumb: string = 'Clients';
  mainRoute: string = '/user/client-list';
  finalBreadcrumb: string;
  isBreadcrumbShow: boolean = true;
  error: any = '';
  globalData: any;
  clientId: any;
  isFilterOpen: any;
  pageTitle: any;

  client: any;
  totalPosition: any;
  clientName: any;
  owner: any;
  isDocsEmpty: any;

  allActivity: any;
  nextActivities: any;
  numberOfElements: any;
  totalElements: any;
  totalPages: any;
  currentActivityPage: number = 0;
  allNotes: any;

  allRates: any;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private clientData: ClientService,
    public dialog: MatDialog
  ) { }

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    //read url params query
    this.activatedRoute.queryParams.subscribe(params => {
      this.clientId = params.cId;
    });

    this.getClientDetails();
  }

  //get details of perticular client
  public getClientDetails() {
    if (this.clientId) {
      this.clientData.getClientDetailsById(this.clientId).pipe(first()).subscribe(response => {
        if (response.success) {
          this.client = response.data.client;
          this.totalPosition = response.data.totalPosition;
          this.clientName = response.data.client.clientName;
          this.owner = response.data.client.owner;
        }
      })
    }
  }

  //open modal to add edit descision makers
  public addDecisionMakerFromDetails(decisionData: any, clientName: any, type: any, clientId: any) {
    let dialogRef = this.dialog.open(ClientDecisionMakerlDialog, {
      width: '900px',
      data: { descisionMakerList: decisionData, clientName: clientName, modalType: type, clientId: clientId },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'decision-maker-added') {
        Swal.fire({
          title: "Added",
          text: "Decision maker added successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.getClientDetails();
      } else if (result === 'decision-maker-updated') {
        Swal.fire({
          title: "Updated",
          text: "Decision maker updated successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.getClientDetails();
      }
    })
  }

  //open modal to update decision maker
  public updateDescisionMaker(decisionData: any, clientName: any, type: any) {
    this.addDecisionMakerFromDetails(decisionData, clientName, type, undefined);
  }

  //delete decision maker
  public deleteDecisionMaker(clientId: any, decisionId: any) {
    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete ?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this.clientData.deleteDesicionMaker(clientId, decisionId).pipe(first()).subscribe(response => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Decision maker deleted successfully.",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.getClientDetails();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //open modal to add edit interviewer
  public addClientInterviewerDetails(interviwerList: any, clientName: any, type: any, clientId: any) {
    let dialogRef = this.dialog.open(ClientInterviwerPannelDialog, {
      width: '900px',
      data: { interviwerLists: interviwerList, clientName: clientName, modalType: type, clientId: clientId },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'interviwer-added') {
        Swal.fire({
          title: "Added",
          text: "Interviewer added successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.getClientDetails();
      } else if (result === 'interviwer-updated') {
        Swal.fire({
          title: "Updated",
          text: "Interviewer updated successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.getClientDetails();
      }
    })
  }

  //update interviewer
  public updateClientInterviwer(intData: any, clientName: any, type: any) {
    this.addClientInterviewerDetails(intData, clientName, type, undefined);
  }

  //delete interviewer
  public deleteInterviewerFromClient(clientId: any, intId: any) {
    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete ?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then((result) => {
      if (result.value) {
        this.clientData.deleteClientInterviewer(clientId, intId).pipe(first()).subscribe(response => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Interviewer deleted successfully.",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.getClientDetails();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //switch between tabs clients
  public tabClick(type: any) {
    if (type.tab.textLabel === "ACTIVITY") {
      this.loadClientActivities();
    } else if (type.tab.textLabel === "NOTES") {
      this.loadClientNotes();
    } else if (type.tab.textLabel === "RATES") {
      this.loadClientRates();
    }
  }

  //load client activity
  public loadClientActivities() {
    this.clientData.getClientActivity(this.clientId, this.currentActivityPage).pipe(first()).subscribe(response => {
      this.allActivity = [];
      if (response.success) {
        this.allActivity = this.manipulateActivityForClientAndPositionLink(response.data.content);
        this.numberOfElements = response.data.numberOfElements;
        this.totalElements = response.data.totalElements;
        this.totalPages = response.data.totalPages;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //get next count activity
  public getNextActivity() {
    this.currentActivityPage = this.currentActivityPage + 1;
    this.clientData.getClientActivity(this.clientId, this.currentActivityPage).pipe(first()).subscribe(response => {
      this.nextActivities = response.data.content;
      this.numberOfElements = response.data.numberOfElements;
      this.totalElements = response.data.totalElements;
      this.totalPages = response.data.totalPages;
      this.manipulateActivityForClientAndPositionLink(this.allActivity.concat(this.nextActivities));
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }
  //find # and $ and replace with link
  public manipulateActivityForClientAndPositionLink(responseData: any) {
    responseData.forEach((activity: any) => {
      var indexClient = activity.message.indexOf("$@");
      var indexPosition = activity.message.indexOf("$#");
      var clientString = activity.message.substring(indexClient, indexPosition);

      var sepraterIndex = clientString.indexOf("_");
      var clientId = clientString.substring(2, sepraterIndex);
      // var clientId = clientId.replace(clientId,"$@");
      var clientName = clientString.substring(sepraterIndex + 1, clientString.length);
      var link = this.baseRoot + "user/client-details?cid=" + clientId;
      link = "<a href=" + link + " class='link-cursor' target='_blank'>" + clientName + "</a> , ";

      var positionIndexLast = this.nth_occurrence(activity.message, "$#", 2);
      var positionString = activity.message.substring(indexPosition, positionIndexLast);
      sepraterIndex = positionString.indexOf("_");
      var positionId = positionString.substring(2, sepraterIndex);
      var positionName = positionString.substring(sepraterIndex + 1, positionString.length);

      var positionLink = this.baseRoot + "user/position-details?pid=" + positionId;
      positionLink = "<a href=" + positionLink + " class='link-cursor' target='_blank'>" + positionName + "</a> ";
      if (indexClient > 0 && indexPosition > 0) {
        activity.message = activity.message.replace(clientString, link);
        activity.message = activity.message.replace(positionString, positionLink);
        activity.message = activity.message.replace("$#", "");
        activity.message = activity.message.replace("$@", "");
      }
      this.allActivity.push(activity)
    })
    return this.allActivity;
  }

  public nth_occurrence(string: any, char: any, nth: any) {
    var first_index = string.indexOf(char);
    var length_up_to_first_index = first_index + 1;
    if (nth == 1) {
      return first_index;
    } else {
      var string_after_first_occurrence = string.slice(length_up_to_first_index);
      var next_occurrence = this.nth_occurrence(string_after_first_occurrence, char, nth - 1);
      if (next_occurrence === -1) {
        return -1;
      } else {
        return length_up_to_first_index + next_occurrence;
      }
    }
  }

  //get all notes for clients
  public loadClientNotes() {
    this.currentActivityPage = 0;
    this.clientData.getClientNotes(this.clientId, this.currentActivityPage).pipe(first()).subscribe(response => {
      if (response.success) {
        this.allNotes = response.data.content;
        this.numberOfElements = response.data.numberOfElements;
        this.totalElements = response.data.totalElements;
        this.totalPages = response.data.totalPages;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //open modal to add clients notes
  public openDialogClientsNotes(noteData: any) {
    let dialogRef = this.dialog.open(ClientNoteslDialog, {
      width: '900px',
      data: { clientId: this.clientId, notesData: noteData },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'notes-added') {
        Swal.fire({
          title: "Added",
          text: "Notes added successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadClientNotes();
      } else if (result === 'notes-updated') {
        Swal.fire({
          title: "Updated",
          text: "Notes updated successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadClientNotes();
      }
    })
  }

  //open modal to update notes
  public updateClientNotes(noteData: any) {
    this.openDialogClientsNotes(noteData);
  }

  //delete notes
  public deleteClientNotes(noteId: any) {
    Swal.fire({
      title: "Alert on delete",
      text: "Are you sure you want to delete this note?",
      type: 'warning',
      confirmButtonText: 'Yes',
      showConfirmButton: true,
      showCancelButton: true,
      allowOutsideClick: false,
      reverseButtons: true
    }).then(result => {
      if (result.value) {
        this.clientData.removeNotesFromClient(noteId).pipe(first()).subscribe(response => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: "Notes deleted successfully.",
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            this.loadClientNotes();
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //get rates details
  public loadClientRates() {
    this.clientData.getAllRates(this.clientId).pipe(first()).subscribe(response => {
      if (response.success) {
        this.allRates = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //open modals to add rates
  public openDialogClientsRates(type: any, rateData: any) {
    let dialogRef = this.dialog.open(ClientRateslDialog, {
      width: '600px',
      data: { clientId: this.clientId, modalType: type, rateData: rateData },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'rates-added') {
        Swal.fire({
          title: "Added",
          text: "Rates added successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadClientRates();
      } else if (result === 'rates-updated') {
        Swal.fire({
          title: "Updated",
          text: "Rate updated successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        this.loadClientRates();
      }
    })
  }

  //update rates
  public updateClientRate(modalType: any, rateDat: any) {
    this.openDialogClientsRates(modalType, rateDat);
  }

}