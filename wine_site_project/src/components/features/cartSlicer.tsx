import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { Order, OrderType, Product } from "../../types/Product";
import { createOrder, getOrders, getSelectedOrder } from "../../api/products";

const storedCart = localStorage.getItem('productCart');
const storedCartItem = localStorage.getItem('productCartItem');

export type CartItem = {
  [x: string]: number;
  wineId: number,
  quantity: number
}

type CartState = {
  items: Product[],
  itemsCart: CartItem[],
  orderItems: Order[],
  orderSeletedItem: Order | null,
  loaded: boolean,
  hasError: string | null
};

const cartState: CartState = {
  items: storedCart ? JSON.parse(storedCart) : [],
  itemsCart: storedCartItem ? JSON.parse(storedCartItem) : [],
  orderItems: [],
  orderSeletedItem: null,
  loaded: true,
  hasError: null
};

const saveToLocalStorage = (state: CartState) => {
  localStorage.setItem('productCart', JSON.stringify(state.items));
  localStorage.setItem('productCartItem', JSON.stringify(state.itemsCart));
};

export const cartOrdersInit = createAsyncThunk<OrderType, OrderType>('cart/fetch', (newOrder) => {
  return createOrder(newOrder)
});

export const cartOrdersGetInit = createAsyncThunk('cartGet/fetch', () => {
  return getOrders();
});

export const cartSelectedOrderInit = createAsyncThunk('getSelectedCart', (orderId: number) => {
  return getSelectedOrder(orderId)
})

export const cartSlicer = createSlice({
  name: 'cart',
  initialState: cartState,
  reducers: {
    setCartProducts: (state, action) => {
      state.items.push({
        ...action.payload,
        wineId: action.payload.id,
      });

      const { quantity, id } = action.payload;

      state.itemsCart.push({
        wineId: id,
        quantity: quantity
      });

      saveToLocalStorage(state);
    },
    deleteCartProducts: (state, action) => {
      state.items = state.items.filter(item => item.id !== action.payload);
      state.itemsCart = state.itemsCart.filter(item => item.wineId !== action.payload);
      saveToLocalStorage(state);
    },
    increaseQuantity: (state, action) => {
      const foundItem = state.items.find(item => item.id === action.payload);

      if (foundItem) {
        foundItem.quantity += 1;
      }

      saveToLocalStorage(state);
    },
    decreaseQuantity: (state, action) => {
      const foundItem = state.items.find(item => item.id === action.payload);

      if (foundItem && foundItem.quantity > 1) {
        foundItem.quantity -= 1;
      }

      saveToLocalStorage(state);
    },
    clearCartProducts: (state) => {
      state.items = [];
      state.itemsCart = [];

      saveToLocalStorage(state);
    }
  },
  extraReducers: (builder) => {
    builder.addCase(cartOrdersGetInit.pending, (state) => {
      state.loaded = true;
    });
    builder.addCase(cartOrdersGetInit.fulfilled, (state, action) => {
      state.orderItems = action.payload;
      state.loaded = false;
    });
    builder.addCase(cartOrdersGetInit.rejected, (state) => {
      state.loaded = false;
      state.hasError = 'Error';
    });
    builder.addCase(cartSelectedOrderInit.pending, (state) => {
      state.loaded = true;
    });
    builder.addCase(cartSelectedOrderInit.fulfilled, (state, action) => {
      state.orderSeletedItem = action.payload;
      state.loaded = false;
    });
    builder.addCase(cartSelectedOrderInit.rejected, (state) => {
      state.loaded = false;
      state.hasError = 'Error';
    });
  }
});


export const {
  setCartProducts,
  deleteCartProducts,
  increaseQuantity,
  decreaseQuantity,
  clearCartProducts
} = cartSlicer.actions;

export default cartSlicer.reducer;