import React, { useState } from 'react';
import './stars.scss';

type Props = {
  totalStars: number,
  editable: boolean,
  rating: number,
  setRating: (rating: number) => void,
}

export const StarRating: React.FC<Props> = ({
  totalStars,
  editable,
  rating,
  setRating
}) => {

  const handleClick = (value: number) => {
    if (editable) {
      setRating(value);
    }

    console.log(rating);
  };

  const renderStars = () => {
    const stars = Array.from({ length: totalStars }, (_, index) => {
      const ratingValue = index + 1;
      const isActive = ratingValue <= rating;
      const starClass = isActive ? 'star_active' : 'star';
      return (
        <span
          key={index}
          className={starClass}
          onClick={() => handleClick(ratingValue)}
        >
        </span>
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

export default StarRating;
