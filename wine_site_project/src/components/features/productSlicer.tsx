import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { getProducts, getSelectedProduct } from "../../api/products";
import { OrderType, Product, ProductDetailsType } from "../../types/Product";

type ProductState = {
  items: Product[],
  itemDetails: ProductDetailsType | null,
  loaded: boolean,
  hasError: string | null,
};

const productState: ProductState = {
  items: [],
  itemDetails: null,
  loaded: true,
  hasError: null,
};

export const productsInit = createAsyncThunk('products/fetch', async () => {
  const products = await getProducts();

  const productsWithQuantity = products.map(product => ({ ...product, quantity: 1 }));

  console.log(products);

  return productsWithQuantity;
});

export const productDetailsInit = createAsyncThunk('productDetails/fetch', (productId: number) => {
  return getSelectedProduct(productId);
});

const productsSlice = createSlice({
  name: 'products',
  initialState: productState,
  reducers: {
    increaseProductQuantity (state) {
      if (state.itemDetails) {
        state.itemDetails.quantity += 1;
      }
    },
    decreaseProductQuantity (state) {
      if (state.itemDetails && state.itemDetails.quantity > 1) {
        state.itemDetails.quantity -= 1
      }
    }
  },
  extraReducers: (builder) => {
    builder.addCase(productsInit.pending, (state) => {
      state.loaded = true;
    });
    builder.addCase(productsInit.fulfilled, (state, action) => {
      state.items = action.payload.map(product => ({
        ...product,
        quantity: 1
      }));
      state.loaded = false;
    });
    builder.addCase(productsInit.rejected, (state) => {
      state.loaded = false;
      state.hasError = 'Error';
    });
    builder.addCase(productDetailsInit.pending, (state) => {
      state.loaded = true;
    });
    builder.addCase(productDetailsInit.fulfilled, (state, action) => {
      state.itemDetails = {
        ...action.payload,
        quantity: 1
      };
      state.loaded = false;
    });
    builder.addCase(productDetailsInit.rejected, (state) => {
      state.loaded = false;
      state.hasError = 'Error';
    });
  }
});

export const { increaseProductQuantity, decreaseProductQuantity } = productsSlice.actions;

export default productsSlice.reducer;
