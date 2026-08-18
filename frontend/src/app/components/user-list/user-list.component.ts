import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { ApiService } from '../../services/api.service';
import { User } from '../../models/user.model';
import { BehaviorSubject } from 'rxjs';
import { CommonModule } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { SkeletonLoaderComponent } from '../skeleton-loader/skeleton-loader.component';

export interface UserListDialogData {
  username: string;
  listType: 'followers' | 'following';
}

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    MatListModule,
    MatButtonModule,
    RouterLink,
    MatDialogModule,
    SkeletonLoaderComponent
  ],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css'
})
export class UserListComponent implements OnInit {
  private usersSubject = new BehaviorSubject<User[]>([]);
  users$ = this.usersSubject.asObservable();

  private currentPage = 0;
  private pageSize = 20;
  hasMore = true;
  isLoading = true;
  title = '';

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: UserListDialogData,
    private apiService: ApiService
  ) {}

  ngOnInit(): void {
    this.title = this.data.listType === 'followers' ? 'Followers' : 'Following';
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    const apiCall = this.data.listType === 'followers'
      ? this.apiService.getFollowers(this.data.username, this.currentPage, this.pageSize)
      : this.apiService.getFollowing(this.data.username, this.currentPage, this.pageSize);

    apiCall.subscribe(page => {
      const currentUsers = this.currentPage === 0 ? [] : this.usersSubject.getValue();
      this.usersSubject.next([...currentUsers, ...page.content]);
      this.hasMore = !page.last;
      this.currentPage++;
      this.isLoading = false;
    });
  }
}
