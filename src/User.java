import java.util.List;

public class User {

	private String name;
	private String password;
	private List<String> tags;

	public User(String name, String password, List<String> tags) {
		this.name = name;
		this.password = password;
		this.tags = tags;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getTags() {
		return tags;
	}

}
