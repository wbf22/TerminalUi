import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import util.AnsiControl;
import util.Pair;
import util.RandomPlus;

public class LsPlus extends TerminalUI {

    private static int LIST_WIDTH = 30; // 30 px
    private static Map<Color, Color> colorCombos = Map.ofEntries(
        // rgb(218, 185, 126) -> rgb(79, 26, 20)
        // rgb(137, 49, 89) -> rgb(15, 46, 17)
        // rgb(24, 24, 24) -> rgb(202, 202, 202)
        // rgb(39, 75, 134) -> rgb(186, 132, 108)
        // rgb(173, 33, 33) -> rgb(200, 165, 117)
        // rgb(226, 221, 131) -> rgb(62, 30, 64)
        Map.entry(new Color(218, 185, 126), new Color(79, 26, 20)),
        Map.entry(new Color(137, 49, 89), new Color(15, 46, 17)),
        Map.entry(new Color(24, 24, 24), new Color(202, 202, 202)),
        Map.entry(new Color(39, 75, 134), new Color(186, 132, 108)),
        Map.entry(new Color(173, 33, 33), new Color(200, 165, 117)),
        Map.entry(new Color(226, 221, 131), new Color(62, 30, 64))
        
    );



    public static void main(String[] args) throws IOException {
        test_weighted();

        // LsPlus plus = new LsPlus();


        // plus.view = new View();

        // plus.onResize(TerminalUI.getTerminalWidth(), TerminalUI.getTerminalHeight());


        // plus.run();
    }


    @Override
    public void onResize(int width, int height) {

        int numListViews = width / LIST_WIDTH;
        int percentage = 100 / numListViews;
        for (int i = 0; i < numListViews; i++) {
            View listView = new View();
            double dullness = RandomPlus.weighted(0, 1.0, 0.8, 0.5);
            listView.backgroundColor = Color.random().dull(dullness);
            listView.width = percentage;
            listView.x = i * percentage;
            listView.xType = UnitType.PERCENTAGE;
            this.view.children.add(listView);
        }



        // Pair<Color, Color> colorCombo = colorCombos.get(
        //     new Random(System.currentTimeMillis()).nextInt(colorCombos.size())
        // );
        // listView.backgroundColor = colorCombo.first;
    }


    public static Color getRandomColor() {
        Random random = new Random(System.nanoTime());
        return LsPlus.colorCombos.entrySet().stream()
            .skip(random.nextInt(LsPlus.colorCombos.size()))
            .findFirst()
            .get()
            .getKey();
    }

    public static Color getText(Color backgroundColor) {
        // rgb(250, 230, 0) -> rgb(220, 200, 80)
        Color complimentary = Color.of(255 - backgroundColor.r, 255 - backgroundColor.g, 255 - backgroundColor.b);
        int average = (backgroundColor.r + backgroundColor.g + backgroundColor.b) / 3;
        boolean isDark = average < 128;
        if (isDark) {
            return complimentary.dull(0.2).lighten(0.2);
        }
        else {
            return complimentary.dull(0.2).darken(0.2);
        }
    }




    // TEST METHODS DELETABLE

    public static void test_weighted() {
        double total = 0;
        for (int i = 0; i < 100; i++) {
            double d = RandomPlus.weighted(0, 1.0, 0.8, 0.1);
            total += d;
            System.out.println(d);
        }
        System.out.println("Average");
        System.out.println(total / 100);
    }

    public static void test_color_hash() {
        Map<Integer, Color> colors = new HashMap<>();
        for (int r = 0; r < 256; r++) {
            for (int g = 0; g < 256; g++) {
                for (int b = 0; b < 256; b++) {
                    Color c = new Color(r, g, b);
                    if (colors.containsKey(c.hashCode())) {
                        System.out.println("Duplicate color: " + c);
                        Color dup = colors.get(c.hashCode());
                        System.out.println(dup);
                        System.out.println(c.hashCode());
                        System.out.println(dup.hashCode());
                    }
                    colors.put(c.hashCode(), c);
                }
            }
        }


    }
    
    public static void test_corners() {
        System.out.print(AnsiControl.CLEAR_SREEN);
        System.out.flush();

        int width = getTerminalWidth();
        int height = getTerminalHeight();

        AnsiControl.setCursor(0, 0);
        System.out.print(AnsiControl.background(45, 152, 171) + "S"); // rgb(45, 152, 171)
        System.out.flush();

        AnsiControl.setCursor(0, height-1);
        System.out.print(AnsiControl.background(100, 152, 171) + "T"); // rgb(100, 45, 171)
        System.out.flush();

        AnsiControl.setCursor(width-1, 0);
        System.out.print(AnsiControl.background(156, 152, 171) + "R"); // rgb(156, 171, 45)
        System.out.flush();

        AnsiControl.setCursor(width-1, height-1);
        System.out.print(AnsiControl.background(23, 240, 150) + "E"); // rgb(23, 240, 150)
        System.out.flush();

        System.out.print(AnsiControl.RESET);
        System.out.flush();

    }

    public static void test_diagonal() {
        System.out.print(AnsiControl.CLEAR_SREEN);
        System.out.flush();

        int width = getTerminalWidth();

        for (int i = 0; i < width; i++) {
            AnsiControl.setCursor(i, i + 1);
            System.out.print(AnsiControl.background(45, 152, 171) + "S"); // rgb(45, 152, 171)
            System.out.flush();
        }

        System.out.print(AnsiControl.RESET);
        System.out.flush();
        
    }


    public static void test_one_pixel() {

        // rgb(0, 255, 0)
        // rgb(255, 0, 0)
        // rgb(0, 0, 255)
        

        System.out.print(AnsiControl.CLEAR_SREEN);
        System.out.flush();

        Pixel pixel = new Pixel();
        pixel.backgroundColor = new Color(0, 0, 0);
        pixel.textColor = new Color(0, 255, 0); // rgb(0, 255, 0)
        pixel.character = 'H';

        System.out.print(
            AnsiControl.background(pixel.backgroundColor.r, pixel.backgroundColor.g, pixel.backgroundColor.b) + 
            AnsiControl.color(pixel.textColor.r, pixel.textColor.g, pixel.textColor.b) + 
            pixel.character
        );
        System.out.print(AnsiControl.RESET);
        System.out.flush();

    }

    public static void test_text_color() {
        String background = "\u001B[48;2;0;0;0m";
        String textColor = "\u001B[38;2;0;255;0m";
        
        String msg = background + textColor + "Hello, World!";
        System.out.println(msg);
        System.out.println(AnsiControl.RESET);


        String test_msg = AnsiControl.background(0,0,0) + AnsiControl.color(0,255,0) + 'H';
        System.out.println(test_msg);
        System.out.println(AnsiControl.RESET);


        Pixel pixel = new Pixel();
        pixel.backgroundColor = new Color(0, 0, 0);
        pixel.textColor = new Color(0, 255, 0); // rgb(0, 255, 0)
        pixel.character = 'H';

        String test_2_msg = AnsiControl.background(pixel.backgroundColor.r, pixel.backgroundColor.g, pixel.backgroundColor.b) + 
                            AnsiControl.color(pixel.textColor.r, pixel.textColor.g, pixel.textColor.b) + 
                            pixel.character;

        System.out.print(test_2_msg);
        System.out.print(AnsiControl.RESET);
        System.out.flush();

    }
}
