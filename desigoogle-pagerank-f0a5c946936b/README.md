# Page Rank Commands

##
To Initialize with Page Ranks for all the domains

```
hadoop -jar PageRank-0.0.1-SNAPSHOT-jar-with-dependencies.jar GetInitialPageRank s3://parentchild/ initialPageRank/
```
##
To perform first iteration of Page Rank

```
hadoop -jar PageRank-0.0.1-SNAPSHOT-jar-with-dependencies.jar IterativePageRankAlgo initialPageRank/ outputPageRank/ 1
```

## To verify sum of all the page ranks of any iteration

```
hadoop -jar PageRank-0.0.1-SNAPSHOT-jar-with-dependencies.jar VerifyPageRankSum foldernameofiteration/
```