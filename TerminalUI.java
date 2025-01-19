

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

import util.AnsiControl;


public abstract class TerminalUI {



    public Pixel[][] last_rendered;
    public View view;
    public double refreshRateFPS = 20;
    public boolean debug = false;
    public boolean isRunning = false;
    public boolean renderNextFrame = false;



    public void run() throws IOException {
        // catches kill signal
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cleanUp();
            }
        });

        // main loop
        try {
            this.isRunning = true;
            System.out.print(AnsiControl.HIDE_CURSOR);
            this.render();
    
            // start user input thread
            InputThread inputThread = new InputThread();
            inputThread.start();
    
            // start main loop
            long lastLoop = System.currentTimeMillis();
            int lastWidth = getTerminalWidth();
            int lastHeight = getTerminalHeight();
            while(this.isRunning) {
    
                // on key check
                if (!inputThread.queue.isEmpty()) {
                    Character c = inputThread.queue.pop();
                    this.onKeyPress(c);
                }
    
                // check for resize
                int width = getTerminalWidth();
                int height = getTerminalHeight();
                if (width != lastWidth || height != lastHeight) {
                    lastWidth = width;
                    lastHeight = height;
                    this.onResize(width, height);
                    this.render();
                }
    
                // render if a render was triggered
                if (renderNextFrame) {
                    this.commit();
                    renderNextFrame = false;
                }
    
                // call every frame method (possibly overriden by user)
                this.everyFrame(System.currentTimeMillis() - lastLoop);
    
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            cleanUp();
        }

        this.isRunning = false;
        cleanUp();
    }

    public static class InputThread extends Thread {

        public ConcurrentLinkedDeque<Character> queue = new ConcurrentLinkedDeque<>(); 

        @Override
        public void run() {
            while(true) {
                try {
                    char c = (char) System.in.read();
                    queue.addLast(c);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // RENDER METHODS
    /*
     * Renders the view assigned to this class to the terminal.
     */
    public void render() {
        if (!this.isRunning) {
            commit();
        }
        else {
            this.renderNextFrame = true;
        }
    }
    private void commit() {
        Pixel[][] pixels = view.render(null, TerminalUI.getTerminalWidth(), TerminalUI.getTerminalHeight());
        write(pixels);
    }

    /*
     * Writes a pixel array to the terminal.
     */
    public void write(Pixel[][] pixels) {
        System.out.flush();

        for (int y = 0; y < pixels[0].length; y++) {
            for (int x = 0; x < pixels.length; x++) {
                Pixel pixel = pixels[x][y];
                if (last_rendered == null || !last_rendered[x][y].equals(pixel)) {
                    AnsiControl.setCursor(x + 1, y + 1);
                    System.out.print(
                        AnsiControl.background(pixel.backgroundColor.r, pixel.backgroundColor.g, pixel.backgroundColor.b) + 
                        AnsiControl.color(pixel.textColor.r, pixel.textColor.g, pixel.textColor.b) + 
                        pixel.character
                    );
                }
            }
            System.out.println();
        }

        System.out.print(AnsiControl.RESET);
        System.out.flush();
        last_rendered = pixels;
    }


    // UTIL METHODS
    public static int getTerminalWidth() {
        int width = 80; // Default width
        try {
            Process process = new ProcessBuilder("sh", "-c", "tput cols 2> /dev/tty").start();
            process.waitFor();
            width = Integer.parseInt(new String(process.getInputStream().readAllBytes()).trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return width;
    }

    public static int getTerminalHeight() {
        int height = 24; // Default height
        try {
            Process process = new ProcessBuilder("sh", "-c", "tput lines 2> /dev/tty").start();
            process.waitFor();
            height = Integer.parseInt(new String(process.getInputStream().readAllBytes()).trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return height - 1;
    }

    private static void cleanUp() {
        System.out.print(AnsiControl.CLEAR_SREEN);
        System.out.print(AnsiControl.SHOW_CURSOR);
        System.out.print(AnsiControl.RESET);
    }

    /**
     * Ends the ui and clears the terminal
     */
    public void quit() {
        this.isRunning = false;
    }

    // EVENT METHODS TO OVERRIDE
    public void onKeyPress(char c) {
        System.out.print(c);
    }

    public void onResize(int width, int height) {
        
    }

    public void everyFrame(double deltaTimeMs) {
        // System.out.print(deltaTimeMs);
    }



    // Classes

    public enum UnitType {
        PIXEL,
        PERCENTAGE,
        FIT_CONTENT
    }

    public static class View {
        // sizing 
        public UnitType widthType = UnitType.PERCENTAGE;
        public int width = 100;
        public UnitType heightType = UnitType.PERCENTAGE;
        public int height = 100;

        // positioning
        public UnitType xType = UnitType.PERCENTAGE;
        public int x = 0;
        public UnitType yType = UnitType.PERCENTAGE;
        public int y = 0;

        // background
        Color backgroundColor = new Color(7, 13, 18); // rgb(7, 13, 18)

        // children
        public List<View> children = new ArrayList<>();

        // user interaction
        public boolean isSelectable = false;
        Color selectedColor = new Color(13, 23, 32); // rgb(13, 23, 32)


        public Pixel[][] render(View parent, int allotedWidth, int allotedHeight) {
            int pixelWidth = getPixelWidth(allotedWidth, allotedHeight);
            int pixelHeight = getPixelHeight(allotedWidth, allotedHeight);

            Pixel[][] pixels = new Pixel[pixelWidth][pixelHeight];
            if (this.backgroundColor != null) {
                for (int yP = 0; yP < pixelHeight; yP++) {
                    for (int xP = 0; xP < pixelWidth; xP++) {
                        pixels[xP][yP] = new Pixel();
                        pixels[xP][yP].backgroundColor = backgroundColor;
                    }
                }
            }

            for (View child : this.children) {
                child.copyToArray(this, pixels);
            }
            return pixels;
        }

        protected void copyToArray(View parent, Pixel[][] pixels) {
            Integer allotedWidth = parent.getPixelWidth(pixels.length, pixels[0].length);
            Integer allotedHeight = parent.getPixelHeight(pixels.length, pixels[0].length);

            List<Integer> coors = View.getPixel(this.x, this.xType, this.y, this.yType, allotedWidth, allotedHeight);
            int pixelX = coors.get(0);
            int pixelY = coors.get(1);

            Pixel[][] renderedPixels = this.render(parent, allotedWidth, allotedHeight);
            for (int xP = 0; xP < renderedPixels.length; xP++) {
                for (int yP = 0; yP < renderedPixels[0].length; yP++) {
                    if (renderedPixels[xP][yP] != null) {
                        int xCoor = xP + pixelX;
                        int yCoor = yP + pixelY;
                        if (xCoor >= 0 && xCoor < pixels.length && yCoor >= 0 && yCoor < pixels[0].length) {
                            pixels[xCoor][yCoor] = renderedPixels[xP][yP];
                        }
                    }
                }
            }
        }

        public int getPixelWidth(Integer allotedWidth, Integer allotedHeight) {
            if (this.widthType == UnitType.FIT_CONTENT) {
                return getFitContentWidth(allotedWidth, allotedHeight);
            }
            else {
                List<Integer> dims = View.getPixel(this.width, this.widthType, this.height, this.heightType, allotedWidth, allotedHeight);
                int pixelWidth = dims.get(0);
                return pixelWidth;
            }
        }
        public int getPixelHeight(Integer allotedWidth, Integer allotedHeight) {
            if (this.heightType == UnitType.FIT_CONTENT) {
                return getFitContentHeight(allotedWidth, allotedHeight);
            }
            else {
                List<Integer> dims = View.getPixel(this.width, this.widthType, this.height, this.heightType, allotedWidth, allotedHeight);
                int pixelHeight = dims.get(1);
                return pixelHeight;
            }
        }
        
        protected Integer getPixelX(View parent, Integer parentWidth) {

            // check for weirdness
            if (parent.widthType == UnitType.FIT_CONTENT && this.xType == UnitType.PERCENTAGE) {
                throw new RuntimeException("Cannot set x to percentage if parent width is FIT_CONTENT");
            }

            // if size types are percentage, convert to pixels
            if (this.xType == UnitType.PERCENTAGE) {
                return (int) (parentWidth * this.x / 100.0);
            }
            else {
                // if size types are pixel, then they are already set
                return this.x;
            }

        }
        protected Integer getPixelY(View parent, Integer parentHeight) {

            // check for weirdness
            if (parent.heightType == UnitType.FIT_CONTENT && this.yType == UnitType.PERCENTAGE) {
                throw new RuntimeException("Cannot set y to percentage if parent height is FIT_CONTENT");
            }

            // if size types are percentage, convert to pixels
            if (this.yType == UnitType.PERCENTAGE) {
                return (int) (parentHeight * this.y / 100.0);
            }
            else {
                // if size types are pixel, then they are already set
                return this.y;
            }
        }
        protected Integer getFitContentWidth(Integer allotedWidth, Integer allotedHeight) {
            if (this.children.isEmpty()) {
                return 0;
            }
            else {
                int width = 0;
                for (View child : this.children) {
                    int xPixel = child.getPixelX(this, allotedWidth);
                    width = Math.max(width, child.getPixelWidth(allotedWidth, allotedHeight) + xPixel);
                }
                return width;
            }
        }
        protected Integer getFitContentHeight(Integer allotedWidth, Integer allotedHeight) {
            if (this.children.isEmpty()) {
                return 0;
            }
            else {
                int height = 0;
                for (View child : this.children) {
                    int yPixel = child.getPixelY(this, allotedHeight);
                    height = Math.max(height, child.getPixelHeight(allotedWidth, allotedHeight) + yPixel);
                }
                return height;
            }
        }


        private static List<Integer> getPixel(int x, UnitType xType, int y, UnitType yType, Integer allotedWidth, Integer allotedHeight) {
            int xPixel = xType == UnitType.PIXEL ? x : (int) Math.ceil(x * allotedWidth / 100.0);
            int yPixel = yType == UnitType.PIXEL ? y : (int) Math.ceil(y * allotedHeight / 100.0);
            return List.of(xPixel, yPixel);
        }
    }

    public static class Text extends View {
        public enum Alignment {
            LEFT,
            CENTER,
            RIGHT,
            TOP,
            BOTTOM
        }

        public String text = "";
        public Color textColor = new Color(225, 225, 225); // rgb(225, 225, 225)
        public Alignment horizontalAlignment = Alignment.LEFT;
        public Alignment verticalAlignment = Alignment.TOP;
        public boolean wrap = false;



        public Text() {}

        public Text(String text) {
            this.text = text;
        }

        @Override
        public Pixel[][] render(View parent, int allotedWidth, int allotedHeight) {
            Pixel[][] pixels = super.render(parent, allotedWidth, allotedHeight);

            if (this.text != null) {

                // get pixel width and height
                int pixelWidth = pixels.length;
                int pixelHeight = pixels[0].length;


                // apply text to pixels
                int y;
                if (verticalAlignment == Alignment.TOP) {
                    y = 0;
                }
                else if (verticalAlignment == Alignment.CENTER) {
                    y = pixelHeight / 2;
                }
                else if (verticalAlignment == Alignment.BOTTOM) {
                    y = pixelHeight - 1;
                }
                else {
                    throw new RuntimeException("verticalAlignment can only be TOP, CENTER or BOTTOM");
                }

                if (horizontalAlignment == Alignment.RIGHT) {
                    int x = pixelWidth - 1;
                    for (int i = text.length() - 1; i >= 0; i--) {
                        if (x < 0) {
                            if (!this.wrap) break;
                            if (y < pixelHeight - 1) y++;
                            else break;
                        }
                        pixels[x][y].character = text.charAt(i);
                        pixels[x][y].textColor = this.textColor;
                        x--;
                    }
                }
                else if (horizontalAlignment == Alignment.LEFT || horizontalAlignment == Alignment.CENTER){
                    int x = horizontalAlignment == Alignment.LEFT? 0 : pixelWidth / 2 - text.length() / 2;
                    for (int i = 0; i < text.length(); i++) {
                        if (x > pixelWidth - 1) {
                            if (!this.wrap) break;
                            if (y < pixelHeight - 1) y++;
                            else break;
                        }
                        pixels[x][y].character = text.charAt(i);
                        pixels[x][y].textColor = this.textColor;
                        x++;
                    }
                } 
                else {
                    throw new RuntimeException("horizontalAlignment can only be LEFT, CENTER or RIGHT");
                }
            }

            return pixels;
        }


        @Override
        protected Integer getFitContentWidth(Integer allotedWidth, Integer allotedHeight) {
            return text.length();
        }

        @Override
        protected Integer getFitContentHeight(Integer allotedWidth, Integer allotedHeight) {
            if (this.widthType == UnitType.FIT_CONTENT) {
                return 1;
            }
            else {
                double div = (double) text.length() / allotedWidth;
                return (int) Math.ceil(div);
            }
        }




    }

    public static class InputBox extends Text {
        public String hint = "Hint";
        public Color hintColor = new Color(125, 125, 125); // rgb(125, 125, 125)


        @Override
        public Pixel[][] render(View parent, int allotedWidth, int allotedHeight) {
            // if text is empty, show hint
            if (text.isEmpty()) {
                this.text = this.hint;
            }

            // set text to hint color
            Pixel[][] pixels = super.render(parent, allotedWidth, allotedHeight);
            for (int x = 0; x < pixels.length; x++) {
                for (int y = 0; y < pixels[0].length; y++) {
                    if (pixels[x][y].textColor != null) {
                        pixels[x][y].textColor = this.hintColor;
                    }
                }
            }
            return pixels;
        }


    }


    public static class Pixel {

        public char character = ' ';
        public Color backgroundColor = new Color(40, 40,40);
        public Color textColor = new Color(200, 200, 200); // rgb(200, 200, 200)

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Pixel)) {
                return false;
            }
            Pixel pixel = (Pixel) obj;
            return character == pixel.character && 
                backgroundColor.r == pixel.backgroundColor.r && 
                backgroundColor.g == pixel.backgroundColor.g && 
                backgroundColor.b == pixel.backgroundColor.b && 
                textColor.r == pixel.textColor.r && 
                textColor.g == pixel.textColor.g && 
                textColor.b == pixel.textColor.b;
        }
    }

    public static class Color {
        public int r;
        public int g;
        public int b;
        

        public Color() {
            r = 0;
            g = 0;
            b = 0;
        }

        public Color(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public static Color of(int r, int g, int b) {
            return new Color(r, g, b);
        }

        public static Color random() {
            Random random = new Random();
            return new Color(
                random.nextInt(0, 256), 
                random.nextInt(0, 256), 
                random.nextInt(0, 256)
            );
        }


        public Color dull(double strength) {
            int average = this.r + this.g + this.b / 3;
            int nr = (int) (this.r * (1 - strength) + average * strength);
            int ng = (int) (this.g * (1 - strength) + average * strength);
            int nb = (int) (this.b * (1 - strength) + average * strength);
            return Color.of(nr, ng, nb);
        }

        public Color darken(double strength) {
            double div = 1 - strength;
            int nr = (int) (this.r * div);
            int ng = (int) (this.g * div);
            int nb = (int) (this.b * div);
            return Color.of(nr, ng, nb);
        }

        public Color lighten( double strength) {
            int nr = (int) (255 * strength + this.r * (1 - strength));
            int ng = (int) (255 * strength + this.g * (1 - strength));
            int nb = (int) (255 * strength + this.b * (1 - strength));
            return Color.of(nr, ng, nb);
        }


        @Override
        public int hashCode() {
            int shift = r << 32;
            int hash = shift ^ g;
            shift = g << 13;
            hash = hash ^ shift;
            shift = b << 21;
            hash = hash ^ shift;

            return hash;
        }

        @Override
        public String toString() {
            return "rgb(" + r + ", " + g + ", " + b + ")";
        }
    }

}