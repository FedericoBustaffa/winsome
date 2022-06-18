import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.TreeSet;

public class ClientHandler implements Runnable {

	private Set<User> users;
	private User user = null;

	private Socket socket;
	private InputStream reader;
	private OutputStream writer;

	public ClientHandler(ServerSocket server_socket, Set<User> users) {
		this.users = users;
		try {
			socket = server_socket.accept();
			reader = socket.getInputStream();
			writer = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void login(String username, String password) throws IOException {
		if (user == null) {
			for (User u : users) {
				if (username.equals(u.getUsername())) {
					if (password.equals(u.getPassword())) {
						if (!u.isLogged()) {
							u.login();
							user = u;
							writer.write("< login effettuato".getBytes());
							return;
						}
					} else {
						writer.write("< password non corretta".getBytes());
						return;
					}
				}
			}

			writer.write("< registrarsi prima di effettuare il login".getBytes());
		} else {
			if (username.equals(user.getUsername()))
				writer.write("< login gia' effettuato".getBytes());
			else
				writer.write("< effettuare prima il logout".getBytes());
		}

	}

	public void listUsers() throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		Set<String> common_tag_users = new TreeSet<String>();
		for (User u : users) {
			if (!u.getUsername().equals(user.getUsername())) {
				for (String tag : user.getTags()) {
					if (u.getTags().contains(tag)) {
						common_tag_users.add(u.getUsername());
						break;
					}
				}
			}
		}

		if (common_tag_users.isEmpty())
			writer.write("< nessun utente trovato".getBytes());
		else {
			StringBuilder users_list = new StringBuilder("< utenti con tag comuni:\n");
			for (String username : common_tag_users) {
				users_list.append("< \t" + username + "\n");
			}
			users_list.setCharAt(users_list.length() - 1, '\0');
			writer.write(users_list.toString().getBytes());
		}
	}

	public void listFollowers() throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}
		writer.write("< list followers".getBytes());
	}

	public void listFollowing() throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}
		writer.write("< list following".getBytes());
	}

	public void logout(String username) throws IOException {
		for (User u : users) {
			if (username.equals(u.getUsername())) {
				u.logout();
				user = null;
				writer.write("< logout effettuato".getBytes());
				return;
			}
		}

		writer.write("< username errato".getBytes());
	}

	public void run() {
		String[] command = null;
		byte[] b = new byte[1024];
		int bytes;
		try {
			do {
				bytes = reader.read(b);
				command = new String(b, 0, bytes).split(" ");

				switch (command[0]) {
					case "login":
						if (command.length != 3)
							writer.write("< USAGE: login <username> <password>".getBytes());
						else
							login(command[1], command[2]);
						break;

					case "list":
						if (command.length == 2) {
							switch (command[1]) {
								case "users":
									listUsers();
									break;

								case "followers":
									listFollowers();
									break;

								case "following":
									listFollowing();
									break;

								default:
									writer.write("< invalid command".getBytes());
									break;
							}
						} else {
							writer.write("< invalid command".getBytes());
						}
						break;

					case "logout":
						if (user == null)
							writer.write("< effettuare prima il login".getBytes());
						else
							logout(user.getUsername());
						break;

					case "exit":
						if (user != null) {
							// se si e' loggati si fa il logout
							user.logout();
						}
						writer.write("< terminato".getBytes());
						socket.close();
						break;

					default:
						writer.write("< invalid command".getBytes());
						break;
				}

			} while (!command[0].equals("exit"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
