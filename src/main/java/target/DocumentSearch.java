package target;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * This application will take user inputs for search term and search method and output
 * the number of occurrences/matches of the search term relative to each text file found in path:
 * ~/target/src/main/java/res/sample_text .
 */
public class DocumentSearch {
    private static final String resPath = System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "main" + File.separator +
            "java" + File.separator +
            "res" + File.separator +
            "sample_text";

    public static void main(String[] args) {
        String searchTerm;
        int searchMethod = -1, searchCount = 0;
        Scanner inputScanner = new Scanner(System.in);

        /*  Search term cannot be null. Search method input needs to be 1, 2, or 3.
            Loops until user provides satisfying inputs.
         */
        while(true) {
            System.out.println("Please enter search term or phrase (cannot be null).");
            searchTerm = inputScanner.nextLine();

            try {
                System.out.println("Please choose search method (input corresponding number): 1. String Match, 2. Regular Expression, 3. Indexed");
                searchMethod = Integer.valueOf(inputScanner.nextLine());
            } catch (NumberFormatException nfe) {
                searchMethod = -1;
            }

            if (searchTerm != null && !searchTerm.isEmpty() && (searchMethod > 0 && searchMethod <= 3)) {
                break;
            } else {
                System.out.println("Invalid input(s). Please type \"q\" to quit application or press enter to continue.");
                if (inputScanner.nextLine().equals("q")) {
                    return;
                }
            }
        }// end while

        System.out.println("SearchTerm is: " + searchTerm + "\n");

        //Create new file instances
        File[] files = new File(resPath).listFiles();
        if (files == null || files.length == 0) {
            System.err.println("Empty resource directory. Please add appropriate text files to: " + resPath);
        } else {
            //Stores number of matches per file
            Map<String, Integer> unsortedResultMap = new HashMap<>();

            //preprocess file content into indexable if search method is Indexed (3)
            if (searchMethod == 3) {
                System.out.println("Need to implement");
            }

            long startTime = System.currentTimeMillis();
            switch (searchMethod) {
                case 1: //String Match
                    for (File file : files) {
                        if (file.isFile()) {
                            searchCount = SearchUtils.simpleStringSearch(searchTerm, file);
                            unsortedResultMap.put(file.getName(), searchCount);
                        }
                    }
                    break;
                case 2: //Regular Expression
                    for (File file : files) {
                        if (file.isFile()) {
                            searchCount = SearchUtils.regexSearch(searchTerm, file);
                            unsortedResultMap.put(file.getName(), searchCount);
                        }
                    }
                    break;
                case 3: //Indexed
                    break;
                default:
                    System.err.println("Unexpected behavior for search method input: " + searchMethod);
            }// end switch
            long endTime = System.currentTimeMillis();

            System.out.println("Search results: \n");

            if (unsortedResultMap.size() == 0) {
                System.err.println("Found no text files. Please add appropriate text files to: " + resPath);
            } else {
                Map<String, Integer> sortedResultMap = sortDescByValue(unsortedResultMap);
                for (Map.Entry<String, Integer> entry : sortedResultMap.entrySet()) {
                    System.out.println("\t" + entry.getValue() + " - " + entry.getKey() + " matches\n");
                }
            }

            System.out.println("Elapsed Time: " + (endTime - startTime) + " ms");
        }

        //clean up
        inputScanner.close();
    }

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
}
