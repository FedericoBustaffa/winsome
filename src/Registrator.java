import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Registrator extends Remote {

	public boolean registration(String name, String password) throws RemoteException;

	public boolean login(String name, String password) throws RemoteException;

}
