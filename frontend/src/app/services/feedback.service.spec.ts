import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OperationFeedbackComponent } from '../components/operation-feedback/operation-feedback.component';
import { FeedbackService } from './feedback.service';

describe('FeedbackService', () => {
  let snackBar: jasmine.SpyObj<MatSnackBar>;
  let service: FeedbackService;

  beforeEach(() => {
    snackBar = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['openFromComponent']);
    snackBar.openFromComponent.and.returnValue({} as never);

    TestBed.configureTestingModule({
      providers: [
        FeedbackService,
        { provide: MatSnackBar, useValue: snackBar }
      ]
    });

    service = TestBed.inject(FeedbackService);
  });

  it('opens a branded, accessible success notification', () => {
    service.success('Rating saved', 'You gave the album 5 out of 5 stars.');

    expect(snackBar.openFromComponent).toHaveBeenCalledWith(
      OperationFeedbackComponent,
      jasmine.objectContaining({
        duration: 3200,
        horizontalPosition: 'right',
        verticalPosition: 'bottom',
        politeness: 'polite',
        panelClass: ['soundrate-feedback-panel', 'soundrate-feedback-panel--success'],
        data: jasmine.objectContaining({
          kind: 'success',
          title: 'Rating saved',
          dismissible: true
        })
      })
    );
  });

  it('keeps progress feedback visible until the operation finishes', () => {
    service.loading('Publishing your review', 'Saving your thoughts…');

    expect(snackBar.openFromComponent).toHaveBeenCalledWith(
      OperationFeedbackComponent,
      jasmine.objectContaining({
        duration: undefined,
        data: jasmine.objectContaining({ kind: 'loading', dismissible: false })
      })
    );
  });
});
