import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Logger extends Remote {

	public void register(String username, String password, List<String> tags)
			throws RemoteException, LoggerException;

	public void login(String username, String password) throws RemoteException, LoggerException;

	public void logout(String username) throws RemoteException, LoggerException;

}
