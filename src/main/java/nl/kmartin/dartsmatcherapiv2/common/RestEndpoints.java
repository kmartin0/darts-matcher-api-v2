package nl.kmartin.dartsmatcherapiv2.common;

public class RestEndpoints {

    private RestEndpoints() {
    }

    // Checkout Endpoints
    public static final String GET_CHECKOUTS = "/checkouts";
    public static final String GET_CHECKOUT = "/checkouts/{remaining}";

    // X01Match Endpoints
    public static final String X01_CREATE_MATCH = "/x01/matches";
    public static final String X01_GET_MATCH = "/x01/matches/{matchId}";
    public static final String X01_GET_MATCHES = "/x01/matches";
    public static final String X01_MATCH_EXISTS = "/x01/matches/{matchId}/exists";
    public static final String X01_RESET_MATCH = "/x01/matches/{matchId}/reset";
    public static final String X01_REPROCESS_MATCH = "/x01/matches/{matchId}/reprocess";
    public static final String X01_DELETE_MATCH = "/x01/matches/{matchId}/delete";
    public static final String X01_ADD_TURN = "/x01/matches/{matchId}/turn/add";
    public static final String X01_EDIT_TURN = "/x01/matches/{matchId}/turn/edit";
    public static final String X01_DELETE_LAST_TURN = "/x01/matches/{matchId}/turn/delete-last";
}