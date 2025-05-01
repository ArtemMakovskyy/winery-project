import { createSlice } from "@reduxjs/toolkit";
import { Product } from "../../types/Product";

const storedFavourites = localStorage.getItem('favouriteProducts');

type FavouriteState = {
  items: Product[],
};

const favouriteState: FavouriteState = {
  items: storedFavourites ? JSON.parse(storedFavourites) : [],
};

const saveToLocalStorage = (state: FavouriteState) => {
  localStorage.setItem('favouriteProducts', JSON.stringify(state.items));
};

export const favouriteSlicer = createSlice({
  name: 'favourite',
  initialState: favouriteState,
  reducers: {
    setFavouritesProducts: (state, action) => {
      state.items.push(action.payload);
      saveToLocalStorage(state);
    },
    deleteFavouritesProducts: (state, action) => {
      state.items = state.items.filter(item => item.id !== action.payload);
      saveToLocalStorage(state);
    }
  }
});


export const {
  setFavouritesProducts,
  deleteFavouritesProducts
} = favouriteSlicer.actions;

export default favouriteSlicer.reducer;