import { Component, OnInit } from '@angular/core';
import { first } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import { UtilService } from '../../utilService/util.service';
import { ProspectService } from '../prospectService/prospect.service';
import Swal from 'sweetalert2';
import { DropdownService } from '../../dropdownService/dropdown.service';
import { SearchService } from './../../searchService/search.service';

@Component({
  selector: 'app-prospect-list',
  templateUrl: './prospect-list.component.html',
  styleUrls: ['./prospect-list.component.css']
})
export class ProspectListComponent implements OnInit {

  error: any;
  isFilterOpen: boolean = false;
  pageTitle: string = 'PROSPECTS LIST';
  mainBreacrumb: string = 'Home';
  mainRoute: string = '/user/client-list';
  finalBreadcrumb: string = 'Prospect';
  isBreadcrumbShow: boolean = false;
  isStatusDivOpen = true;
  statusDivIcon: string = 'fa-minus';
  currentDivIcon: string = 'fa-minus';
  isCurrentLocationDivOpen: boolean = true;
  clientNameDivIcon: string = 'fa-minus';
  isClientNameDivOpen: boolean = true;
  isLocationSearch: boolean = false;
  isNameSearch: boolean = false;

  data: any[] = [];
  globalData: any;
  currentPage: any;
  sortBy: any;
  sortDropdown: Array<any> = [];

  prospectList: any = [];
  pageSize: any;
  totalElements: any;
  pageNumberTo: any;
  firstView: any;
  totalNumberOfElements: any;
  onPage: any = 1;
  totalPageCount: any;
  goToPage: any;
  allStatusList: any = [];
  filterObject: any = {
    statusList: [],
    nameList: [],
    locationList: []
  };
  clientLocations: Array<any> = [];

  filteredClientName: any;
  clientNames: Array<any> =[];
  filteredLocation: any;
  isLoading: boolean;
  errorMsg: any;
  deleteInput: boolean = false;
  displayFn: any;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private utilService: UtilService,
    private prospect: ProspectService,
    private dropdown: DropdownService,
    private serachFilter: SearchService
  ) { }

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
      value: 'companyName|asc'
    },
    {
      key: 'Name Z-A',
      value: 'companyName|desc'
    },
    {
      key: 'Modification Date',
      value: 'modificationDate|desc'
    },
    {
      key: 'Conversion Probability 0-10',
      value: 'companyName|min'
    },
    {
      key: 'Conversion Probability 10-0',
      value: 'companyName|max'
    }];

    setTimeout(() => {
      this.LoadProspectList();
    }, 100);

  }

  public formatLabel(value: number) {
    if (value >= 1) {
      return Math.round(value / 1) + '%';
    }
    return value;
  }

  // filter
  public openFilter(){
    this.isFilterOpen = !this.isFilterOpen;
    if(this.isFilterOpen && this.allStatusList.length == 0) {
      this.LoadProspectStatus();
    }
  }

  public toggleFilterDiv(type:string) {
    if(type == 'status'){
      this.isStatusDivOpen = !this.isStatusDivOpen;
      if(this.isStatusDivOpen) this.statusDivIcon='fa-minus';
      else this.statusDivIcon = 'fa-plus';
    } else if(type == 'location'){
      this.isCurrentLocationDivOpen = !this.isCurrentLocationDivOpen;
      if(this.isCurrentLocationDivOpen) this.currentDivIcon = 'fa-minus';
      else this.currentDivIcon = 'fa-plus';
    } else {
      this.isClientNameDivOpen = !this.isClientNameDivOpen;
      if(this.isClientNameDivOpen) this.clientNameDivIcon='fa-minus';
      else this.clientNameDivIcon = 'fa-plus';
    }
  }

  public  search(event:any,type:string){
    let value=event.target.value;
    if(value==''){
      this.isNameSearch=false;
      this.isLocationSearch=false;
    }else{
      if(type=='location') this.isLocationSearch=true;
      else this.isNameSearch=true;
    }
  }

  // get all prospects
  public LoadProspectList() {
    let verifySortKey = this.utilService.findSortKeyIndex(this.sortDropdown, this.sortBy);
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
    let pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);
    //callng the api to get all client list
    this.prospect.getAllProspect(pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder).pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.prospectList = response.data.content;
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

  // on page change
  public getProspectListOnPageChange(event: any) {
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

  //sort by function
  public onSortByChange(sortItem: string) {
    this.sortBy = sortItem;
    let currentUrl = this.router.url;
    let urls = currentUrl.split('?')[0];
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() =>
      this.router.navigate([urls], { queryParams: { page: this.currentPage, sort: sortItem } })
    );
  }

  // load prospect status
  public LoadProspectStatus() {
    this.dropdown.getProspectStatusList().pipe(first()).subscribe(response => {
      if (response.success === true) {
        this.allStatusList = response.data;
      }
    }, error => {
      console.log('Error : ' + JSON.stringify(error));
      this.error = error;
    })
  }

  //use filter status client
  public prospectStatusFilter (status: any, isChecked: boolean) {
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
      this.LoadProspectList();
    } else {

      this.serachFilter.getClientSearchResults(filterObject, pageNo, sortField, sortOrder).pipe(first()).subscribe(response => {
        if (response.success === true) {
          this.prospectList = response.data.content;
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

   //remove filter status client
   public removeStatusProspect(value: any, arrayList: any, index: any) {
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);
    arrayList[index].checkStatus = false;
    this.filterObject.statusList.splice(this.filterObject.statusList.indexOf(value), 1);
    //api function
    this.universalFuction(this.filterObject, pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder)
  }

  //remove location
  public removeLocation(value: any, arrayList: any, index: any) {
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);
    arrayList[index].checkStatus = false;
    this.filterObject.locationList.splice(this.filterObject.locationList.indexOf(value), 1);
    this.universalFuction(this.filterObject, pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder);
  }

   //remove client name
   public removeClientName(value: any, arrayList: any, index: any) {
    var pageableObject = this.utilService.getPageableObject(this.sortBy, this.currentPage);
    arrayList[index].checkStatus = false;
    this.filterObject.nameList.splice(this.filterObject.nameList.indexOf(value), 1);
    this.universalFuction(this.filterObject, pageableObject.pageNo, pageableObject.sortField, pageableObject.sortOrder);
  }

  //get location on typehead
  public onInputChanged(currLoc: any): void {
    this.isLoading = true;
    this.serachFilter.getFilteredDataLocationProspect(currLoc.target.value).pipe(first()).subscribe(response => {
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

   //get client name on typehead
   public onInputProspecttName(name: any): void {
    this.isLoading = true;
    this.serachFilter.getFilteredDataNameProspect(name.target.value).pipe(first()).subscribe(response => {
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
   public selectedProspectName(location: any) {
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

  //go to details page
  public goToUrl(clientId: any, url: any) {
    window.open(url + '?pId=' + clientId, '_blank');
  }

}
