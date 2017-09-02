Sfilter is a simple spam filter created during a course of Machine Learning by Giulio Muntoni

To compile it just type:
"javac filter.java Training.java HashSer.java ArcSer.java Word.java"
You need java jdk installed.

To run it type:

-If you want to train the filter(all the files must contain "ham" or "spam" string in their names):
 "java -tr filter ./file1 ./file2 ./fileN"
or:
 "java -tr filter ./folder1 ./folder2 ./folderN"
	
-If you want to classify some mails(txt files)
  "java filter ./folder1 ./folder2 ./folderN"
or
  "java filter ./folder1 ./folder2 ./folderN"


After training Sfilter will create 4 files to be used for classification:
- spamdb
- hamdb
- arcdb
- stats