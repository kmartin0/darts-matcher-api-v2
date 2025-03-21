package nl.kmartin.dartsmatcherapiv2.exceptionhandler.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class WebSocketErrorResponse extends ErrorResponse {

	private String destination;

	public WebSocketErrorResponse(String error, String description, int code, String destination, Map<String, String> details) {
		super(error, description, code, details);
		this.destination = destination;
	}

	public WebSocketErrorResponse(ApiErrorCode apiErrorCode, String description, String destination, TargetError... details) {
		super(apiErrorCode, description, details);
		this.destination = destination;
	}

	public WebSocketErrorResponse(ApiErrorCode apiErrorCode, String destination, String description) {
		super(apiErrorCode, description);
		this.destination = destination;
	}
}
