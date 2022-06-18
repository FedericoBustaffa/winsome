import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends UnicastRemoteObject implements Registrator {

	// WINSOME core
	private Set<User> users;

	// RMI
	private Registry registry;

	// TCP
	private ServerSocket server_socket;
	private ExecutorService pool;

	// server settings
	public static final String NAME = "WINSOME";
	public static final String IP = "192.168.1.21";
	public static final int REGISTER_PORT = 4000;
	public static final int CORE_PORT = 5000;

	public Server() throws RemoteException {
		users = new TreeSet<User>();
		try {

			LocateRegistry.createRegistry(REGISTER_PORT);
			registry = LocateRegistry.getRegistry(REGISTER_PORT);

			server_socket = new ServerSocket(CORE_PORT);
			pool = Executors.newFixedThreadPool(20);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Porta " + CORE_PORT + " occupata");
		}
	}

	public void start() {
		try {
			registry.rebind(Server.NAME, this);
			for (int i = 0; i < 20; i++)
				pool.execute(new ClientHandler(server_socket, users));
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

		if (!users.add(new User(username, password, tags)))
			throw new LogException("< utente gia' registrato");
	}

	public void shutdown() {
		try {
			registry.unbind(Server.NAME);
			UnicastRemoteObject.unexportObject(this, true);
			while (!pool.isTerminated())
				;
			server_socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws RemoteException {
		Server winsome;
		winsome = new Server();
		winsome.start();
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
		winsome.shutdown();
	}

}
