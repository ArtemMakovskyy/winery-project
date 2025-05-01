import classNames from 'classnames';
import { useAppDispatch, useAppSelector } from '../../app/hooks';
import heartImg from '../../images/heart.svg';
import plusImg from '../../images/plus.svg';
import { Product } from '../../types/Product';
import '../ProductCard/product.scss';
import * as favouriteActions from '../features/favouriteSlicer';
import * as cartActions from '../features/cartSlicer';

type Props = {
  product: Product;
}

export const ProductCartAction: React.FC<Props> = ({ product }) => {
  const dispatch = useAppDispatch();

  const favouriteProducts = useAppSelector(state => state.favourite.items);
  const cartProducts = useAppSelector(state => state.cart.items);

  const isFavourite = favouriteProducts.some(
    (favProduct) => favProduct.id === product.id,
  );

  const isCartProduct = cartProducts.some(
    (cartProduct) => cartProduct.id === product.id
  );

  const handleAddFavourite = (newProduct: Product) => {
    if (isFavourite) {
      dispatch(favouriteActions.deleteFavouritesProducts(newProduct.id));
    } else {
      dispatch(favouriteActions.setFavouritesProducts(newProduct));
    }
  };

  const handleAddCartProduct = (newProduct: Product) => {
    if (isCartProduct) {
      dispatch(cartActions.deleteCartProducts(newProduct.id));
    } else {
      dispatch(cartActions.setCartProducts(newProduct));
    }
  }

  return (
    <div className='product__button-container'>
      <button
        className={
          classNames('product__button-heart',
          {'product__button-heart_active': isFavourite })}
        onClick={() => handleAddFavourite(product)}
      >
        {/* <img src={heartImg}/> */}
        <div />
      </button>
      <button
        className={
          classNames('product__button-adding',
          {'product__button-adding_active': isCartProduct})
        }
        onClick={() => handleAddCartProduct(product)}
      >
        {/* <img src={plusImg}/> */}
      </button>
    </div>
  )
}