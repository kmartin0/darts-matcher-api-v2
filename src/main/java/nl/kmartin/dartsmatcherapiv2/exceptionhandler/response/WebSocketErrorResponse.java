package nl.kmartin.dartsmatcherapiv2.exceptionhandler.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketErrorResponse extends ErrorResponse {

	private String destination;

	public WebSocketErrorResponse(ApiErrorCode apiErrorCode, String description, String destination, TargetError... details) {
		super(apiErrorCode, description, details);
		this.destination = destination;
	}

	public WebSocketErrorResponse(ApiErrorCode apiErrorCode, String description, String destination) {
		super(apiErrorCode, description);
		this.destination = destination;
	}
}
