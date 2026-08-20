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
  mark: string;
  image: string;
}

@Component({
  selector: 'app-about-page',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './about-page.component.html',
  styleUrl: './about-page.component.css'
})
export class AboutPageComponent {
  readonly features: AboutFeature[] = [
    {
      icon: 'travel_explore',
      image: '/albums-wallpaper.jpeg',
      imageAlt: 'A collage of album covers',
      label: 'Discover',
      title: 'Find the album you did not know you needed.',
      description:
        'Search a world of artists and records through the Deezer catalog, then follow your curiosity wherever it leads.',
      reversed: false
    },
    {
      icon: 'star',
      image: '/rate-image.jpeg',
      imageAlt: 'A listener rating an album on Soundrate',
      label: 'Rate',
      title: 'Turn every listen into a memory.',
      description:
        'Give albums and tracks an honest rating, write down what moved you and build a lasting history of your taste.',
      reversed: true
    },
    {
      icon: 'forum',
      image: '/share-image.jpeg',
      imageAlt: 'Listeners sharing their music opinions',
      label: 'Share',
      title: 'Make music a conversation.',
      description:
        'Publish reviews, like the thoughts that resonate and follow listeners who always seem to find something special.',
      reversed: false
    },
    {
      icon: 'bookmark',
      image: '/save-later-image.jpeg',
      imageAlt: 'Albums saved to listen to later',
      label: 'Save for later',
      title: 'Keep your next listen within reach.',
      description:
        'Save promising albums as you discover them and come back when you have the time to give each one a proper listen.',
      reversed: true
    }
  ];

  readonly technologies: Technology[] = [
    { name: 'Java', mark: 'J', image: '/technologies/java.png' },
    { name: 'Spring Boot', mark: 'SB', image: '/technologies/spring-boot.png' },
    { name: 'TypeScript', mark: 'TS', image: '/technologies/typescript.png' },
    { name: 'Angular', mark: 'A', image: '/technologies/angular.png' },
    { name: 'PostgreSQL', mark: 'PG', image: '/technologies/postgresql.png' },
    { name: 'Angular Material', mark: 'AM', image: '/technologies/angular-material.png' },
    { name: 'RxJS', mark: 'RX', image: '/technologies/rxjs.png' },
    { name: 'Docker', mark: 'D', image: '/technologies/docker.png' }
  ];

  hideUnavailableImage(event: Event): void {
    const image = event.target;

    if (image instanceof HTMLImageElement) {
      image.hidden = true;
    }
  }
}
