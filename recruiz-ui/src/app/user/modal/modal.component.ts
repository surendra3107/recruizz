import { Component, OnInit, ElementRef, Input, ViewEncapsulation, OnDestroy } from '@angular/core';
import { ModalService } from 'src/app/modal-service/modal.service';

@Component({
  selector: 'jw-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ModalComponent implements OnInit, OnDestroy {

  @Input() id:string;
  private element:any;

  constructor(private el:ElementRef,private _modalService:ModalService) {
    this.element= el.nativeElement;
  }

  ngOnInit() {
    if(!this.id) return;
    document.body.appendChild(this.element);
    this.element.addEventListener('click',el =>{
      if(el.target.className==='jw-modal') this.close();
    });
    this._modalService.add(this);
  }

  open():void{
    this.element.style.display='block';
    document.body.classList.add('jw-modal-open');
  }

  close():void{
    this.element.style.display='none';
    document.body.classList.remove('jw-modal-open');
  }

  ngOnDestroy():void{
    this._modalService.remove(this.id);
    this.element.remove();
  }


}