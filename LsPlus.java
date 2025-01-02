import util.AnsiControl;

public class LsPlus extends TerminalUI {
    public static void main(String[] args) {
        

        LsPlus plus = new LsPlus();
        plus.run();
    }
    
    public void run() {
        
        Text text = new Text("Hi there!");
        text.y = 10;
        text.height = 10;
        text.backgroundColor = new Color(61, 61, 61); // rgb(61, 61, 61)
        text.horizontalAlignment = Text.Alignment.CENTER;

        InputBox inputBox = new InputBox();
        inputBox.y = 20;
        inputBox.height = 10;
        inputBox.backgroundColor = new Color(47, 47, 47); // rgb(47, 47, 47)
        inputBox.horizontalAlignment = Text.Alignment.CENTER;


        this.view = new View();
        this.view.children.add( text );
        this.view.children.add( inputBox );


        this.render();

        // test();

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
        System.out.print(AnsiControl.CLEAR_SREEN);
        System.out.flush();

        Pixel pixel = new Pixel();
        pixel.backgroundColor = new Color(40, 40, 40);
        pixel.textColor = new Color(200, 200, 200);
        pixel.character = 'H';

        System.out.print(
            AnsiControl.background(pixel.backgroundColor.r, pixel.backgroundColor.g, pixel.backgroundColor.b) + 
            AnsiControl.color(pixel.textColor.r, pixel.textColor.b, pixel.textColor.g) + 
            pixel.character
        );
        System.out.print(AnsiControl.RESET);
        System.out.flush();

    }
}
