package util;

public enum AnsiControl {

    
    RESET("\u001B[0m"),
    CLEAR_SREEN("\u001B[2J\u001B[H"),
    RESET_FONT_SIZE("\u001B[0m"),
    SIZE("\u001B[=18h"),
    SET_FONT_SIZE("\u001B[");

    
    // public static String CLEAR_SREEN = "\u001B[2J\u001B[H";
    // public static String RESET_FONT_SIZE = "\u001B[0m";
    // public static String SET_FONT_SIZE = "\u001B[";


    private final String code;
    AnsiControl(String code) {
        this.code = code;
    }


    public static void setCursor(int x, int y) {
        System.out.print("\u001B[" + y + ";" + x + "H");
    }

    public static String color(int r, int g, int b) {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }

    public static String color(int x) {
        return "\u001B[38;5;" + x + "m";
    }

    public static String background(int r, int g, int b) {
        return "\u001B[48;2;" + r + ";" + g + ";" + b + "m";
    }

    public static String background(int x) {
        return "\u001B[48;5;" + x + "m";
    }


    @Override
    public String toString() {
        return code;
    }
}
