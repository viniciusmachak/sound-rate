import { StarRatingComponent } from './star-rating.component';

describe('StarRatingComponent', () => {
  let component: StarRatingComponent;

  beforeEach(() => {
    component = new StarRatingComponent();
  });

  it('increments the rating by half a star with the keyboard', () => {
    component.rating = 3;
    spyOn(component.ratingChange, 'emit');
    const event = jasmine.createSpyObj<KeyboardEvent>('KeyboardEvent', ['preventDefault'], {
      key: 'ArrowRight'
    });

    component.handleKeydown(event);

    expect(component.ratingChange.emit).toHaveBeenCalledWith(3.5);
    expect(event.preventDefault).toHaveBeenCalled();
  });

  it('clears the rating when ArrowLeft moves from half a star to zero', () => {
    component.rating = 0.5;
    spyOn(component.ratingChange, 'emit');
    const event = jasmine.createSpyObj<KeyboardEvent>('KeyboardEvent', ['preventDefault'], {
      key: 'ArrowLeft'
    });

    component.handleKeydown(event);

    expect(component.ratingChange.emit).toHaveBeenCalledWith(null);
  });

  it('does not react to unrelated keys', () => {
    component.rating = 4;
    spyOn(component.ratingChange, 'emit');
    const event = jasmine.createSpyObj<KeyboardEvent>('KeyboardEvent', ['preventDefault'], {
      key: 'Tab'
    });

    component.handleKeydown(event);

    expect(component.ratingChange.emit).not.toHaveBeenCalled();
    expect(event.preventDefault).not.toHaveBeenCalled();
  });
});
