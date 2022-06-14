import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;

public class RegistratorService extends RemoteObject implements Registrator {

	private Registrator registrator;
	private Registry registration_registry;
	private int port;

	public RegistratorService(int port) {
		this.port = port;
		try {
			registrator = (Registrator) UnicastRemoteObject.exportObject(this, 0);
			LocateRegistry.createRegistry(port);
			registration_registry = LocateRegistry.getRegistry(port);
			System.out.println("REGISTRATION SERVICE ON PORT: " + port);
		} catch (RemoteException e) {
			System.out.println("Winsome build failed\n" + e.getMessage());
		}
	}

	public int getPort() {
		return port;
	}

	public void openService() {
		try {
			registration_registry.rebind("REGISTRATION-SERVICE", registrator);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		System.out.println("Registration service open");
	}

	public void closeService() {
		try {
			registration_registry.unbind("REGISTRATION-SERVICE");
			UnicastRemoteObject.unexportObject(this, true);
			System.out.println("Registration service closed");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	// Interface methods
	public void addNewUser(String name, String password) {

	}
}
