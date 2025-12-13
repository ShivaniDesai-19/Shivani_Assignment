#!/bin/bash




for i in {1..10}; do
        LEVEL=$(shuf -n1 -e INFO DEBUG ERROR WARN)
        USER_ID=$(shuf -n1 -e Shivani abc def Kani Ganesh)
        STATUS=$(shuf -n1 -e SUCCESS FAILURE)
        MESSAGE=$(shuf -n1 -e\
                 "Read a file" \
                 "Wrote to a file" \
                 "Executed a file" \
                 "Modified a file" \
                 "Changed Password" \
                 "Trying to access a file" \
                 "trying to access a directory")
        echo "$(date '+%Y-%m-%d %H:%M:%S') $LEVEL $USER_ID $STATUS $MESSAGE" >> log1.txt
        sleep 1
done


#Generating log2 file

for i in {1..10}; do
	LEVEL=$(shuf -n1 -e INFO DEBUG ERROR WARN)
	USER_ID=$(shuf -n1 -e Shivani abc def Kani Ganesh)
	STATUS=$(shuf -n1 -e SUCCESS FAILURE)
	MESSAGE=$(shuf -n1 -e\
		 "Read a file" \
		 "Wrote to a file" \
                 "Executed a file" \
                 "Modified a file" \
                 "Changed Password" \
                 "Trying to access a file" \
                 "trying to access a directory")
	echo "$(date '+%Y-%m-%d %H:%M:%S') $LEVEL $USER_ID $STATUS $MESSAGE" >> log2.txt
	sleep 1
done


