type Props = {
  name: string,
  phone: string,
  email: string,
  zipCode: string,
  region: string,
  city: string,
  street: string,
  comment: string,
  setName: (name: string) => void;
  setPhone: (name: string) => void;
  setEmail: (name: string) => void;
  setZipCode: (name: string) => void;
  setRegion: (name: string) => void;
  setCity: (name: string) => void;
  setStreet: (name: string) => void;
  setComment: (name: string) => void;

}

export const CartForm: React.FC<Props> = ({
  name,
  phone,
  email,
  zipCode,
  region,
  city,
  street,
  comment,
  setName,
  setPhone,
  setEmail,
  setZipCode,
  setRegion,
  setCity,
  setStreet,
  setComment
}) => {

    const handleNameUpdate = (event: React.ChangeEvent<HTMLInputElement>) => {
    setName(event.target.value);
  }

  const handlePhoneUpdate = (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    const regex = /^[0-9+]*$/;

    if (regex.test(value)) {
      setPhone(value);
    }
  }

  const handleEmailUpdate = (event: React.ChangeEvent<HTMLInputElement>) => {
    setEmail(event.target.value);
  }

  const handleZipCodeUpdate = (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;

    if (!isNaN(value as any)) {
      setZipCode(value);
    }
  }
  const handleRegionUpdate = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRegion(event.target.value);
  }

  const handleCityUpdate = (event: React.ChangeEvent<HTMLInputElement>) => {
    setCity(event.target.value);
  }

  const handleStreetUpdate = (event: React.ChangeEvent<HTMLInputElement>) => {
    setStreet(event.target.value);
  }

  const handleCommentUpdate = (event: React.ChangeEvent<HTMLInputElement>) => {
    setComment(event.target.value);
  }

  return (
    <div className="cart-container">
        <h1 className="cart__title">Checkout</h1>
        <h2 className="cart__subtitle">Personal data</h2>
        <div className='cart__inputs-container'>
          <div className='cart__input-container'>
            <label className="cart__label">Enter Name of the Recepient</label>
            <input
              type='text'
              className="cart__input"
              placeholder="Name Surname"
              value={name}
              onChange={handleNameUpdate}
              required
            ></input>
          </div>
          <div className='cart__input-container'>
            <label className="cart__label">Enter Telephone Number</label>
            <input
              type='tel'
              className="cart__input"
              placeholder="+38 000 000 00 00"
              maxLength={13}
              value={phone}
              onChange={handlePhoneUpdate}
              required
            ></input>
          </div>
          <div className='cart__input-container'>
            <label className="cart__label">Email</label>
            <input
              type='email'
              className="cart__input"
              placeholder="name.surname@email.com"
              value={email}
              onChange={handleEmailUpdate}
              required
            ></input>
          </div>
        </div>
        <h2 className="cart__subtitle">Shipping adress</h2>
        <div className='cart__inputs-container'>
          <div className='cart__input-container'>
            <label className="cart__label">Zip Code</label>
            <input
              type='text'
              className="cart__input"
              placeholder="00000"
              value={zipCode}
              onChange={handleZipCodeUpdate}
              maxLength={5}
              required
            ></input>
          </div>
          <div className='cart__input-container'>
            <label className="cart__label">Region</label>
            <input
              className="cart__input"
              placeholder="Kyiv region"
              value={region}
              onChange={handleRegionUpdate}
              required
            ></input>
          </div>
          <div className='cart__input-container'>
            <label className="cart__label">City</label>
            <input
              className="cart__input"
              placeholder="Kyiv"
              value={city}
              onChange={handleCityUpdate}
              required
            ></input>
          </div>
          <div className='cart__input-container'>
            <label className="cart__label">Street Adress</label>
            <input
              className="cart__input"
              placeholder="Lobanovskogo str, 13/1, ap 16"
              value={street}
              onChange={handleStreetUpdate}
              required
            ></input>
          </div>
          <div className='cart__input-container'>
            <label className="cart__label">Comment</label>
            <input
              className="cart__input"
              placeholder="Please, provide any additional info"
              value={comment}
              onChange={handleCommentUpdate}
              required
            ></input>
          </div>
        </div>
        <div>

      </div>
      </div>
  )
}