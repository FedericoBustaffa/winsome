import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Client extends UnicastRemoteObject implements Notifier {

	private User user = null;

	// RMI
	private Registrator registrator;

	// TCP
	private Socket socket;
	private InputStream reader;
	private OutputStream writer;

	// JSON
	private ObjectMapper mapper;
	private File user_file;

	// configurazione
	private static String HOST;
	private static String MULTICAST;
	private static String REG_HOST;
	private static int REG_PORT;
	private static int TCP_PORT;
	private static int UDP_PORT;
	private static int MC_PORT;
	private static int TIMEOUT;

	public Client() throws ConnectException, RemoteException, NotBoundException, UnknownHostException, IOException {
		try (BufferedReader config = new BufferedReader(new FileReader("files/config.md"))) {
			String line;
			String[] values;
			while ((line = config.readLine()) != null) {
				if (!line.startsWith("#") && !line.isBlank()) {
					values = line.split("=");
					switch (values[0]) {
						case "SERVER":
							HOST = values[1];
							break;

						case "MULTICAST":
							MULTICAST = values[1];
							break;

						case "REGHOST":
							REG_HOST = values[1];
							break;

						case "REGPORT":
							REG_PORT = Integer.parseInt(values[1]);
							break;

						case "TCPPORT":
							TCP_PORT = Integer.parseInt(values[1]);
							break;

						case "UDPPORT":
							UDP_PORT = Integer.parseInt(values[1]);
							break;

						case "MCPORT":
							MC_PORT = Integer.parseInt(values[1]);
							break;

						case "TIMEOUT":
							TIMEOUT = Integer.parseInt(values[1]);
							break;

						default:
							break;
					}
				}
			}

		} catch (FileNotFoundException e) {
			System.out.println("file di configurazione non trovato");
		} catch (IOException e) {
			e.printStackTrace();
		}

		Registry registry = LocateRegistry.getRegistry(REG_PORT);
		registrator = (Registrator) registry.lookup(Server.NAME);

		socket = new Socket(HOST, TCP_PORT);
		reader = socket.getInputStream();
		writer = socket.getOutputStream();

		mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		user_file = new File("files/user.json");
	}

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

	public void listFollowers() {
		if (user == null) {
			System.out.println("< effettuare prima il login");
			return;
		}

		if (user.followers().size() == 0) {
			System.out.println("< nessun follower al momento");
			return;
		}

		System.out.println("< follower:");
		for (String f : user.followers()) {
			System.out.println("\t- " + f);
		}
	}

	public void notifyFollow(String username) throws RemoteException {
		System.out.print("\r< " + username + " ha iniziato a seguirti\n> ");
		user.addFollower(username);
	}

	public void notifyUnfollow(String username) throws RemoteException {
		System.out.print("\r< " + username + " ha smesso di seguirti\n> ");
		user.removeFollower(username);
	}

	public void handle(String command) {
		switch (command) {
			// client handled
			case "list followers":
				this.listFollowers();
				break;

			// server handled
			default:
				try {
					String response;
					byte[] b = new byte[1024];
					int bytes;
					writer.write(command.getBytes());
					bytes = reader.read(b);
					response = new String(b, 0, bytes);

					if (response.equals("< login effettuato")) {
						user = mapper.readValue(user_file, User.class);
						registrator.registerForCallback(user.getUsername(), this);
					} else if (response.equals("< logout effettuato") || response.equals("< terminato")) {
						if (user != null) {
							registrator.unregisterForCallback(user.getUsername());
							user = null;
						}
					}

					System.out.println(response);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
		}
	}

	public static void main(String[] args) {
		try {
			Client client = new Client();
			Scanner scanner = new Scanner(System.in);
			String command;
			do {
				System.out.print("> ");
				command = scanner.nextLine();
				if (command.isBlank())
					continue;
				if (command.contains("register"))
					client.register(command.split(" "));
				else
					client.handle(command);

			} while (!command.equals("exit"));
			scanner.close();
			UnicastRemoteObject.unexportObject(client, true);
		} catch (ConnectException e) {
			System.out.println("C nessun servizio sulla porta " + REG_PORT);
			return;
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		} catch (NotBoundException e) {
			System.out.println("NB nessun servizio sulla porta " + TCP_PORT);
			return;
		} catch (UnknownHostException e) {
			System.out.println("host sconosciuto");
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
