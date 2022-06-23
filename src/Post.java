import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Post {

	private int id;
	private String title;
	private String content;
	private String author;
	private Map<String, Integer> votes;
	private Map<String, String> comments;

	private static int id_count = 0;

	public Post(String title, String content, String author) {
		this.id = id_count;
		id_count++;
		this.title = title;
		this.content = content;
		this.author = author;
		this.votes = new HashMap<String, Integer>();
		this.comments = new LinkedHashMap<String, String>();
	}

	public int id() {
		return id;
	}

	public String title() {
		return title;
	}

	public String content() {
		return content;
	}

	public String author() {
		return author;
	}

	public boolean rate(String user, int vote) {
		if (!votes.keySet().contains(user)) {
			votes.put(user, vote);
			return true;
		} else
			return false;
	}

	public Map<String, Integer> getUpVotes() {
		Map<String, Integer> up_votes = new HashMap<String, Integer>();
		for (String user : votes.keySet()) {
			if (votes.get(user) > 0)
				up_votes.put(user, 1);
		}
		return up_votes;
	}

	public Map<String, Integer> getDownVotes() {
		Map<String, Integer> down_votes = new HashMap<String, Integer>();
		for (String user : votes.keySet()) {
			if (votes.get(user) < 0)
				down_votes.put(user, -1);
		}
		return down_votes;
	}

	public void comment(String user, String comment) {
		comments.put(user, comment);
	}

	public Map<String, String> comments() {
		return comments;
	}
}
