import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';


@Component({
  selector: 'app-review-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './review-dialog.component.html',
  styleUrl: './review-dialog.component.css'
})
export class ReviewDialogComponent {
  readonly maxReviewLength = 2000;
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ReviewDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { existingText?: string }
  ) {
    this.form = this.fb.group({
      text: [data?.existingText || '', [
        Validators.required,
        Validators.minLength(10),
        Validators.maxLength(this.maxReviewLength),
        Validators.pattern(/\S/)
      ]]
    });
  }

  get isEditing(): boolean {
    return this.data.existingText !== undefined;
  }

  get reviewLength(): number {
    return this.form.get('text')?.value?.length ?? 0;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.form.valid) {
      this.dialogRef.close({ text: this.form.value.text.trim() });
    }
  }
}
