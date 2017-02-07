package target

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

    def setupSpec() {
        //sets up index
        SearchUtils.indexFilesInDir(indexDirPath, resPath)
    }

    def 'readFile'() {
        given:
        File file = new File(resPath + File.separator + "test.txt")

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
}