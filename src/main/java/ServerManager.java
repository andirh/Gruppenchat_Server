

public class ServerManager {

    public static void main(String[] args) {

        int port = 5000;
        Server server = new Server(port);
        server.start();

    }


}