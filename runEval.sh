!/bin/bash
FILES=/home/arpitg1991/TweetSearchLucene/output/jobim/*
OUTPUTDIR=/home/arpitg1991/TweetSearchLucene/eval/
for f in $FILES
do
  echo "Processing $f file..."
  # take action on each file. $f store current file name
  a=$(basename $f)
  #echo $a
  #gzip $f
  python mb12-eval.py -l 1 adhoc-qrels.txt $f > $OUTPUTDIR$a.txt

done
