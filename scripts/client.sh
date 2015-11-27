#! /bin/bash
echo ""
echo ""
echo ""
echo ""
echo "Compiling Client.."


cd "/home/rkmalik/Desktop/finalproject/client1"
rm *.class
javac "Peer.java"
java Peer start.cfg
