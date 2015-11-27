#! /bin/bash


clear 
echo "Copying data from eclipse directory to server and client folders on desktop..."

PROG_DIRS[0]='/home/rkmalik/Desktop/finalproject/server/'
PROG_DIRS[1]='/home/rkmalik/Desktop/finalproject/client1/'
PROG_DIRS[2]='/home/rkmalik/Desktop/finalproject/client2/'
PROG_DIRS[3]='/home/rkmalik/Desktop/finalproject/client3/'
PROG_DIRS[4]='/home/rkmalik/Desktop/finalproject/client4/'
PROG_DIRS[5]='/home/rkmalik/Desktop/finalproject/client5/'

FILES="/home/rkmalik/Desktop/finalproject/client1/
/home/rkmalik/Desktop/finalproject/client2/
/home/rkmalik/Desktop/finalproject/client3/
/home/rkmalik/Desktop/finalproject/client4/
/home/rkmalik/Desktop/finalproject/client5/"


echo -n "Server Directory " 
echo ${PROG_DIRS[0]}
var1=${PROG_DIRS[0]}'*.java'

echo "Removing all the inside the server."
echo "Removing "${var1}
rm ${var1}

echo "Removing all the inside all client directories."
for file in $FILES
do
 var1=${file}'*.java'
 echo "Removing "${var1}
 rm ${var1}
 var1=${file}'*.class'
 echo "Removing "${var1}
 rm ${var1}
 var1=${file}'output/'
 rm -r ${var1}
done

CLIENT_SRC_DIR="/home/rkmalik/work/allworkspaces/semester3/cn/src/finalprojectclient"
SERVER_SRC_DIR="/home/rkmalik/work/allworkspaces/semester3/cn/src/finalprojectserver"


## The idea here is to go to each file in the client src directory and the server directory

## edit the first line and copy to the run follder
for srcfilename in ${CLIENT_SRC_DIR}/*.java
do
  echo ""
  ##echo ${srcfilename}
  for file in $FILES
  do
    cd ${file}
    echo ${file}
    b=$(basename $srcfilename)
    echo "Copying file "$b
    cat ${srcfilename} | sed -e "1d" > $b
  done
done

for file in $FILES
do
 cd ${file}
 javac *.java
done

echo ""
echo "Copying files from server."
file=${PROG_DIRS[0]}
for srcfilename in ${SERVER_SRC_DIR}/*.java
do
  ##echo ${srcfilename}
  ##echo ${file}
  cd ${file}
  b=$(basename $srcfilename)
  echo "Copying file "$b
  cat ${srcfilename} | sed -e "1d" > $b
done
javac *.java

## var1 will have input folder
## var2 will have output folder
## logic : Loop through all the file in a folder
## get the name of the file 
## cat the file and put it in the target folder


##for f in /home/rkmalik/Desktop/finalproject/client1/*.java
##	if [  -f $f ]
##	then 
##	print $f


