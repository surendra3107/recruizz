import { Component, OnInit } from '@angular/core';
import { RecruizHelperService } from './../../toggle_service/recruiz-helper.service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnInit {

  isSidebarToggle:string='true';

  constructor(private _recruizHelperService : RecruizHelperService) { }

  ngOnInit() {
    this._recruizHelperService.sidebarToggleEvent.subscribe((response)=>{
      this.isSidebarToggle=response.toString();
    });
  }

}
