import React from 'react';
import { Product } from "../../types/Product";
import plusImg from '../../images/plusImg.svg';
import minusImg from '../../images/minusImg.svg';
import { useAppDispatch } from "../../app/hooks";
import * as cartActions from '../features/cartSlicer';
import { BASE_URL } from '../../utils/fetchClient';

type Props = {
  product: Product;
}

export const CartCard: React.FC<Props> = React.memo(({ product }) => {
  const dispatch = useAppDispatch();

  const handleAddCartProduct = () => {
    dispatch(cartActions.deleteCartProducts(product.id));
  }

  const handleAddProductQuantity = () => {
    dispatch(cartActions.increaseQuantity(product.id));
  }

  const handleDeleteProductQuantity = () => {
    dispatch(cartActions.decreaseQuantity(product.id));
  }

  return (
    <div className="cart__card">
      <div className="cart__card-img-container">
        <img src={`${BASE_URL}/${product.pictureLink?.slice(4)}`} className="cart__card-img" alt={product.name} />
        <h1 className="cart__card__title">{product.name}</h1>
      </div>
      <div className='cart__card__button-container'>
        <button
          className='cart__card__button cart__card__button-minus'
          type='button'
          onClick={handleDeleteProductQuantity}
        >
          <img src={minusImg} alt="Minus" />
        </button>
        <h2 className='cart__card__number'>{product.quantity}</h2>
        <button
          className='cart__card__button cart__card__button-plus'
          type='button'
          onClick={handleAddProductQuantity}
        >
          <img src={plusImg} alt="Plus" />
        </button>
      </div>
      <div>
        <h2 className="cart__card__price">
          {`${product.price} UAH`}
        </h2>
      </div>
      <button
        onClick={handleAddCartProduct}
        className="cart__card__button__cross"
      >
        <img src="/images2/cross.svg" alt="Cross" />
      </button>
    </div>
  );
});
