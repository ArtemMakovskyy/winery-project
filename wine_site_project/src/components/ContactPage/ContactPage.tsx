import backImg from '../../images/back-contact-img.svg';
import './Contact.scss';
import '../GeneralStyle/Page.scss';
import map from '../../images/map.svg';

export const ContactPage = () => {
  return (
    <main className="contact page">
      {/* <section className='contact__header__section'>
        <img src={backImg} className='contact-img' alt={backImg}/>
        <div className='page-container contact-container'>
          <h1 className='page__title'>8 wine cellar galleries <br></br>10,000 bottles of wine </h1>
          <p className='page__subtitle contact__subtitle'>Stored and aged, including about 7,000 bottles of rare wines <br></br> bottled in the last century.</p>
        </div>
      </section> */}
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
    </main>
  )
}
