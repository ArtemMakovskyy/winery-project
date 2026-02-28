package com.winestoreapp.wineryadminui.core.observability;

public final class ObservationNames {
    // Wine domain
    public static final String WINE_CREATE = "wine/create";
    public static final String WINE_FIND_ALL = "wine/find-all";
    public static final String WINE_DELETE = "wine/delete";
    public static final String WINE_UPDATE_IMAGE = "wine/update-image";

    // Order domain
    public static final String ORDER_FIND_ALL = "order/find-all";
    public static final String ORDER_SET_PAID = "order/set-paid";
    public static final String ORDER_DELETE = "order/delete";

    // User domain
    public static final String USER_UPDATE_ROLE = "user/update-role";

    // Auth domain
    public static final String AUTH_LOGIN = "auth/login";
    public static final String AUTH_LOGOUT = "auth/logout";

    // Health domain
    public static final String HEALTH_CHECK = "health/check";
    public static final String HEALTH_INIT = "health/init";

    // UI domain
    public static final String UI_WINE_FORM = "ui/wine-form";
    public static final String UI_ORDER_FORM = "ui/order-form";
    public static final String UI_USER_ROLE_FORM = "ui/user-role-form";
    public static final String UI_DASHBOARD_VIEW = "ui/dashboard-view";

    private ObservationNames() {
    }

}
