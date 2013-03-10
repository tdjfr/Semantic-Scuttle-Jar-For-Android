package uk.co.jarofgreen.semanticscuttlejar;


public class ScuttleAPIException extends Exception {

	private static final long serialVersionUID = 5234928229285853691L;

	public ScuttleAPIException(String detailMessage) {
		super(detailMessage);
	}

	public ScuttleAPIException(Throwable throwable) {
		super(throwable);
	}

	public ScuttleAPIException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
