import './About.scss';
import '../GeneralStyle/Page.scss';
import '../../components/ContactPage/Contact.scss';
import map from '../../images/map.svg';
import { ProductCard } from '../ProductCard/ProductCard';
import { ProductList } from '../ProductCard/ProductList';
import arrowImg from '../../images/arrow.svg';

export const AboutPage = () => {
  return (
    <section className="about">
      {/* <section className="passionate__section"> */}
        {/* <div className='passionate-img-container'> */}
          {/* <img src={mainImg} className='passionate-img' alt={mainImg}/> */}
        {/* </div> */}
        {/* <div className='passionate-container'>
          <h1 className="passionate__title">PASSIONATE ABOUT FREEDOM<br/> AND WINE</h1>
          <p className="passionate__subtitle">Trubetskoy chateau, occupied since the war's start, suffers <br></br> daily from Russian ammunition. The third war sparks <br></br> change, leading to renaming—Stoic Winery</p>
          <button className="passionate__button">Try it</button>
        </div> */}
      {/* </section> */}
      <section className='story__section'>
        <div className='story-container story-container-first'>
          <h1 className='story__title'>The story</h1>
          <p className='story__paragraph'>The Chateau of Prince Trubetskoy is the only historical chateau in Ukraine in <br></br> the village of Vesele, 73 km from Kherson. This is wine production, 200 <br></br> hectares of its own vineyards, a historical castle and cellars where wine is <br></br> stored, a restaurant and a hotel.</p>
        </div>
        <div className='story-container-additional'>
          <div className='story-container story-container-second'>
            <p className='story__paragraph story__paragraph-second'>The first vineyards were planted by the prince on the recommendation of the <br></br> chief winemaker, Prince Lev Golitsyn. And in the chateau, wine cellars for 180 <br></br> thousand buckets were built and a winery was built.</p>
            <div className='story-img story-img-1' />
          </div>
        </div>
        <div className='story-container story-container-third'>
          <div className='story-img story-img-2' />
          <p className='story__paragraph story__paragraph-third'>At the World Exhibition in Paris, the best wine from the Trubetskoy estate - <br></br> Riesling - received the highest prize - the Grand Prix.</p>
        </div>
        <div className='story-course-container'>
          <video src='/video/pexels.mp4' autoPlay muted loop className='story-course-video' />
          {/* <iframe src={videoFile} frameBorder="0" allowFullScreen></iframe> */}
          {/* <div className='story-course-video-back'/> */}
          <div className='story-course__link-container'>
            <h1 className='story-course__title'>Sommelier course</h1>
            <a>
              <img src={arrowImg} className='story-course-img' />
            </a>
            <p className='story-course__paragraph'>Join our passionate wine community</p>
          </div>
        </div>
        <div className='story-container story-container-fourth'>
          <p className='story__paragraph story__paragraph-fourth'>One of the 8 underground galleries, starting in 1958, preserves a collection of<br></br> more than 10,000 dances, among which there are close <br></br>7,000 wines are rare copies, and a few dances were preserved from the prince’s<br></br> own hours.</p>
          <div className='story-img story-img-3' />
        </div>
        <div className='story-container story-container-fifth'>
          <div className='story-img story-img-4' />
          <p className='story__paragraph story__paragraph-fifth'>The chateau was plundered by the Russian military. But now it is waiting to <br></br> be restored to its former greatness.<br></br><br></br>

In fact, not just in Ukraine, but on the territory of most similar European East <br></br> Countries, there aren`t many other wineries that could rightfully be called a <br></br> chateau (from the French Château) - the name of a small winery. <br></br> representative of the high aristocracy with a park and <br></br> winegrowing dominion.</p>
        </div>
      </section>
      <section className='contact__section'>
        <div>
          <h1 className='contact__title'>Contacts</h1>
          <div>
            <h2 className='contact__caption'>Adress</h2>
            <p className='contact__paragraph'>Vesele, Kherson District, Ukraine, 74344</p>
          </div>
          <div>
            <h2 className='contact__caption'>Phone</h2>
            <p className='contact__paragraph'>+38 067 678 32 88</p>
          </div>
          <div>
            <h2 className='contact__caption'>E-mail</h2>
            <p className='contact__paragraph'>prince.trubettskoy@gmail.com</p>
          </div>
        </div>
        <img src={map} />
      </section>
      <ProductList />
    </section>
  )
}