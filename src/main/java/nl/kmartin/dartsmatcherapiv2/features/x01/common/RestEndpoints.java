package nl.kmartin.dartsmatcherapiv2.features.x01.common;

public class RestEndpoints {

    private RestEndpoints() {
    }

    // Checkout Endpoints
    public static final String GET_CHECKOUTS = "/checkouts";
    public static final String GET_CHECKOUT = "/checkouts/{remaining}";

    // X01Match Endpoints
    public static final String X01_CREATE_MATCH = "/matches";
    public static final String X01_GET_MATCH = "/matches/{matchId}";
    public static final String X01_MATCH_EXISTS = "/matches/{matchId}/exists";
    public static final String X01_RESET_MATCH = "/matches/x01/{matchId}/reset";
    public static final String X01_DELETE_MATCH = "/matches/x01/{matchId}/delete";
    public static final String X01_ADD_TURN = "/matches/x01/{matchId}/turn/add";
    public static final String X01_EDIT_TURN = "/matches/x01/{matchId}/turn/edit";
    public static final String X01_DELETE_LAST_TURN = "/matches/x01/{matchId}/turn/delete-last";
}