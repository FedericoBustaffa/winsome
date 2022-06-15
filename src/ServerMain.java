import java.util.Scanner;

public class ServerMain {
	public static void main(String[] args) throws InterruptedException {
		Server winsome = new Server("WINSOME", 4000);
		winsome.start();
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
		winsome.shutdown();
	}
}
