package target;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
     * String constants used for index search
     */
    private final static String FILENAME_FIELD = "filename";
    private final static String CONTENTS_FIELD = "contents";

    /**
     * Searches for {@code searchTerm} in all text files in the {@code resPath} directory path
     * using simple string matching utilities.
     * @param searchTerm used to search in file
     * @param resPath directory containing the text files
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
     * @param searchTerm used to search in file
     * @param resPath directory containing the text files
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
     * Searches for {@code searchTerm} in all text files in the {@code resPath} directory path
     * using index created from {@link #indexFilesInDir(String, String)}.
     * This uses the Lucene search library.
     * @param searchTerm used to search in file
     * @param indexedFiles indexed file names
     * @param indexDirPath directory containing the index
     * @return the map containing the file name (key) and its corresponding search count (value)
     * of {@code searchTerm}
     */
    public static Map<String, Integer> indexSearch(String searchTerm, List<String> indexedFiles, final String indexDirPath) {
        int searchCount;
        //Stores number of matches per file
        Map<String, Integer> unsortedResultMap = new HashMap<>();
        if (searchTerm == null || searchTerm.isEmpty()) {
            return unsortedResultMap;
        }

        searchTerm = stringFilter(searchTerm);
        try {
            //reads and prepares index
            IndexReader reader = DirectoryReader.open(FSDirectory.open((new File(indexDirPath)).toPath()));
            //make index searchable
            IndexSearcher searcher = new IndexSearcher(reader);
            //queries index for matches
            Query query = new QueryParser("contents", new WhitespaceAnalyzer()).parse(searchTerm);
            //find top 10000 matching documents
            TopDocs topDocs = searcher.search(query, 10000);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;

            //iterates through each matching document to get frequency of search term
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                Terms terms = reader.getTermVector(scoreDoc.doc, "contents");
                TermsEnum itr = terms.iterator();
                BytesRef term;
                Map<String, List<Integer>> termsPosMap = new HashMap<>();
                List<String> searchTermTokens = Arrays.asList(searchTerm.split(" "));
                PostingsEnum postings = null;
                int termFreq;
                //iterates through each term and their frequency
                while ((term = itr.next()) != null) {
                    String termText = term.utf8ToString();
                    //if search term contains term, then put term and its positions into map
                    if (searchTermTokens.contains(termText.toLowerCase())) {
                        postings = itr.postings(postings, PostingsEnum.POSITIONS);
                        List<Integer> positions = new ArrayList<>();

                        while (postings.nextDoc() != PostingsEnum.NO_MORE_DOCS) {
                            termFreq = postings.freq();
                            for (int idx = 0; idx < termFreq; idx++) {
                                int pos = postings.nextPosition();
                                positions.add(pos);
                            }
                        }
                        termsPosMap.put(termText, positions);
                    }
                }//end while

                int searchTermFreq;
                searchTermFreq = getFreq(termsPosMap, searchTermTokens);
                unsortedResultMap.put(doc.getField(FILENAME_FIELD).stringValue(), searchTermFreq);
            }//end for
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.err.println("Error during Index search.");
        } catch (ParseException pe) {
            pe.printStackTrace();
            System.err.println("Error parsing search term to generate search query.");
        }

        //populate map for files there had zero matches
        for (String indexedFile : indexedFiles) {
            if (!unsortedResultMap.containsKey(indexedFile)) {
                unsortedResultMap.put(indexedFile, 0);
            }
        }
        return sortDescByValue(unsortedResultMap);
    }

    /**
     * Index search helper method
     * This returns the frequency of the search term found in document
     * @param terms the map of each elements of {@code searchTerms} and its corresponding list of positions
     * @param searchTerms tokenized search term (ex. "the military history" >> ["the", "military", "history"]
     * @return the frequency of search term match
     */
    private static int getFreq(Map<String, List<Integer>> terms, List<String> searchTerms) {
        int size = searchTerms.size();
        if (terms.size() != size) {
            return 0;
        } else if (size == 1) {
            return terms.get(searchTerms.get(0)).size();
        }

        int freq = 0;
        List<Integer> curPositions = terms.get(searchTerms.get(0));
        for (int idx = 0; idx < size; idx++) {
            List<Integer> nextPositions;
            List<Integer> nextCurPositions = new ArrayList<>();
            if (idx + 1 == size) {
                break;
            }
            if (idx + 1 <= size) {
                nextPositions = terms.get(searchTerms.get(idx + 1));
                for (int curPos : curPositions) {
                    for (int nextPos : nextPositions) {
                        if (nextPos - curPos == 1) {
                            //last comparing nested loop
                            if (idx + 2 == size) {
                                freq++;
                            }
                            nextCurPositions.add(nextPos);
                        }
                    }
                }//end for
                curPositions = nextCurPositions;
            }//end if
        }//end for

        return freq;
    }

    /**
     * Index search helper method
     * Creates index in {@code indexDirPath} path from the text files found in
     * {@code resPath} path. This uses the Lucene search library.
     * @param indexDirPath directory containing the index
     * @param resPath directory containing the text files
     * @return list of file names indexed
     * @throws IOException when error indexing
     */
    public static List<String> indexFilesInDir(String indexDirPath, String resPath) throws IOException {
        List<String> filenames = new ArrayList<>();
        //this analyzer delimits text in a document based on white space
        WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();

        //FSDirectory determines the index is stored in the file system
        FSDirectory dir = FSDirectory.open((new File(indexDirPath).toPath()));

        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        //creates new index or opens if exist
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter indexWriter = new IndexWriter(dir, indexWriterConfig);

        //creates index for all text files found in the directory path
        File[] files = new File(resPath).listFiles();

        if (files == null || files.length == 0) {
            System.err.println("Empty resource directory. Please add appropriate text files to: " + resPath);
        } else {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String fileStr = "";
                        String lineStr;
                        while ((lineStr = br.readLine()) != null) {
                            fileStr += " " + lineStr;
                        }
                        //filters file content of unwanted characters
                        fileStr = stringFilter(fileStr);

                        //index file name
                        Document doc = new Document();
                        doc.add(new StringField(FILENAME_FIELD, file.getName(), Field.Store.YES));

                        //index file contents
                        FieldType fieldType = new FieldType();
                        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
                        fieldType.setStored(true);
                        fieldType.setStoreTermVectors(true);
                        fieldType.setStoreTermVectorPositions(true);
                        fieldType.setTokenized(true);
                        doc.add(new Field(CONTENTS_FIELD, fileStr, fieldType));

                        indexWriter.addDocument(doc);
                        System.out.println("Added file to be indexed: " + file.getName());
                        filenames.add(file.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Cannot add file \"" + file.getName() + "\" due to unknown error.");
                    }
                }//end if
            }//end for
        }//end if

        //clean up: make sure to close to create index
        indexWriter.close();
        return filenames;
    }

    /**
     * Helper method
     * Sorts {@code unsortedMap} into descending order by comparing values
     * @param unsortedMap the map to be sorted
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
     * @param searchTerm string to be filtered
     * @return The filtered string
     */
    public static String stringFilter(String searchTerm) {
        return searchTerm.replaceAll("[^a-zA-Z0-9 ]", " ").replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * Helper method
     * Reads the content of the file, filters out unwanted characters
     * using {@link #stringFilter(String)}, and returns the content as a string.
     * @param searchFile file to be read for its contents
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
