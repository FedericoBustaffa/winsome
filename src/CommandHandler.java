import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandHandler extends UnicastRemoteObject implements Registrator {

	private Set<User> users;

	// RMI
	private Registry registry;

	// TCP
	private ServerSocket server_socket;
	private ExecutorService pool;

	public CommandHandler(Set<User> users) throws RemoteException {
		this.users = users;
		try {
			LocateRegistry.createRegistry(Server.REGISTER_PORT);
			registry = LocateRegistry.getRegistry(Server.REGISTER_PORT);

			server_socket = new ServerSocket(Server.CORE_PORT);
			pool = Executors.newFixedThreadPool(20);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Porta " + Server.CORE_PORT + " occupata");
		}
	}

	public void start() {
		try {
			registry.rebind(Server.NAME, this);
			for (int i = 0; i < 20; i++)
				pool.execute(new ClientHandler(server_socket, users));

			System.out.println("ok");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
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

	public synchronized void register(String username, String password, List<String> tags)
			throws RemoteException, LogException {

		if (tags.size() > 5)
			throw new LogException("inserire al massimo 5 tag");

		if (!users.add(new User(username, password, tags)))
			throw new LogException("utente gia' registrato");
	}

}
