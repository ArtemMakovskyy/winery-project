import { Link, NavLink } from "react-router-dom";
import '../../images/LOGO.svg';
import './Footer.scss';
import classNames from "classnames";
import woodImg from '../../images/wood.svg';


export const Footer = () => {

  const getActiveClass = ({ isActive }: { isActive: boolean }) =>
    classNames('footer__link', {'footer__link_active': isActive });

  return (
    <footer className="footer">
      <div className="footer-logo-container">
        <Link to="/about" className="footer__link">
          <div className="footer-logo" />
        </Link>
        <div className="footer__main-container">
          <div className="footer-container footer-container-first">
            <NavLink to="/" className={getActiveClass}>
              <h1 className="footer__title">
                About
              </h1>
            </NavLink>
            <NavLink to={'/products'} className={getActiveClass}>
              <h1 className="footer__title">
                Wine
              </h1>
            </NavLink>
          </div>
          <img src={woodImg} />
          <div className="footer-container footer-container-second">
            <NavLink to='more' className={getActiveClass}>
              <h1 className="footer__title">
                More
              </h1>
            </NavLink>
            <NavLink to='contact' className={getActiveClass}>
              <h1 className="footer__title">
                Contact
              </h1>
            </NavLink>
          </div>
        </div>
      </div>
      <div className="footer-container footer-container-third">
        <div className="footer__icon-plane"/>
        <div className="footer__icon-heart"/>
      </div>
    </footer>
  )
}
