import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Server {

	// WINSOME core
	private Set<User> users;

	// server settings
	public static final String NAME = "WINSOME";
	public static final String IP = "192.168.1.21";
	public static final int REGISTER_PORT = 4000;
	public static final int CORE_PORT = 5000;

	// core handler
	private CommandHandler command_handler;

	public Server() {
		users = new TreeSet<User>();
		try {
			command_handler = new CommandHandler(users);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		command_handler.start();
	}

	public void shutdown() {
		command_handler.shutdown();
	}

	public static void main(String[] args) {
		Server winsome;
		winsome = new Server();
		winsome.start();
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
		winsome.shutdown();
	}

}
