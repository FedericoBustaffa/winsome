import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Core extends Remote {

	public List<String> listUsers() throws RemoteException;

	public List<String> listFollowers() throws RemoteException;

	public List<String> listFollowing() throws RemoteException;

	public void followUser(String username) throws RemoteException;

	public void unfollowUser(String username) throws RemoteException;

	public List<Post> viewBlog() throws RemoteException;

	public void createPost(String title, String content) throws RemoteException;

	public List<Post> showFeed() throws RemoteException;

	public Post showPost(int post_id) throws RemoteException;

	public void deletePost(int post_id) throws RemoteException;

	public void rewinPost(int post_id) throws RemoteException;

	public void ratePost(int post_id, int vote) throws RemoteException;

	public void addComment(int post_id, String comment) throws RemoteException;
}
