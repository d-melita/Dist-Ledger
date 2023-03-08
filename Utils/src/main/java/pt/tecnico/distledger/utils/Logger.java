package pt.tecnico.distledger.utils;

public final class Logger {
    private static boolean active;

    public static void setlogger() {
        Logger.active = (System.getProperty("debug") != null);
    }

    public static void log(Object o) {
        if (active) {
            System.err.println(o.toString());
        }
    }
}
