import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';

//services
import { OnBoardService } from "../onBoardService/on-board.service";

@Component({
  selector: 'app-my-dialog',
  templateUrl: './add-new-category.component.html',
  styleUrls: ['../onboarding/onboarding.component.css']
})
export class OnboardSubcategoryDialog implements OnInit {
  public Editor = ClassicEditor;
  constructor(
    public thisDialogRef: MatDialogRef<OnboardSubcategoryDialog>, @Inject(MAT_DIALOG_DATA)
    public dataOption: any,
    private _onboard: OnBoardService,
  ) { }

  error = '';
  globalData: any;
  bodyData: any = '';
  category: string;
  allCategory: any;
  allSubCategory: any;
  subCategoryList: any;
  subCategory: string;
  manualSubCat: string;
  titleTask: string;
  createTaskData: any = [];
  template: string;
  taskId: any;
  modalTitle: string;
  btnText: string;

  ngOnInit() {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
    this.loadSubCategory();
    this.template = this.dataOption.template;
    this.modalTitle = 'Add New Onboarding Steps';
    this.btnText = 'Save Task;'
    //on edit
    if (this.dataOption.taskData) {
      this.taskId = this.dataOption.taskData.id;
      this.bodyData = this.dataOption.taskData.description;
      this.titleTask = this.dataOption.taskData.title;
      this.category = this.dataOption.taskData.onboardCategory;
      this.subCategory = this.dataOption.taskData.subCategoryName;
      this.modalTitle = 'Update Onboarding Steps';
      this.btnText = 'Update Task';
    }
  }

  //get list of category
  public loadCategory() {
    this._onboard.getCategory().pipe(first()).subscribe((response: any) => {
      this.allCategory = response.data;
      if (this.taskId) {
        this.categoryList(this.category);
      } else {
        this.categoryList(this.allCategory[0].id);
      }

    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //change category
  public categoryList(categoryItem: any) {
    this.subCategoryList = [];
    Object.keys(this.allSubCategory).forEach(key => {
      if (categoryItem === key) {
        this.allSubCategory[key].forEach((val: any) => {
          this.subCategoryList.push(val.subCategoryName)
        })
      }
    })
  }

  //get list of sub category
  public loadSubCategory() {
    this._onboard.getSubCategory().pipe(first()).subscribe((response: any) => {
      this.allSubCategory = response.data;
      this.loadCategory();
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //add task
  public addSubCategory() {
    if (this.subCategory === 'Create new sub category') {
      var subCat = this.manualSubCat;
    } else {
      var subCat = this.subCategory;
    }

    //add task fom data
    if (this.taskId) {
      var taskData: any
      taskData = {
        'subCategoryName': subCat,
        'onboardCategory': this.category,
        'title': this.titleTask,
        'description': this.bodyData,
        'id': this.taskId
      }
    } else {
      taskData = {
        'subCategoryName': subCat,
        'onboardCategory': this.category,
        'title': this.titleTask,
        'description': this.bodyData
      }
    }

    this.createTaskData.push(taskData);
    this._onboard.postTask(this.createTaskData, this.template).pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.thisDialogRef.close(true);
      }
    })

  }

  //update notes
  public updateNotesToPosition() {

  }

  onCloseConfirm() {
    this.thisDialogRef.close();
  }

  onCloseCancel() {
    this.thisDialogRef.close();
  }

}
