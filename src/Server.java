import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends UnicastRemoteObject implements Registrator {

	// core
	private Map<String, User> users;
	private Map<String, Notifier> online_users;
	private Map<Integer, Post> posts;

	// RMI
	private Registry registry;

	// TCP
	private ServerSocket server_socket;
	private ExecutorService pool;

	// server settings
	public static final String NAME = "WINSOME";
	private static String HOST;
	private static String MULTICAST;
	private static String REG_HOST;
	private static int REG_PORT;
	private static int TCP_PORT;
	private static int UDP_PORT;
	private static int MC_PORT;
	private static int TIMEOUT;

	public Server() throws RemoteException {
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

		users = new HashMap<String, User>();
		online_users = new HashMap<String, Notifier>();
		posts = new HashMap<Integer, Post>();

		pool = Executors.newFixedThreadPool(10);
		try {
			LocateRegistry.createRegistry(REG_PORT);
			registry = LocateRegistry.getRegistry(REG_PORT);

			server_socket = new ServerSocket(TCP_PORT);

			System.out.println("Server attivo");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Porta " + TCP_PORT + " occupata");
		}
	}

	public void start() {
		try {
			registry.rebind(Server.NAME, this);
			for (int i = 0; i < 10; i++)
				pool.execute(new ClientHandler(server_socket, users, online_users, posts));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public synchronized void register(String username, String password, List<String> tags)
			throws RemoteException, LogException {

		if (tags.size() > 5)
			throw new LogException("< inserire al massimo 5 tag");

		if (tags.size() == 0)
			throw new LogException("< inserire almeno un tag");

		if (users.get(username) == null)
			users.put(username, new User(username, password, tags));
		else
			throw new LogException("< nome utente non disponibile");

	}

	public void registerForCallback(String username, Notifier client) throws RemoteException {
		if (!online_users.containsKey(username))
			online_users.put(username, client);
	}

	public void unregisterForCallback(String username) throws RemoteException {
		online_users.remove(username);
	}

	public void shutdown() {
		try {
			registry.unbind(Server.NAME);
			UnicastRemoteObject.unexportObject(this, true);
			server_socket.close();
			pool.shutdown();
			while (!pool.isTerminated())
				;
			System.out.println("Server chiuso");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws RemoteException {
		Server winsome = new Server();
		winsome.start();
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
		winsome.shutdown();
	}
}
