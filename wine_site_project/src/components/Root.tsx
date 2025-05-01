import { Routes, Route, HashRouter } from "react-router-dom";
import App from "../App";
import { AboutPage } from "./AboutPage/AboutPage";
import { ContactPage } from "./ContactPage/ContactPage";
import { MorePage } from "./MorePage/MorePage";
import { Provider } from "react-redux";
import { store } from "../app/store";
import { WinePage } from "./WinePage/WinePage";
import { ProductDetails } from "./ProductCard/ProductDetails";
import { ScrollToTop } from "./ScrollToTop/ScrollToTop";
import { FavouritePage } from "./FavouritePage/FavouritePage";
import { CartPage } from "./CartPage/CartPage";
import { QuestionPage } from "./QuestionPage/QuestionPage";
import { PageNotFound } from "./ErrorPage/PageNotFound";

export const Root = () => {
  return (
    <HashRouter>
      <ScrollToTop />
      <Provider store={store}>
        <Routes>
          <Route index element={<QuestionPage />} />
          <Route path="/" element={<App />}>
              <Route path="*" element={<PageNotFound />} />

            <Route index path="/about" element={<AboutPage />} />
            <Route path="/products">
              <Route index element={<WinePage />} />
              <Route path=":productId" element={<ProductDetails />} />
            </Route>
            <Route path="/contact" element={<ContactPage />} />
            <Route path="/more" element={<MorePage />} />
            <Route path="/favourites" element={<FavouritePage />} />
            <Route path="/cart" element={<CartPage />} />
          </Route>

        </Routes>
      </Provider>
    </HashRouter>
  )
}