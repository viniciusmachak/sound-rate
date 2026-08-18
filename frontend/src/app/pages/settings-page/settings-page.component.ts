import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { User } from '../../models/user.model';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ConfirmationDialogComponent } from '../../components/confirmation-dialog/confirmation-dialog.component';

export const passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('newPassword');
  const confirmPassword = control.get('confirmPassword');
  return password && confirmPassword && password.value !== confirmPassword.value ? { passwordMismatch: true } : null;
};
@Component({
  selector: 'app-settings-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
],
  templateUrl: './settings-page.component.html',
  styleUrl: './settings-page.component.scss'
})
export class SettingsPageComponent implements OnInit {
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  avatarForm!: FormGroup;
  selectedFile: File | null = null;
  imagePreview: string | ArrayBuffer | null = null;
  currentUser!: User | null;

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    const currentUser = this.authService.currentUserValue;
    this.imagePreview = this.currentUser?.avatarUrl || null;

    this.profileForm = this.fb.group({
      email: [currentUser?.email || '', [Validators.required, Validators.email]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: passwordMatchValidator });

    this.avatarForm = this.fb.group({
      avatarUrl: [this.currentUser?.avatarUrl || '', [Validators.required, Validators.pattern('https?://.+')]]
    });
  }

  onUpdateProfile(): void {
    if (this.profileForm.invalid) return;

    this.apiService.updateProfile(this.profileForm.value).subscribe({
      next: (updatedUser) => {
        this.snackBar.open('Profile updated successfully!', 'Close', { duration: 3000 });
      },
      error: (err) => this.snackBar.open('Error updating profile. Email may already be in use.', 'Close', { duration: 3000 })
    });
  }

  onUpdatePassword(): void {
    if (this.passwordForm.invalid) return;

    const { currentPassword, newPassword } = this.passwordForm.value;
    this.apiService.updatePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.snackBar.open('Password changed successfully!', 'Close', { duration: 3000 });
        this.passwordForm.reset();
      },
      error: (err) => this.snackBar.open('Error changing password. Check your current password.', 'Close', { duration: 3000 })
    });
  }

  deleteAccount(): void {

    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '400px',
      data: {
        title: 'Delete Account?',
        message: 'Are you sure you want to delete this account? This action cannot be undone'
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.apiService.deleteCurrentUser().subscribe({
          next: () => {
            this.snackBar.open('Account deleted successfully. We will miss you!', 'Close', { duration: 5000 });
            this.authService.logout();
          },
          error: (err) => this.snackBar.open('An error occurred while deleting your account.', 'Close', { duration: 5000 })
        });
      }
    });
  }

  onFileSelected(event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
      this.selectedFile = target.files[0];
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  onUploadAvatar(): void {
    if (!this.selectedFile) {
      return;
    }

    this.apiService.updateAvatar(this.selectedFile).subscribe({
      next: (updatedUser) => {
        this.snackBar.open('Profile picture updated successfully!', 'Close', { duration: 3000 });
        this.authService.updateCurrentUser(updatedUser);
        this.selectedFile = null;
      },
      error: (err) => this.snackBar.open('Error uploading image.', 'Close', { duration: 3000 })
    });
  }

  onResetAvatar(): void {
    this.apiService.resetAvatar().subscribe({
      next: (updatedUser) => {
        this.snackBar.open('Profile picture reset to default.', 'Close', { duration: 3000 });
        this.authService.updateCurrentUser(updatedUser);
        this.imagePreview = updatedUser.avatarUrl;
        this.selectedFile = null;
      },
      error: (err) => this.snackBar.open('Error resetting photo.', 'Close', { duration: 3000 })
    });
  }
}