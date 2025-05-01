import './more.scss';
import '../../components/GeneralStyle/Page.scss';
import moreImg1 from '../../images/more-img-1.svg';
import moreImg2 from '../../images/more-img-2.svg';
import { Comment } from '../Comment/Comment';


export const MorePage = () => {

  const avatarImage1 = '/images2/Face1.svg';
  const avatarImage2 = '/images2/Face2.svg';
  const commentParagraph1 = <p className='comment__paragraph'>A sensory journey through stunning vineyards and exquisite wines. From the warm hospitality to the enchanting ambiance, it's an experience that leaves a lasting impression</p>;
  const commentParagraph2 = <p className='comment__paragraph'>Enriching experience, providing deep insights into the world of wine. I gained the knowledge and confidence to navigate the complexities of wine with finesse and expertise</p>

  return (
    <div className='more'>
      <section className='event__section'>
        <div className='event__main-container'>
          <div className='event-container event-container-first'>
            <h1 className='event__title'>Our events</h1>
            <h2 className='event__caption'>We have various tours, tastings and excursions,that <br></br> will suit all kind of guests.</h2>
            <p className='event__paragraph'>Begin your tasting journey at Stoic Winery, nestled between Black Sea and <br></br> South Steppes. We know our wines and love to share our passion for them with <br></br> our guests. We invite you to come and taste our bold, award-winning lineup <br></br> for yourself at our tasting room.</p>
          </div>
          <img src={moreImg1} className='event-img event-img-first'/>
        </div>
        <div className='event__main-container event__main-container-second'>
          <img src={moreImg2} className='event-img event-img-second'/>
          <div className='event-container event-container-second'>
            <h1 className='event__title'>Canvas & Corks</h1>
            <h2 className='event__caption'>Immersive Artistic Experiences amidst the Vines - <br></br> Painting Tours in the Winery.</h2>
            <p className='event__paragraph'>Led by experienced local artists, our tours offer participants the opportunity to <br></br> unleash their inner creativity while savoring the finest wines our vineyard has <br></br> to offer. Whether you're a seasoned painter or a novice seeking inspiration, our <br></br> guided sessions cater to all skill levels, encouraging exploration and self-<br></br>expression in a relaxed and supportive environment.</p>
          </div>
        </div>
        <div className='event-container event-container-third'>
          <h1 className='event__title'>Do not forget to book a visit in advance</h1>
          <button className='page__button event__button'>Book</button>
          <h2 className='event__paragraph'>Some tours can only be booked for groups,and some tours can be joined daily.</h2>
        </div>
      </section>
      <section className='mastering__section'>
        <div className='mastering__main-container'>
          <div className='mastering-container'>
            <div>
              <h1 className='mastering__title'>
                Mastering the Art of Wine
              </h1>
              <h2 className='mastering__subtitle'>
                Unlock the Secrets of the Grape
              </h2>
              <p className='mastering__paragraph'>
                Led by experienced local artists, our tours offer participants the opportunity to unleash their <br></br> inner creativity while savoring the finest wines our vineyard has to offer. Whether you're a <br></br> seasoned painter or a novice seeking inspiration, our guided sessions cater to all skill levels, <br></br> encouraging exploration and self-expression in a relaxed and supportive environment.
              </p>
              <button className='page__button mastering__button'>
                become a somelier
              </button>
            </div>
          </div>
          <div className='mastering-img'/>
        </div>
        <div className='mastering-comment-container'>
          <Comment
            avatarImage={avatarImage1}
            paragraph={commentParagraph1}
            name='Kate Caught'
          />
          <Comment
            avatarImage={avatarImage2}
            paragraph={commentParagraph2}
            name='Tim Miles'
          />
        </div>
      </section>
    </div>
  )
}
