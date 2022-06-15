import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
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
	public void userRegistration(String name, String password) throws RemoteException {
		users.add(new User(name, password));
	}

	public void listUsers() throws RemoteException {
		for (User u : users) {
			System.out.println(u.getName());
		}
	}

}
