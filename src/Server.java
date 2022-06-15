import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.Vector;

public class Server extends RemoteServer implements Registrator {

	private String name;
	private Registry registration_registry;
	private Vector<User> users;

	public Server(String name, int port) {
		this.name = name;
		this.users = new Vector<User>();
		try {
			UnicastRemoteObject.exportObject(this, 0);
			LocateRegistry.createRegistry(port);
			registration_registry = LocateRegistry.getRegistry(port);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		try {
			registration_registry.rebind(name, this);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		try {
			registration_registry.unbind(name);
			UnicastRemoteObject.unexportObject(this, true);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	// REGISTRATOR INTERFACE
	public boolean registration(String name, String password) throws RemoteException {
		for (User u : users) {
			if (u.getName().equals(name))
				return false;
		}
		users.add(new User(name, password));
		return true;
	}

	public boolean login(String name, String password) throws RemoteException {

		for (User u : users) {
			if (u.getName().equals(name) && u.getPassword().equals(password)) {
				return true;
			}
		}

		return false;
	}

	public static void main(String[] args) throws InterruptedException {
		Server winsome = new Server("WINSOME", 4000);
		winsome.start();
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
		winsome.shutdown();
	}

}
