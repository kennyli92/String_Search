### Source file directory locations

Production Java source         -> src/main/java/target  
Resource files (text files)    -> src/main/java/res/sample_text  
Index directory                -> src/main/java/res/index  
Test directory                 -> src/test/groovy/target  

***

### Required software:

* Java
* Groovy
* Gradle
* Intellij (optional, this was the IDE I used)

***

### How to run application:

```

gradlew assemble
gradlew run

```

Then, follow application prompt for further directions. 

### How to run unit test:

```

gradlew assemble
gradlew -Dtest.single=SearchUtilsSpec test

```

### How to run performance test:

```

gradlew assemble
gradlew -Dtest.single=PerformanceSpec test

```

Please be aware the performance test cases will take approximately 80 minutes to complete.

***

### Assumptions made regarding design of application:

* Assume each line in the text files fed to the application contains less than or equal to (2^31 - 1) characters

* Spaces (include new line, tabs, etc) and punctuations between alphanumeric will be delimited for both the input
search term and strings in the documents. I recognize that this will render some searches inaccurate 
(ex. search term "Test_s"  will match "test s", "test's", "test.s"). I made this decision due to some special rules
in the Lucene library toward punctuations, and to maintain functional consistencies between the 3 search methods. 
For example, one rule is that commas will not be tokenized, but commas between numeric characters are ok. 

* Search will be case insensitive, punctuations will be ignored and by whole character sequences. For example,
search term "test" will not be registered as a match for "Tests", but will match "test.".

* The application only read and search .txt files.

* Regarding the Index search method, I have excluded the preprocessing index portion of the code from the execution
time because should this be used in a production environment, the preprocessing should only be executed once during
the lifecycle of the application's process. 

* For the purpose of this code study, I hard-coded 10,000 as the number of files allowed to index and search.

* Every time the application runs, existing index will be overwritten. 

### Performance results

Due to time constraint, I only ran 500k searches with random single string (no phrases) search term for 
each search method. However, since the search operations should be consistent, we can extrapolate the performance 
time for a 2M search by multiplying the result execution time by 4. Each search was done on 5 .txt files of varying
size found in src/main/java/res/sample_text. The final execution times listed below are the average of 3 independent
runs.

Time (execution time for 500k searches):

String match               - 6m 22s

Regular Expression match   - 7m 18s

Indexed match              - 5m 58s

Indexed match searching appears to provide the best performance of the 3 search methods. This is because single
strings (delimited by spaces and punctuations) are tokenized and indexed in the file system. Then, these index
were accessed based on the search term, which should theoretically achieve time complexity of O(1). Obviously, 
from my results, my implementation did not quite achieve O(1) due to my unfamiliarity with the Lucene library. 
I would to emphasize that the Lucene library tokenizes single string, which would negatively impact the performance
of multi-word term searches because further logical manipulation would need to take place to figure out if tokens
can be ordered sequentially based on their positions to find a multi-word match. 

The string match and regular expression match methods need to iterate through the document. Trivially, we can
tell the time complexity for these two methods are O(n). However, the regular expression search will be more
expensive of the two as it takes additional cpu resource to match longer strings. 

Finally, I would expect Index search to work significantly better if there are a larger set of files because 
file I/O overhead grows linearly relative to the number of files for the string and regular expression search methods.

***

### Software/Hardware suggestions:

* Software: As proven by my test results, the index search method proves to be the best candidate for production use.
Based on my research, I would recommend the use of the Lucene library due to its long legacy of stability and usage by 
popular softwares for its tested scalability. Furthermore, I would do a statistical analysis to find the most popular 
files based on the number of accesses made to it. Then, we can have the index of popular files cached in memory for
better performance, but also have redundant index in the file system/database in case the server is recovering from an 
outage. Ultimately, most of the files should be indexed and stored in a file system/database as it is not scalable to 
cache index in memory due to physical limitations (RAM is volatile, so the re-caching the index in memory would take 
time after an outage), cost, and memory space limitations. 

* Hardware: Regarding large request volumes, an individual server can only spin up a limited amount of threads to 
handle search and may not be able to process this capacity. So, redundant servers should be employed to handle
large request volumes by distributing the load equally across the servers by some load balancer. The redundant servers
can also maintain stability and service should some of them goes offline due to hacks, power outages, or system failures.












