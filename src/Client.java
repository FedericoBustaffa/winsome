import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

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
		Client client = new Client(args[0], args[1]);
		if (!client.registration()) {
			client.login();
		} else {
			System.out.println("Registrazione effettuata");
		}
	}
}
