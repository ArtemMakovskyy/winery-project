import './review.scss';
import { CommentResponseType, CommentType } from "../../types/Product"
import { StarRatingWithoutEdit } from '../Rating/StarRatingWithoutEdir';

type Props = {
  comment: CommentResponseType
}

export const Review: React.FC<Props> = ({ comment }) => {

  return (
    <div className="review">
      <div className='review-container'>
        <h1 className="review__title">{comment.userFirstName + ' ' + comment.userLastName}</h1>
        <p className='review__paragraph'>{comment.message}</p>
      </div>
      <div className='review__time-container'>
        <p className='review__time'>{comment.reviewDate}</p>

      </div>
      <div className='review__stars-container'>
        <StarRatingWithoutEdit rating={comment.rating} />
      </div>
    </div>
  )
}
