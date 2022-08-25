import { Component, OnInit, HostListener} from '@angular/core';
import { ModalService } from 'src/app/modal-service/modal.service';

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks.component.html',
  styleUrls: ['./tasks.component.css']
})
export class TasksComponent implements OnInit {

  //To Close the modal on escape button Click.
 @HostListener ('document:keydown', ['$event']) 
   onKeyDown(event : KeyboardEvent) {
     let key = event.keyCode;
     if(key === 27){
      this.closeModal('task_1');
     }
 }

  selectedTab:string = 'all';
  taskDataArray:any[] = [];

  constructor(private _modalService:ModalService) { }

  ngOnInit() {
  }

  tabSelected(tabName){
    this.taskDataArray=[];
    this.selectedTab = tabName;
    this.taskDataArray.push('List1');
    this.taskDataArray.push('List2');
    this.taskDataArray.push('List3');
    this.taskDataArray.push('List4');
    this.taskDataArray.push('List5');
    console.log(tabName);
  }

  openModal(id:string){
    this._modalService.open(id);
  }

  closeModal(id:string){
    this._modalService.close(id);
  }
  

}