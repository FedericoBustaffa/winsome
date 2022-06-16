import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class ServerWIN {

	// WINSOME core
	private Set<User> users;

	// services
	private LoggerImpl logger;

	public ServerWIN(int port) throws RemoteException {
		users = new TreeSet<User>();
		logger = new LoggerImpl(port, users);
	}

	public void start() {
		try {
			logger.start();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		try {
			logger.shutdown();
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
