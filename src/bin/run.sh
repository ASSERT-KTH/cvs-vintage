ps auxww | grep run.jar | grep -v grep | head -1 | awk " { split(\$0,bob,\" \*\"); print \"kill -9 \", bob[2] }" | sh
java -jar run.jar
