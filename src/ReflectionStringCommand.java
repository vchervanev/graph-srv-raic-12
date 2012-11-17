import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionStringCommand {
    private String result;
    private Graphics2D graphics = (Graphics2D) MainForm.canvas.image.getGraphics();

    public void reflect(String methodName, String ...parameters) throws Exception {
        Method method = getMethod(methodName, this, parameters.length);
        Object instance = this;

        if (method == null) {
            method = getMethod(methodName, graphics, parameters.length);
            instance = graphics;
        }

        if (method == null) {
            result = String.format("Method [%s] with [%d] parameter(s) not found", methodName, parameters.length);
            return;
        }

        Object[] realParameters = new Object[parameters.length];
        Class[] parameterTypes = method.getParameterTypes();
        for(int i = 0; i < parameters.length; i++) {
            realParameters[i] = create(parameterTypes[i], parameters[i]);
        }
        Object objectResult = method.invoke(instance, realParameters);
        result = objectResult == null ? "OK" : objectResult.toString();
    }

    private Method getMethod(String methodName, Object graphics, double parametersCount) {
        Method method = null;
        for(Method aMethod : graphics.getClass().getMethods()) {
            if (aMethod.getName().equals(methodName) && aMethod.getParameterTypes().length == parametersCount) {
                method = aMethod;
                break;
            }
        }
        return method;
    }

    public Object create(Class aClass, String value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (aClass.getName().equals("int")) {
            aClass = Integer.class;
        } else if(aClass.getName().equals("double")) {
            aClass = Double.class;
        } else if(aClass.getName().equals("boolean")) {
            aClass = Boolean.class;
        } else if(aClass.getName().equals("float")) {
            aClass = Float.class;
        }
        @SuppressWarnings("unchecked")
        Constructor constructor = aClass.getConstructor(new Class[]{String.class});
        return constructor.newInstance(value);
    }

    public String execute(String command) {
        System.out.println(command);
        result = "";
        String[] strings = command.split(" ");
        try {
            reflect(strings[0], Arrays.copyOfRange(strings, 1, strings.length));
        }  catch (Exception e)
        {
            e.printStackTrace();
            return  e.getMessage();
        }
        return result;
    }

    @SuppressWarnings("unused")
    public void setColor(String color) throws Exception {
        Field field = Class.forName("java.awt.Color").getField(color);
        graphics.setColor((Color)field.get(null));
    }

    @SuppressWarnings("unused")
    public void update() {
        MainForm.canvas.repaint();
    }

    @SuppressWarnings("unused")
    public void drawString(String string, int x, int y) {
        graphics.drawString(string, x, y);
    }

    @SuppressWarnings("unused")
    public void resizeImage(int w, int h) {
        MainForm.canvas.resizeImage(w, h);
        graphics = (Graphics2D) MainForm.canvas.image.getGraphics();
    }
}
