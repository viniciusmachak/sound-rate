import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from '../services/auth.service';
import { jwtInterceptor } from './jwt.interceptor';

describe('jwtInterceptor', () => {
  let httpTesting: HttpTestingController;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['getToken', 'logout']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService }
      ]
    });

    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('adds the bearer token when one is available', () => {
    authService.getToken.and.returnValue('test-token');

    const http = TestBed.inject(HttpClient);
    http.get('/api/v1/test').subscribe();

    const request = httpTesting.expectOne('/api/v1/test');
    expect(request.request.headers.get('Authorization')).toBe('Bearer test-token');
    request.flush({});
  });

  it('logs out after an unauthorized response', () => {
    authService.getToken.and.returnValue('expired-token');
    const http = TestBed.inject(HttpClient);

    http.get('/api/v1/test').subscribe({ error: () => undefined });
    httpTesting.expectOne('/api/v1/test').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(authService.logout).toHaveBeenCalledTimes(1);
  });

  it('preserves the session after a forbidden response', () => {
    authService.getToken.and.returnValue('valid-token');
    const http = TestBed.inject(HttpClient);

    http.get('/api/v1/test').subscribe({ error: () => undefined });
    httpTesting.expectOne('/api/v1/test').flush({}, { status: 403, statusText: 'Forbidden' });

    expect(authService.logout).not.toHaveBeenCalled();
  });
});
