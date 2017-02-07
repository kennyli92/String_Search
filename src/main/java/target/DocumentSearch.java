package target;

import java.io.File;
import java.util.Map;
import java.util.Scanner;

/**
 * This application will take user inputs for search term and search method and output
 * the number of exact occurrences/matches of the search term relative to each text file
 * found in path: ~/target/src/main/java/res/sample_text .
 */
public class DocumentSearch {
    /**
     * relative path containing the text files
     */
    private static final String resPath = System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "main" + File.separator +
            "java" + File.separator +
            "res" + File.separator +
            "sample_text";
    /**
     * relative path containing the index created for each text file found in {@code resPath}
     */
    private static final String indexDirPath = System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "main" + File.separator +
            "java" + File.separator +
            "res" + File.separator +
            "index";

    public static void main(String[] args) {
        String searchTerm;
        int searchMethod;
        Scanner inputScanner = new Scanner(System.in);

        while(true) {
        /*  Search term cannot be null. Search method input needs to be 1, 2, or 3.
            Loops until user provides satisfying inputs.
         */
            while (true) {
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
                    if (inputScanner.nextLine().equalsIgnoreCase("q")) {
                        return;
                    }
                }
            }// end while

            //Stores number of matches per file
            Map<String, Integer> sortedResultMap = null;

            //preprocess file content into indexable if search method is Indexed (3)
            if (searchMethod == 3) {
                System.out.println("Need to implement");
            }

            //search starts here
            long startTime = System.currentTimeMillis();
            switch (searchMethod) {
                case 1: //String Match
                    sortedResultMap = SearchUtils.simpleStringSearch(searchTerm, resPath);
                    break;
                case 2: //Regular Expression
                    sortedResultMap = SearchUtils.regexSearch(searchTerm, resPath);
                    break;
                case 3: //Indexed
                    break;
                default:
                    System.err.println("Unexpected behavior for search method input: " + searchMethod);
            }// end switch
            long endTime = System.currentTimeMillis();

            System.out.println("Search results: \n");

            //prints out text file name and corresponding search count ordered relevancy
            if (sortedResultMap != null && sortedResultMap.size() == 0) {
                System.err.println("Found no text files. Please add appropriate text files to: " + resPath);
            } else if (sortedResultMap != null) {
                for (Map.Entry<String, Integer> entry : sortedResultMap.entrySet()) {
                    System.out.println("\t" + entry.getValue() + " - " + entry.getKey() + " matches\n");
                }
            }

            System.out.println("Elapsed Time: " + (endTime - startTime) + " ms");
            System.out.println("Press enter to continue or \"q\" to quit.");
            searchTerm = inputScanner.nextLine();
            if (searchTerm.equalsIgnoreCase("q")) {
                break;
            }
        }

        //clean up
        inputScanner.close();
    }
}
