
public class Winsome {

    private final String name = "WINSOME";
    private RegistratorService rs;

    public Winsome() {
        rs = new RegistratorService(2000);
    }

    public final String getServerName() {
        return name;
    }

    public void start() {
        rs.openService();
    }

    public void shutdown() {
        rs.closeService();
    }

    public static void main(String[] args) {
        Winsome winsome = new Winsome();
        winsome.start();
        winsome.shutdown();
    }
}
