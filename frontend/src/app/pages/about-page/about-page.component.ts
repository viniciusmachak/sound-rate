import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

interface AboutFeature {
  icon: string;
  image: string;
  imageAlt: string;
  label: string;
  title: string;
  description: string;
  reversed: boolean;
}

interface Technology {
  name: string;
  image: string;
}

@Component({
  selector: 'app-about-page',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './about-page.component.html',
  styleUrl: './about-page.component.css',
})
export class AboutPageComponent {
  isTechnologyMarqueePaused = false;

  readonly features: AboutFeature[] = [
    {
      icon: 'travel_explore',
      image: '/albums-wallpaper.png',
      imageAlt: 'A collage of album covers',
      label: 'Discover',
      title: 'Find the album you did not know you needed.',
      description:
        'Search a world of artists and records through the Deezer catalog, then follow your curiosity wherever it leads.',
      reversed: false,
    },
    {
      icon: 'star',
      image: '/rate-image.png',
      imageAlt: 'A listener rating an album on Soundrate',
      label: 'Rate',
      title: 'Turn every listen into a memory.',
      description:
        'Give albums and tracks an honest rating, write down what moved you and build a lasting history of your taste.',
      reversed: true,
    },
    {
      icon: 'forum',
      image: '/share-image.png',
      imageAlt: 'Listeners sharing their music opinions',
      label: 'Share',
      title: 'Make music a conversation.',
      description:
        'Publish reviews, like the thoughts that resonate and follow listeners who always seem to find something special.',
      reversed: false,
    },
    {
      icon: 'bookmark',
      image: '/listen-later.png',
      imageAlt: 'Albums saved to listen to later',
      label: 'Save for later',
      title: 'Keep your next listen within reach.',
      description:
        'Save promising albums as you discover them and come back when you have the time to give each one a proper listen.',
      reversed: true,
    },
  ];

  readonly technologies: Technology[] = [
    {
      name: 'Java',
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/java/java-original.svg',
    },
    {
      name: 'Spring Boot',
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/spring/spring-original.svg',
    },
    {
      name: 'TypeScript',
      image:
        'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/typescript/typescript-original.svg',
    },
    {
      name: 'Angular',
      image:
        'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/angular/angular-original.svg',
    },
    {
      name: 'PostgreSQL',
      image:
        'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/postgresql/postgresql-original.svg',
    },
    {
      name: 'Angular Material',
      image: 'https://cdn.simpleicons.org/materialdesign',
    },
    {
      name: 'RxJS',
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/rxjs/rxjs-original.svg',
    },
    {
      name: 'Docker',
      image: 'https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/docker/docker-original.svg',
    },
  ];

  hideUnavailableImage(event: Event): void {
    const image = event.target;

    if (image instanceof HTMLImageElement) {
      image.hidden = true;
    }
  }

  toggleTechnologyMarquee(): void {
    this.isTechnologyMarqueePaused = !this.isTechnologyMarqueePaused;
  }
}
