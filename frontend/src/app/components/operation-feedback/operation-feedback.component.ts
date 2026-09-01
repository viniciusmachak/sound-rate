import { Component, Inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import {
  MAT_SNACK_BAR_DATA,
  MatSnackBarRef
} from '@angular/material/snack-bar';

export type OperationFeedbackKind = 'success' | 'error' | 'warning' | 'info' | 'loading';

export interface OperationFeedbackData {
  kind: OperationFeedbackKind;
  title: string;
  message: string;
  dismissible: boolean;
  actionLabel?: string;
}

@Component({
  selector: 'app-operation-feedback',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './operation-feedback.component.html',
  styleUrl: './operation-feedback.component.css'
})
export class OperationFeedbackComponent {
  private readonly icons: Record<OperationFeedbackKind, string> = {
    success: 'check_circle',
    error: 'error',
    warning: 'warning_amber',
    info: 'info',
    loading: 'progress_activity'
  };

  constructor(
    @Inject(MAT_SNACK_BAR_DATA) readonly data: OperationFeedbackData,
    private readonly snackBarRef: MatSnackBarRef<OperationFeedbackComponent>
  ) {}

  get icon(): string {
    return this.icons[this.data.kind];
  }

  close(): void {
    this.snackBarRef.dismiss();
  }

  runAction(): void {
    this.snackBarRef.dismissWithAction();
  }
}
