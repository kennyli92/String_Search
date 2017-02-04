package com.targetsearch.target;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public final class SearchUtils {
    public static String replaceSpacesWithSpace(String origStr) {
        return origStr.replaceAll("\\s+", " ");
    }

    public static int simpleStringMatch(String searchTerm, File searchFile) {
        int searchCount = 0;
        if (searchTerm == null || searchTerm.isEmpty()) {
            return searchCount;
        }

        searchTerm = replaceSpacesWithSpace(searchTerm);
        searchTerm = searchTerm.toLowerCase();
        String fileStr = "";
        String errMsg = "";
        try {
            int lastIdx = 0;
            String lineStr;
            BufferedReader br = new BufferedReader(new FileReader(searchFile));
            while ((lineStr = br.readLine()) != null) {
                fileStr += " " + lineStr;
            }
            fileStr = replaceSpacesWithSpace(fileStr);
            fileStr = fileStr.toLowerCase();
            while (lastIdx != -1) {
                lastIdx = fileStr.indexOf(searchTerm, lastIdx);
                if (lastIdx != -1) {
                    searchCount++;
                    lastIdx += searchTerm.length();
                }
            }
        } catch (FileNotFoundException fnfe) {
            errMsg += "File not found or read was interrupted: " + searchFile.getPath();
            errMsg += "\n" + fnfe.getMessage();
        } catch (IOException ioe) {
            errMsg += "I/O operation was interrupted.";
            errMsg += "\n" + ioe.getMessage();
        } finally {
            System.err.println(errMsg);
        }

        return searchCount;
    }
}
