import { useAppSelector } from "../../app/hooks"
import { YourCartCard } from "./YourCartCard";
import '../buttonActions/commentForm.scss';
import { NavLink } from "react-router-dom";
import '../GeneralStyle/Page.scss';
import { ProductList } from "../ProductCard/ProductList";

type Props = {
  setIsActive: (isActive: boolean) => void;
}

export const AddToCart: React.FC<Props> = ({
  setIsActive
}) => {

    const cartProducts = useAppSelector(state => state.cart.items);

    const totalAmount = cartProducts.reduce((acc, product) => (
        acc + product.quantity
    ), 0);

    const totalPrice = cartProducts.reduce((acc, product) => (
      acc + (product.price * product.quantity)
    ), 0);

    const handleSetActive = () => {
      setIsActive(false);
    }

    return (
      <div className="your-cart" style={{ overflowY: 'auto' }}>
        <div className="your-cart__main-container">
        <div>
          <h1 className="your-cart__title">Your cart</h1>
          <div className="your-cart-container">
            {cartProducts.map((product) => (
              <YourCartCard key={product.id} product={product}/>
            ))}
          </div>
        </div>
        <div className="order__total-container">
          <h1 className="order__total">Total</h1>
          <h1 className="order__card__amount">{totalAmount}</h1>
          <h1 className="order__card__price">{`${totalPrice}UAH`}</h1>
        </div>
        <div className="your-cart__button-container">
          <a onClick={handleSetActive} className='your-cart__button-back' >Back Shopping</a>
          <NavLink className='page__button your-cart__button' to='/cart' >Go to checkout</NavLink>
        </div>
        <div>
          <h1>You might also like</h1>
          <div className="your-cart__slider">
            <ProductList setIsActive={setIsActive} />
          </div>
        </div>
        </div>
      </div>
    )
  }