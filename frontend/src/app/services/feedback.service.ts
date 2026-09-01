import { Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarRef } from '@angular/material/snack-bar';
import {
  OperationFeedbackComponent,
  OperationFeedbackData,
  OperationFeedbackKind
} from '../components/operation-feedback/operation-feedback.component';

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  constructor(private readonly snackBar: MatSnackBar) {}

  success(title: string, message: string, duration = 3200): MatSnackBarRef<OperationFeedbackComponent> {
    return this.show('success', title, message, duration);
  }

  error(title: string, message: string, duration = 4800): MatSnackBarRef<OperationFeedbackComponent> {
    return this.show('error', title, message, duration);
  }

  warning(title: string, message: string, duration = 4200): MatSnackBarRef<OperationFeedbackComponent> {
    return this.show('warning', title, message, duration);
  }

  info(title: string, message: string, duration = 3600): MatSnackBarRef<OperationFeedbackComponent> {
    return this.show('info', title, message, duration);
  }

  loading(title: string, message: string): MatSnackBarRef<OperationFeedbackComponent> {
    return this.show('loading', title, message);
  }

  retry(title: string, message: string, actionLabel = 'Try again'): MatSnackBarRef<OperationFeedbackComponent> {
    return this.show('error', title, message, 6000, actionLabel);
  }

  private show(
    kind: OperationFeedbackKind,
    title: string,
    message: string,
    duration?: number,
    actionLabel?: string
  ): MatSnackBarRef<OperationFeedbackComponent> {
    const data: OperationFeedbackData = {
      kind,
      title,
      message,
      dismissible: kind !== 'loading',
      actionLabel
    };

    return this.snackBar.openFromComponent(OperationFeedbackComponent, {
      data,
      duration,
      horizontalPosition: 'right',
      verticalPosition: 'bottom',
      panelClass: ['soundrate-feedback-panel', `soundrate-feedback-panel--${kind}`],
      politeness: kind === 'error' || kind === 'warning' ? 'assertive' : 'polite',
      announcementMessage: `${title}. ${message}`
    });
  }
}
