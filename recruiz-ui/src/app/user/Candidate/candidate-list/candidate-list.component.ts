import { Component, OnInit } from '@angular/core';

import { Router, ActivatedRoute } from '@angular/router';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';
import { MatDialog } from '@angular/material/dialog';

//services
import { CandidateResumelDialog } from '../modals/candidate-resume/candidate-resume-dialog.component';
import { UtilService } from './../../utilService/util.service';
import { CandidateService } from './../candidateService/candidate.service';
import { DropdownService } from './../../dropdownService/dropdown.service';
import { SearchService } from './../../searchService/search.service';

@Component({
  selector: 'app-candidate-list',
  templateUrl: './candidate-list.component.html',
  styleUrls: ['./candidate-list.component.css']
})
export class CandidateListComponent implements OnInit {
 
  isFilterOpen: boolean = false;
  isStatusDivOpen = true;
  statusDivIcon: string = 'fa-minus';
  currentDivIcon: string = 'fa-minus';
  isCurrentLocationDivOpen: boolean = true;
  clientNameDivIcon: string = 'fa-minus';
  isClientNameDivOpen: boolean = true;
  isLocationSearch: boolean = false;
  isNameSearch: boolean = false;
  skillLimit: any = 5;
  endLimit: any;
  error: any;

  data: any[] = [];
  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private _utilService: UtilService,
    private _candidate: CandidateService,
    private dropdown: DropdownService,
    private searchFilter: SearchService,
    public dialog: MatDialog
  ) { }

  globalData: any;
  currentPage: any;
  sortBy: any;
  sortDropdown: Array<any> = [];

  candidateList: any;
  pageSize: any;
  totalElements: any;
  pageNumberTo: any;
  firstView: any;
  totalNumberOfElements: any;
  onPage: any = 1;
  totalPageCount: any;

  candidateListPagesSelection: any;
  singleCandidateSelection: any;
  selectedEmails: Array<any> = [];
  selectedCandidateIds: Array<any> = [];
  candidateCardSelected: any;
  candidateSelection: any;
  allStatusList: any;
  activeCandidate: any;
  onHoldCandidate: any;
  deleteInput: any;
  displayFn: any;
  isLoading: any;
  errorMsg: any;
  filteredLocation: any = [];
  filteredClientName: any = [];
  goToPage: any;

  ngOnInit(): void {
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    //read url params query
    this.activatedRoute.queryParams.subscribe(params => {
      this.currentPage = params.page;
      this.sortBy = params.sort;
    });

    // Providing sorting option in dropdown list
    this.sortDropdown = [{
      key: 'Name A-Z',
      value: 'clientName|asc'
    },
    {
      key: 'Name Z-A',
      value: 'clientName|desc'
    },
    {
      key: 'Modification Date',
      value: 'modificationDate|desc'
    }];

    //load candidate function
    this.loadAllCandidates();
    //status drop down
    this.loadCandidateStatus();
  }

  openFilter() {
    this.isFilterOpen = !this.isFilterOpen;
  }

  toggleFilterDiv(type: string) {
    if (type == 'status') {
      this.isStatusDivOpen = !this.isStatusDivOpen;
      if (this.isStatusDivOpen) this.statusDivIcon = 'fa-minus';
      else this.statusDivIcon = 'fa-plus';
    } else if (type == 'location') {
      this.isCurrentLocationDivOpen = !this.isCurrentLocationDivOpen;
      if (this.isCurrentLocationDivOpen) this.currentDivIcon = 'fa-minus';
      else this.currentDivIcon = 'fa-plus';
    } else {
      this.isClientNameDivOpen = !this.isClientNameDivOpen;
      if (this.isClientNameDivOpen) this.clientNameDivIcon = 'fa-minus';
      else this.clientNameDivIcon = 'fa-plus';
    }
  }

  //get all candidate
  public loadAllCandidates() {
    var verifySortKey = this._utilService.findSortKeyIndex(this.sortDropdown, this.sortBy);
    if (verifySortKey === -1) {
      // if sortKey does not matches should take deafult sort key by 'index = 2' and pageNo=1 and redirected
      let currentUrl = this.router.url;
      let urls = currentUrl.split('?')[0];
      this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
        this.router.navigate([urls], { queryParams: { page: '1', sort: 'modificationDate|desc' } })
      );
      return;
    }
    // getting pageable object
    var pageableObject = this._utilService.getPageableObject(this.sortBy, this.currentPage);

    //call api
    this._candidate.getAllCandidates(pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder).pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.candidateList = response.data.content;
        this.pageSize = response.data.size;
        this.totalElements = response.data.totalElements;
        this.totalPageCount = response.data.totalPages;
        this.onPage = this.currentPage;
        //show page no
        this.pageNumberTo = parseInt(response.data.number) + parseInt("1")
        this.firstView = response.data.number + '1';
        if (response.data.numberOfElements === 10) {
          this.totalNumberOfElements = response.data.numberOfElements * this.pageNumberTo;
        } else {
          this.totalNumberOfElements = this.totalElements;
        }
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //pagination
  public getCandidateListOnPageChange(event: any) {
    if (this.totalPageCount < parseInt(event) || parseInt(event) === 0) {
      Swal.fire({
        title: "Not allowed...",
        text: "Page number entered does not exists.",
        type: 'warning',
        showConfirmButton: true,
        showCancelButton: false,
        allowOutsideClick: false
      })
      return true;
    }
    this.onPage = event;
    let currentUrl = this.router.url;
    let urls = currentUrl.split('?')[0];
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([urls], { queryParams: { page: this.onPage, sort: this.sortBy } })
    );
  }

  //get resumes
  public getResume(resumeUlr: any) {
    let dialogRef = this.dialog.open(CandidateResumelDialog, {
      width: '900px',
      data: { linkUrl: resumeUlr },
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result) {
      }
    })
  }

  // check candidate to perform action
  public selectCandidate(candidateData: any, toggle: boolean) {
    this.singleCandidateSelection = toggle;
    this.candidateListPagesSelection = (toggle == true ? false : true);
    this.candidateSelection = (this.candidateListPagesSelection == false ? null : 'candidateList');
    var index = this.selectedEmails.indexOf(candidateData.email);
    if (index >= 0) {
      this.selectedEmails.splice(index, 1);
      this.selectedCandidateIds.splice(index, 1);
    } else {
      this.selectedEmails.push(candidateData.email);
      this.selectedCandidateIds.push(candidateData.cid);
    }
    // one ore more candidate selected bottom slider shows
    this.candidateCardSelected = this.selectedEmails.length > 0 ? true : false;
  }

  //status drop down function
  public loadCandidateStatus() {
    this.dropdown.getCommanStatusList().pipe(first()).subscribe(response => {
      this.allStatusList = response.data;
      this.activeCandidate = this.allStatusList[0].id;
      this.onHoldCandidate = this.allStatusList[1].id;
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //change status
  public changeCandidateStatus(status: any, id: any, index: any, candidateList: any) {
    this._candidate.switchCandidateStatus(status, id).pipe(first()).subscribe(response => {
      if (response.success) {
        this.candidateList[index] = response.data;
        Swal.fire({
          title: "Status Changed",
          text: "Status has been successfully updated",
          type: "success",
          timer: 2000,
          showConfirmButton: false
        });
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //delete candidate
  public deleteCandidateById(candidateId: any, index: any, data: any) {
    this._candidate.checkForCandidateInvoice(candidateId).pipe(first()).subscribe(response => {
      if (response.success) {
        if (response.data === 'invoice_exist_for_candidate') {
          var found = false;
          var permissionArray = ['Delete Candidate', 'Super Admin', 'Global Delete'];
          for (var i = 0; i < permissionArray.length; i++) {
            if (this.globalData.permissions.indexOf(permissionArray[i]) > -1) {
              found = true;
              break;
            }
          }
          if (found) {
            Swal.fire({
              title: "Alert on Delete",
              text: "Candidate invoice has been generated, do you still want to delete.",
              type: "warning",
              showConfirmButton: true,
              showCancelButton: true,
              allowOutsideClick: false
            }).then(result => {
              if (result.value) {
                this._candidate.deleteCandidateWithInvoice(candidateId, true).pipe(first()).subscribe(response => {
                  if (response.success) {
                    Swal.fire({
                      title: "Deleted",
                      text: "Candidate deleted successfully with invoice.",
                      type: "success",
                      timer: 2000,
                      showConfirmButton: false
                    });
                    data.splice(index, 1);
                  }
                }, error => {
                  console.log('Error : ' + JSON.stringify(error));
                  this.error = error;
                })
              }
            });
          }
        }
      }
      if (response.success === false) {
        this.deleteCandidateWithoutInvoiceWithId(candidateId, index, data)
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //delete candidate
  public deleteCandidateWithoutInvoiceWithId(candidateId: any, index: any, data: any) {
    var found = false;
    var permissionArray = ['Delete Candidate', 'Super Admin', 'Global Delete'];
    for (var i = 0; i < permissionArray.length; i++) {
      if (this.globalData.permissions.indexOf(permissionArray[i]) > -1) {
        found = true;
        break;
      }
    }
    if (found) {
      Swal.fire({
        title: "Alert on Delete",
        text: "Are you sure you want to delete this candidate.",
        type: "warning",
        showConfirmButton: true,
        showCancelButton: true,
        allowOutsideClick: false
      }).then(result => {
        if (result.value) {
          this._candidate.deleteCandidate(candidateId).pipe(first()).subscribe(response => {
            if (response.success) {
              Swal.fire({
                title: "Deleted",
                text: "Candidate deleted successfully.",
                type: "success",
                timer: 2000,
                showConfirmButton: false
              });
              data.splice(index, 1);
            }
          })
        }
      }, error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
    }
  }

  public onSortByChange(sortName: any) {

  }

  public clientStatusFilter(statusId: any, status: any) {

  }

  public onInputChanged(event: any) {

  }

  public selectedLocation(event: any) {

  }

  public onInputClientName(event: any) {

  }

  public selectedClientName(event: any) {

  }

  public checkPermission(type: any, key: any) {

  }
}
