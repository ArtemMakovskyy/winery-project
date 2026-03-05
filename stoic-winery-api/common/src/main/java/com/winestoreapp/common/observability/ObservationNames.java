package com.winestoreapp.common.observability;

public final class ObservationNames {

    // Wine
    public static final String WINE_CREATE = "wine.create";
    public static final String WINE_FIND = "wine.find";
    public static final String WINE_FIND_ALL = "wine.find.all";
    public static final String WINE_DELETE = "wine.delete";
    public static final String WINE_EXISTS = "wine.exists";
    public static final String WINE_UPDATE_IMAGE = "wine.update.image";
    public static final String WINE_UPDATE_RATING = "wine.update.rating";

    // Review
    public static final String REVIEW_CREATE = "review.create";
    public static final String REVIEW_FIND_BY_WINE = "review.find.by.wine";

    // Order
    public static final String ORDER_CREATE = "order.create";
    public static final String ORDER_FIND_ALL = "order.find.all";
    public static final String ORDER_DELETE = "order.delete";
    public static final String ORDER_SET_PAID = "order.set.paid";
    public static final String ORDER_FIND_BY_ID = "order.find.by.id";
    public static final String ORDER_FIND_BY_NUMBER = "order.find.by.number";
    public static final String ORDER_FIND_BY_USER = "order.find.by.user";

    // User
    public static final String USER_GET_OR_CREATE = "user.get.or.create";
    public static final String USER_REGISTER = "user.register";
    public static final String USER_SYNC_DATA = "user.sync.data";
    public static final String USER_UPDATE_ROLE = "user.update.role";
    public static final String USER_UPDATE_TG_ID = "user.update.tg.id";
    public static final String USER_FIND_BY_ID = "user.find.by.id";
    public static final String USER_FIND_BY_EMAIL = "user.find.by.email";
    public static final String USER_FIND_BY_TG_ID = "user.find.by.tg.id";
    public static final String USER_FIND_BY_ROLE = "user.find.by.role";

    // Auth
    public static final String AUTH_AUTHENTICATE = "auth.authenticate";
    public static final String AUTH_LOGIN = "auth.login";
    public static final String AUTH_LOGOUT = "auth.logout";
    public static final String AUTH_REGISTER = "auth.register";
    public static final String AUTH_VALIDATE_TOKEN = "auth.validate.token";
    public static final String AUTH_LOAD_USER = "auth.load.user";

    // Telegram
    public static final String TELEGRAM_START = "telegram.start";
    public static final String TELEGRAM_RECEIVE_UPDATE = "telegram.receive.update";
    public static final String TELEGRAM_SEND_NOTIFICATION = "telegram.send.notification";
    public static final String TELEGRAM_SEND_PICTURE = "telegram.send.picture";
    public static final String TELEGRAM_REGISTER_BY_ORDER = "telegram.register.by.order";

    // Image
    public static final String IMAGE_DELETE = "image.delete";
    public static final String IMAGE_UPDATE = "image.update";

    private ObservationNames() {
    }
}