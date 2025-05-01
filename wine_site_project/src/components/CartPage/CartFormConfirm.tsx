import { useState } from "react"
import { Product } from "../../types/Product"
import { CartList } from "./CartList"

type Props = {
  cartProducts: Product[],
  productPrice: number,
  handleCreateOrder: () => void,
  isError: boolean,
  error: string,
  handleErrorClose: () => void
}

export const CartFromConfirm: React.FC<Props> = ({
  cartProducts,
  productPrice,handleCreateOrder,
  isError,
  error,
  handleErrorClose
}) => {
  const [isChecked, setIsChecked] = useState(false);

  const handleCheckboxChange = () => {
    setIsChecked(!isChecked);
  };

  return (
    <div className="cart-container cart-container-second">
        <div className='cart__products-container'>
          <h2 className="cart__subtitle">Your order</h2>
          <CartList products={cartProducts} />
        </div>
        <div className='cart__total-container'>
          <h3 className='cart__total'>Total</h3>
          <h1>{`${productPrice} UAH`}</h1>
        </div>
        <div className='cart__radio-container'>
          <label className='cart__radio__label'>
            <input
              type="checkbox"
              className='cart__radio'
              checked={isChecked}
              onChange={handleCheckboxChange}
            ></input>
            This is order is a Gift
          </label>
          <button
            className="page__button"
            onClick={handleCreateOrder}
          >
            Confirm order
          </button>
        </div>
        {isError &&
        <div className='error'>
          <button
            className='error__button'
            onClick={handleErrorClose}
          >
            <img src='images2/cross.svg' />
          </button>
          <h1 className='error__title'>{error}</h1>
        </div>
        }
    </div>
  )
}
