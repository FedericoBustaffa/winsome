import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

	private String username;
	private Registrator registrator;
	private Socket socket;
	private InputStream reader;
	private OutputStream writer;

	public Client() {
		username = null;
		try {
			Registry registry = LocateRegistry.getRegistry(Server.REGISTER_PORT);
			registrator = (Registrator) registry.lookup(Server.NAME);

			socket = new Socket(Server.IP, Server.CORE_PORT);
			reader = socket.getInputStream();
			writer = socket.getOutputStream();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	// registrator
	public void register(String[] command) {
		try {
			if (command.length < 3) {
				System.out.println("< USAGE: register <username> <password> <tags>");
				return;
			}

			List<String> tags = new ArrayList<String>();
			for (int i = 3; i < command.length; i++) {
				if (!tags.contains(command[i]))
					tags.add(command[i]);
			}

			registrator.register(command[1], command[2], tags);
			System.out.println("< registrazione effettuata");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (LogException e) {
			System.out.println(e.getMessage());
		}
	}

	public void send(String command) {
		String response;
		byte[] b = new byte[1024];
		int bytes;
		try {
			writer.write(command.getBytes());
			bytes = reader.read(b);
			response = new String(b, 0, bytes);
			System.out.println(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Client client = new Client();
		Scanner scanner = new Scanner(System.in);
		String command;
		do {
			System.out.print("> ");
			command = scanner.nextLine();
			if (command.isBlank())
				continue;
			if (command.contains("register")) {
				client.register(command.split(" "));
			} else {
				client.send(command);
			}
		} while (!command.equals("exit"));
		scanner.close();
	}
}
