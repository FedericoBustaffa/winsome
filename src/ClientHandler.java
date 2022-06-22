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
	private Map<Integer, Post> posts;

	// TCP
	private ServerSocket server_socket;
	private InputStream reader;
	private OutputStream writer;

	// JSON
	private ObjectMapper mapper;
	private File user_file;

	public ClientHandler(ServerSocket server_socket, Map<String, User> users, Map<String, Notifier> online_users,
			Map<Integer, Post> posts) {
		this.server_socket = server_socket;
		this.users = users;
		this.online_users = online_users;
		this.posts = posts;
		mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		user_file = new File("json_files/user.json");
	}

	public void login(String username, String password) throws IOException {
		if (this.user == null) {
			User u = users.get(username);
			if (u == null) {
				writer.write("< registrarsi prima di effettuare il login".getBytes());
				return;
			} else if (password.equals(u.getPassword())) {
				if (!u.isLogged()) {
					u.login();
					this.user = u;
					mapper.writeValue(this.user_file, this.user);
					writer.write("< login effettuato".getBytes());
					return;
				} else {
					writer.write("< profilo gia' connesso su un altro dispositivo".getBytes());
					return;
				}
			} else {
				writer.write("< password non corretta".getBytes());
				return;
			}
		} else {
			if (username.equals(user.getUsername()))
				writer.write("< login gia' effettuato con questo profilo".getBytes());
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

	public void followUser(String username) throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		if (username.equals(user.getUsername())) {
			writer.write("< non puoi seguire questo utente".getBytes());
			return;
		}

		User u = users.get(username);
		if (u != null) {
			if (u.addFollower(user.getUsername())) {
				user.follow(username);
				writer.write(("< ora segui " + username).getBytes());
			} else {
				writer.write("< segui gia' questo utente".getBytes());
				return;
			}
		} else {
			writer.write("< utente non trovato".getBytes());
			return;
		}

		Notifier client = online_users.get(username);
		if (client != null)
			client.notifyFollow(user.getUsername());
	}

	public void unfollowUser(String username) throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		User u = users.get(username);
		if (u != null) {
			if (u.removeFollower(user.getUsername())) {
				user.unfollow(username);
				writer.write(("< hai smesso di seguire " + username).getBytes());
			} else {
				writer.write("< non segui questo utente".getBytes());
				return;
			}
		} else {
			writer.write("< utente non trovato".getBytes());
			return;
		}

		Notifier client = online_users.get(username);
		if (client != null)
			client.notifyUnfollow(user.getUsername());
	}

	public void listFollowing() throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		if (user.following().size() == 0) {
			writer.write("< non segui nessuno al momento".getBytes());
			return;
		}

		String output = "seguiti:\n";
		for (String f : user.following())
			output = output + ("\t- " + f);

		writer.write(output.getBytes());
	}

	private boolean parsePost(String[] command) throws IOException {
		int i = 0, counter = 0;
		for (i = 0; i < command.length; i++) {
			if (command[i].startsWith("\""))
				counter++;
			if (command[i].endsWith("\""))
				counter++;
		}
		if (counter != 4) {
			writer.write("< titolo e contenuto devono essere tra virgolette".getBytes());
			return false;
		}

		StringBuilder sb = new StringBuilder();
		i = 1;
		do {
			if (command[i].endsWith("\""))
				sb.append(command[i]);
			else
				sb.append(command[i] + " ");
		} while (!command[i++].endsWith("\""));
		sb.delete(0, 1);
		sb.delete(sb.length() - 1, sb.length());
		String title = sb.toString();

		sb.delete(0, sb.length());
		do {
			if (command[i].endsWith("\""))
				sb.append(command[i]);
			else
				sb.append(command[i] + " ");
		} while (!command[i++].endsWith("\""));
		sb.delete(0, 1);
		sb.delete(sb.length() - 1, sb.length());
		String content = sb.toString();

		command[0] = title;
		command[1] = content;

		return true;
	}

	public void createPost(String title, String content) throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		if (title.length() > 20) {
			writer.write("< titolo troppo lungo (max 20 caratteri)".getBytes());
			return;
		}

		if (content.length() > 500) {
			writer.write("< testo troppo lungo (max 500 caratteri)".getBytes());
			return;
		}

		Post p = new Post(title, content, user.getUsername());
		posts.put(p.id(), p);
		user.addPost(p.id());

		writer.write(("< post pubblicato (" + p.id() + ")").getBytes());
	}

	public void showPost(int id) throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		Post post = posts.get(id);
		if (post == null) {
			writer.write("< post inesistente".getBytes());
			return;
		}

		String output = "< titolo: " + post.title() + "\n";
		output = output + "< contenuto: " + post.content() + "\n";
		output = output + "< voti: " + post.getUpVotes() + " positivi, " + post.getDownVotes() + " negativi\n";
		if (post.comments().size() == 0)
			output = output + "< commenti: nessun commento";
		else {
			for (String c : post.comments())
				output = output + "\t- " + c + "\n";
		}

		writer.write(output.getBytes());
	}

	public void deletePost(int id) throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		Post post = posts.get(id);
		if (post == null) {
			writer.write("< post inesistente".getBytes());
			return;
		}

		if (post.author().equals(user.getUsername())) {
			posts.remove(id);
			user.removePost(id);

			// rimozione dei rewin se presenti
			for (User u : users.values())
				u.removePost(id);

			writer.write(("< post " + id + " cancellato").getBytes());
		} else
			writer.write("< non sei l'autore di questo post".getBytes());
	}

	public void rewinPost(int id) throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		Post post = posts.get(id);
		if (post == null) {
			writer.write("< post inesistente".getBytes());
			return;
		}

		if (user.getUsername().equals(post.author())) {
			writer.write("sei l'autore di questo post".getBytes());
			return;
		}

		if (user.addPost(id))
			writer.write(("< rewin post " + id).getBytes());
		else
			writer.write("< hai gia' fatto il rewin di questo post".getBytes());
	}

	public void viewBlog() throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		String output = "";
		for (Integer id : posts.keySet()) {
			Post post = posts.get(id);
			if (user.getUsername().equals(post.author())) {
				output = output + "< titolo: " + post.title() + "\n";
				output = output + "< contenuto: " + post.content() + "\n";
				output = output + "< voti: " + post.getUpVotes() + " positivi, " + post.getDownVotes() + " negativi\n";
				if (post.comments().size() == 0)
					output = output + "< commenti: nessun commento\n";
				else {
					for (String c : post.comments())
						output = output + "\t- " + c + "\n";
				}
				output = output + "< ******\n";
			}
		}
		output = output.substring(0, output.length() - 1); // togle l'ultimo \n
		writer.write(output.getBytes());
	}

	public void showFeed() throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		if (user.following().size() == 0) {
			writer.write("< non segui ancora nessuno".getBytes());
			return;
		}

		String output = "";
		Post post;
		for (String f : user.following()) {
			for (Integer id : users.get(f).getPosts()) {
				post = posts.get(id);
				output = output + "< titolo: " + post.title() + "\n";
				output = output + "< contenuto: " + post.content() + "\n";
				output = output + "< voti: " + post.getUpVotes() + " positivi, " + post.getDownVotes() + " negativi\n";
				if (post.comments().size() == 0)
					output = output + "< commenti: nessun commento\n";
				else {
					for (String c : post.comments())
						output = output + "\t- " + c + "\n";
				}
				output = output + "< ******\n";
			}
		}
		output = output.substring(0, output.length() - 1);

		if (!output.isBlank())
			writer.write(output.getBytes());
		else
			writer.write("< feed vuoto".getBytes());
	}

	public void logout(String username) throws IOException {
		if (user == null) {
			writer.write("< effettuare prima il login".getBytes());
			return;
		}

		User u = users.get(username);
		if (u != null) {
			u.logout();
			user = null;
			writer.write("< logout effettuato".getBytes());
			return;
		}

		writer.write("< nome utente errato".getBytes());
	}

	public void exit() throws IOException {
		if (user != null) {
			user.logout();
			user = null;
		}

		writer.write("< terminato".getBytes());
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
							if (command[1].equals("users"))
								listUsers();
							else if (command[1].equals("following"))
								listFollowing();
						} else if (command.length > 2) {
							if (command[1].equals("users"))
								writer.write("< USAGE: list users".getBytes());
							else if (command[1].equals("following"))
								writer.write("< USAGE: list following".getBytes());
							else
								writer.write("< invalid command".getBytes());
						} else
							writer.write("< invalid command".getBytes());
						break;

					case "follow":
						if (command.length == 2)
							followUser(command[1]);
						else
							writer.write("< USAGE: follow <username>".getBytes());
						break;

					case "unfollow":
						if (command.length == 2)
							unfollowUser(command[1]);
						else
							writer.write("< USAGE: unfollow <username>".getBytes());
						break;

					case "post":
						if (parsePost(command))
							createPost(command[0], command[1]);
						break;

					case "blog":
						viewBlog();
						break;

					case "show":
						if (command.length >= 2) {
							if (command[1].equals("post")) {
								if (command.length == 3)
									showPost(Integer.parseInt(command[2]));
								else
									writer.write("< USAGE: show post <id>".getBytes());
							} else if (command[1].equals("feed")) {
								if (command.length == 2)
									showFeed();
								else
									writer.write("< USAGE: show feed".getBytes());
							} else
								writer.write("< invalid command".getBytes());
						} else
							writer.write("< invalid command".getBytes());
						break;

					case "delete":
						if (command.length == 2)
							deletePost(Integer.parseInt(command[1]));
						else
							writer.write("< USAGE: delete <id>".getBytes());
						break;

					case "rewin":
						if (command.length == 2)
							rewinPost(Integer.parseInt(command[1]));
						else
							writer.write("< USAGE: rewin <id>".getBytes());
						break;

					case "logout":
						if (command.length != 1)
							writer.write("< USAGE: logout".getBytes());
						else
							logout(user.getUsername());
						break;

					case "exit":
						if (command.length != 1)
							writer.write("< USAGE: exit".getBytes());
						else
							exit();
						break;

					default:
						writer.write("< invalid command".getBytes());
						break;
				}

			} while (!command[0].equals("exit"));
		} catch (

		SocketException e) {
			// interruzione della accept non gestita
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
