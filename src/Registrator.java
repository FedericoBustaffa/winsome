import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Registrator extends Remote {

	public void userRegistration(String name, String password) throws RemoteException;

	public void listUsers() throws RemoteException;

}
