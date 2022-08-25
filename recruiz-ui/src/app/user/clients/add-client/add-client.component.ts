import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { Router, ActivatedRoute } from '@angular/router';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';

import { MatDialog } from '@angular/material/dialog';
import { ClientDecisionMakerlDialog } from '../modals/decision-maker/decision-maker.component';
import { ClientInterviwerPannelDialog } from '../modals/client-interviewer/client-interviwer-pannel.component';
//services
import { ClientService } from '../clientService/client.service';

@Component({
  selector: 'app-add-client',
  templateUrl: './add-client.component.html',
  styleUrls: ['./add-client.component.css', '../client-detail/client-detail.component.css']
})
export class AddClientComponent implements OnInit {
  public Editor = ClassicEditor;

 
  firstFormGroup: UntypedFormGroup;
  secondFormGroup: UntypedFormGroup;
  options: any;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private _formBuilder: UntypedFormBuilder,
    private _client: ClientService,
    public dialog: MatDialog
  ) { }
  error: any = '';
  globalData: any;
  translateName: any;
  translateNameSingle: any;
  clientName: any;
  isClientExixts: boolean;
  formatted_address: string;
  descisionMakerList: Array<any> = [];
  interviwerList: Array<any> = [];

  ngOnInit() {
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
    if (this.globalData.orgType === "Corporate") {
      this.translateName = 'Departments';
      this.translateNameSingle = 'Department';
    } else {
      this.translateName = 'Clients';
      this.translateNameSingle = 'Client';
    }

    this.firstFormGroup = this._formBuilder.group({
      clientName: ['', Validators.required],
      location: ['', Validators.required],
      address: [''],
      website: [''],
      employeeStrength: [''],
      turnover: [''],
      bodyData: ['']
    });

    this.secondFormGroup = this._formBuilder.group({
      secondCtrl: ['']
    });
  }

  //check if client name exits
  public validateClient() {
    this.clientName = this.firstFormGroup.value.clientName;
    this._client.validateClientName(this.clientName).pipe(first()).subscribe(response => {
      if (response.success) {
        if (response.data.exists === 'false') {
          this.isClientExixts = false;
        } else {
          this.isClientExixts = true;
          this.firstFormGroup.controls['clientName'].setErrors({ 'incorrect': true });
        }
      }
    })
  }

  //open modal to add descision makers
  public addDecisionMakerFromAdd(addType: string) {
    let dialogRef = this.dialog.open(ClientDecisionMakerlDialog, {
      width: '900px',
      data: { modalType: addType, descisionMakerList: this.descisionMakerList },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.descisionMakerList = result.concat(this.descisionMakerList);
      }
    })
  }

  // remove descision maker
  public removeDescisionMaker(index: any) {
    this.descisionMakerList.splice(index, 1);
  }

  //open modal to add edit interviewer
  public addInterviewPanelFromAdd(type: any) {
    let dialogRef = this.dialog.open(ClientInterviwerPannelDialog, {
      width: '900px',
      data: { interviwerLists: this.interviwerList, modalType: type },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.interviwerList = result.concat(this.interviwerList);
      }
    })
  }

  // remove descision maker
  public removeInterviewPanel(index: any) {
    this.interviwerList.splice(index, 1);
  }


  // create client
  public createClient() {
    var formData = {
      clientName: this.firstFormGroup.value.clientName,
      address: this.firstFormGroup.value.address,
      clientLocation: this.formatted_address,
      website: this.firstFormGroup.value.website,
      empSize: this.firstFormGroup.value.employeeStrength,
      turnOvr: this.firstFormGroup.value.turnover,
      notes: this.firstFormGroup.value.bodyData,
      clientDecisionMaker: this.descisionMakerList,
      clientInterviewerPanel: this.interviwerList,
      customField: {}
    }

    //call api to add client
    this._client.postClientInfo(formData).pipe(first()).subscribe(response => {
      if (response.success) {
        Swal.fire({
          title: "Created",
          text: "New " + this.translateNameSingle + " created successfully.",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
        var clientId = response.data.id;
        this.router.navigate(['/user/client-details'], { queryParams: { cId: clientId } })
      }
    })
  }

  // fetching the location entered by user
  public handleAddressChange(address: any) {
    this.formatted_address = address.formatted_address
  }

}
