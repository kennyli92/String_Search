package target

import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class SearchUtilsSpec extends Specification {
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

    @Unroll
    def "simpleStringSearch - Search #searchTerm"() {
        when:
        Map<String, Integer> resultMap = SearchUtils.simpleStringSearch(searchTerm, resPath)

        then:
        resultMap.get("testSearch.txt") == searchCount

        where:
        searchTerm                          | searchCount
        'testing'                           | 4
        'testing the app like'              | 4
        'testing the app like heck'         | 3
        'testing the app like heck 1 23'    | 1
        'p unct at ion'                     | 1
        ''                                  | null
    }

    @Unroll
    def "regexSearch - Search #searchTerm"() {
        when:
        Map<String, Integer> resultMap = SearchUtils.regexSearch(searchTerm, resPath)

        then:
        resultMap.get("testSearch.txt") == searchCount

        where:
        searchTerm                          | searchCount
        'testing'                           | 4
        'testing the app like'              | 4
        'testing the app like heck'         | 3
        'testing the app like heck 1 23'    | 1
        'p unct at ion'                     | 1
        ''                                  | null
    }

    @Unroll
    def "indexSearch - Search #searchTerm"() {
        when:
        Map<String, Integer> resultMap = SearchUtils.indexSearch(searchTerm, indexedFiles, indexDirPath)

        then:
        resultMap.get("testSearch.txt") == searchCount

        where:
        searchTerm                          | searchCount
        'testing'                           | 4
        'testing the app like'              | 4
        'testing the app like heck'         | 3
        'testing the app like heck 1 23'    | 1
        'p unct at ion'                     | 1
        ''                                  | null
    }

    def 'readFile'() {
        given:
        File file = new File(resPath + File.separator + "simpleTest.txt")

        when:
        String result = SearchUtils.readFile(file)

        then:
        result == ' spock test result'

        and:
        noExceptionThrown()
    }

    @Unroll
    def "stringFilter - testing with #stringArg"() {
        when:
        String result = SearchUtils.stringFilter(stringArg)

        then:
        result == expected

        where:
        stringArg               | expected
        '.  \t test,result__\n' | ' test result '
        ' 1,000 t,e.s&t'        | ' 1 000 t e s t'
    }

    def 'stringFilter - null argument'() {
        when:
        SearchUtils.stringFilter(null)

        then:
        thrown(NullPointerException)
    }

    def 'sortDescByValue'() {
        given:
        Map<String, Integer> unsortedMap = ['a':4, 'b':3, 'c':2, 'd':1]
        Map<String, Integer> sortedMap
        Map<String, Integer> expectedMap = ['d':1, 'c':2, 'b':3, 'a':4]

        when:
        sortedMap = SearchUtils.sortDescByValue(unsortedMap)

        then:
        sortedMap == expectedMap
    }

    def 'getFreq'() {
        given:
        Map<String, List<Integer>> terms = ['hello':[8, 1, 4, 5], 'world':[7, 2, 6]]
        List<String> searchTerms = ['hello', 'world']

        when:
        int result = SearchUtils.getFreq(terms, searchTerms)

        then:
        result == 2
    }

    /*** Performance Tests ***/
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