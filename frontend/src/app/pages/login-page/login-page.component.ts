import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterLink } from '@angular/router';
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
  selector: 'app-login-page',
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
  templateUrl: './login-page.component.html',
  styleUrl: '../auth-page.shared.css'
})
export class LoginPageComponent {
  loginForm: FormGroup;
  errorMessage: string | null = null;
  hide = true;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private feedback: FeedbackService
  ) {

    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.authService.login(this.loginForm.value).pipe(
      finalize(() => this.isLoading = false)
    ).subscribe({
      next: () => {
        const username = this.authService.currentUserValue?.username ?? this.loginForm.value.username;
        this.feedback.success('Welcome back', `Signed in as @${username}. Your music is waiting.`);
      },
      error: () => {
        this.errorMessage = 'Invalid login or password. Please try again.';
      }
    });
  }
}
