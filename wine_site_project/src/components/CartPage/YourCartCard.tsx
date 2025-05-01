import { Product } from "../../types/Product";
import './order.scss';
import './your-cart.scss';
import * as cartActions from '../features/cartSlicer';
import * as productActions from '../features/productSlicer';
import { useAppDispatch } from "../../app/hooks";
import plusImg from '../../images/plusImg.svg';
import minusImg from '../../images/minusImg.svg';
import { BASE_URL } from '../../utils/fetchClient';

type Props = {
  product: Product;
}

export const YourCartCard: React.FC<Props> = ({ product }) => {

  const dispatch = useAppDispatch();

  const handleAddProductQuantity = (newProduct: Product) => {
    dispatch(cartActions.increaseQuantity(newProduct.id));
    dispatch(productActions.increaseProductQuantity())
    console.log(product);
  }

  const handleDecreaseProductQuantity = (newProduct: Product) => {
    dispatch(cartActions.decreaseQuantity(newProduct.id));
    dispatch(productActions.decreaseProductQuantity())
  }

  const handleDeleteProduct = (newProduct: Product) => {
    dispatch(cartActions.deleteCartProducts(newProduct.id));
  }

  return (
    <div className="order__card">
      <button
        className="order__card__button-cross"
        onClick={() => handleDeleteProduct(product)}
      >
        <img src="images2/close.svg" />
      </button>
      <div className="order__card-img-container">
        <img src={`${BASE_URL}/${product.pictureLink?.slice(4)}`} className="cart__card-img"/>
        <h1 className="order__card__title">{product.name}</h1>
      </div>
      <div className="order__card__price-container">
        <div className='product-details__button-container product-details__button-container-second'>
        <button
          className='product-details__button product-details__button-minus'
          onClick={() => handleDecreaseProductQuantity(product)}
        >
          <img src={minusImg} />
        </button>
        <h2 className='product-details__number'>{product.quantity}</h2>
        <button
          className='product-details__button product-details__button-plus'
          onClick={() => handleAddProductQuantity(product)}
        >
          <img src={plusImg} />
        </button>
      </div>
        <h1 className="order__card__price">{`${product.price}UAH`}</h1>
      </div>
    </div>
  )
}
