import { Injectable } from '@angular/core';
import Swal from "sweetalert2";

@Injectable({
  providedIn: 'root'
})
export class UtilService {

  constructor() { }

  findSortKeyIndex(sortDropdown: any, sortKey: any) {
    for (var i = 0; i < sortDropdown.length; i += 1) {
      if (sortDropdown[i].value === sortKey) {
        return i;
        break;
      }
    }
    return -1;
  };

  getPageableObject(sortKey: any, pageNo: any) {
    var pageableObject: any = {};
    // pipe is use to differentiate sortField and sortOrder in URL
    // splitting key word like sortField and sort order into array
    var sortArr = sortKey.split('|');
    var sortField = sortArr[0];
    var sortOrder = sortArr[1];
    // decrease by 1 requires for back-end, because server pagination starts with 0 page
    pageNo = pageNo - 1;
    pageableObject = {
      pageNo: pageNo,
      sortField: sortField,
      sortOrder: sortOrder
    };
    return pageableObject;
  }

  //check client position
  isClientOrPositionActive(clientStatus: any, positionStatus: any) {
    if (clientStatus === "Active" && positionStatus === "Active" || positionStatus === "StopSourcing") {
      return true;
    } else {
      // if client not active and position not active/pause sourcing not active showing sweet alert
      Swal.fire({
        title: "Client's or position's status is 'On Hold' or 'Closed'",
        showConfirmButton: true
      });
      return false;
    }
  }
}
