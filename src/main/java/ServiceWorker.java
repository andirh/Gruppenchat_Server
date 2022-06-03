import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServiceWorker extends Thread {


    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;

    public ServiceWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        handleClientSocket();
    }


    public String getLogin() {
        return login;
    }

    private void handleClientSocket() {
        try {
            InputStream inputStream = clientSocket.getInputStream();
            this.outputStream = clientSocket.getOutputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String input;

            while ((input = bufferedReader.readLine()) != null) {
                String[] tokens = StringUtils.split(input);
                if (tokens != null && tokens.length > 0) {
                    String command = tokens[0];
                    if ("logoff".equalsIgnoreCase(command) || "quit".equalsIgnoreCase(command) || "exit".equalsIgnoreCase(command)) {
                        //Format: command
                        handleLogoff();
                        break;
                    } else if ("login".equalsIgnoreCase(command)) {
                        //Format: command name password
                        handleLogin(outputStream, tokens);
                    } else if ("msg".equalsIgnoreCase(command)){
                        //Format: command to message
                        handleMessage(tokens);
                    }
                    else {
                        String message = "unknown: " + command + System.lineSeparator();
                        outputStream.write(message.getBytes());
                    }
                }
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(String[] tokens) throws IOException {
        String to = tokens[1];
        StringBuilder message = new StringBuilder();
        for (int i = 2; i < tokens.length; i++){
            message.append(tokens[i]).append(" ");
        }

        List<ServiceWorker> workers = server.getWorkers();
        for (ServiceWorker serviceWorker : workers) {
            if (to.equalsIgnoreCase(serviceWorker.getLogin())){
                String output = "msg " + login + " " + message + System.lineSeparator();
                serviceWorker.send(output);
            }
        }
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        clientSocket.close();
        List<ServiceWorker> workers = server.getWorkers();
        //send other users the newly logged off user
        String onlineMessage = "disconnected: " + login + System.lineSeparator();
        for (ServiceWorker serviceWorker : workers) {
            if (!login.equals(serviceWorker.getLogin())) {
                serviceWorker.send(onlineMessage);
            }
        }
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String name = tokens[1];
            String password = tokens[2];

            if ((name.equals("guest") && password.equals("guest")) || (name.equals("admin") && password.equals("admin"))) {
                String message = "Logged in as " + name + System.lineSeparator();
                outputStream.write(message.getBytes());
                this.login = name;
                System.out.println("User " + login + " logged in successfully");
                List<ServiceWorker> workers = server.getWorkers();

                //send current user all other logged in users
                for (ServiceWorker serviceWorker : workers) {
                    if (serviceWorker.getLogin() != null) {
                        if (!login.equals(serviceWorker.getLogin())) {
                            String loginMessage = "online: " + serviceWorker.getLogin() + System.lineSeparator();
                            send(loginMessage);
                        }
                    }
                }
                //send other users the newly logged in user
                String onlineMessage = "online: " + login + System.lineSeparator();
                for (ServiceWorker serviceWorker : workers) {
                    if (!login.equals(serviceWorker.getLogin())) {
                        serviceWorker.send(onlineMessage);
                    }
                }

            } else {
                String message = "Error occurred while logging in" + System.lineSeparator();
                send(message);
            }
        }
    }

    private void send(String message) throws IOException {
        if (login != null) {
            outputStream.write(message.getBytes());
        }
    }

}
