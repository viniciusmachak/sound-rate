import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home-page/home-page.component').then(m => m.HomePageComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login-page/login-page.component').then(m => m.LoginPageComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register-page/register-page.component').then(m => m.RegisterPageComponent)
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./pages/forget-password-page/forget-password-page.component').then(m => m.ForgetPasswordPageComponent)
  },
  {
    path: 'reset-password',
    loadComponent: () => import('./pages/reset-password-page/reset-password-page.component').then(m => m.ResetPasswordPageComponent)
  },
  {
    path: 'user/:username',
    loadComponent: () => import('./pages/user-profile-page/user-profile-page.component').then(m => m.UserProfilePageComponent)
  },
  {
    path: 'album/:id',
    loadComponent: () => import('./pages/album-details-page/album-details-page.component').then(m => m.AlbumDetailsPageComponent)
  },
  {
    path: 'artist/:id',
    loadComponent: () => import('./pages/artist-details-page/artist-details-page.component').then(m => m.ArtistDetailsPageComponent)
  },
  {
    path: 'settings',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/settings-page/settings-page.component').then(m => m.SettingsPageComponent)
  },
  {
    path: 'listen-later',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/listen-later-page/listen-later-page.component').then(m => m.ListenLaterPageComponent)
  },
  { path: '**', redirectTo: '' }
];
