import { DOCUMENT } from '@angular/common';
import { Component, DestroyRef, HostListener, Inject } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs/operators';
import { FooterComponent } from '../footer/footer.component';
import { HeaderComponent } from '../header/header.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent {
  showBackToTop = false;
  isHomePage = false;

  constructor(
    router: Router,
    destroyRef: DestroyRef,
    @Inject(DOCUMENT) private document: Document
  ) {
    this.isHomePage = router.url.split('?')[0] === '/';

    router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      takeUntilDestroyed(destroyRef)
    ).subscribe(() => {
      this.isHomePage = router.url.split('?')[0] === '/';
      this.document.defaultView?.setTimeout(() => {
        this.document.getElementById('main-content')?.focus();
        this.updateBackToTopVisibility();
      });
    });
  }

  @HostListener('window:scroll')
  updateBackToTopVisibility(): void {
    const view = this.document.defaultView;
    if (!view) return;

    const page = this.document.documentElement;
    const distanceFromBottom = page.scrollHeight - (view.scrollY + view.innerHeight);
    this.showBackToTop = view.scrollY > 320 && distanceFromBottom < 180;
  }

  scrollToTop(): void {
    const view = this.document.defaultView;
    if (!view) return;

    const reduceMotion = view.matchMedia('(prefers-reduced-motion: reduce)').matches;
    view.scrollTo({ top: 0, behavior: reduceMotion ? 'auto' : 'smooth' });
  }
}
