package nl.kmartin.dartsmatcherapiv2.utils;

public class Endpoints {

    private Endpoints() {
    }

    // Checkout Endpoints
    public static final String GET_CHECKOUTS = "/checkouts";
    public static final String GET_CHECKOUT = "/checkouts/{remaining}";


    // X01Match Endpoints
    public static final String CREATE_MATCH = "/matches";
}
