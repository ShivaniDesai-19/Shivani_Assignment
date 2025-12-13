#!/bin/bash


#LEVEL_ARG="$1"
#USER_ARG="$2"
#STATUS_ARG="$3"
#MESSAGE_ARG="$4"


#awk -v level="$LEVEL" \
 #   -v user="$USER_ARG" \
 #  -v status="$STATUS_ARG" \
  #  -v message="$MESSAGE_ARG"
  



awk -v level="$1" -v user="$2" -v status="$3" -v message="$4" '
{
	level_ok = (level == "" || $3 == level)
	user_ok = (user == "" || $4 ~ user)
	status_ok = (status == "" || $5 == status)

	msg = ""
	for( i=6; i <=NF; i++){
		msg = msg $i " "
	}
	message_ok = (message == "" || msg ~ message)
	if(level_ok && user_ok && status_ok && message_ok)
		print
}
' log1.txt log2.txt



