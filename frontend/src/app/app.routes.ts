import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    title: 'Discover and rate music | Soundrate',
    loadComponent: () => import('./pages/home-page/home-page.component').then(m => m.HomePageComponent)
  },
  {
    path: 'login',
    title: 'Login | Soundrate',
    loadComponent: () => import('./pages/login-page/login-page.component').then(m => m.LoginPageComponent)
  },
  {
    path: 'register',
    title: 'Create account | Soundrate',
    loadComponent: () => import('./pages/register-page/register-page.component').then(m => m.RegisterPageComponent)
  },
  {
    path: 'forgot-password',
    title: 'Recover password | Soundrate',
    loadComponent: () => import('./pages/forget-password-page/forget-password-page.component').then(m => m.ForgetPasswordPageComponent)
  },
  {
    path: 'reset-password',
    title: 'Reset password | Soundrate',
    loadComponent: () => import('./pages/reset-password-page/reset-password-page.component').then(m => m.ResetPasswordPageComponent)
  },
  {
    path: 'user/:username',
    title: 'User profile | Soundrate',
    loadComponent: () => import('./pages/user-profile-page/user-profile-page.component').then(m => m.UserProfilePageComponent)
  },
  {
    path: 'album/:id',
    title: 'Album details | Soundrate',
    loadComponent: () => import('./pages/album-details-page/album-details-page.component').then(m => m.AlbumDetailsPageComponent)
  },
  {
    path: 'artist/:id',
    title: 'Artist details | Soundrate',
    loadComponent: () => import('./pages/artist-details-page/artist-details-page.component').then(m => m.ArtistDetailsPageComponent)
  },
  {
    path: 'settings',
    title: 'Account settings | Soundrate',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/settings-page/settings-page.component').then(m => m.SettingsPageComponent)
  },
  {
    path: 'listen-later',
    title: 'Listen Later | Soundrate',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/listen-later-page/listen-later-page.component').then(m => m.ListenLaterPageComponent)
  },
  {
    path: 'about',
    title: 'About | Soundrate',
    loadComponent: () => import('./pages/about-page/about-page.component').then(m => m.AboutPageComponent)
  },
  { path: '**', redirectTo: '' }
];
