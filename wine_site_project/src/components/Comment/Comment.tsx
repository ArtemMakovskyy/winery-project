import './comment.scss';

type Props = {
  avatarImage: string,
  paragraph: JSX.Element,
  name: string
}

export const Comment: React.FC<Props> = ({
  avatarImage,
  paragraph,
  name
}) => {

  return (
    <div className="comment">
      <div className='comment-container'>
        <div className='comment__avatar-container'>
          <img src='/images2/quotes.svg' className='comment-img comment-img-first'/>
          <img src={avatarImage} />
        </div>
        <div className='comment-lower-container'>
          <div className='comment__paragraph-container'>
            {/* <p className='comment__paragraph'>{paragraph}</p> */}
            {paragraph}
            <p className='comment__name'>{name}</p>
          </div>
          <img src='/images2/quotes.svg' className='comment-img comment-img-second'/>
        </div>

      </div>
    </div>
  )
}