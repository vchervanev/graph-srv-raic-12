import javax.swing.*;
import java.io.IOException;

public class MainForm extends JFrame {

    public static final PaintPanel canvas = new PaintPanel();
    public MainForm() {
        setTitle("Net Graph Server");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        add(canvas);
        try {
            new Thread(new Server(8888)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainForm ex = new MainForm();
                ex.setVisible(true);

            }
        });
    }
}
