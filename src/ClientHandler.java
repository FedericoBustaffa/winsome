import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

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
		for (User u : users) {
			if (username.equals(u.getUsername())) {
				if (password.equals(u.getPassword())) {
					if (!u.isLogged()) {
						u.login();
						user = u;
						writer.write("login effettuato".getBytes());
						return;
					} else {
						writer.write("login gia' effettuato".getBytes());
						return;
					}
				} else {
					writer.write("password non corretta".getBytes());
					return;
				}
			}
		}

		writer.write("registrarsi prima di effettuare il login".getBytes());
	}

	public void logout(String username) throws IOException {
		if (username == null) {
			writer.write("effettuare prima il login".getBytes());
			return;
		}

		for (User u : users) {
			if (username.equals(u.getUsername())) {
				u.logout();
				user = null;
				writer.write("logout effettuato".getBytes());
				return;
			}
		}

		writer.write("username errato".getBytes());
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
							writer.write("USAGE: login <username> <password>".getBytes());
						else
							login(command[1], command[2]);
						user.setUsername(command[1]);
						break;

					case "logout":
						logout(user.getUsername());
						break;

					case "exit":
						logout(user.getUsername());
						socket.close();
						break;

					default:
						writer.write("invalid syntax".getBytes());
						break;
				}

			} while (!command[0].equals("exit"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
