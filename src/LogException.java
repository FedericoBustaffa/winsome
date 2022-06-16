
public class LogException extends Exception {

	private String msg;

	public LogException(String msg) {
		this.msg = msg;
	}

	@Override
	public String getMessage() {
		return msg;
	}

}
