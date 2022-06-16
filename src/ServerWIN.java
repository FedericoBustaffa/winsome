import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class ServerWIN {

	// WINSOME core
	private Set<User> users;

	// services
	private RegistratorImpl registrator;

	public ServerWIN(int port) throws RemoteException {
		users = new TreeSet<User>();
		registrator = new RegistratorImpl(port, users);
	}

	public void start() {
		try {
			registrator.start();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		try {
			registrator.shutdown();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ServerWIN winsome;
		try {
			winsome = new ServerWIN(4000);
			winsome.start();
			Scanner scanner = new Scanner(System.in);
			scanner.nextLine();
			scanner.close();
			winsome.shutdown();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
