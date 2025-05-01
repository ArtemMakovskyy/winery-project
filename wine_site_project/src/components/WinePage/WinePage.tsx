import { useAppDispatch, useAppSelector } from "../../app/hooks";
import { DropDown, OptionsType } from "../DropDown/DropDown";
import { ProductCard } from "../ProductCard/ProductCard"
import './wine.scss';
import arrowImg from '../../images/arrow.svg';
import '../AboutPage/About.scss';
import courseImg from '../../images/tasting-room.svg';
import { useEffect, useState } from "react";
import * as actions from '../features/productSlicer';
import { Product } from "../../types/Product";
import { SortTypeAge, SortTypeByType, SortTypeColor, SortTypeTaste } from "../../Enums/SortType";
import { useSearchParams } from "react-router-dom";
import { UpperLoader } from "../Loader/UpperLoader";

const colors = [
  { text: 'No sorting' },
  { text: 'RED' },
  { text: 'WHITE' },
];

const types = [
  { text: 'No sorting' },
  { text: 'DRY' },
  { text: 'SEMI-DRY' },
];

const aging = [
  { text: 'No sorting' },
  { text: '2020', name: 'MRD2020' },
  { text: '2019', name: 'MRD2019' },
  {  text: '2018', name: 'MRD2018' },
  {  text: '2017', name: 'MRD2017' },
  {  text: '2016', name: 'MRD2016' },
];

const tastes = [
  { text: 'No sorting' },
  { text: 'asian food' },
  { text: 'sea food' },
  {  text: 'red meat' },
  {  text: 'fish' },
  {  text: 'fruits' },
  {  text: 'deserts' },
];

export const WinePage = () => {

  const [searchParams, setSearchParams] = useSearchParams();
  const dispatch = useAppDispatch();
  const { items: products, loaded: loading } = useAppSelector(state => state.products);

  const colorParams = searchParams.get('color') || 'No sorting';
  const typeParams = searchParams.get('type') || 'No sorting';
  const ageParams = searchParams.get('age') || 'No sorting';
  const [color, setColor] = useState<string>(colorParams);
  const [type, setType] = useState<string>(typeParams);
  const [age, setAge] = useState<string>(ageParams);
  const [taste, setTaste] = useState<string>(SortTypeTaste.NoSorting);

  const handleColorChange = (selectedColor: OptionsType) => {
    const updatedParams = new URLSearchParams(searchParams.toString());
    updatedParams.set('color', selectedColor.text);
    setSearchParams(updatedParams);
    setColor(selectedColor.text);
  }

  const handleTypeChange = (selectedType: OptionsType) => {
    const updatedParams = new URLSearchParams(searchParams.toString());
    updatedParams.set('type', selectedType.text);
    setSearchParams(updatedParams);
    setType(selectedType.text);
  }

  const handleAgeChange = (selectedAge: OptionsType) => {
    const updatedParams = new URLSearchParams(searchParams.toString());
    updatedParams.set('age', selectedAge.text);
    setSearchParams(updatedParams);
    setAge(selectedAge.text);
  }

  const handleTasteChange = (selectedTaste: OptionsType) => {
    const updatedParams = new URLSearchParams(searchParams.toString());
    updatedParams.set('age', selectedTaste.text);
    setSearchParams(updatedParams);
    setTaste(selectedTaste.text);
  }

  useEffect(() => {
    dispatch(actions.productsInit());

  }, []);

    const filterProducts = (
      currentSortByColor: string | null,
      currentSortByType: string | null,
      currentSortByAge: string | null,
      currentSortByTaste: string | null,
      productsArray: Product[]
    ) => {
      let filteredProducts = [...productsArray];

      filteredProducts = filteredProducts.filter(product => {
        switch(currentSortByColor) {
          case SortTypeColor.NoSorting:
            return true;
          case SortTypeColor.Red:
            return product.wineColor === SortTypeColor.Red;
          case SortTypeColor.White:
            return product.wineColor === SortTypeColor.White;
        }
      })

      filteredProducts = filteredProducts.filter(product => {
        switch(currentSortByType) {
          case SortTypeByType.NoSorting:
            return true;
          case SortTypeByType.Dry:
            return product.wineType === SortTypeByType.Dry;
          case SortTypeByType.SemiDry:
            return product.wineType === SortTypeByType.SemiDry;
        }
      })

      filteredProducts = filteredProducts.filter(product => {
        switch(currentSortByAge) {
          case SortTypeAge.NoSorting:
            return true;
          case SortTypeAge.SortBy2016:
            return String(product.year) === SortTypeAge.SortBy2016;
          case SortTypeAge.SortBy2017:
            return String(product.year) === SortTypeAge.SortBy2017;
          case SortTypeAge.SortBy2018:
            return String(product.year) === SortTypeAge.SortBy2018;
          case SortTypeAge.SortBy2019:
            return String(product.year) === SortTypeAge.SortBy2019;
          case SortTypeAge.SortBy2020:
            return String(product.year) === SortTypeAge.SortBy2020;
        }
      });

      filteredProducts = filteredProducts.filter(product => {
        switch(currentSortByTaste) {
          case SortTypeByType.NoSorting:
            return true;
          case SortTypeTaste.AsianFood:
            return product.tasteWine === SortTypeTaste.AsianFood;
          case SortTypeTaste.Deserts:
            return product.tasteWine === SortTypeTaste.Deserts;
          case SortTypeTaste.Fish:
            return product.tasteWine === SortTypeTaste.Fish;
          case SortTypeTaste.Fruits:
            return product.tasteWine === SortTypeTaste.Fruits;
          case SortTypeTaste.RedMeat:
            return product.tasteWine === SortTypeTaste.RedMeat;
          case SortTypeTaste.SeaFood:
            return product.tasteWine === SortTypeTaste.SeaFood;
        }
      })



      return filteredProducts;
    }

  const sortedProducts = filterProducts(
    color,
    type,
    age,
    taste,
    products
  )

  const isSortingEnabled = () => {
    return color !== 'No sorting' || type !== 'No sorting' || age !== 'No sorting' || taste !== SortTypeTaste.NoSorting;
  }

  return (
    <div className="wine">
      <h1 className="wine__title">EXPLORE OUR COLLECTION</h1>
      <p className="wine__paragraph">We produce premium, small-lot Wines made with 100% estate-grown Grapes.</p>
      <div className="wine__main-container">
        <div className="wine__dropdown-container">
          <DropDown
            name="color"
            options={colors}
            selectedOption={color}
            setSelectedOption={handleColorChange}
          />
          <DropDown
            name="type"
            options={types}
            selectedOption={type}
            setSelectedOption={handleTypeChange}
          />
          <DropDown
            name="aging"
            options={aging}
            selectedOption={age}
            setSelectedOption={handleAgeChange}
          />
          <DropDown
            name="to taste with"
            options={tastes}
            selectedOption={taste}
            setSelectedOption={handleTasteChange}
          />
        </div>
        {!isSortingEnabled() ? (
        <div>
          {/* <UpperLoader /> */}
          {loading && <UpperLoader />}
          {!loading && <div className="wine-container">
            {sortedProducts.slice(0, 8).map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
        </div>}
        <div className='story-course-container wine-course-container'>
          <img src='/images2/tastingRoom.jpg' className='wine-course-img' alt={courseImg}/>
          <div className='story-course__link-container'>
            <h1 className='story-course__title'>Tasting room</h1>
            <a>
              <img src={arrowImg} className='story-course-img' />
            </a>
            <p className='story-course__paragraph'>Experience our Wines</p>
          </div>
        </div>
        {!loading &&
        <div className="wine-container wine-container-second">
          {sortedProducts.slice(8).map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>}
        <div className='story-course-container'>
          <video src='/video/pexels.mp4' autoPlay muted loop className='story-course-video' />
          <div className='story-course__link-container'>
            <h1 className='story-course__title'>Sommelier course</h1>
            <a>
              <img src={arrowImg} className='story-course-img' />
            </a>
            <p className='story-course__paragraph'>Join our passionate wine community</p>
          </div>
        </div>
        </div>
        ) : (
          <div className="wine-container">
            {!loading && sortedProducts.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
            {!sortedProducts.length && <h1 className="wine__title">There are no results by your request</h1>}
          </div>
        )}
      </div>
    </div>
  )
}
