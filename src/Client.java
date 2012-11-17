import java.io.*;
import java.net.Socket;

public class Client implements Runnable {

    private final Socket clientSocket;
    private final ReflectionStringCommand command = new ReflectionStringCommand();
    public Client(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    @Override
    public void run() {
        try {
            System.out.println("Client connected");
            BufferedReader in = new BufferedReader(new InputStreamReader (clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String response;
            out.println("Welcome!");
            String line = in.readLine();
            while (line != null && !line.equalsIgnoreCase("exit")) {
                if (line.isEmpty()) {
                    response = "Send \"exit\" to disconnect";
                } else {
                    response = command.execute(line);
                }
                out.println(response);
                line = in.readLine();
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Client disconnected");

    }
}
