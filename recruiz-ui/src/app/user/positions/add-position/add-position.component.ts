import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import { first } from 'rxjs/operators';
import { ClientService } from '../../clients/clientService/client.service';

@Component({
  selector: 'app-add-position',
  templateUrl: './add-position.component.html',
  styleUrls: ['./add-position.component.css']
})
export class AddPositionComponent implements OnInit {
  separatorKeysCodes: number[] = [ENTER, COMMA];
  public Editor = ClassicEditor;

  constructor(
    private _formBuilder: UntypedFormBuilder,
    private _client: ClientService
  ) { }

  @ViewChild('locationInput') locationInput: ElementRef<HTMLInputElement>;
  @ViewChild('skillInput') skillInput: ElementRef<HTMLInputElement>;
  @ViewChild('goodSkillInput') goodSkillInput: ElementRef<HTMLInputElement>;
  @ViewChild('qualificationInput') qualificationInput: ElementRef<HTMLInputElement>;

  firstFormGroup: UntypedFormGroup;
  secondFormGroup: UntypedFormGroup;
  options: any;
  globalData: any;
  translateName: any;
  translateNameSingle: any;
  ClientList: any = [];
  formatted_address: any;
  locations: any = [];
  skills: any = [];
  goodSkills: any = [];
  isMinMAxValidate: boolean = false;
  qualificationList: any = [];

  ngOnInit(): void {
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
      positionName: ['', Validators.required],
      requisitionID: [''],
      closeByDate: ['', Validators.required],
      totalPosition: ['1', Validators.required],
      location: ['', Validators.required],
      bodyData: ['', Validators.required],
      skill: ['', Validators.required],
      goodSkill: [''],
      minExperience: ['', Validators.required],
      maxExperience: ['', Validators.required],
      qulification: ['', Validators.required]
    });
    this.LoadClientList();
  }

  // get client list
  public LoadClientList() {
    this._client.getClientList().pipe(first()).subscribe(response => {
      if (response.success) {
        this.ClientList = response.data;
      }
    })
  }

  // fetching the location entered by user
  public handleAddressChange(address: any) {
    this.formatted_address = address.formatted_address
    const value = (this.formatted_address || '').trim();
    // Add our fruit
    if (value) {
      this.locations.push(value);
    }
    // Clear the input value
    //address?.chipInput!.clear();
    this.firstFormGroup.patchValue({ location: null })
  }

  // remove location
  public remove(place: any) {
    const index = this.locations.indexOf(place);
    if (index >= 0) {
      this.locations.splice(index, 1);
    }
  }

  public handleSkillChange(event: any) {
    const value = this.firstFormGroup.get('skill').value;
    // Add our fruit
    if (value) {
      this.skills.push(value);
    }
    // Clear the input value
    //address?.chipInput!.clear();
    this.firstFormGroup.patchValue({ skill: null })
  }

  // remove skill
  public removeSkill(place: any) {
    const index = this.skills.indexOf(place);
    if (index >= 0) {
      this.skills.splice(index, 1);
    }
  }

  public handleGoodSkillChange(event: any) {
    const value = this.firstFormGroup.get('goodSkill').value;
    // Add our fruit
    if (value) {
      this.goodSkills.push(value);
    }
    // Clear the input value
    //address?.chipInput!.clear();
    this.firstFormGroup.patchValue({ goodSkill: null })
  }

  // remove GoodSkill
  public removeGoodSkill(place: any) {
    const index = this.goodSkills.indexOf(place);
    if (index >= 0) {
      this.goodSkills.splice(index, 1);
    }
  }

  // check min max range
  public checkExpRange() {
    if (Number(this.firstFormGroup.get('minExperience').value) > Number(this.firstFormGroup.get('maxExperience').value)) {
      this.isMinMAxValidate = true;
    } else {
      this.isMinMAxValidate = false;
    }
  }

  // add qualification
  public handleEducationQualification(event: any) {
    const value = this.firstFormGroup.get('qulification').value;
    // Add our fruit
    if (value) {
      this.qualificationList.push(value);
    }
    // Clear the input value
    //address?.chipInput!.clear();
    this.firstFormGroup.patchValue({ qulification: null })
  }

  // remove qalification
  public removeQualification(education: any) {
    const index = this.qualificationList.indexOf(education);
    if (index >= 0) {
      this.qualificationList.splice(index, 1);
    }
  }
}
