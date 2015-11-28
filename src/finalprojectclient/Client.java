package finalprojectclient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Client {

	private static int SERVER_PORT;
	private static int SERVER_ID;

	private static int DOWNLOAD_FROM_PEER;
	private static int DOWNLOAD_FROM_PEER_PORT;

	private static int MY_CLIENT_ID;
	private static int MY_CLIENT_PORT;

	private static int TOTAL_CHUNK_COUNT;

	private static String summaryFilePath = null;

	private static ConcurrentHashMap<Integer, Data> datacache = new ConcurrentHashMap<Integer, Data>();
	private static HashSet<Integer> requiredata = new HashSet<Integer>();
	private static HashSet<Integer> peerList = new HashSet<Integer>();

	static class UploadToPeer implements Runnable {

		private Socket socket;
		private int clientId;
		private int clientPortID;
		private DataOutputStream dos = null;
		private DataInputStream dis = null;

		public UploadToPeer(Socket socket, int clientId, int clientPortId) {
			this.socket = socket;
			this.clientId = clientId;
			this.clientPortID = clientPortId;
		}

		private void processPacketRequest() {
			try {
				int requestedPacketId = dis.readInt();
				System.out.println("RECEIVED request for CHUNK " + requestedPacketId);
				Data data = datacache.get(requestedPacketId);
				if (data != null) {
					byte byteBuffer[] = data.getData();
					dos.writeInt(requestedPacketId);
					System.out.println("UPLOADED " + requestedPacketId);
					dos.writeInt(byteBuffer.length);
					dos.write(byteBuffer);
				} else {
					System.out.println("CHUNK " + requestedPacketId + " not available.");
					dos.writeInt(-1);
				}
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void processPacketList() {
			try {
				int size = datacache.size();
				dos.writeInt(size);
				int i = 0;
				for (int packetId : datacache.keySet()) {
					dos.writeInt(packetId);
					if (i == size - 1) {
						System.out.println("Size : " + size + " i " + i);
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {

			System.out.print("Ready to Upload the data to the Peer....");

			try {
				dos = new DataOutputStream(socket.getOutputStream());
				dis = new DataInputStream(socket.getInputStream());
				while (true) {

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						System.out.println(" Sleep Failed : " + e.getMessage());
					}

					/**
					 * This protocol consists of 3 different type of messaages
					 * from the client.
					 * 
					 * 1. The request value can be -1 that means that the client
					 * has got all the packets and it don't need any further
					 * packet.
					 * 
					 * 2. The request value can be -2 that means the client need
					 * the list of packets server is having. In that condition,
					 * server will send all list of all the packets.
					 * 
					 * 
					 * 3. The request will be -3 the client requests a
					 * particular packet with specific packet Id.
					 * 
					 */

					int requestedType = dis.readInt();

					if (requestedType == -1) {
						break;
					} else if (requestedType == -2) {
						processPacketList();
					} else if (requestedType == -3) {
						processPacketRequest();
					}
				}

			} catch (IOException e) {
				if (requiredata.size() > 0) {
					// e.printStackTrace();
					System.out.println("Connection is dead. Retry Again...");
					throw new RuntimeException("Connection broke retry again.");
				}

				System.out.println("All Chunks DOWNLOADED .");
			} finally {

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					System.out.println(" Sleep Failed : " + e.getMessage());
				}

				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}
	}

	static class DownloadFromPeer implements Runnable {

		private int downloadFromClientId;
		private int downloadFromClientPortId;
		private DataInputStream dis = null;
		private DataOutputStream dos = null;

		private Socket socket;
		private static final int CHUNK_SIZE = 100000;

		public DownloadFromPeer(int peerId, int portId) {
			this.downloadFromClientId = peerId;
			this.downloadFromClientPortId = portId;
		}

		private void getPeerChunkList() {

			System.out.println("\n\nRequested CHUNK list");
			try {

				dos.writeInt(-2);
				int size = dis.readInt();
				System.out.println("Received following CHUNK list");

				for (int i = 0; i < size; i++) {
					int packetId = dis.readInt();
					peerList.add(packetId);
					System.out.print(packetId + ", ");
				}
				System.out.println("\n\n");

				ArrayList<Integer> removeList = new ArrayList<Integer>();
				for (int id : peerList) {
					if (!requiredata.contains(id))
						removeList.add(id);
				}

				peerList.removeAll(removeList);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void downloadPeerChunks() {
			FileWriter summaryFileWriter = null;

			try {
				summaryFileWriter = new FileWriter(new File(summaryFilePath), true);
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (int requestedPacketId : peerList) {

				try {

					dos.writeInt(-3);
					dos.writeInt(requestedPacketId);
					System.out.println("SENT request for CHUNK " + requestedPacketId);

					int receivedPacketId = dis.readInt();

					if (receivedPacketId >= 0 && receivedPacketId == requestedPacketId) {
						int receivedPacketSize = dis.readInt();
						int size = receivedPacketSize;
						byte[] bytearray = new byte[size];
						int offset = 0;
						while (size > 0) {
							int bytesread = dis.read(bytearray, offset, size);
							offset += bytesread;
							size -= bytesread;
						}

						// requiredata.remove(requestedPacketId);
						datacache.put(receivedPacketId, new Data(bytearray));
						String summary = String.format("%20s%30d\n", receivedPacketId, bytearray.length);
						summaryFileWriter.write(summary);
						requiredata.remove(receivedPacketId);
						System.out.println("DOWNLOADED : " + receivedPacketId);
						System.out.println("Remaining chunks to DOWNLOAD : " + requiredata.size());

					} else {
						// requiredata.addLast(requestedPacketId);
						System.out.println("DOWNLOAD : Peer " + downloadFromClientId + " don't have "
								+ requestedPacketId + " packet.");
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				summaryFileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		public void run() {
			int millisec = 100;
			while (true) {
				millisec += millisec;
				try {

					System.out.println("Connecting to peer on " + downloadFromClientPortId);
					socket = new Socket("127.0.0.1", downloadFromClientPortId);
					break;
				} catch (IOException e) {
					System.out.println("Connection failed. Retrying..");
					try {
						Thread.sleep(millisec);
					} catch (InterruptedException ee) {
						ee.printStackTrace();
					}
				}
			}

			System.out.println("Connected to client on port " + downloadFromClientPortId);

			try {
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());

				while (requiredata.size() > 0) {

					System.out.println("\n--------------------------------");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						System.out.println(" Sleep Failed : " + e.getMessage());
					}

					getPeerChunkList();

					downloadPeerChunks();

				}

				dos.writeInt(-1);
				System.out.println("DOWNLOADED ALL PACKETS.");

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					dis.close();
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	static class DownloadClient extends Thread {

		private Socket socket;
		private int port;
		private String downloadfilepath;
		private String serverip;
		private static final int CHUNK_SIZE = 100000;

		private int clientid;
		private InputStream is;

		public DownloadClient(int port, String ip, int clientid) {
			this.socket = null;
			this.port = port;
			this.serverip = ip;
			this.clientid = clientid;
		}

		public void run() {

			DataInputStream dis = null;
			DataOutputStream dos = null;

			try {
				socket = new Socket("127.0.0.1", port);
				System.out.println("Connecting to localhost on " + port);

				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());

				dos.writeInt(clientid);
				int bytesread = 0;
				System.out.println("Connected the server");

				TOTAL_CHUNK_COUNT = dis.readInt();

				int chunksToDownload = dis.readInt();
				System.out.println("Chunks count to Download : " + chunksToDownload);

				for (int i = 0; i < TOTAL_CHUNK_COUNT; i++) {
					requiredata.add(i);
				}

				ArrayList<Integer> ids = new ArrayList<Integer>();

				URL location = Client.class.getProtectionDomain().getCodeSource().getLocation();
				String outputpath = location.getPath() + "output/";
				File dir = new File(outputpath);
				if (!dir.exists()) {
					try {
						// System.out.println("Created folder " +
						// outputpath);
						dir.mkdir();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}

				outputpath += String.valueOf(MY_CLIENT_ID) + "/";
				dir = new File(outputpath);
				if (!dir.exists()) {
					try {
						dir.mkdir();
					} catch (SecurityException e) {
						e.printStackTrace();
					}
				}

				summaryFilePath = outputpath + "/" + "summary.txt";
				FileWriter summaryFileWriter = new FileWriter(new File(summaryFilePath), true);

				for (int i = 0; i < chunksToDownload; i++) {

					int packetId = dis.readInt();
					int size = dis.readInt();

					String outputfile = outputpath + "/" + String.valueOf(packetId);

					try {

						FileOutputStream fos;
						BufferedOutputStream bos;
						fos = new FileOutputStream(outputfile);

						bos = new BufferedOutputStream(fos);
						byte[] bytearray = new byte[size];
						int offset = 0;
						while (size > 0) {
							bytesread = dis.read(bytearray, offset, size);
							offset += bytesread;
							size -= bytesread;
						}

						System.out.println("DOWNLOADED : ChunkID " + packetId + " with size " + bytearray.length);
						String summary = String.format("%20s%30d\n", packetId, bytearray.length);
						bos.write(bytearray);

						// ids.add(packetId);
						requiredata.remove(packetId);
						datacache.put(packetId, new Data(bytearray));
						summaryFileWriter.write(summary);

						try {
							fos.close();
							bos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				summaryFileWriter.close();

				/*
				 * for (int i = ids.size() - 1; i >= 0; i--) { int id =
				 * ids.get(i); requiredata.remove(id); }
				 */
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

				try {
					dis.close();
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			System.out.println("Download is completed...");
		}
	}

	public static void main(String args[]) {

		// Get the current folder path and retrieve the ini file from the
		// current location
		ClassLoader clientPath = Client.class.getClassLoader();
		String iniFile = clientPath.getResource("").getPath();

		// Initialize the ini file path
		if (args.length > 0) {
			iniFile += args[0];
		} else {
			System.out.println("Please provide the ini file name.");
			System.out.println("Exiting...");
			return;
		}

		try {
			FileReader inputFile = new FileReader(iniFile);
			BufferedReader inputReader = new BufferedReader(inputFile);
			String line = null;

			// Check if there are more than one line in the ini or less than
			// more more than 4 tokens then inform the
			// user about this.

			if ((line = inputReader.readLine()) != null) {
				String[] tokens = line.split(" ");
				if (tokens.length == 2) {
					SERVER_ID = Integer.parseInt(tokens[0]);
					SERVER_PORT = Integer.parseInt(tokens[1]);
				} else {
					inputFile.close();
					throw new IllegalArgumentException(args[0] + " is not a valid format. Correct the format.");
				}
			}

			if ((line = inputReader.readLine()) != null) {
				String[] tokens = line.split(" ");
				if (tokens.length == 2) {
					DOWNLOAD_FROM_PEER = Integer.parseInt(tokens[0]);
					DOWNLOAD_FROM_PEER_PORT = Integer.parseInt(tokens[1]);
				} else {
					inputFile.close();
					throw new IllegalArgumentException(args[0] + " is not a valid format. Correct the format.");
				}
			}

			if ((line = inputReader.readLine()) != null) {
				String[] tokens = line.split(" ");
				if (tokens.length == 2) {
					MY_CLIENT_ID = Integer.parseInt(tokens[0]);
					MY_CLIENT_PORT = Integer.parseInt(tokens[1]);
				} else {
					inputFile.close();
					throw new IllegalArgumentException(args[0] + " is not a valid format. Correct the format.");
				}
			}

			inputFile.close();
		} catch (IOException e) {
			System.out.println("");
			e.printStackTrace();
		}

		Thread downloadclient = new Thread(new DownloadClient(SERVER_PORT, "", MY_CLIENT_ID));
		downloadclient.start();

		try {
			downloadclient.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		/****** Working till Here ******/

		Thread downloadHandler = new Thread(new DownloadFromPeer(DOWNLOAD_FROM_PEER, DOWNLOAD_FROM_PEER_PORT));
		downloadHandler.start();

		ServerSocket listener = null;
		Thread uploadHandler = null;

		try {
			listener = new ServerSocket(MY_CLIENT_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Socket soc = null;

		try {
			soc = listener.accept();
			System.out.println("Created Socket on the Port : " + MY_CLIENT_PORT + " " + soc);
			uploadHandler = new Thread(new UploadToPeer(soc, MY_CLIENT_ID, MY_CLIENT_PORT));
			uploadHandler.run();
			System.out.println("Completed the Thread.");
		} catch (RuntimeException e) {
			System.out.println("Exception while sending data. Try connectioin again.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			System.out.println("Closing the socket..");
			soc.close();
			listener.close();
		} catch (IOException e) {
			System.out.println("Failed to close the socket successfully.");
		}

		try {
			System.out.println("Waiting to join all the workers...");
			downloadHandler.join();
			uploadHandler.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (datacache.size() == TOTAL_CHUNK_COUNT) {
			System.out.println("Joining the chunks into file...");
			FileJoiner filejoiner = new FileJoiner(MY_CLIENT_ID, datacache);
			filejoiner.joinFiles();
		}

		System.out.println("Closing client " + MY_CLIENT_ID);

	}
}
