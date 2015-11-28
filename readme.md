
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
6. Each Download peer first pings the Upload Peer to retrieve the available list of packet Ids with the Upload
   peer. Based on the required chunks in that list it sends the request to the Upload Peer and downloads only the 
   required chunks.
7. After downloading all the chunks it creates the target.pdf under clientDir.


Summary of all the commands to run:

Server : 
	-> java Server start.ini
	-> Give File name as input
Client1:
	-> java Client client1.ini
Client2:
	-> java Client client2.ini
Client3:
	-> java Client client3.ini
Client4:
	-> java Client client4.ini
Client5:
	-> java Client client5.ini
	

	

