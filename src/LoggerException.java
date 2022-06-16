
public class LoggerException extends Exception {

	private String msg;

	public LoggerException(String msg) {
		this.msg = msg;
	}

	@Override
	public String getMessage() {
		return msg;
	}

}
