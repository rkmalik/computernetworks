#! /bin/bash


clear 
echo "Copying data from eclipse directory to server and client folders on desktop..."

PROG_DIRS[0]=/home/rkmalik/Desktop/finalproject/server/
PROG_DIRS[1]=/home/rkmalik/Desktop/finalproject/client1/
PROG_DIRS[2]=/home/rkmalik/Desktop/finalproject/client2/
PROG_DIRS[3]=/home/rkmalik/Desktop/finalproject/client3/
PROG_DIRS[4]=/home/rkmalik/Desktop/finalproject/client4/
PROG_DIRS[5]=/home/rkmalik/Desktop/finalproject/client5/

FILES="/home/rkmalik/Desktop/finalproject/client1/
/home/rkmalik/Desktop/finalproject/client2/
/home/rkmalik/Desktop/finalproject/client3/
/home/rkmalik/Desktop/finalproject/client4/
/home/rkmalik/Desktop/finalproject/client5/"


echo -n "Server Directory " 
echo ${PROG_DIRS[0]}

echo "Removing all the inside the server."
rm ${PROG_DIRS[0]}"*.*"

echo "Removing all the inside all client directories."
for file in $FILES
do
 rm ${file}"*.*"
done

CLIENT_SRC_DIR="/home/rkmalik/work/allworkspaces/semester3/networks/bin/finalprojectclient"
SERVER_SRC_DIR="/home/rkmalik/work/allworkspaces/semester3/networks/bin/finalprojectserver"

for srcfilename in CLIENT_SRC_DIR/*.java
do
  for file in $FILES
  do
    echo srcfilename
    name=basename srcfilename
    echo "copying "file$name
    cat srcfilename | sed -e "1d" > file$name
 done
done

## var1 will have input folder
## var2 will have output folder
## logic : Loop through all the file in a folder
## get the name of the file 
## cat the file and put it in the target folder


##for f in /home/rkmalik/Desktop/finalproject/client1/*.java
##	if [  -f $f ]
##	then 
##	print $f


