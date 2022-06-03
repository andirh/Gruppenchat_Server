import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class ServiceWorker extends Thread {


    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private final HashSet<String> rooms = new HashSet<>();

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
                    } else if ("join".equalsIgnoreCase(command)) {
                        //Format: command room
                        handleJoin(tokens);
                    } else if ("leave".equalsIgnoreCase(command)) {
                        //Format: command room
                        handleLeave(tokens);
                    } else if ("msg".equalsIgnoreCase(command)) {
                        //Format: command to message
                        //Format: command #toRoom message
                        handleMessage(StringUtils.split(input, null, 3));
                    } else {
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

    private void handleLeave(String[] tokens) throws IOException {
        if (tokens.length > 1) {
            String room = tokens[1];
            if (this.isMemberOfRoom(room)) {
                rooms.remove(room);
                String message = "You left the room " + room;
                outputStream.write(message.getBytes());
            }
        }
    }

    public boolean isMemberOfRoom(String room) {
        return rooms.contains(room);
    }

    private void handleJoin(String[] tokens) throws IOException {
        if (tokens.length > 1) {
            String room = tokens[1];
            rooms.add(room);
            String message = "You joined the room " + room;
            outputStream.write(message.getBytes());
        }
    }

    private void handleMessage(String[] tokens) throws IOException {
        String to = tokens[1];
        String message = tokens[2];

        boolean isRoomCommand = (to.charAt(0) == '#');

        List<ServiceWorker> workers = server.getWorkers();
        for (ServiceWorker serviceWorker : workers) {
            if (isRoomCommand) {
                if (this.isMemberOfRoom(to)) {
                    if (serviceWorker.isMemberOfRoom(to)) {
                        String output = "Room: " + to + " - Message from " + login + ": " + message + System.lineSeparator();
                        serviceWorker.send(output);
                    }
                }
            } else {
                if (to.equalsIgnoreCase(serviceWorker.getLogin())) {
                    String output = "Message from " + login + ": " + message + System.lineSeparator();
                    serviceWorker.send(output);
                }
            }
        }
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        clientSocket.close();
        List<ServiceWorker> workers = server.getWorkers();
        //send other users the newly logged off user
        String onlineMessage = "User " + login + " disconnected" + System.lineSeparator();
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
