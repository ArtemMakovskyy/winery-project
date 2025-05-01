import { Product } from "../../types/Product"
import { CartCard } from "./CartCard";

type Props = {
  products: Product[];
}

export const CartList: React.FC<Props> = ({ products }) => {
  return (
    <>
      {products.map((product) => (
        <CartCard key={product.id} product={product} />
      ))}
    </>
  )
}