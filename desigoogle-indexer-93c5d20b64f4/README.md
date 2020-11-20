# Indexing Pipeline
## Run ParseBody to extract all words from body of html documents
```
hadoop -jar indexer-1.0-SNAPSHOT-jar-with-dependencies.jar ParseBody s3://minidocs/ inputtfidf/
```
## Run TFIDF on the above output
```
hadoop -jar indexer-1.0-SNAPSHOT-jar-with-dependencies.jar TFIDF inputtfidf/ inputtfidfunsorted/
```
## Run SimpleTFIDF on the above output for chunking
```
hadoop -jar indexer-1.0-SNAPSHOT-jar-with-dependencies.jar SimpleTFIDF inputtfidfunsorted/ inputtfidfsorted/
```
### To store the tf-idf results into S3 run the following command

```
hdfs dfs -cp inputtfidfsorted/ s3://bucketname/
```

