

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.AnsiControl;


public class TerminalUI {



    public Pixel[][] last_rendered;
    public View view;



    public void render() {
        Pixel[][] pixels = view.render(null, TerminalUI.getTerminalWidth(), TerminalUI.getTerminalHeight());
        render(pixels);
    }

    public void render(Pixel[][] pixels) {
        System.out.print(AnsiControl.CLEAR_SREEN);
        System.out.flush();

        for (int y = 0; y < pixels[0].length; y++) {
            for (int x = 0; x < pixels.length; x++) {
                Pixel pixel = pixels[x][y];
                if (last_rendered == null || !last_rendered[x][y].equals(pixel)) {
                    AnsiControl.setCursor(x + 1, y + 1);
                    System.out.print(
                        AnsiControl.background(pixel.backgroundColor.r, pixel.backgroundColor.g, pixel.backgroundColor.b) + 
                        AnsiControl.color(pixel.textColor.r, pixel.textColor.b, pixel.textColor.g) + 
                        pixel.character
                    );
                }
            }
            System.out.println();
        }

        System.out.print(AnsiControl.RESET);
        System.out.flush();
    }


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

        public Pixel[][] render(View parent, int allotedWidth, int allotedHeight) {
            refreshHeightAndWidth(allotedWidth, allotedHeight);

            Pixel[][] pixels = new Pixel[this.width][this.height];
            if (this.backgroundColor != null) {
                for (int yP = 0; yP < this.height; yP++) {
                    for (int xP = 0; xP < this.width; xP++) {
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
            List<Integer> coors = View.getPixel(this.x, this.xType, this.y, this.yType);
            int pixelX = coors.get(0);
            int pixelY = coors.get(1);

            List<Integer> sizes = View.getPixel(this.width, this.widthType, this.height, this.heightType);
            int pixelWidth = sizes.get(0);
            int pixelHeight = sizes.get(1);

            Pixel[][] renderedPixels = this.render(parent, pixelWidth, pixelHeight);
            for (int xP = 0; xP < pixelWidth; xP++) {
                for (int yP = 0; yP < pixelHeight; yP++) {
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

        public int getContentWidth(Integer allotedWidth, Integer allotedHeight) {
            refreshHeightAndWidth(allotedWidth, allotedHeight);
            return this.width;
        }
        public int getContentHeight(Integer allotedWidth, Integer allotedHeight) {
            refreshHeightAndWidth(allotedWidth, allotedHeight);
            return this.height;
        }
        
        protected void refreshHeightAndWidth(Integer allotedWidth, Integer allotedHeight) {
            // resize view if any size types are set to FIT_CONTENT
            this.setFitContentWidthAndHeight(allotedWidth, allotedHeight);
            
            // if size types are percentage, convert to pixels
            if (this.widthType == UnitType.PERCENTAGE) {
                this.width = (int) (allotedWidth * this.width / 100.0);
            }
            if (this.heightType == UnitType.PERCENTAGE) {
                this.height = (int) (allotedHeight * this.height / 100.0);
            }

            // if size types are pixel, set width and height to pixel values
            if (this.widthType == UnitType.PIXEL) {
                this.width = allotedWidth;
            }
            if (this.heightType == UnitType.PIXEL) {
                this.height = allotedHeight;
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
        protected void setFitContentWidthAndHeight(Integer allotedWidth, Integer allotedHeight) {
            if (this.widthType == UnitType.FIT_CONTENT && this.heightType == UnitType.FIT_CONTENT) {
                if (this.children.isEmpty()) {
                    this.width = 0;
                    this.height = 0;
                }
                else {
                    for (View child : this.children) {
                        int xPixel = child.getPixelX(this, allotedWidth);
                        int yPixel = child.getPixelY(this, allotedHeight);
                        this.width = Math.max(width, child.getContentWidth(allotedWidth, allotedHeight) + xPixel);
                        this.height = Math.max(height, child.getContentHeight(allotedWidth, allotedHeight) + yPixel);
                    }
                }
            }
            else if (widthType == UnitType.FIT_CONTENT) {
                if (this.children.isEmpty()) {
                    this.width = 0;
                }
                else {
                    for (View child : this.children) {
                        int xPixel = child.getPixelX(this, allotedWidth);
                        this.width = Math.max(width, child.getContentWidth(allotedWidth, allotedHeight) + xPixel);
                    }
                }
            }
            else if (heightType == UnitType.FIT_CONTENT) {
                if (this.children.isEmpty()) {
                    this.height = 0;
                }
                else {
                    for (View child : this.children) {
                        int yPixel = child.getPixelY(this, allotedHeight);
                        this.height = Math.max(height, child.getContentHeight(allotedWidth, allotedHeight) + yPixel);
                    }
                }
            }
        }
        
        private static List<Integer> getPixel(int x, UnitType xType, int y, UnitType yType) {
            int xPixel = xType == UnitType.PIXEL ? x : (int) (x * getTerminalWidth() / 100.0);
            int yPixel = yType == UnitType.PIXEL ? y : (int) (y * getTerminalHeight() / 100.0);
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
        public Color textColor = new Color(255, 255, 255); // rgb(255, 255, 255)
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


                // apply text to pixels
                int y;
                if (verticalAlignment == Alignment.TOP) {
                    y = 0;
                }
                else if (verticalAlignment == Alignment.CENTER) {
                    y = this.height / 2;
                }
                else if (verticalAlignment == Alignment.BOTTOM) {
                    y = this.height - 1;
                }
                else {
                    throw new RuntimeException("verticalAlignment can only be TOP, CENTER or BOTTOM");
                }

                if (horizontalAlignment == Alignment.RIGHT) {
                    int x = this.width - 1;
                    for (int i = text.length() - 1; i >= 0; i--) {
                        if (x < 0) {
                            if (!this.wrap) break;
                            if (y < this.height - 1) y++;
                            else break;
                        }
                        pixels[x][y].character = text.charAt(i);
                        x--;
                    }
                }
                else if (horizontalAlignment == Alignment.LEFT || horizontalAlignment == Alignment.CENTER){
                    int x = horizontalAlignment == Alignment.LEFT? 0 : this.width / 2 - text.length() / 2;
                    for (int i = 0; i < text.length(); i++) {
                        if (x > this.width - 1) {
                            if (!this.wrap) break;
                            if (y < this.height - 1) y++;
                            else break;
                        }
                        pixels[x][y].character = text.charAt(i);
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
        protected void setFitContentWidthAndHeight(Integer allotedWidth, Integer allotedHeight) {
            if (this.widthType == UnitType.FIT_CONTENT && this.heightType == UnitType.FIT_CONTENT) {
                this.width = text.length();
                this.height = 1;
            }
            else if (widthType == UnitType.FIT_CONTENT) {
                this.width = text.length();
            }
            else if (heightType == UnitType.FIT_CONTENT) {
                double div = (double) allotedWidth / text.length();
                this.height = (int) Math.ceil(div);
            }
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

        public static Color random() {
            Random random = new Random();
            return new Color(
                random.nextInt(0, 256), 
                random.nextInt(0, 256), 
                random.nextInt(0, 256)
            );
        }
    }

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
}