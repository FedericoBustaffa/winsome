import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Set;

public class LoggerImpl extends UnicastRemoteObject implements Logger {

	public static final String NAME = "LOGGER";
	private Registry registry;

	private Set<User> users;

	public LoggerImpl(int port, Set<User> users) throws RemoteException {
		LocateRegistry.createRegistry(port);
		registry = LocateRegistry.getRegistry(port);
		this.users = users;
	}

	public void start() throws RemoteException {
		registry.rebind(LoggerImpl.NAME, this);
	}

	public void shutdown() throws RemoteException, NotBoundException {
		registry.unbind(LoggerImpl.NAME);
		UnicastRemoteObject.unexportObject(this, true);
	}

	// INTERFACE IMPLEMENTATION
	public void register(String username, String password, List<String> tags)
			throws RemoteException, LoggerException {
		if (!users.add(new User(username, password, tags)))
			throw new LoggerException("utente gia' registrato");
	}

	public void login(String username, String password) throws RemoteException, LoggerException {
		for (User u : users) {
			if (username.equals(u.getUsername())) {
				if (password.equals(u.getPassword())) {
					if (!u.isLogged()) {
						u.login();
						return;
					} else {
						throw new LoggerException("login gia' effettuato");
					}
				} else {
					throw new LoggerException("password non corretta");
				}
			}
		}

		throw new LoggerException("registrarsi prima di effettuare il login");
	}

	public void logout(String username) throws RemoteException, LoggerException {
		if (username == null)
			throw new LoggerException("effettuare prima il login");

		for (User u : users) {
			if (username.equals(u.getUsername())) {
				u.logout();
				return;
			}
		}

		throw new LoggerException("username errato");
	}

}
