import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{

    @SuppressWarnings("FieldCanBeLocal")
    private boolean stopped = false;
    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        Socket socket;
        while (!stopped) {
            try {
                socket = serverSocket.accept();
                new Thread( new Client(socket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
