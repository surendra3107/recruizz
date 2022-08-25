import { throwError as observableThrowError, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PipelineService {

  //base url
  baseRoot: any = environment.baseUrl;
  apiUrl: any = environment.apiUrl;

  constructor(private http: HttpClient) { }

  //check pipeline permission
  checkPipelinePermission(positionCode: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/position/board/permitted?positionCode=' + positionCode, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get pipe line data info
  getBoardDataInfo(positionCode: any, status: any, sourcedBy: any, timePeriod: any, startDate: any, endDate: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    let params = new HttpParams();
    params = params.append('positionCode', positionCode);
    if (status) {
      params = params.append('status', status);
    }
    if (sourcedBy) {
      params = params.append('sourcedBy', sourcedBy);
    }
    var data = {
      'startDate': startDate,
      'endDate': endDate,
      'timePeriod': timePeriod
    }
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/position/getBoard', data, { params: params, headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get board status
  getBoardStatusList() {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/board/status', { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //change status
  changecandidateStatus(input: any, forceChange: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/round/candidate/status?changeExistingStatus=' + forceChange, input, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get round for pipeline
  getRoundName(positionCode: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/round/id/map?positionCode=' + positionCode, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //method to move candidates from one round to another.
  moveCandidates(dropRoundId: any, dragRoundId: any, candidateData: any, cardIndex: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/round/candidate/move?destRoundId=' + dropRoundId + '&sourceRoundId=' + dragRoundId + '&cardIndex=' + cardIndex, candidateData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //approve candidate status brfore moving
  changecandidateStatusforMove(fomrmData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/round/candidate/candidateStatus', fomrmData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //method to move candidates from one round to another with status change.
  moveCandidatesWithStatus(dropRoundId: any, dragRoundId: any, candidateData: any, cardIndex: any, roundCandidateDataId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/round/candidate/move?destRoundId=' + dropRoundId + '&sourceRoundId=' + dragRoundId + '&cardIndex=' + cardIndex + '&roundCandidateDataId=' + roundCandidateDataId, candidateData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //save rounds/ stages
  saveRound(boardId: any, roundData: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.put<any>(this.baseRoot + this.apiUrl + '/board/' + boardId + '/round', roundData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //delete rounds
  deleteRound(boardId: any, roundIds: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.delete<any>(this.baseRoot + this.apiUrl + '/board/' + boardId + '/round?roundIdList=' + roundIds, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //get candidate for sourcing
  getCandidatesToSource(boardId: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.get<any>(this.baseRoot + this.apiUrl + '/board/candidate/source?boardId=' + boardId, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

  //add candidate to pipeline
  addCandidatesToSource(candidateData: any, sourceMode: any) {
    let headers: HttpHeaders = new HttpHeaders();
    headers.append('Content-Type', 'application/json; charset=UTF-8');
    return this.http.post<any>(this.baseRoot + this.apiUrl + '/round/candidate/source?sourceMode=' + sourceMode, candidateData, { headers: headers })
      .pipe(map(user => {
        return user;
      }));
  }

}
