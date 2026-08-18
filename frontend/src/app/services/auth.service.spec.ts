import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { User } from '../models/user.model';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  const user: User = {
    id: 1,
    username: 'listener',
    email: 'listener@example.com',
    avatarUrl: '/avatar.png'
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        { provide: Router, useValue: jasmine.createSpyObj<Router>('Router', ['navigate']) }
      ]
    });
  });

  afterEach(() => localStorage.clear());

  it('requires both a stored user and a token for authentication', () => {
    localStorage.setItem('currentUser', JSON.stringify(user));
    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBeFalse();

    localStorage.setItem('jwt_token', 'test-token');
    expect(service.isAuthenticated()).toBeTrue();
  });

  it('clears a malformed stored session instead of failing construction', () => {
    localStorage.setItem('currentUser', '{invalid-json');
    localStorage.setItem('jwt_token', 'test-token');

    const service = TestBed.inject(AuthService);

    expect(service.currentUserValue).toBeNull();
    expect(localStorage.getItem('currentUser')).toBeNull();
    expect(localStorage.getItem('jwt_token')).toBeNull();
  });
});
