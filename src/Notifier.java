import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Notifier extends Remote {

	public void notifyFollow(String username) throws RemoteException;

	public void notifyUnfollow(String username) throws RemoteException;

}
