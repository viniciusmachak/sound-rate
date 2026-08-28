import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError } from 'rxjs';
import { Page } from '../../models/page.model';
import { SocialUser } from '../../models/social-user.model';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { UserListComponent, UserListDialogData } from './user-list.component';

describe('UserListComponent', () => {
  let apiService: jasmine.SpyObj<ApiService>;

  const firstPage: Page<SocialUser> = {
    content: [
      {
        id: 2,
        username: 'listener',
        avatarUrl: '/listener.png',
        isFollowedByCurrentUser: false
      }
    ],
    totalPages: 2,
    totalElements: 24,
    number: 0,
    size: 20,
    last: false,
    first: true
  };

  const emptyPage: Page<SocialUser> = {
    content: [],
    totalPages: 0,
    totalElements: 0,
    number: 0,
    size: 20,
    last: true,
    first: true
  };

  beforeEach(() => {
    apiService = jasmine.createSpyObj<ApiService>('ApiService', [
      'getFollowers',
      'getFollowing',
      'getFollowingArtists',
      'followUser',
      'unfollowUser'
    ]);
    apiService.getFollowers.and.returnValue(of(firstPage));

    TestBed.configureTestingModule({
      imports: [UserListComponent],
      providers: [
        provideRouter([]),
        provideNoopAnimations(),
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            username: 'author',
            listType: 'followers',
            totalCount: 24
          } satisfies UserListDialogData
        },
        { provide: ApiService, useValue: apiService },
        {
          provide: AuthService,
          useValue: { isAuthenticated: () => false, currentUserValue: null }
        },
        { provide: MatSnackBar, useValue: jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']) }
      ]
    });
  });

  it('shows the total and the loaded-item counter', () => {
    const fixture = TestBed.createComponent(UserListComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Followers · 24');
    expect(fixture.nativeElement.textContent).toContain('Load more · 1 of 24');
  });

  it('retries the current page after an error', () => {
    apiService.getFollowers.and.returnValues(
      throwError(() => new Error('network error')),
      of(emptyPage)
    );
    const fixture = TestBed.createComponent(UserListComponent);
    fixture.detectChanges();

    const retryButton = Array.from(
      fixture.nativeElement.querySelectorAll('button') as NodeListOf<HTMLButtonElement>
    ).find(button => button.textContent?.includes('Try again'));
    retryButton?.click();
    fixture.detectChanges();

    expect(apiService.getFollowers).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.textContent).toContain('No one follows @author yet.');
  });

  it('debounces server-side search', fakeAsync(() => {
    const fixture = TestBed.createComponent(UserListComponent);
    fixture.detectChanges();

    fixture.componentInstance.searchControl.setValue(' listener ');
    tick(300);

    expect(apiService.getFollowers).toHaveBeenCalledWith('author', 0, 20, 'listener');
  }));
});
