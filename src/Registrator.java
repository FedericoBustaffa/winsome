import java.rmi.Remote;

public interface Registrator extends Remote {

	public void addNewUser(String name, String password);

}
