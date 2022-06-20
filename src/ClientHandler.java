import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ClientHandler implements Runnable {

	private User user = null;
	private Map<String, User> users;
	private Map<String, Notifier> online_users;

	// TCP
	private ServerSocket server_socket;
	private InputStream reader;
	private OutputStream writer;

	// JSON
	private ObjectMapper mapper;
	private File user_file;

	public ClientHandler(ServerSocket server_socket, Map<String, User> users, Map<String, Notifier> online_users) {
		this.server_socket = server_socket;
		this.users = users;
		this.online_users = online_users;
		mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		user_file = new File("json_files/user.json");
	}

	public void login(String username, String password) throws IOException {
		if (user == null) {
			user = users.get(username);
			if (user == null)
				writer.write("< registrarsi prima di effettuare il login".getBytes());
			else if (password.equals(user.getPassword())) {
				if (!user.isLogged()) {
					user.login();
					mapper.writeValue(user_file, user);
					writer.write("< login effettuato".getBytes());
				} else {
					writer.write("< profilo gia' connesso su un altro dispositivo".getBytes());
				}
			} else
				writer.write("< password non corretta".getBytes());
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
		for (User u : users.values()) {
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
				users_list.append("\t- " + username + "\n");
			}
			users_list.setCharAt(users_list.length() - 1, '\0');
			writer.write(users_list.toString().getBytes());
		}
	}

	public void listFollowing() throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}
		writer.write("< list following".getBytes());
	}

	public void followUser(String username) throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		users.get(username).addFollower(user.getUsername());
		Notifier u = online_users.get(username);
		if (u != null)
			u.notifyFollow(user.getUsername());

		writer.write(("< ora segui " + username).getBytes());
	}

	public void unfollowUser(String username) throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		users.get(username).removeFollower(user.getUsername());
		Notifier u = online_users.get(username);
		if (u != null)
			u.notifyUnfollow(user.getUsername());

		writer.write(("< hai smesso di seguire " + username).getBytes());
	}

	public void logout(String username) throws IOException {
		User u = users.get(username);
		if (u != null) {
			u.logout();
			user = null;
			writer.write("< logout effettuato".getBytes());
			return;
		}

		writer.write("< username errato".getBytes());
	}

	public void run() {
		try {
			Socket socket = server_socket.accept();
			reader = socket.getInputStream();
			writer = socket.getOutputStream();

			String[] command = null;
			byte[] b = new byte[1024];
			int bytes;
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

					case "follow":
						if (command.length == 2)
							followUser(command[1]);
						else
							writer.write("< invalid command".getBytes());
						break;

					case "unfollow":
						if (command.length == 2)
							unfollowUser(command[1]);
						else
							writer.write("< invalid command".getBytes());
						break;

					case "logout":
						if (user == null)
							writer.write("< effettuare prima il login".getBytes());
						else
							logout(user.getUsername());
						break;

					case "exit":
						if (user != null)
							user.logout();

						writer.write("< terminato".getBytes());
						socket.close();
						break;

					default:
						writer.write("< invalid command".getBytes());
						break;
				}

			} while (!command[0].equals("exit"));
		} catch (SocketException e) {
			// interruzione della accept non gestita
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
