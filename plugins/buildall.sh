#!/bin/sh


# for each plugin-directory
for file in $(find . -maxdepth 1 -type d)
do

if [ "$file" != "." ] 
then
# cd into directory
echo $file
cd "$file"

# if build.xml file exists 
# -> run "ant"
if [ -e "build.xml" ]
then
sh ant
fi

# leave directory
cd ..
fi
done
