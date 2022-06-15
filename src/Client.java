import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

	private User user;
	private Registrator registrator;

	public Client(String name, String password) {
		this.user = new User(name, password);
		try {
			Registry registry = LocateRegistry.getRegistry(4000);
			registrator = (Registrator) registry.lookup("WINSOME");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	// REGISTRATION INTERFACE
	public void registration() {
		try {
			registrator.userRegistration(user.getName(), user.getPassword());
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}

	public void listUsers() {
		try {
			registrator.listUsers();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
