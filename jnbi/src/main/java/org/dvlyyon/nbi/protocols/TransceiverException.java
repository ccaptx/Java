package org.dvlyyon.nbi.protocols;

public class TransceiverException extends Exception {
	String    response;
	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	Exception exception;
	
	public TransceiverException(String message) {
		super(message);
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

}
