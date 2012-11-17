import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static java.lang.Math.min;
import static java.lang.Math.round;

public class PaintPanel extends JPanel {

    public BufferedImage image = new BufferedImage(1280, 900, BufferedImage.TYPE_INT_RGB);

    public void resizeImage(int x, int y) {
        image = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dimension = getSize();
        int w = (int)round(min(image.getWidth(), dimension.getWidth()));
        int h = (int)round(min(image.getHeight(), dimension.getHeight()));
        g.drawImage(image,
                0, 0, w, h,
                this);

    }
}

