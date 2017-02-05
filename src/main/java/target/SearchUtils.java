package target;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SearchUtils {
    public static String replaceSpacesWithSpace(String origStr) {
        return origStr.replaceAll("\\s+", " ");
    }

    private static String readFile(File searchFile) {
        String fileStr = "";
        String errMsg = "";
        try (BufferedReader br = new BufferedReader(new FileReader(searchFile))) {
            String lineStr;
            while ((lineStr = br.readLine()) != null) {
                fileStr += " " + lineStr;
            }
            fileStr = replaceSpacesWithSpace(fileStr);
            fileStr = fileStr.toLowerCase();
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

    public static int simpleStringSearch(String searchTerm, File searchFile) {
        int searchCount = 0;
        if (searchTerm == null || searchTerm.isEmpty()) {
            return searchCount;
        }

        searchTerm = replaceSpacesWithSpace(searchTerm);
        searchTerm = searchTerm.toLowerCase();

        int lastIdx = 0;
        String fileStr = readFile(searchFile);
        while (lastIdx != -1) {
            lastIdx = fileStr.indexOf(searchTerm, lastIdx);
            if (lastIdx != -1) {
                searchCount++;
                lastIdx += searchTerm.length();
            }
        }

        return searchCount;
    }

    public static int regexSearch(String searchTerm, File searchFile) {
        int searchCount = 0;
        if (searchTerm == null || searchTerm.isEmpty()) {
            return searchCount;
        }

        searchTerm = replaceSpacesWithSpace(searchTerm);
        searchTerm = searchTerm.toLowerCase();

        String fileStr = readFile(searchFile);

        Pattern pattern = Pattern.compile(searchTerm);
        Matcher matcher = pattern.matcher(fileStr);
        while (matcher.find()) {
            searchCount++;
        }

        return searchCount;
    }
}
