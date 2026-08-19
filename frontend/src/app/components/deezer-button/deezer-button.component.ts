import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-deezer-button',
  standalone: true,
  templateUrl: './deezer-button.component.html',
  styleUrl: './deezer-button.component.css'
})
export class DeezerButtonComponent {
  @Input({ required: true }) href!: string;
  @Input() label = 'Listen on Deezer';
}
