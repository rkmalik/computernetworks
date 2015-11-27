
Course CNT5106C:
Course Title:  Computer Networks

Name : Rohit Kumar Malik


This package is having the server and the client directories. 

server client1 client2 client3 client4 client5.


Command to Start Server
	java Server \inifileName
Command to Start Client
	java Client \inifileName


Steps to run the server:

1. The Server code is present in the Server directory. 
2. input data file location is input folder.
3. Place the input file in this folder. 
4. Start the server with following command
   	jave Server \inifileName
5. Then server will ask for input file
	give the input file name available in the input directory


Steps to run the Client:
1. Client automatically creates the CHUNK Files in the output/ClientID directory.
2. Client creates the output file with "target.pdf" name.
3. Each 5 client directories client1, client2, client3, client4, client5 are having their own ini file
4. Start the client with follwoing command
	java Client \inifileName
5. The client creates all the chunks inside the clientDir/<ClientId>/ directory. It also creates the 
   summary file inside the same directory.
6. After downloading all the chunks it creates the target.pdf under clientDir.
