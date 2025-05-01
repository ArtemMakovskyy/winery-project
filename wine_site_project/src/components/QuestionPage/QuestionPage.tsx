import { NavLink } from "react-router-dom"
import './question.scss';

export const QuestionPage = () => {
  return (
    <div className="question">
       <iframe src="https://player.vimeo.com/video/797802998?autoplay=1&controls=0&loop=1&title=0&byline=0&portrait=0&muted=1" className="question__video"
        frameBorder="0"
        allow="autoplay; fullscreen"
        allowFullScreen
      ></iframe>
      <div className="question-back" />

      <div className="question-container">
        <h1 className="question__title ">Are you 18?</h1>
        <p className="question__paragraph">
          You can use the web-site only if you are already an Adult
        </p>
        <div className="question__button-container">
          <NavLink
            className='question__button question__button-accept'
            to='/about'
          >
            yes, i`m already 18
          </NavLink>
        </div>
      </div>
    </div>
  )
}
