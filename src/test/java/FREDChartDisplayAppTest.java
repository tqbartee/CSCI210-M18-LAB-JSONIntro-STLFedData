import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple, resilient grading tests for FREDChartDisplayApp.
 * Designed to accommodate structural and naming variations across AI-generated
 * student submissions while verifying all core prompt requirements without
 * hanging on modal Swing dialogs or failing in headless grading environments.
 */
public class FREDChartDisplayAppTest {

    /**
     * Reads all Java source files in src/main/java so the test works whether
     * the AI placed helper classes inside FREDChartDisplayApp.java or in separate files.
     */
    private String loadAllMainJavaSources() throws IOException {
        Path srcDir = Paths.get("src", "main", "java");
        assertTrue(Files.exists(srcDir), "Directory src/main/java must exist.");

        Path mainAppFile = srcDir.resolve("FREDChartDisplayApp.java");
        assertTrue(Files.exists(mainAppFile), "src/main/java/FREDChartDisplayApp.java must exist.");

        try (Stream<Path> paths = Files.walk(srcDir)) {
            return paths
                    .filter(p -> p.toString().endsWith(".java"))
                    .map(p -> {
                        try {
                            return Files.readString(p, StandardCharsets.UTF_8);
                        } catch (IOException e) {
                            return "";
                        }
                    })
                    .collect(Collectors.joining("\n"));
        }
    }

    @Test
    @DisplayName("1. FREDChartDisplayApp compiles and has a valid public static void main(String[]) entry point")
    public void testClassAndMainMethodExist() throws Exception {
        Class<?> appClass = assertDoesNotThrow(
                () -> Class.forName("FREDChartDisplayApp"),
                "FREDChartDisplayApp class must compile and exist in the default package."
        );

        assertTrue(Modifier.isPublic(appClass.getModifiers()),
                "FREDChartDisplayApp class should be public.");

        Method mainMethod = assertDoesNotThrow(
                () -> appClass.getMethod("main", String[].class),
                "FREDChartDisplayApp must declare public static void main(String[] args)."
        );

        assertTrue(Modifier.isStatic(mainMethod.getModifiers()),
                "main method must be static.");
        assertEquals(void.class, mainMethod.getReturnType(),
                "main method must return void.");
    }

    @Test
    @DisplayName("2. Source uses required libraries (Swing, org.json, Jackson, TreeMap) and FRED endpoints/directories")
    public void testRequiredLibrariesAndDataStructures() throws IOException {
        String source = loadAllMainJavaSources();

        // Required data structure: Java TreeMap
        assertTrue(source.contains("TreeMap"),
                "Application must use Java TreeMap to store observations data.");

        // Required JSON libraries: org.json and Jackson
        assertTrue(source.contains("org.json") || source.contains("JSONObject"),
                "Application must use org.json (JSONObject/JSONArray) for JSON parsing.");
        assertTrue(source.contains("jackson") || source.contains("ObjectMapper"),
                "Application must use Jackson (ObjectMapper) for pretty-printed JSON output.");

        // Required GUI & threading: Swing
        assertTrue(source.contains("javax.swing") && source.contains("SwingUtilities"),
                "Application must use Java Swing and proper SwingUtilities threading conventions.");

        // Required FRED API endpoints and JSON keys
        assertTrue(source.contains("api.stlouisfed.org/fred") && source.contains("observations"),
                "Application must query the FRED API series and observations endpoints.");

        // Required output directories and CSV header convention
        assertTrue(source.contains("FREDData-JSON"),
                "Application must reference the datafiles/FREDData-JSON directory.");
        assertTrue(source.contains("FREDData-CSV"),
                "Application must reference the datafiles/FREDData-CSV directory.");
        assertTrue(source.contains("_Value"),
                "CSV output must format the header with '{symbol}_Value'.");
    }

    @Test
    @DisplayName("3. Application includes the three required menu options and chart plotting")
    public void testMenuOptionsAndChartFeatures() throws IOException {
        String sourceLower = loadAllMainJavaSources().toLowerCase();

        // Menu bar & items
        assertTrue(sourceLower.contains("jmenubar") && sourceLower.contains("jmenuitem"),
                "Application must construct a Swing menu bar (JMenuBar / JMenuItem).");

        // 3.1 Fetch FRED Data
        assertTrue(sourceLower.contains("fetch fred data"),
                "Menu must include the 'Fetch FRED Data' option.");

        // 3.2 Display FRED Data from Saved File (allow slight phrasing variation)
        assertTrue(sourceLower.contains("display fred data") || sourceLower.contains("saved file"),
                "Menu must include the 'Display FRED Data from Saved File' option.");
        assertTrue(sourceLower.contains("jfilechooser"),
                "Application must use JFileChooser to pick saved JSON files.");

        // 3.3 Exit
        assertTrue(sourceLower.contains("\"exit\""),
                "Menu must include the 'Exit' option.");

        // Charting support (JFreeChart or custom Swing chart panel)
        assertTrue(sourceLower.contains("jfreechart") || sourceLower.contains("chartpanel") || sourceLower.contains("createtimeserieschart"),
                "Application must display a chart for the time series data.");
    }
}

