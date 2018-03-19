#!/bin/bash
for n in 1 2 3 4
do

echo "remove outputfile"
rm /mnt/hgfs/task/IndexOutput/*
echo "Copy TweetsTest$n" 
cp /mnt/hgfs/task/TweetsTest$n /mnt/hgfs/task/IndexingInput
echo "Start indexing"
java -cp ./out/production/project/:../lucene-core-7.2.1.jar:../lucene-analyzers-common-7.2.1.jar:../commons-lang3-3.7.jar TweetsIndexer.TweetsIndexMain -index /mnt/hgfs/task/IndexOutput -docs /mnt/hgfs/task/IndexingInput

done
