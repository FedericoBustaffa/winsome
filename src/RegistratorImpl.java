import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Set;

public class RegistratorImpl extends UnicastRemoteObject implements Registrator {

	public static final String NAME = "REGISTRATOR";
	private Registry registry;

	private Set<User> users;

	public RegistratorImpl(int port, Set<User> users) throws RemoteException {
		LocateRegistry.createRegistry(port);
		registry = LocateRegistry.getRegistry(port);
		this.users = users;
	}

	public void start() throws RemoteException {
		registry.rebind(RegistratorImpl.NAME, this);
	}

	public void shutdown() throws RemoteException, NotBoundException {
		registry.unbind(RegistratorImpl.NAME);
		UnicastRemoteObject.unexportObject(this, true);
	}

	// INTERFACE IMPLEMENTATION
	public void register(String username, String password, List<String> tags)
			throws RemoteException, LogException {
		if (!users.add(new User(username, password, tags)))
			throw new LogException("utente gia' registrato");
	}

	public void login(String username, String password) throws RemoteException, LogException {
		for (User u : users) {
			if (username.equals(u.getUsername())) {
				if (password.equals(u.getPassword())) {
					if (!u.isLogged()) {
						u.login();
						return;
					} else {
						throw new LogException("login gia' effettuato");
					}
				} else {
					throw new LogException("password non corretta");
				}
			}
		}

		throw new LogException("registrarsi prima di effettuare il login");
	}

	public void logout(String username) throws RemoteException, LogException {
		if (username == null)
			throw new LogException("effettuare prima il login");

		for (User u : users) {
			if (username.equals(u.getUsername())) {
				u.logout();
				return;
			}
		}

		throw new LogException("username errato");
	}

}
