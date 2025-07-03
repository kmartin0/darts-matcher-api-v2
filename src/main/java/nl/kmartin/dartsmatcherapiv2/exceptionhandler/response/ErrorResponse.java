package nl.kmartin.dartsmatcherapiv2.exceptionhandler.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorResponse implements Serializable {
    // The Status
    private String error;

    // A developer-facing human-readable error description in English.
    private String description;

    // The HTTP Status Code
    private int code;

    // (Optional) Additional user-friendly error information that the client code can use to handle
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> details;

    public ErrorResponse(ApiErrorCode apiErrorCode, String description, TargetError... details) {
        this.code = apiErrorCode.getHttpStatus().value();
        this.description = description;
        this.error = apiErrorCode.name();
        this.details = detailsArrToHashMap(details);
    }

    public ErrorResponse(ApiErrorCode apiErrorCode, String description) {
        this.code = apiErrorCode.getHttpStatus().value();
        this.description = description;
        this.error = apiErrorCode.name();
        this.details = new HashMap<>();
    }

    private HashMap<String, String> detailsArrToHashMap(TargetError... details) {
        if (details == null) return null;

        HashMap<String, String> tmpDetails = new HashMap<>();
        for (TargetError targetError : details) {
            String target = targetError.target() != null ? targetError.target() : "body";
            tmpDetails.put(target, targetError.error());
        }
        return tmpDetails;
    }
}
