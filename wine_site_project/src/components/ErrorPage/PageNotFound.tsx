import { NavLink } from "react-router-dom";
import './errorPage.scss';
import '../GeneralStyle/Page.scss'

export const PageNotFound = () => {
  return (
    <div className="founded">
      <img src="images2/404.svg" />
      <div className="founded-container">
        <h1 className="founded__title">
          The page is not found
        </h1>
        <h2 className="founded__subtitle">
          Let`s try with the Home Page again!
        </h2>
        <NavLink to='/about' className="page__button founded__button">
          <img src="/images2/home.png" />
          Home page
        </NavLink>
      </div>
    </div>
  )
}