package target;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SearchUtils {

    /**
     * Searches for {@code searchTerm} in all text files in the {@code resPath} directory path
     * using simple string matching utilities.
     * @param searchTerm
     * @param resPath
     * @return the map containing the file name (key) and its corresponding search count (value)
     * of {@code searchTerm}
     */
    public static Map<String, Integer> simpleStringSearch(String searchTerm, final String resPath) {
        int searchCount;
        //Stores number of matches per file
        Map<String, Integer> unsortedResultMap = new HashMap<>();
        if (searchTerm == null || searchTerm.isEmpty()) {
            return unsortedResultMap;
        }

        searchTerm = stringFilter(searchTerm);
        //Create new file instances
        File[] files = new File(resPath).listFiles();

        if (files == null || files.length == 0) {
            System.err.println("Empty resource directory. Please add appropriate text files to: " + resPath);
        } else {
            for (File file : files) {
                searchCount = 0;
                if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                    //search starts here
                    int lastIdx = 0;
                    String fileStr = readFile(file);
                    while (lastIdx != -1) {
                        lastIdx = fileStr.indexOf(searchTerm, lastIdx);
                        if (lastIdx != -1) {
                            searchCount++;
                            lastIdx += searchTerm.length();
                        }
                    }
                    unsortedResultMap.put(file.getName(), searchCount);
                }//end if
            }//end for
        }//end if

        return sortDescByValue(unsortedResultMap);
    }

    /**
     * Searches for {@code searchTerm} in all text files in the {@code resPath} directory path
     * using regular expression.
     * @param searchTerm
     * @param resPath
     * @return the map containing the file name (key) and its corresponding search count (value)
     * of {@code searchTerm}
     */
    public static Map<String, Integer> regexSearch(String searchTerm, final String resPath) {
        int searchCount;
        //Stores number of matches per file
        Map<String, Integer> unsortedResultMap = new HashMap<>();
        if (searchTerm == null || searchTerm.isEmpty()) {
            return unsortedResultMap;
        }

        searchTerm = stringFilter(searchTerm);
        //Create new file instances
        File[] files = new File(resPath).listFiles();

        if (files == null || files.length == 0) {
            System.err.println("Empty resource directory. Please add appropriate text files to: " + resPath);
        } else {
            for (File file : files) {
                searchCount = 0;
                if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                    //search starts here
                    String fileStr = readFile(file);

                    Pattern pattern = Pattern.compile("\\b" + searchTerm + "\\b");
                    Matcher matcher = pattern.matcher(fileStr);
                    while (matcher.find()) {
                        searchCount++;
                    }
                    unsortedResultMap.put(file.getName(), searchCount);
                }//end if
            }//end for
        }//end if

        return sortDescByValue(unsortedResultMap);
    }

    /**
     * Helper method
     * Sorts {@code unsortedMap} into descending order
     * @param unsortedMap
     * @return sorted map in descending order
     */
    private static Map<String, Integer> sortDescByValue(Map<String, Integer> unsortedMap) {
        List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortedMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
                return obj2.getValue() - obj1.getValue();
            }
        });

        Map<String, Integer> sortedHashMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }

        return sortedHashMap;
    }

    /**
     * Helper method
     * Filters {@code searchTerm} of punctuations, convert space characters (tabs, new line, multiple spaces)
     * to single space, and changes to lowercase for searching purposes.
     * @param searchTerm
     * @return The filtered string
     */
    public static String stringFilter(String searchTerm) {
        return searchTerm.replaceAll("[^a-zA-Z0-9 ]", " ").replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Reads the content of the file, filters out unwanted characters
     * using {@link #stringFilter(String)}, and returns the content as a string.
     * @param searchFile
     * @return the filtered content of the file as a string
     */
    private static String readFile(File searchFile) {
        String fileStr = "";
        String errMsg = "";
        try (BufferedReader br = new BufferedReader(new FileReader(searchFile))) {
            String lineStr;
            while ((lineStr = br.readLine()) != null) {
                fileStr += " " + lineStr;
            }
            fileStr = stringFilter(fileStr);
        } catch (FileNotFoundException fnfe) {
            errMsg += "File not found or read was interrupted: " + searchFile.getPath();
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            errMsg += "I/O operation was interrupted.";
            ioe.printStackTrace();
        } catch (Exception e) {
            errMsg += "Unexpected exception.";
            e.printStackTrace();
        } finally {
            System.err.println(errMsg);
        }

        return fileStr;
    }
}
