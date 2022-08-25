import { TestBed } from '@angular/core/testing';

import { OnBoardService } from './on-board.service';

describe('OnBoardService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: OnBoardService = TestBed.get(OnBoardService);
    expect(service).toBeTruthy();
  });
});
