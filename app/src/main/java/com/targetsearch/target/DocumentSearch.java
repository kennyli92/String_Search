package com.targetsearch.target;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DocumentSearch {
    private static final String resPath =
            System.getProperty("user.dir") + File.separator +
            "app" + File.separator +
            "src" + File.separator +
            "main" + File.separator +
            "res" + File.separator +
            "sample_text" + File.separator;

    public static void main(String[] args) {
        String searchTerm;
        int searchMethod = -1, searchCount = 0;
        final String frenchArmedForcesName = "french_armed_forces.txt";
        final String hitchHikersName = "hitchhikers.txt";
        final String warpDriveName = "warp_drive.txt";
        Scanner inputScanner = new Scanner(System.in);

        //Search term cannot be null. Search method input needs to be 1, 2, or 3
        while(true) {
            System.out.println("Please enter search term or phrase (cannot be null).");
            searchTerm = inputScanner.nextLine();

            try {
                System.out.println("Please choose search method (input corresponding number): 1. String Match, 2. Regular Expression, 3. Indexed");
                searchMethod = Integer.valueOf(inputScanner.nextLine());
            } catch (NumberFormatException nfe) {
                System.err.println("Please input corresponding number for search method: 1, 2, or 3");
            }

            if (searchTerm != null && !searchTerm.isEmpty() && (searchMethod > 0 && searchMethod <= 3)) {
                break;
            }
        }

        System.out.println("SearchTerm is: " + searchTerm + "\n");

        //Create new file instances for the 3 txt files
        File frenchArmedForcesFile = new File(resPath + frenchArmedForcesName);
        File hitchHikersFile = new File(resPath + hitchHikersName);
        File warpDriveFile = new File(resPath + warpDriveName);

        //Stores number of matches per file
        Map<String, Integer> unsortedResultMap = new HashMap<>();

        //preprocess file content into indexable if search method is Indexed (3)
        if (searchMethod == 3) {
            System.out.println("Need to implement");
        }

        long startTime = System.currentTimeMillis();
        switch (searchMethod) {
            case 1: //String Match
                searchCount = SearchUtils.simpleStringSearch(searchTerm, frenchArmedForcesFile);
                unsortedResultMap.put(frenchArmedForcesName, searchCount);
                searchCount = SearchUtils.simpleStringSearch(searchTerm, hitchHikersFile);
                unsortedResultMap.put(hitchHikersName, searchCount);
                searchCount = SearchUtils.simpleStringSearch(searchTerm, warpDriveFile);
                unsortedResultMap.put(warpDriveName, searchCount);
                break;
            case 2: //Regular Expression
                searchCount = SearchUtils.regexSearch(searchTerm, frenchArmedForcesFile);
                unsortedResultMap.put(frenchArmedForcesName, searchCount);
                searchCount = SearchUtils.regexSearch(searchTerm, hitchHikersFile);
                unsortedResultMap.put(hitchHikersName, searchCount);
                searchCount = SearchUtils.regexSearch(searchTerm, warpDriveFile);
                unsortedResultMap.put(warpDriveName, searchCount);
                break;
            case 3: //Indexed
                break;
            default:
                System.err.println("Unexpected behavior for search method input: " + searchMethod);
        }
        long endTime = System.currentTimeMillis();

        System.out.println("Search results: \n");
        Map<String, Integer> sortedResultMap = sortDescByValue(unsortedResultMap);
        for (Map.Entry<String, Integer> entry : sortedResultMap.entrySet()) {
            System.out.println("\t" + entry.getValue() + " - " + entry.getKey() + " matches\n");
        }

        System.out.println("Elapsed Time: " + (endTime - startTime) + " ms");
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
