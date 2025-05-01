import './cart.scss';
import '../../components/GeneralStyle/Page.scss';
import { useAppDispatch, useAppSelector } from '../../app/hooks';
import { CartCard } from './CartCard';
import { FormEvent, useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import * as actions from '../features/cartSlicer';
import { NewOrder } from './NewOrder';
import { OrderError } from '../../Enums/OrderError';
import { CartForm } from './CartForm';
import { CartList } from './CartList';
import { CartFromConfirm } from './CartFormConfirm';

export const CartPage = () => {

  const dispatch = useAppDispatch();

  const cartProducts = useAppSelector(state => state.cart.items);

  const cartItemProducts = useAppSelector(state => state.cart.itemsCart);

  const productPrice = cartProducts.reduce(
    (total, item) => total + (item.quantity * item.price), 0,
  );

  const orders = useAppSelector(state => state.cart.orderItems);
  const selectedOrder = useAppSelector(state => state.cart.orderSeletedItem);

  const [error, setError] = useState('');
  const [isError, setIsError] = useState(false);

  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [zipCode, setZipCode] = useState('');
  const [region, setRegion] = useState('');
  const [city, setCity] = useState('');
  const [street, setStreet] = useState('');
  const [comment, setComment] = useState('');
  const [isActive, setIsActive] = useState(false);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
  };

  const handleCreateOrder = async () => {
    const purchaseObjects = cartItemProducts.map(product => ({
      ...product,
      wineId: product.wineId,
      quantity: product.quantity
    }));

    const newOrder = {
      userFirstAndLastName: name,
      email,
      phoneNumber: phone,
      createShoppingCardDto: {
        purchaseObjects: purchaseObjects
      },
      createOrderDeliveryInformationDto: {
        zipCode: zipCode,
        region: region,
        city: city,
        street: street,
        comment: comment
      }
    }

    try {
      if (
        name.trim() &&
        email.trim() &&
        phone.trim() &&
        zipCode.trim() &&
        region.trim() &&
        city.trim() &&
        street.trim() &&
        comment.trim()
      ) {
        const response: any = await dispatch(actions.cartOrdersInit(newOrder));

        if (response.payload.status === 400) {
          setError(OrderError.WrongData);
          setIsError(true);
        } else {
          window.scrollTo({
            top: 0,
            behavior: "smooth"
          });
          setIsActive(true);
        }
      } else {
        setError("Please fill out all required fields.");
      }
    } catch (error) {
      setError(OrderError.WrongData);
      setIsError(true);
    }
  }

  const handleErrorClose = () => {
    setIsError(false);
  }

  useEffect(() => {
    dispatch(actions.cartOrdersGetInit());
    dispatch(actions.cartSelectedOrderInit(orders.length));

    console.log('Количество заказов:', orders);
    console.log('Последний заказ', selectedOrder);
  }, [])

  return (
  <>
    {cartProducts.length ?
      <form
        onSubmit={handleSubmit}
        className="cart form"
      >
      <CartForm
        name={name}
        phone={phone}
        email={email}
        zipCode={zipCode}
        region={region}
        city={city}
        street={street}
        comment={comment}
        setName={setName}
        setPhone={setPhone}
        setEmail={setEmail}
        setZipCode={setZipCode}
        setRegion={setRegion}
        setCity={setCity}
        setStreet={setStreet}
        setComment={setComment}
      />
      <CartFromConfirm
        cartProducts={cartProducts}
        productPrice={productPrice}
        handleCreateOrder={handleCreateOrder}
        isError={isError}
        error={error}
        handleErrorClose={handleErrorClose}
      />
    </form>
    :
    <div className='cart-empty'>
      <div className='cart-empty-container'>
        <img src='images2/cart-empty.png' className='cart-empty-img' />
        <div className='cart-empty__title-container'>
          <h1 className='cart-empty__title'>Your cart is empty</h1>
          <h2 className='cart-empty__subtitle'>Let`s try with the Home Page again!</h2>
          <NavLink to="/products" className='cart-empty__button'>
            <img src='images2/cart-img.svg' />Shop now</NavLink>
        </div>
      </div>
    </div>
    }
    {isActive && <NewOrder isActive={isActive} setIsActive={setIsActive}/>}
  </>
  )
}
