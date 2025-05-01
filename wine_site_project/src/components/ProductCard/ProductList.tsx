import { useEffect, useState } from "react";
import { useAppDispatch, useAppSelector } from "../../app/hooks";
import { ProductCard } from "./ProductCard";
import './product.scss';
import * as actions from '../features/productSlicer';
import buttonImg from '../../images/button-arrow.svg';
import { Product } from "../../types/Product";

type Props = {
  setIsActive?: (isActive: boolean) => void;
}

export const ProductList: React.FC<Props> = ({
  setIsActive
}) => {
  const dispatch = useAppDispatch();
  const distance = 296 + 32;
  const { items: products } = useAppSelector(state => state.products);
  const initialPosition = (products.length * distance / 2) - (distance * 2);
  const endPosition = (products.length * (distance) / 2) - (distance) * (products.length - 1);


  useEffect(() => {
    dispatch(actions.productsInit());
  }, []);

  useEffect(() => {
    setPosition((products.length * distance / 2) - (distance * 2));
  }, [products]);

  const [position, setPosition] = useState((products.length * distance / 2) - (distance * 2));

  const nextButtonHandle = () => {

    if (position === endPosition + 328) {
      setPosition(initialPosition);
    } else {
      setPosition((prevPosition) => prevPosition - distance);
    }

    console.log(position)

  }

  const prevButtonHandle = () => {

    if (position === initialPosition) {
      setPosition(endPosition)
    }

    setPosition((prevPosition) => prevPosition + distance)
  }

  return (
    <div className="product__list-container">
      <button
        className="
        product__button
        product__button-arrow
        product__button-arrow-left
        "
        onClick={prevButtonHandle}
      >
        <img src={buttonImg}/>
      </button>
      <div className="product__list">
        {products.map((product: Product) => (
          <li
            key={product.id}
            style={{
              listStyle: 'none',
              transform: `translateX(${position}px)`,
              transition: '0.5s',
            }}
          >
            <ProductCard
              product={product}
              setIsActive={setIsActive}
            />
          </li>
        ))}
      </div>
      <button
        className="product__button product__button-arrow"
        onClick={nextButtonHandle}
      >
        <img src={buttonImg}/>
      </button>
    </div>
  )
}
