export type Product = {
  id: number,
  year: number,
  vendorCode: string,
  reserveType: any,
  name: string,
  shortName: string,
  averageRatingScore: any,
  price: number,
  grape: string,
  isDecantation: boolean,
  wineType: string,
  strengthFrom: number,
  strengthTo: number,
  wineColor: string,
  colorDescribing: string,
  taste: string,
  aroma: string,
  gastronomy: string,
  description: string,
  pictureLink: string | null,
  pictureLink2: string | null,
  quantity: number,
  tasteWine: string,
}

export type ProductDetailsType = {
  id: number;
  year: number;
  vendorCode: string;
  reserveType: string;
  name: string;
  shortName: string;
  averageRatingScore: number;
  price: number;
  grape: string;
  isDecantation: boolean;
  wineType: string;
  strengthFrom: number;
  strengthTo: number;
  wineColor: string;
  colorDescribing: string;
  taste: string;
  aroma: string;
  gastronomy: string;
  description: string;
  pictureLink: string | null,
  pictureLink2: string | null,
  quantity: number,
  tasteWine: string
};

export type CommentType = {
  [x: string]: any;
  wineId: number,
  userFirstAndLastName: string,
  message: string,
  rating: number,
}

export type CommentResponseType = {
  wineId: number,
  userFirstAndLastName: string,
  id: number,
  userFirstName: string,
  userLastName: string,
  message: string,
  rating: number,
  reviewDate: string
}

type CartProduct = {
  wineId: number,
  quantity: number
}

type OrderInformation = {
  zipCode: string,
  region: string,
  city: string,
  street: string,
  comment: string
}

type PurchaseObject = {
  wineId: number,
  quantity: number
}

type CreateShoppingCardDto = {
  purchaseObjects: PurchaseObject[]
}

export type OrderType = {
  userFirstAndLastName: string,
  phoneNumber: string,
  email: string,
  createShoppingCardDto: CreateShoppingCardDto,
  createOrderDeliveryInformationDto: OrderInformation
}

interface PurchaseObjectInfo {
  wineId: number;
  winePictureLink: string;
  wineName: string;
  quantity: number;
  price: number;
}

interface OrderDeliveryInformationDto {
  id: number;
  zipCode: string;
  region: string;
  city: string;
  street: string;
  comment: string;
}

interface ShoppingCardDto {
  id: number;
  purchaseObjects: PurchaseObjectInfo[];
  totalCost: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  userId: number;
  shoppingCardDto: ShoppingCardDto;
  orderDeliveryInformationDto: OrderDeliveryInformationDto;
  registrationTime: string;
  completedTime: string;
  paymentStatus: string;
}