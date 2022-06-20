import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Notifier extends Remote {

	public void newFollower(String username) throws RemoteException;

	public void unfollow(String username) throws RemoteException;

}
