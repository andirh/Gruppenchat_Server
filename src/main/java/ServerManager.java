import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerManager {

    public static void main(String[] args) {

        int port = 5000;
        Server server = new Server(port);

        server.start();
            /*out = new PrintWriter(clientSocket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Thread sender = new Thread(new Runnable() {
                String message; //Nachricht des Nutzers

                @Override
                public void run() {
                    while (true) {
                        message = scanner.nextLine(); //Input lesen
                        out.println(message);    // daten lagern
                        out.flush();   // daten senden
                    }
                }
            });
            sender.start();

            Thread receive = new Thread(new Runnable() {
                String msg;

                @Override
                public void run() {
                    try {
                        msg = in.readLine();

                        while (msg != null) {
                            System.out.println("Client : " + msg);
                            msg = in.readLine();
                        }

                        System.out.println("Client disconnected");

                        out.close();
                        clientSocket.close();
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            receive.start();

             */


    }


}