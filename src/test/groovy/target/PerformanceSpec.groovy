package target

import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification

class PerformanceSpec extends Specification {
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

    private static final int searchTermSize = 2000000

    @Shared
    List<String> indexedFiles;

    @Shared
    String[] searchTerms;

    def setupSpec() {
        //sets up index
        indexedFiles = SearchUtils.indexFilesInDir(indexDirPath, resPath)

        //generates 2 million random search term for performance testing
        Random random = new Random();
        searchTerms = new String[searchTermSize]
        for (int idx = 0; idx < searchTermSize; idx++) {
            searchTerms[idx] = RandomStringUtils.randomAlphanumeric(random.nextInt(15) + 1)
        }
    }

    def "simpleStringSearch - performance test - search #searchTermSize random terms"() {
        expect:
        long startTime = System.currentTimeMillis();
        for (int idx = 0; idx < searchTermSize; idx++) {
            SearchUtils.simpleStringSearch(searchTerms[idx], resPath)
        }
        long endTime = System.currentTimeMillis();

        System.out.println("String Match Elapsed Time: " + (endTime - startTime) + " ms");
    }

    def "regexSearch - performance test - search #searchTermSize random terms"() {
        expect:
        long startTime = System.currentTimeMillis();
        for (int idx = 0; idx < searchTermSize; idx++) {
            SearchUtils.regexSearch(searchTerms[idx], resPath)
        }
        long endTime = System.currentTimeMillis();

        System.out.println("Regex Search Elapsed Time: " + (endTime - startTime) + " ms");
    }

    def "indexSearch - performance test - search #searchTermSize random terms"() {
        expect:
        long startTime = System.currentTimeMillis();
        for (int idx = 0; idx < searchTermSize; idx++) {
            SearchUtils.indexSearch(searchTerms[idx], indexedFiles, indexDirPath)
        }
        long endTime = System.currentTimeMillis();

        System.out.println("Index Search Elapsed Time: " + (endTime - startTime) + " ms");
    }
}