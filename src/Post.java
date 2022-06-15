import java.util.List;
import java.util.Vector;

public class Post {

	private int id;
	private String title;
	private String content;
	private String author;
	private int up_vote;
	private int down_vote;
	private List<String> comments;

	private static int id_count = 0;

	public Post(String title, String content, String author) {
		this.id = id_count;
		id_count++;
		this.title = title;
		this.content = content;
		this.author = author;
		this.up_vote = 0;
		this.down_vote = 0;
		this.comments = new Vector<String>();
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

	public int getUpVote() {
		return up_vote;
	}

	public void upVote() {
		up_vote++;
	}

	public int getDownVote() {
		return down_vote;
	}

	public void downVote() {
		down_vote++;
	}

	public List<String> comments() {
		return comments;
	}

}
