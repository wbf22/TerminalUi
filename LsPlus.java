import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import util.AnsiControl;
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
    private File selectedFile;
    private Map<String, Color> fileColors = new HashMap<>();
    private File[][] cells = new File[0][0];



    public static void main(String[] args) throws IOException {
        // test_weighted();
        

        LsPlus plus = new LsPlus();
        plus.selectedFile = new File(System.getProperty("user.dir"));

        plus.view = new View();

        plus.onResize(TerminalUI.getTerminalWidth(), TerminalUI.getTerminalHeight());


        plus.run();
    }


    @Override
    public void onResize(int width, int height) {

        // set up list views
        int numListViews = width / LIST_WIDTH;
        int percentage = 100 / numListViews;
        this.view.children.clear();
        for (int i = 0; i < numListViews; i++) {
            View listView = new View();
            listView.width = percentage;
            listView.x = i * percentage;
            listView.xType = UnitType.PERCENTAGE;
            this.view.children.add(listView);
        }


        // add files to list views
        int terminalHeight = TerminalUI.getTerminalHeight();
        this.cells = new File[numListViews][terminalHeight];
        int depth = numListViews / 2;


        // SET CELLS
        // get files in current dir and index of selected
        int lastViewSize = 0;
        List<File> thisLevelFiles = listDirectorySortedByDate(this.selectedFile.getParentFile());
        int selectedIndex = thisLevelFiles.indexOf(this.selectedFile);


        Map<Integer, List<File>> depthToFiles = new HashMap<>();
        for (int i = 0; i < numListViews; i++) depthToFiles.put(i, new ArrayList<>());
        Map<File, List<File>> fileToChildren = new HashMap<>();

        int upperIndex = selectedIndex - 1;
        int lowerIndex = selectedIndex + 1;
        while (depthToFiles.get(numListViews).size() > terminalHeight && (upperIndex >= 0 || lowerIndex < thisLevelFiles.size())) {
            if (upperIndex >= 0) {
                File file = thisLevelFiles.get(upperIndex);
                scanDirectory(file, depthToFiles, fileToChildren, depth, numListViews);
                upperIndex--;
            }
            if (lowerIndex < thisLevelFiles.size()) {
                File file = thisLevelFiles.get(lowerIndex);
                scanDirectory(file, depthToFiles, fileToChildren, depth, numListViews);
                lowerIndex++;
            }
        }


        // get file at this level, go all the way down, set cells in reverse backwards




    }

    private void scanDirectory(File file, Map<Integer, List<File>> depthToFiles, Map<File, List<File>> fileToChildren, int depth, int numListViews) {

        
        // do selected file
        depthToFiles.get(depth).add(selectedFile);
        fileToChildren.put(this.selectedFile, listDirectorySortedByDate(this.selectedFile));

        // do parent files
        File currentFile = this.selectedFile;
        for (int i = depth; i >= 0; i--) {
            File parent = currentFile.getParentFile();
            depthToFiles.get(depth - i).add(parent);
            fileToChildren.put(parent, listDirectorySortedByDate(parent));
            currentFile = parent;
        }

        // do children files
        List<File> children = fileToChildren.get(this.selectedFile);
        for (int i = depth; i < numListViews; i++) {
            List<File> newChildren = new ArrayList<>();
            for (File child : children) {
                depthToFiles.get(i).add(child);
                List<File> grandChildren = listDirectorySortedByDate(child);
                fileToChildren.put(child, grandChildren);
                newChildren.addAll(grandChildren);
            }
            children = newChildren;
        }

    }

    // 27 - escape
    // 13 - enter
    // 27 91 68 - left
    // 27 91 67 - right
    // 27 91 65 - up
    // 27 91 66 - down
    private boolean isEscapeSeq = false;
    @Override
    public void onKeyPress(char c) {

        // handle escape sequences
        if (c == 27 || isEscapeSeq) {
            isEscapeSeq = true;
            if (c == 91) {
                // do nothing
            }
            else {
                if (c == 68) {
                    // left
                }
                else if (c == 67) {
                    // right
                }
                else if (c == 65) {
                    // up
                }
                else if (c == 66) {
                    // down
                }
                isEscapeSeq = false;
            }
        }
        // handle any other key press
        else {
            if (c == 'q') {
                this.exit();
            }
        }
    }

    public void exit() {
        this.quit();
        System.out.println("PATH:" + getCurrentDir());

    }

    // HELPER METHODS   
    private List<List<File>> getDescendantGroupsAtDepth(File startingDir, int depth) {
        List<List<File>> fileGroups = new ArrayList<>();
        if (startingDir.isDirectory()) {
            
            // get decendants at depth
            List<File> files = listDirectorySortedByDate(startingDir);
            for (int i = 0; i <= depth; i++) {
                List<File> currentDepthFiles = new ArrayList<>();
                for (File file : files) {
                    if (file.isDirectory()) {
                        currentDepthFiles.addAll(listDirectorySortedByDate(file));
                    }
                }
                files = currentDepthFiles;
            }

            // split files into groups
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                List<File> group = new ArrayList<>(listDirectorySortedByDate(file));
                fileGroups.add(group);
            }

        }

        return fileGroups;
    }

    private List<File> listDirectorySortedByDate(File file) {
        return Arrays.stream(file.listFiles())
            .sorted((fileFirst, fileOther) -> Long.compare(fileFirst.lastModified(), fileOther.lastModified()))
            .toList();
    }

    private String getCurrentDir() {
        String currentDir = this.selectedFile.getAbsolutePath();
        if (!this.selectedFile.isDirectory()) {
            currentDir = this.selectedFile.getParent();
        }
        return currentDir;
    }


    public static Color getRandomColor() {
        Random random = new Random(System.nanoTime());
        return LsPlus.colorCombos.entrySet().stream()
            .skip(random.nextInt(LsPlus.colorCombos.size()))
            .findFirst()
            .get()
            .getKey();
    }

    public static String getDatePretty(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
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


    // HELPER CLASSES
    public static class FileTree {



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
