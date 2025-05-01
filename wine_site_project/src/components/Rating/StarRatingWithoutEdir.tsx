import React from 'react';
import './stars.scss';

type Props = {
  rating?: number;
};

export const StarRatingWithoutEdit: React.FC<Props> = ({ rating = 0 }) => {
  const renderStars = () => {
    const stars = Array.from({ length: 5 }, (_, index) => {
      const ratingValue = index + 1;
      const isActive = ratingValue <= (rating || 0);
      const starClass = isActive ? 'star_active' : 'star';
      return (
        <span key={index} className={starClass}></span>
      );
    });

    return stars;
  };

  return (
    <div className='stars'>
      {renderStars()}
    </div>
  );
};
