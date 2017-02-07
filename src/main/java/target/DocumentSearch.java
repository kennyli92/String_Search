package target;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
        int searchMethod = 0;
        boolean quit = false;
        Scanner inputScanner = new Scanner(System.in);

        while(true) {
        /*  Search term cannot be null. Search method input needs to be 1, 2, or 3.
            Loops until user provides satisfying inputs.
         */
            while (true) {
                System.out.println("Please enter search term or phrase (cannot be null) or \"q\" to quit.");
                searchTerm = inputScanner.nextLine();

                if (searchTerm.equalsIgnoreCase("q")) {
                    quit = true;
                    break;
                }

                try {
                    System.out.println("Please choose search method (input corresponding number): 1. String Match, 2. Regular Expression, 3. Indexed");
                    searchMethod = Integer.valueOf(inputScanner.nextLine());
                } catch (NumberFormatException nfe) {
                    searchMethod = -1;
                }

                if (!searchTerm.isEmpty() && (searchMethod > 0 && searchMethod <= 3)) {
                    break;
                } else {
                    System.out.println("Invalid input(s). Please type \"q\" to quit application or press enter to continue.");
                    if (inputScanner.nextLine().equalsIgnoreCase("q")) {
                        return;
                    }
                }
            }// end while

            if (quit)
                break;

            //Stores number of matches per file
            Map<String, Integer> sortedResultMap = null;

            //preprocess file content into indexable if search method is Indexed (3)
            boolean indexed = true;
            List<String> indexedFiles = null;
            if (searchMethod == 3) {
                try {
                    indexedFiles = SearchUtils.indexFilesInDir(indexDirPath, resPath);
                } catch (IOException ioe){
                    indexed = false;
                    ioe.printStackTrace();
                    System.err.println("Error indexing files in: " + resPath);
                }
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
                    if (indexed && indexedFiles != null && indexedFiles.size() > 0) {
                        sortedResultMap = SearchUtils.indexSearch(searchTerm, indexedFiles, indexDirPath);
                    }
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
                    System.out.println("\t" + entry.getKey() + " - " + entry.getValue() + " matches\n");
                }
            }

            System.out.println("Elapsed Time: " + (endTime - startTime) + " ms");
        }

        //clean up
        inputScanner.close();
    }
}
