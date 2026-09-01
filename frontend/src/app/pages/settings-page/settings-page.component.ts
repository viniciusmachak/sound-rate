import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { MatDialog } from '@angular/material/dialog';
import { User } from '../../models/user.model';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ConfirmationDialogComponent } from '../../components/confirmation-dialog/confirmation-dialog.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { finalize } from 'rxjs/operators';
import { FeedbackService } from '../../services/feedback.service';

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
    MatProgressSpinnerModule
],
  templateUrl: './settings-page.component.html',
  styleUrl: './settings-page.component.css'
})
export class SettingsPageComponent implements OnInit {
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  selectedFile: File | null = null;
  imagePreview: string | ArrayBuffer | null = null;
  currentUser!: User | null;
  hideCurrentPassword = true;
  hideNewPassword = true;
  hideConfirmPassword = true;
  isUpdatingProfile = false;
  isUpdatingPassword = false;
  isUploadingAvatar = false;
  isResettingAvatar = false;
  isDeletingAccount = false;

  get hasProfileChanges(): boolean {
    if (!this.profileForm) return false;

    return this.profileForm.get('email')?.value !== (this.currentUser?.email || '')
      || this.normalizeBio(this.profileForm.get('bio')?.value) !== this.normalizeBio(this.currentUser?.bio);
  }

  constructor(
    private fb: FormBuilder,
    private apiService: ApiService,
    private authService: AuthService,
    private feedback: FeedbackService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    this.imagePreview = this.currentUser?.avatarUrl || null;

    this.profileForm = this.fb.group({
      email: [this.currentUser?.email || '', [Validators.required, Validators.email]],
      bio: [this.currentUser?.bio || '', [Validators.maxLength(280)]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: passwordMatchValidator });
  }

  private normalizeBio(bio: string | null | undefined): string {
    return bio?.trim() || '';
  }

  onUpdateProfile(): void {
    if (this.profileForm.invalid || !this.hasProfileChanges) return;

    this.isUpdatingProfile = true;
    this.apiService.updateProfile(this.profileForm.value).pipe(
      finalize(() => this.isUpdatingProfile = false)
    ).subscribe({
      next: (updatedUser) => {
        this.currentUser = updatedUser;
        this.authService.updateCurrentUser(updatedUser);
        this.profileForm.markAsPristine();
        this.feedback.success(
          'Profile updated',
          `@${updatedUser.username}, your email and bio are up to date.`
        );
      },
      error: () => this.feedback.error(
        'Couldn’t update your profile',
        'That email may already be in use. Review it and try again.'
      )
    });
  }

  onUpdatePassword(): void {
    if (this.passwordForm.invalid) return;

    const { currentPassword, newPassword } = this.passwordForm.value;
    this.isUpdatingPassword = true;
    this.apiService.updatePassword({ currentPassword, newPassword }).pipe(
      finalize(() => this.isUpdatingPassword = false)
    ).subscribe({
      next: () => {
        this.feedback.success('Password changed', 'Your account now uses the new password.');
        this.passwordForm.reset();
      },
      error: () => this.feedback.error(
        'Couldn’t change your password',
        'Check your current password and try again.'
      )
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
        this.isDeletingAccount = true;
        this.apiService.deleteCurrentUser().pipe(
          finalize(() => this.isDeletingAccount = false)
        ).subscribe({
          next: () => {
            this.feedback.success(
              'Account deleted',
              `Your Soundrate data was removed${this.currentUser?.username ? `, @${this.currentUser.username}` : ''}. We’ll miss you.`,
              5200
            );
            this.authService.logout();
          },
          error: () => this.feedback.error(
            'Couldn’t delete your account',
            'Nothing was removed. Please wait a moment and try again.',
            5200
          )
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

    this.isUploadingAvatar = true;
    this.apiService.updateAvatar(this.selectedFile).pipe(
      finalize(() => this.isUploadingAvatar = false)
    ).subscribe({
      next: (updatedUser) => {
        this.feedback.success('Profile photo updated', 'Your new image is now visible across Soundrate.');
        this.currentUser = updatedUser;
        this.authService.updateCurrentUser(updatedUser);
        this.imagePreview = updatedUser.avatarUrl;
        this.selectedFile = null;
      },
      error: () => this.feedback.error(
        'Couldn’t upload your photo',
        'Choose a valid image and try again.'
      )
    });
  }

  onResetAvatar(): void {
    this.isResettingAvatar = true;
    this.apiService.resetAvatar().pipe(
      finalize(() => this.isResettingAvatar = false)
    ).subscribe({
      next: (updatedUser) => {
        this.feedback.success('Default photo restored', 'Your profile is using the Soundrate default image.');
        this.currentUser = updatedUser;
        this.authService.updateCurrentUser(updatedUser);
        this.imagePreview = updatedUser.avatarUrl;
        this.selectedFile = null;
      },
      error: () => this.feedback.error(
        'Couldn’t reset your photo',
        'Your current profile image was kept. Please try again.'
      )
    });
  }
}
