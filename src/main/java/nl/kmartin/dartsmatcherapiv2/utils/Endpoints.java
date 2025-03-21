package nl.kmartin.dartsmatcherapiv2.utils;

public class Endpoints {

    private Endpoints() {
    }

    // Checkout Endpoints
    public static final String GET_CHECKOUTS = "/checkouts";
    public static final String GET_CHECKOUT = "/checkouts/{remaining}";


    // X01Match Endpoints
    public static final String CREATE_MATCH = "/matches";
    public static final String GET_MATCH = "/matches/{matchId}";
    public static final String X01_ADD_TURN = "/matches/x01/turn/add";
    public static final String X01_EDIT_TURN = "/matches/x01/turn/edit";
    public static final String X01_DELETE_TURN = "/matches/x01/turn/delete";
    public static final String X01_DELETE_LEG = "/matches/x01/leg/delete";
    public static final String X01_DELETE_SET = "/matches/x01/set/delete";
}