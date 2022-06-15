import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {

	private User user;
	private Registrator registrator;

	public Client() {
		try {
			Registry registry = LocateRegistry.getRegistry(4000);
			registrator = (Registrator) registry.lookup("WINSOME");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	// REGISTRATION INTERFACE
	public boolean registration() {
		try {
			return registrator.registration(user.getName(), user.getPassword());
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
		return false;
	}

	public void login() {
		try {
			Scanner input = new Scanner(System.in);
			while (!registrator.login(user.getName(), user.getPassword())) {
				System.out.println("Nome utente o password non validi");
				System.out.print("Nome utente: ");
				user.setName(input.nextLine());
				System.out.print("Password: ");
				user.setPassword(input.nextLine());
			}
			input.close();
			System.out.println("Login effettuato");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("USAGE: java Client <name> <password>");
			return;
		}

		Client client = new Client();
		System.out.printf("1: Registration\n2: Login\nChoice: ");
		int choice;
		Scanner scanner = new Scanner(System.in);
		do {
			choice = scanner.nextInt();
			scanner.nextLine();
		} while (choice != 1 && choice != 2);

		if (choice == 1)
			client.registration();
		else
			client.login();

		scanner.close();
	}
}
