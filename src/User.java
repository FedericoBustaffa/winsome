import java.util.List;

public class User implements Comparable<User> {

	private String username;
	private String password;
	private List<String> tags;
	private boolean logged;

	public User(String username, String password, List<String> tags) {
		this.username = username;
		this.password = password;
		this.tags = tags;
		this.logged = false;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public boolean isLogged() {
		return logged;
	}

	public void login() {
		logged = true;
	}

	public void logout() {
		logged = false;
	}

	@Override
	public int compareTo(User other) {
		return this.username.compareTo(other.username);
	}

	@Override
	public boolean equals(Object obj) {
		User other = (User) obj;
		return this.username.equals(other.username);
	}

	@Override
	public String toString() {
		return "username: " + this.username;
	}

}
