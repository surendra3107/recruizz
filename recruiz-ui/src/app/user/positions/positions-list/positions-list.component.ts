import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';

//services
import { UtilService } from './../../utilService/util.service';
import { PositionService } from './../positionService/position.service';
import { DropdownService } from './../../dropdownService/dropdown.service';
import { SearchService } from './../../searchService/search.service';
import { PipelineService } from './../../pipeline/pipelineService/pipeline.service';
@Component({
  selector: 'app-positions-list',
  templateUrl: './positions-list.component.html',
  styleUrls: ['./positions-list.component.css']
})
export class PositionsListComponent implements OnInit {

  globalData: any;
  sortDropdown: Array<any> = [];
  currentPage: string;
  sortBy: string = 'modificationDate|desc';
  currentTab: string;
  error: any;
  goToPage: any;

  positionList: any;
  pageSize: any;
  totalElements: any;
  pageNumberTo: any;
  firstView: any;
  totalNumberOfElements: any;
  onPage: any = 1;
  totalPageCount: any;

  allStatusList: any;

  isFilterOpen: boolean = false;
  isStatusDivOpen = true;
  statusDivIcon: string = 'fa-minus';
  currentDivIcon: string = 'fa-minus';
  isCurrentLocationDivOpen: boolean = true;
  clientNameDivIcon: string = 'fa-minus';
  isClientNameDivOpen: boolean = true;
  isLocationSearch: boolean = false;
  isNameSearch: boolean = false;

  pageTitle: string = 'POSITIONS';
  mainBreacrumb: string = 'Position';
  mainRoute: string = '/user/positions-list';
  finalBreadcrumb: string = 'Recruiz-Test';
  isBreadcrumbShow: boolean = false;
  filterObject = {
    statusList: [],
    typeList: [],
    closeByDate: [],
    skills: [],
    nameList: [],
    locationList: []
  };

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private utilService: UtilService,
    private position: PositionService,
    private dropdown: DropdownService,
    private searchFilter: SearchService,
    private _board: PipelineService
  ) { }

  ngOnInit(): void {
    //local storage data
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));

    //read url params query
    this.activatedRoute.queryParams.subscribe(params => {
      this.currentPage = params.page;
      this.sortBy = params.sort;
      this.currentTab = params.tab;
    });
    // Providing sorting option in dropdown list
    this.sortDropdown = [{
      key: 'Name A-Z',
      value: 'title|asc'
    },
    {
      key: 'Name Z-A',
      value: 'title|desc'
    },
    {
      key: 'Modification Date',
      value: 'modificationDate|desc'
    },
    {
      key: 'Close by Date',
      value: 'closeByDate|desc'
    }];

    // load all position function
    this.loadPositions();
    this.loadStatusList();
  }

  //load all position
  public loadPositions() {
    // this will check if current tab is position/position request else set as deafult
    if (this.currentTab !== 'position' && this.currentTab !== 'positionRequest' && this.currentTab !== 'prospectPositionRequest') {
      let currentUrl = this.router.url;
      let urls = currentUrl.split('?')[0];
      this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
        this.router.navigate([urls], { queryParams: { page: '1', sort: 'modificationDate|desc', tab: 'position' } })
      );
      return;
    }

    var verifySortKey = this.utilService.findSortKeyIndex(this.sortDropdown, this.sortBy);
    if (verifySortKey === -1) {
      // if sortKey does not matches should take deafult sort key by 'index = 2' and pageNo=1 and redirected
      let currentUrl = this.router.url;
      let urls = currentUrl.split('?')[0];
      this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
        this.router.navigate([urls], { queryParams: { page: '1', sort: 'modificationDate|desc', tab: 'position' } })
      );
      return;
    }

    //get pageable object
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);

    //api call
    this.position.getAllPositions(pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder).pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.positionList = response.data.content;
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
  };

  //sort by function
  public onSortByChange(sortItem: string) {
    this.sortBy = sortItem;
    let currentUrl = this.router.url;
    let urls = currentUrl.split('?')[0];
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([urls], { queryParams: { page: this.currentPage, sort: sortItem, tab: this.currentTab } })
    );
  }

  //call function on page change
  public getPositionListOnPageChange(event: any) {
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
      this.router.navigate([urls], { queryParams: { page: this.onPage, sort: this.sortBy, tab: this.currentTab } })
    );
  }

  // get all status list
  public loadStatusList() {
    this.dropdown.getCommanStatusList().pipe(first()).subscribe(data => {
      if (data.success === true) {
        this.allStatusList = data.data;
      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
  }

  //delete position
  public deletePosition(position: any, index: any, data: any) {
    //check for permission
    var found = false;
    var permissionArray = ['Delete Position', 'Super Admin', 'Global Delete'];
    for (var i = 0; i < permissionArray.length; i++) {
      if (this.globalData.permissions.indexOf(permissionArray[i]) > -1) {
        found = true;
        break;
      }
    }
    if (found) {
      Swal.fire({
        title: "Alert on delete",
        text: "Are you sure you want to delete this position ?",
        type: 'warning',
        confirmButtonText: 'Yes',
        showConfirmButton: true,
        showCancelButton: true,
        allowOutsideClick: false,
        reverseButtons: true
      }).then((result) => {
        if (result.value) {
          this.position.deletePositionList(position.id).pipe(first()).subscribe(response => {
            if (response.success === true) {
              Swal.fire({
                title: "Deleted",
                text: "Position has been successfully deleted.",
                type: "success",
                timer: 2000,
                showConfirmButton: false
              });
              data.splice(index, 1);
              if (data.length === 0) {
                let currentUrl = this.router.url;
                let urls = currentUrl.split('?')[0];
                this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
                  this.router.navigate([urls], { queryParams: { page: this.onPage, sort: this.sortBy, tab: this.currentTab } })
                );
              }
            }
          },
            error => {
              console.log('Error : ' + JSON.stringify(error));
              this.error = error;
            })
        }
      })

    } else {
      Swal.fire({
        title: "Permission denied. Request your admin to grant permission to delete the positions.",
        type: "warning",
        showConfirmButton: true
      });
    }
  }

  //change status
  public changeStatus(status: any, id: any, index: any, clientStatus: any) {
    if (clientStatus === "OnHold") {
      Swal.fire({
        title: "Not allowed...",
        text: "Client's status is 'On Hold'",
        type: 'warning',
        showConfirmButton: true,
        showCancelButton: false,
        allowOutsideClick: false
      })
    } else if (clientStatus === "StopSourcing") {
      Swal.fire({
        title: "Not allowed...",
        text: "Client's status is 'Stop Sourcing'",
        type: 'warning',
        showConfirmButton: true,
        showCancelButton: false,
        allowOutsideClick: false
      })
    } else {
      this.position.changePosStatus(status, id).pipe(first()).subscribe(response => {
        if (response.success === true) {
          this.positionList[index] = response.data;
          Swal.fire({
            title: "Status Changed",
            text: "Status has been successfully updated",
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
        }
      },
        error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
    }
  }

  //publish position
  public publishPosition(isPublish: boolean, id: any, index: any, position: any) {
    if (position.clientStatus === "OnHold") {
      Swal.fire({
        title: "Not allowed...",
        text: "Client's status is 'On Hold'",
        type: 'warning',
        showConfirmButton: true,
        showCancelButton: false,
        allowOutsideClick: false
      })
    } else if (position.clientStatus === "StopSourcing") {
      Swal.fire({
        title: "Not allowed...",
        text: "Client's status is 'Stop Sourcing'",
        type: 'warning',
        showConfirmButton: true,
        showCancelButton: false,
        allowOutsideClick: false
      })
    } else {
      this.position.publishingPosition(isPublish, id).pipe(first()).subscribe(response => {
        if (isPublish) {
          var pubInfo = 'published';
        } else {
          var pubInfo = 'unpublished';
        }
        if (response.success === true) {
          this.positionList[index] = response.data;
          Swal.fire({
            title: "Position " + pubInfo,
            text: "Position is successfully " + pubInfo,
            type: "success",
            timer: 2000,
            showConfirmButton: false
          });
        }
      },
        error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
    }
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


  search(event: any, type: string) {
    let value = event.target.value;
    if (value == '') {
      this.isNameSearch = false;
      this.isLocationSearch = false;
    } else {
      if (type == 'location') this.isLocationSearch = true;
      else this.isNameSearch = true;
    }
  }

  //go to details page
  public goToUrl(positionId: any, url: any) {
    window.open(url + '?pId=' + positionId, '_blank');
  }

  //status filters
  public changeStatusPosition(stastusId: any, status: boolean) {
    // getting pageable object
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);
    if (this.filterObject.hasOwnProperty('statusList')) {
      status === true ? this.filterObject.statusList.push(stastusId) :
        this.filterObject.statusList.splice(this.filterObject.statusList.indexOf(stastusId), 1);
    } else {
      this.filterObject.statusList = [stastusId];
    }

    //api function
    this.universalFuction(this.filterObject, undefined, pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder);
  }

  //remove status list
  public removeStatusPosition(value: any, arrayList: any, index: any) {
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);
    arrayList[index].checkStatus = false;
    this.filterObject.statusList.splice(this.filterObject.statusList.indexOf(value), 1);
    //api function
    this.universalFuction(this.filterObject, undefined, pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder)
  }

  //calling api to apply filters
  public universalFuction(filterObject: any, clientName: any, pageNo: any, sortField: any, sortOrder: any) {
    //check if filters object is empty call the main api
    if (filterObject.statusList === undefined) {
      filterObject.statusList = [];
    }
    if (filterObject.typeList === undefined) {
      filterObject.typeList = [];
    }
    if (filterObject.closeByDate === undefined) {
      filterObject.closeByDate = [];
    }
    if (filterObject.skills === undefined) {
      filterObject.skills = [];
    }
    if (filterObject.nameList === undefined) {
      filterObject.nameList = [];
    }
    if (filterObject.locationList === undefined) {
      filterObject.locationList = [];
    }
    let arrs = [filterObject.statusList, filterObject.typeList, filterObject.closeByDate, filterObject.skills, filterObject.nameList, filterObject.locationList];
    var resettingFilterList = arrs.reduce((a, b) => [...a, ...b], []);

    if (resettingFilterList.length === 0) {
      this.loadPositions();
    } else {
      this.searchFilter.getPositionSearchResults(filterObject, clientName, pageNo, sortField, sortOrder).pipe(first()).subscribe(response => {
        if (response.success === true) {
          this.positionList = response.data.content;
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
      },
        error => {
          console.log('Error : ' + JSON.stringify(error));
          this.error = error;
        })
    }
  }

  //go to pipeline
  public moveToBoard(path:any, positionCode: any) {
    var found = false;
    var permissionArray = ['View Edit Board'];
    for (var i = 0; i < permissionArray.length; i++) {
      if (this.globalData.permissions.indexOf(permissionArray[i]) > -1) {
        found = true;
        break;
      }
    }
    if (found) {
      this._board.checkPipelinePermission(positionCode).pipe(first()).subscribe(response => {
        if (response.success) {
          this.router.navigate([path], { queryParams: { pId: positionCode } });
        }
      })
    }
  }

}
