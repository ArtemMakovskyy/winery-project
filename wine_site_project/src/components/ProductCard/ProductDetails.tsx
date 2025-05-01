import './product.scss';
import penImg from '../../images/pen.svg';
import '../../components/GeneralStyle/Page.scss';
import { useEffect, useState } from 'react';
import cn from 'classnames';
import { ProductList } from './ProductList';
import { NavLink, useParams } from 'react-router-dom';
import { ProductCardDetailsAction } from '../buttonActions/ProductCardDetailsAction';
import { useAppDispatch, useAppSelector } from '../../app/hooks';
import * as productsActions from '../features/productSlicer';
import * as commentsActions from '../features/commentSlicer';
import { AddingCommentForm } from '../buttonActions/addingComment';
import { Review } from './Review';
import { StarRatingWithoutEdit } from '../Rating/StarRatingWithoutEdir';
import { AddToCart } from '../CartPage/AddToCart';
import { Loader } from '../Loader/Loader';
import { BASE_URL } from '../../utils/fetchClient';

export const ProductDetails = () => {

  const [toggle, setToggle] = useState(true);

  const [isActiveCart, setIsActiveCart] = useState(false);

  const {productId} = useParams();

  const dispatch = useAppDispatch();

  const currentProduct = useAppSelector(state => state.products.itemDetails);

  const { loaded: loading } = useAppSelector(state => state.products);

  const comments = useAppSelector(state => state.comments.items);

  const noComments = 'There is no reviews. You could be first.';

  const [isActive, setIsActive] = useState(false);

  useEffect(() => {
    dispatch(commentsActions.commentsInit(Number(productId)));

    console.log(currentProduct)

  }, [])



  useEffect(() => {
    dispatch(productsActions.productDetailsInit(Number(productId)));

    console.log(currentProduct);
  }, [productId]);

  const capitalizeDescription = (caption: string) => {
    return caption.charAt(0).toUpperCase() + caption.slice(1);
  }

  const color = currentProduct?.colorDescribing ? capitalizeDescription(currentProduct.colorDescribing) : '';
  const taste = currentProduct?.taste ? capitalizeDescription(currentProduct.taste) : '';
  const aroma = currentProduct?.aroma ? capitalizeDescription(currentProduct.aroma) : '';
  const gastronomy = currentProduct?.gastronomy ? capitalizeDescription(currentProduct.gastronomy) : '';

  const vendorCode = currentProduct?.vendorCode.slice(0, 3) + ' ' + currentProduct?.vendorCode.slice(3);

  const handleAddReview = () => {
    setIsActive(!isActive);
  }

  return (
    <>
    {isActive &&
      <AddingCommentForm
        isActive={isActive}
        setIsActive={setIsActive}
      />
    }
    {isActiveCart &&
    <AddToCart
      setIsActive={setIsActiveCart}
    />}
    {loading ? <Loader /> :
     <div className='product-details'>
      <div className="product-details-container">
        <ul className='product-details__list'>
          <li className='product-details__item product-details__item-first'>
            <span className='product-details__span'>Stoic Winery</span>
          </li>
          <li className='product-details__item'>
            <span className='product-details__span'>
              <NavLink to='/products' className="product-details__link">
                Wine
              </NavLink>
            </span>
          </li>
          <li className='product-details__item'>
            <span className='product-details__span'>{currentProduct?.name}</span>
          </li>
        </ul>
        <div className='product-details__title-container'>
          <h1 className='product-details__title'>{currentProduct?.name}</h1>
          <h2 className='product-details__mrd'>{vendorCode}</h2>
        </div>
        <h2 className='product-details__edition'>{currentProduct?.reserveType !== null ? currentProduct?.reserveType : ' '}</h2>
        <div className='product-details-img-container'>
          <img src={`${BASE_URL}/${currentProduct?.pictureLink?.slice(4)}`} className='product-details-img' />
          <div>
            <div className='product-details__grade__main-container'>
              <div>
                <div className='product-details__grade-container'>
                  <h2 className='product-details__grade'>
                    {currentProduct?.averageRatingScore
                     ?
                  currentProduct?.averageRatingScore : 0
                    }
                  </h2>
                  <div className='product-details__star-container'>
                    <StarRatingWithoutEdit
                      rating={currentProduct?.averageRatingScore}
                    />
                  </div>
                </div>
                <div className='product-details__review-container'>
                  <h2 className='product-details__grade'>{comments.length}</h2>
                  <button
                    className='product-details__button-add-review'
                    onClick={handleAddReview}
                  >
                    <img src={penImg} className='product-details-pen' />
                    <h2 className='product-details__review'>add review</h2>
                  </button>
                </div>
              </div>
              <div>
                <h1 className='product-details__price'>{`${currentProduct?.price}UAH`}</h1>
              </div>
            </div>
            <div className='product-details__keys-container'>
              <div className='product-details__key-container'>
                <h1 className='product-details__key'>Grape:</h1>
                <h2 className='product-details__value'>{currentProduct?.grape}</h2>
              </div>
              <div className='product-details__key-container'>
                <h1 className='product-details__key'>Decantation:</h1>
                <h2 className='product-details__value'>{currentProduct?.isDecantation ? 'Yes' : 'No'}</h2>
              </div>
              <div className='product-details__key-container'>
                <h1 className='product-details__key'>Type:</h1>
                <h2 className='product-details__value'>{currentProduct?.wineType}</h2>
              </div>
              <div className='product-details__key-container'>
                <h1 className='product-details__key'>Strength:</h1>
                <h2 className='product-details__value'>{`${currentProduct?.strengthFrom}% - ${currentProduct?.strengthTo}%`}</h2>
              </div>
              <div className='product-details__key-container'>
                <h1 className='product-details__key'>Colour:</h1>
                <h2 className='product-details__value'>{color}</h2>
              </div>
              <div className='product-details__key-container'>
                <h1 className='product-details__key'>Taste:</h1>
                <h2 className='product-details__value'>{taste}</h2>
              </div>
              <div className='product-details__key-container'>
                <h1 className='product-details__key'>Aroma:</h1>
                <h2 className='product-details__value'>{aroma}</h2>
              </div>
              <div className='product-details__key-container'>
                <h1 className='product-details__key'>Gastronomy:</h1>
                <h2 className='product-details__value'>{gastronomy}</h2>
              </div>
              <ProductCardDetailsAction
                product={currentProduct}
                setIsActive={setIsActiveCart}
              />
            </div>
          </div>
        </div>
        <div className='product-details__menu'>
          <h1
            className={cn('product-details__menu__title',
              { 'product-details__menu__title-active': toggle })}
            onClick={() => setToggle(true)}
          >
            Description
          </h1>
          <h1
            className={cn('product-details__menu__title',
              { 'product-details__menu__title-active': !toggle })}
            onClick={() => setToggle(false)}
          >
            Reviews
          </h1>
        </div>
        <div className='product-details__menu__paragraph'>
          {toggle && <p>{currentProduct?.description}</p>}
          {!toggle && comments.length > 0 && comments.map((comment) => (
            <Review key={comment.id} comment={comment} />
          ))}
          {!toggle && comments.length === 0 && noComments}
        </div>
      </div>
    </div>}
    <div className='product-details-container-second'>
      <h1 className='product-details__title_ad'>You might also like</h1>
      <div className='product-details__slider'>
        <ProductList />
      </div>
    </div>
    </>
  )
}
