import '../ProductCard/product.scss';
import wineImg from '../../images/wineImg.svg';
import heartImg from '../../images/heart.svg';
import starImg from '../../images/star.svg';
import plusImg from '../../images/plus.svg';
import { Product } from '../../types/Product';
import { NavLink } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { BASE_URL } from '../../utils/fetchClient';

type ProductType = {
  product: Product;
}

export const FavouriteCard
: React.FC<ProductType>
= ({
  product
}) => {
  const [productWithImage, setProductWithImage] = useState(null);


  const productWineColorFormat = product.wineColor.at(0) + product.wineColor.slice(1).toLowerCase();
  const productWineTypeFormat = product.wineType.at(0) + product.wineType.slice(1).toLowerCase();
  const wineYear = product.vendorCode.slice(3);

  const capitalizeWords = (str: string) => {
    return str.toLowerCase().replace(/(^|\s)\S/g, (char) => char.toUpperCase());
  }

  const productShortName = capitalizeWords(product.shortName);

  return (
    <div className='product__card'>
      <NavLink to={`/products/${product.id}`}>
        <img src={`${BASE_URL}/${product.pictureLink?.slice(4)}`} className='product-img'/>
      </NavLink>
      <div className='product-container'>
        <div className='product__title-container'>
          <h2 className='product__title'>{wineYear + ' ' + productShortName}</h2>
          <div className='product__grade-container'>
            <h3 className='product__grade'>{product.averageRatingScore ? product.averageRatingScore : 0}</h3>

            <img src={starImg} className="product__star"/>
          </div>
        </div>
        <h3 className='product__subtitle'>{productWineColorFormat + ' ' + productWineTypeFormat}</h3>
      </div>
      <div className='product__price-container'>
        <h3 className='product__price'>{`${product.price}UAH`}</h3>
        <div className='product__button-container'>
          <button className='product__button'>
            <img src={heartImg}/>
          </button>
          <button className='product__button'>
            <img src={plusImg}/>
          </button>
        </div>
      </div>
    </div>
  )
}
