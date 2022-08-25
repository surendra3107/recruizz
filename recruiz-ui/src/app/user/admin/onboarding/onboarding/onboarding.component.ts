import { Component, OnInit } from '@angular/core';

import { Router, ActivatedRoute } from '@angular/router';
import { first } from "rxjs/operators";
import Swal from 'sweetalert2';
import { MatDialog } from '@angular/material/dialog';
import { OnboardSubcategoryDialog } from '../onboard-modals/add-new-category.component';

//sevices
import { OnBoardService } from '../onBoardService/on-board.service';
@Component({
  selector: 'app-onboarding',
  templateUrl: './onboarding.component.html',
  styleUrls: ['./onboarding.component.css']
})
export class OnboardingComponent implements OnInit {

  constructor(
    private router: Router,
    public dialog: MatDialog,
    private _boarding: OnBoardService
  ) { }
  error: any = '';
  globalData: any;
  allSubCategory: any;
  subCategory: any;
  templateList: any = [];
  templateName: any = 'master';
  allTemplate: any;
  showActivity: boolean = true;
  openSubCategory: boolean;
  taskId: any = [];

  ngOnInit() {
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
   // this.loadMasterActivityList();
    this.loadTemplateList();
    this.loadSubCategory();
  }

  //get activity
  public loadMasterActivityList() {
    this._boarding.getMasterActivityList().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.allSubCategory = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

 //get sub category
  public loadSubCategory() {
    this._boarding.getSubCategory().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.subCategory = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //load template list
  public loadTemplateList() {
    this._boarding.getTemplateList().pipe(first()).subscribe((response: any) => {
      if (response.success) {
        this.allTemplate = response.data;
        if (JSON.stringify(this.allTemplate) !== '{}') {
          Object.keys(this.allTemplate).forEach(key => {
            this.templateList.push(key);
          });
        }
        if (this.templateName) {
          this.switchTemplate(this.templateName);
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //change template
  public switchTemplate(template: any) {
    this.allSubCategory = {};
    if (template === 'master') {
      this.loadMasterActivityList();
      this.showActivity = true;
    } else {
      Object.keys(this.allTemplate).forEach(key => {
        if (template === key) {
          this.allSubCategory = this.allTemplate[key];
        }
      });
      if (Object.keys(this.allSubCategory).length > 0) {
        this.showActivity = true;
      } else {
        this.showActivity = false;
      }
    }
  }

  //open modal add new category
  public addSubCategory(taskData: any) {
    let dialogRef = this.dialog.open(OnboardSubcategoryDialog, {
      width: '900px',
      data: { taskData: taskData, template: this.templateName },
      autoFocus: false,
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (result) {
          Swal.fire({
            title: "Success",
            text: "Task added/updated successfully.",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });

          if (this.templateName == "" || this.templateName == 'master') {
            this.switchTemplate(this.templateName);
          } else {
            this.loadTemplateList();
          }
        }
      }
    })
  }

  //delete category
  public deleteSubCategory(itemKey: any, subItemKey: any) {
    var categoryId: any
    this.subCategory[itemKey].forEach((item: any) => {
      if (subItemKey === item.subCategoryName) {
        categoryId = item.id;
      }
    })

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
        this._boarding.deleteSubCategories(categoryId).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: 'Category has been deleted',
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            if (this.templateName == "" || this.templateName == 'master') {
              this.switchTemplate(this.templateName);
            } else {
              this.loadTemplateList();
            }
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //delete task
  public deleteTask(task: any) {
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
        this._boarding.deleteTask(task.id).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: 'Task has been deleted',
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            if (this.templateName == "" || this.templateName == 'master') {
              this.switchTemplate(this.templateName);
            } else {
              this.loadTemplateList();
            }
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //delete template
  public deleteTemplate(template: any) {
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
        this._boarding.deleteCurrentTemplate(template).pipe(first()).subscribe((response: any) => {
          if (response.success) {
            Swal.fire({
              title: "Deleted",
              text: 'Task has been deleted',
              type: "success",
              timer: 2000,
              showConfirmButton: false
            });
            let currentUrl = this.router.url;
            this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
              this.router.navigate([currentUrl]));
          }
        }, error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
      }
    })
  }

  //show hide header
  public ShowHeader(subItem: any) {
    subItem.show = subItem.show ? false : true;
    if (subItem.show === true) {
      subItem.arrowClass = 'keyboard_arrow_down';
    } else {
      subItem.arrowClass = 'keyboard_arrow_right';
    }
  }

  //check all category
  public checkAllCategory(data: any) {
    this.taskId = [];
    if (data.headerChecked) {
      data.value.forEach((item: any) => {
        item.subItemChecked = true;
        this.taskId.push(item.id);
      })
    } else {
      data.value.forEach((item: any) => {
        item.subItemChecked = false;
        this.taskId.splice(this.taskId.indexOf(item.id), 1);
      })
    }
  }

  //check single category
  public checkindividual(taskData: any, subItem: any) {
    if (subItem.headerChecked) {
      subItem.headerChecked = false;
    }
    if (taskData.subItemChecked) {
      this.taskId.push(taskData.id);
    } else {
      this.taskId.splice(this.taskId.indexOf(taskData.id), 1);
    }
  }

  //create template
  public createTemplate() {
    if (this.taskId.length === 0) {
      Swal.fire({
        title: "Warning",
        text: 'Please select a category.',
        type: "warning",
        showConfirmButton: true
      });
      return true;
    }

    Swal.fire({
      title: "Template Name",
      input: 'text',
      inputAttributes: {
        autocapitalize: 'off'
      },
      showCancelButton: true,
      reverseButtons: true,
      confirmButtonText: 'Procees',
      showLoaderOnConfirm: true,
      preConfirm: (text) => {
        if (text === '') {
          Swal.showValidationMessage("Please add template name.");
          return false
        } else {
          this._boarding.createTemplates(text, this.taskId).pipe(first()).subscribe((response: any) => {
            if (response.success) {
              Swal.fire({
                title: "Template Created",
                text: "Template created successfully",
                type: "success",
                timer: 2000,
                showConfirmButton: false
              });
              this.taskId = [];
              let currentUrl = this.router.url;
              this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
                this.router.navigate([currentUrl]));
            }
          }, error => {
            console.log('Error : ' + JSON.stringify(error));
            this.error = error;
          })
        }
      }
    })

  }

  //clear selection
  public clearSelection() {
    let currentUrl = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([currentUrl]));
  }

}
