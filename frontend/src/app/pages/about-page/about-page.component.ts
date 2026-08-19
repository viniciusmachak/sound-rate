import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

interface AboutFeature {
  icon: string;
  title: string;
  description: string;
}

interface TechnicalTopic {
  icon: string;
  label: string;
  title: string;
  description: string;
  details: string[];
}

@Component({
  selector: 'app-about-page',
  standalone: true,
  imports: [RouterLink, MatIconModule],
  templateUrl: './about-page.component.html',
  styleUrl: './about-page.component.css'
})
export class AboutPageComponent {
  readonly features: AboutFeature[] = [
    {
      icon: 'travel_explore',
      title: 'Discover',
      description: 'Search the Deezer catalog and find albums and artists worth hearing next.'
    },
    {
      icon: 'star',
      title: 'Rate',
      description: 'Turn every listen into a personal record with a rating and an honest review.'
    },
    {
      icon: 'forum',
      title: 'Share',
      description: 'Follow other listeners and exchange opinions with a community that cares.'
    },
    {
      icon: 'bookmark',
      title: 'Save for later',
      description: 'Keep interesting albums in one place until you are ready to press play.'
    }
  ];

  readonly technicalTopics: TechnicalTopic[] = [
    {
      icon: 'code',
      label: 'Stack',
      title: 'A modern web experience',
      description:
        'The interface is built as a responsive Angular application and connects Soundrate data with the Deezer music catalog.',
      details: ['Angular', 'TypeScript', 'RxJS', 'Angular Material', 'Deezer API']
    },
    {
      icon: 'favorite',
      label: 'Purpose',
      title: 'Give every listen a place to live',
      description:
        'Soundrate is a study project about transforming music discovery into memory, conversation and community.',
      details: ['Discover music', 'Track your taste', 'Connect listeners']
    }
  ];
}
