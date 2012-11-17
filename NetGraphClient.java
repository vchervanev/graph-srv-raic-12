import java.lang.SuppressWarnings;

import model.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

import static java.lang.Math.*;

public class NetGraphClient {
    private Socket socket;
    BufferedReader in;
    PrintWriter out;
    World world;
    boolean firstRun = true;

    public NetGraphClient(String host, int port)  {
        try {
            InetAddress ipAddress = InetAddress.getByName(host);
            socket = new Socket(ipAddress, port);
            in = new BufferedReader(new InputStreamReader (socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println(in.readLine());
        } catch (IOException e) {
            socket = null;
            e.printStackTrace();
        }
    }

    public void sendCommand(String format, Object... args) {
        sendCommand(String.format(format,  args));
    }

    public void sendCommand(String command) {
        try {
            out.println(command);
            String result = in.readLine();
            if (!result.equals("OK"))
                System.out.println(result);
        } catch (IOException e) {
            socket = null;
            e.printStackTrace();
        }
    }

    public void drawUnit(Unit unit) {
        long[] x = new long[5];
        long[] y = new long[5];
        double dx = unit.getHeight() / 2;
        double dy = unit.getWidth() / 2;
        double r = sqrt(pow(dx, 2)+ pow(dy, 2));
        double angle = atan(abs(dx/dy));
        double selfAngle = unit.getAngle();

        x[0] = round(unit.getX() + r*cos(selfAngle - angle));
        x[1] = round(unit.getX() + r*cos(selfAngle + angle));
        x[2] = round(unit.getX() + r*cos(selfAngle - PI - angle));
        x[3] = round(unit.getX() + r*cos(selfAngle - PI + angle));
        x[4] = x[0];

        y[0] = round(unit.getY() + r*sin(selfAngle - angle));
        y[1] = round(unit.getY() + r*sin(selfAngle + angle));
        y[2] = round(unit.getY() + r * sin(selfAngle - PI - angle));
        y[3] = round(unit.getY() + r*sin(selfAngle - PI + angle));
        y[4] = y[0];

        for(int i = 0; i < 4; i++) {
            drawLine(x[i], y[i], x[i+1], y[i+1]);
        }
        if (unit instanceof Tank) {
            Tank tank = (Tank)unit;

            double ddx = 5*cos(selfAngle);
            double ddy = 5*sin(selfAngle);
            drawLine(x[2]+ddx, y[2]+ddy, x[3]+ddx, y[3]+ddy);
            drawCircle(unit.getX(), unit.getY(), min(unit.getHeight()/2, unit.getWidth()/2));
            double turretAngle = tank.getTurretRelativeAngle() + selfAngle;

            if (tank.getCrewHealth() > 0 && tank.getHullDurability() > 0) {
                setColor("green");
                ddx = 1000*cos(turretAngle);
                ddy = 1000*sin(turretAngle);
                drawLine(unit.getX(), unit.getY(), unit.getX()+ddx, unit.getY()+ddy);
                setColor("black");
            }

            ddx = 30*cos(turretAngle);
            ddy = 30*sin(turretAngle);
            drawLine(unit.getX(), unit.getY(), unit.getX() + ddx, unit.getY() + ddy);

            ddx = unit.getWidth()/2;
            if (unit.getX() >  world.getWidth()/3)
                ddx *= -1;
            ddy = unit.getHeight()/2 + 50;
            if (unit.getY() >  world.getHeight()/2)
                ddy *= -1;
            drawString(tank.getPlayerName() +"["+ tank.getTeammateIndex()+"]", unit.getX() + ddx, unit.getY() + ddy );

            if (tank.getCrewHealth() != 0) {
                ddy += 15;
                setColor("yellow");
                drawString(String.format("%d/%d", tank.getCrewHealth(), tank.getCrewMaxHealth()), unit.getX() + ddx, unit.getY() + ddy );
            }

            if (tank.getHullDurability() != 0) {
                ddy += 15;
                setColor("blue");
                drawString(String.format("%d/%d", tank.getHullDurability(), tank.getHullMaxDurability()), unit.getX() + ddx, unit.getY() + ddy );
            }

            if (tank.getHullDurability() != 0 && tank.getCrewHealth() != 0){
                ddy += 15;
                setColor("magenta");
                drawString(String.format("%d/%d", tank.getRemainingReloadingTime(), tank.getReloadingTime()), unit.getX() + ddx, unit.getY() + ddy );
            }

            setColor("black");
        }
        if(unit instanceof Shell) {
            double ddx = 850*cos(unit.getAngle());
            double ddy = 850*sin(unit.getAngle());
            setColor("pink");
            drawLine(unit.getX(), unit.getY(), unit.getX() + ddx, unit.getY() + ddy);
            setColor("black");
        }

    }

    protected void drawCircle(double x, double y, double r) {
        sendCommand(String.format("drawArc %d %d %d %d %d %d", round(x-r), round(y-r), round(2*r), round(2*r), 0, 360));
    }

    protected void drawLine(double x1, double y1, double x2, double y2) {
        sendCommand("drawLine %d %d %d %d", round(x1), round(y1), round(x2), round(y2));
    }
    
    protected void setColor(String color) {
        sendCommand("setColor %s", color);
    }

    protected void clearRect(double x, double y, double width, double height) {
        sendCommand("clearRect %d %d %d %d", round(x), round(y), round(width), round(height));
    }

    protected void drawString(String string, double x, double y) {
        sendCommand("drawString %s %d %d", string, round(x), round(y));
    }

    protected void resizeImage(double w, double h) {
        sendCommand("resizeImage %s %d", round(w), round(h));
    }

    public void update(World world) {
        if (socket == null)
            return;
        if (firstRun) {
            resizeImage(world.getWidth(), world.getHeight()+40);
            firstRun = false;
        }
        this.world = world;
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setColor("white");
        sendCommand(String.format("fill3DRect 0 0 %d %d false", round(world.getWidth()), round(world.getHeight())));
        setColor("black");

        for(Tank tank : world.getTanks()) {
            drawUnit(tank);
        }

        for(Bonus bonus : world.getBonuses()) {
            String color;
            switch (bonus.getType()) {
                case AMMO_CRATE:
                    color = "red";
                    break;
                case MEDIKIT:
                    color = "yellow";
                    break;
                case REPAIR_KIT:
                    color = "blue";
                    break;
                default:
                    color = "black";
            }
            setColor(color);
            drawUnit(bonus);
            setColor("black");
        }

        for(Shell shell : world.getShells()) {
            if (shell.getType() == ShellType.PREMIUM) {
                setColor("red");
            }
            drawUnit(shell);
            setColor("black");
        }

        //clearRect(world.getWidth(), 0, 200, world.getHeight());
        clearRect(0, world.getHeight(), world.getWidth(), 40);
        setColor("red");
        drawString(Long.toString(world.getTick()), 10, 820);
        sendCommand("update");

    }
}
