import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientWIN {

	private String username;
	private Logger logger;

	public ClientWIN() {
		username = null;
		try {
			Registry registry = LocateRegistry.getRegistry(4000);
			logger = (Logger) registry.lookup(LoggerImpl.NAME);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	// LOGGER
	public void register(String[] command) {
		try {
			// controllo numero minimo di argomenti
			if (command.length < 3)
				throw new LoggerException("USAGE: register <username> <password> <tags>");

			// controllo numero massimo di argomenti
			if (command.length > 8)
				throw new LoggerException("inserire al massimo 5 tag");

			// creazione lista tag
			List<String> tags = new ArrayList<String>();
			for (int i = 3; i < command.length; i++) {
				tags.add(command[i]);
			}

			// accesso al servizio
			logger.register(command[1], command[2], tags);
			System.out.println("registrazione effettuata");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (LoggerException e) {
			System.out.println("registrazione fallita: " + e.getMessage());
		}
	}

	public void login(String[] command) {
		try {
			logger.login(command[1], command[2]);
			username = command[1];
			System.out.println("login effettuato");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (LoggerException e) {
			System.out.println("login fallito: " + e.getMessage());
		} catch (IndexOutOfBoundsException e) {
			System.out.println("COMMAND USAGE: login <username> <password>");
		}
	}

	public void logout() {
		try {
			logger.logout(username);
			username = null;
			System.out.println("logout effettuato");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (LoggerException e) {
			System.out.println("logout fallito: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		ClientWIN client = new ClientWIN();
		Scanner scanner = new Scanner(System.in);
		String[] command;
		do {
			command = scanner.nextLine().split(" ");
			if (command[0].equals("register")) {
				client.register(command);
			} else if (command[0].equals("login")) {
				client.login(command);
			} else if (command[0].equals("logout")) {
				client.logout();
			} else if (command[0].equals("exit")) {
				if (client.getUsername() != null)
					client.logout();
			} else {
				System.out.println("comando non valido");
			}
		} while (!command[0].equals("exit"));
		scanner.close();
	}
}
