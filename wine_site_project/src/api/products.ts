import { Product, CommentType, OrderType, Order } from "../types/Product";
import { client } from "../utils/fetchClient";

export function getProducts() {
  return client.get<Product[]>('/wines');
}

export function getSelectedProduct(productId: number) {
  return client.get<Product>(`/wines/${productId}`);
}

export function getOrders() {
  return client.get<Order[]>('/orders');
}

export function getSelectedOrder(orderId: number) {
  return client.get<Order>(`/orders/${orderId}`);
}

export function createOrder({
  userFirstAndLastName,
  phoneNumber,
  email,
  createShoppingCardDto,
  createOrderDeliveryInformationDto
}: OrderType ) {

  return client.post<OrderType>(`/orders`, {
    userFirstAndLastName,
    phoneNumber,
    email,
    createShoppingCardDto,
    createOrderDeliveryInformationDto
  })
}

export function getReviews(productId: number) {
  return client.get<CommentType>(`/reviews/wines/${productId}`)
}

export function createReview({wineId, userFirstAndLastName, message, rating}: CommentType) {
  return client.post('/reviews', {wineId, userFirstAndLastName, message, rating})
}