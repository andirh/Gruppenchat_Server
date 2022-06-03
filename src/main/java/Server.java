import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {


    private final int port;

    private final List<ServiceWorker> workers = new ArrayList<>();

    public Server(int port) {
        this.port = port;
    }

    public List<ServiceWorker> getWorkers(){
        return workers;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println("Client connection");
                Socket clientSocket = serverSocket.accept();
                System.out.println("accepted connection from: " + clientSocket);
                ServiceWorker serviceWorker = new ServiceWorker(this, clientSocket);
                workers.add(serviceWorker);
                serviceWorker.start();
            }
        } catch (IOException e) {
            System.out.println("exception");
            e.printStackTrace();
        }
    }

    public void removeWorker(ServiceWorker serviceWorker) {
        workers.remove(serviceWorker);
    }
}
