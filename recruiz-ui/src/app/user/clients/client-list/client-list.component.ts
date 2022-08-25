import { Component, OnInit } from '@angular/core';

import { Router, ActivatedRoute } from '@angular/router';
import { first } from 'rxjs/operators';
import Swal from 'sweetalert2';

//services
import { UtilService } from './../../utilService/util.service';
import { ClientService } from './../clientService/client.service';
import { DropdownService } from './../../dropdownService/dropdown.service';
import { SearchService } from './../../searchService/search.service';

@Component({
  selector: 'app-client-list',
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.css']
})
export class ClientListComponent implements OnInit {

 
  isFilterOpen: boolean = false;
  isStatusDivOpen = true;
  statusDivIcon: string = 'fa-minus';
  currentDivIcon: string = 'fa-minus';
  isCurrentLocationDivOpen: boolean = true;
  clientNameDivIcon: string = 'fa-minus';
  isClientNameDivOpen: boolean = true;
  isLocationSearch: boolean = false;
  isNameSearch: boolean = false;
  error: any;
  displayFn: any;

  pageTitle: string = 'CLIENT LIST';
  mainBreacrumb: string = 'Clients';
  mainRoute: string = '/user/client-list';
  finalBreadcrumb: string = 'Recruiz-Test';
  isBreadcrumbShow: boolean = false;

  isClient: boolean = false;
  isPosition: boolean = false;


  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private utilService: UtilService,
    private client: ClientService,
    private dropdown: DropdownService,
    private searchFilter: SearchService
  ) { }

  globalData: any;
  translateName: any;
  translateNameSingle: any;
  sortDropdown: Array<any>;
  currentPage: any;
  sortBy: any;

  clientList: any;
  pageSize: any;
  totalElements: any;
  pageNumberTo: any;
  firstView: any;
  totalNumberOfElements: any;
  onPage: any = 1;
  totalPageCount: any;

  allStatusList: any;
  activeClient: any;
  onHoldClient: any;
  closedClient: any;
  stopSourcingClient: any;
  goToPage: any;

  filterObject = {
    statusList: [],
    nameList: [],
    locationList: []
  };

  filteredLocation: any;
  isLoading: boolean;
  errorMsg: any;
  deleteInput: boolean = false;
  clientLocations: Array<any> = [];

  filteredClientName: any;
  clientNames: Array<any> =[];

  ngOnInit(): void {
    this.globalData = JSON.parse(localStorage.getItem('userInfo'));
    if (this.globalData.orgType === "Corporate") {
      this.translateName = 'Departments';
      this.translateNameSingle = 'Department';
    } else {
      this.translateName = 'Clients';
      this.translateNameSingle = 'Client';
    }

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


    //load all clients
    this.getClientLists();
    this.loadStatusList();
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

  // get all client list
  public getClientLists() {
    var verifySortKey = this.utilService.findSortKeyIndex(this.sortDropdown, this.sortBy);
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
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);

    //callng the api to get all client list
    this.client.getAllClients(pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder).pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.clientList = response.data.content;
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

  //call function on page change
  public getClientListOnPageChange(event: any) {
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


  //go to details page
  public goToUrl(clientId: any, url: any) {
    window.open(url + '?cId=' + clientId, '_blank');
  }

  //sort by function
  public onSortByChange(sortItem: string) {
    this.sortBy = sortItem;
    let currentUrl = this.router.url;
    let urls = currentUrl.split('?')[0];
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([urls], { queryParams: { page: this.currentPage, sort: sortItem } })
    );
  }

  // get all status list 
  public loadStatusList() {
    this.dropdown.getCommanStatusList().pipe(first()).subscribe(data => {
      if (data.success === true) {
        this.allStatusList = data.data;
        this.activeClient = this.allStatusList[0].value;
        this.onHoldClient = this.allStatusList[1].value;
        this.closedClient = this.allStatusList[2].value;
        this.stopSourcingClient = this.allStatusList[3].value;
      }
    },
      error => {
        console.log('Error : ' + JSON.stringify(error));
        this.error = error;
      })
  }

  //change status
  public changeStatusClient(status: any, id: any, index: any, data: any) {
    this.client.changeClientStatus(status, id).pipe(first()).subscribe(response => {
      if (response.success === true) {
        data[index].client = response.data;
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

  //use filter status client
  public clientStatusFilter(status: any, isChecked: boolean) {
    // getting pageable object
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);
    if (this.filterObject.hasOwnProperty('statusList')) {
      isChecked === true ? this.filterObject.statusList.push(status) :
        this.filterObject.statusList.splice(this.filterObject.statusList.indexOf(status), 1);
    } else {
      this.filterObject.statusList = [status];
    }

    //api function
    this.universalFuction(this.filterObject, pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder);
  }

  //remove filter status client
  public removeStatusClient(value: any, arrayList: any, index: any) {
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);
    arrayList[index].checkStatus = false;
    this.filterObject.statusList.splice(this.filterObject.statusList.indexOf(value), 1);
    //api function
    this.universalFuction(this.filterObject, pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder)
  }

  //get location on typehead
  public onInputChanged(currLoc: any): void {
    this.isLoading = true;
    this.searchFilter.getFilteredDataLocation(currLoc.target.value).pipe(first()).subscribe(response => {
      if (response.success) {
        if (response.data === null) {
          this.filteredLocation = [];
          this.isLoading = false;
          this.errorMsg = 'Not found...';
        } else {
          this.filteredLocation = response.data;
          this.isLoading = false;
          this.errorMsg = null;
        }
       
      }
      
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    });
  }
  //select location
  public selectedLocation(location: any) {
    // getting pageable object
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);
    this.clientLocations.push({ name: location, checkStatus: true });
    if (this.filterObject.hasOwnProperty('locationList')) {
      this.filterObject.locationList.push(location)
    } else {
      this.filterObject.locationList = [location];
    }
    //api function
    this.deleteInput = true;
    this.universalFuction(this.filterObject, pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder);
  }
  //remove location
  public removeLocation(value: any, arrayList: any, index: any) {
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);
    arrayList[index].checkStatus = false;
    this.filterObject.locationList.splice(this.filterObject.locationList.indexOf(value), 1);
    this.universalFuction(this.filterObject, pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder);
  }

  //get client name on typehead
  public onInputClientName(currLoc: any): void {
    this.isLoading = true;
    this.searchFilter.getFilteredDataClientName(currLoc.target.value).pipe(first()).subscribe(response => {
      if (response.success) {
        if (response.data === null) {
          this.filteredClientName = [];
          this.isLoading = false;
          this.errorMsg = 'Not found...';
        } else {
          this.filteredClientName = response.data;
          this.isLoading = false;
          this.errorMsg = null;
        }

      }

    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    });
  }
  //select client name
  public selectedClientName(location: any) {
    // getting pageable object
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);
    this.clientNames.push({ name: location, checkStatus: true });
    if (this.filterObject.hasOwnProperty('nameList')) {
      this.filterObject.nameList.push(location)
    } else {
      this.filterObject.nameList = [location];
    }
    //api function
    this.deleteInput = true;
    this.universalFuction(this.filterObject, pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder);
  }
  //remove client name
  public removeClientName(value: any, arrayList: any, index: any) {
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);
    arrayList[index].checkStatus = false;
    this.filterObject.nameList.splice(this.filterObject.nameList.indexOf(value), 1);
    this.universalFuction(this.filterObject, pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder);
  }

  //filters calling universal function
  public universalFuction(filterObject: any, pageNo: any, sortField: any, sortOrder: any) {
    if (filterObject.statusList === undefined) {
      filterObject.statusList = [];
    }

    if (filterObject.nameList === undefined) {
      filterObject.nameList = [];
    }
    if (filterObject.locationList === undefined) {
      filterObject.locationList = [];
    }
    let arrs = [filterObject.statusList, filterObject.nameList, filterObject.locationList];
    var resettingFilterList = arrs.reduce((a, b) => [...a, ...b], []);

    if (resettingFilterList.length === 0) {
      this.getClientLists();
    } else {

      this.searchFilter.getClientSearchResults(filterObject, pageNo, sortField, sortOrder).pipe(first()).subscribe(response => {
        if (response.success === true) {
          this.clientList = response.data.content;
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
        });
    }
  }

  // delete client
  public deleteClient(clentId: any, index: any, clientLists: any) {

  }

}