import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Registrator extends Remote {

	public void register(String username, String password, List<String> tags) throws RemoteException, LogException;

	public void registerForCallback(String username, Notifier client) throws RemoteException;

	public void unregisterForCallback(String username) throws RemoteException;

}
