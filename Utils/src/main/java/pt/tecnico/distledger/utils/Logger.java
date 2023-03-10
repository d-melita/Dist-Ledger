package pt.tecnico.distledger.utils;
import java.lang.reflect.*;

public final class Logger {
    private static boolean active = (System.getProperty("debug") != null);

    public static void log(Object o) {
        if (active) {
            String className[] = Thread.currentThread().getStackTrace()[2].getClassName().split("[.]");
            System.err.println("[" + className[className.length - 1].toUpperCase() + "]: " + o);
        }
    }
}
