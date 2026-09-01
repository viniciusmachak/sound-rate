import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIcon } from '@angular/material/icon';
import { finalize } from 'rxjs/operators';
import { FeedbackService } from '../../services/feedback.service';
@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIcon
],
  templateUrl: './register-page.component.html',
  styleUrl: '../auth-page.shared.css'
})

export class RegisterPageComponent {
  registerForm: FormGroup;
  errorMessage: string | null = null;
  hide = true;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private feedback: FeedbackService
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    this.authService.register(this.registerForm.value).pipe(
      finalize(() => this.isLoading = false)
    ).subscribe({
      next: () => {
        const username = this.authService.currentUserValue?.username ?? this.registerForm.value.username;
        this.feedback.success('Your Soundrate profile is ready', `Welcome, @${username}. Start rating what you hear.`);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'An error occurred while registering. Please try again.';
      }
    });
  }
}
