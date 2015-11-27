package finalprojectserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;


public class Server {

	private static int SERVER_PORT = 8000;
	private static int SERVER_ID;
	private static String filepath = null;
	private static String fileName = null;
	private static FileSplitter filesplitter;

	private static HashSet<Integer> clientIDList;

	public static class RequestHandler extends Thread {

		private int clientId;
		private Socket socket;

		public RequestHandler(Socket socket, int clientNum) {
			this.socket = socket;
			this.clientId = clientNum;
		}

		public void run() {

			DataOutputStream dos = null;
			DataInputStream dis = null;

			try {

				int totalchunks = (int) filesplitter.getChunkCount();
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());

				clientId = dis.readInt();

				int clientsCount = clientIDList.size();
				System.out.println("Total Client Counts " + clientsCount);

				int chunkstosend = (totalchunks / clientsCount) + (totalchunks % clientsCount >= clientId ? 1 : 0);
				System.out.println("Total chunks to send " + chunkstosend);

				dos.writeInt(totalchunks);
				dos.writeInt(chunkstosend);

				for (int i = clientId - 1; i < totalchunks; i += clientsCount) {

					dos.writeInt(i);

					byte[] buffer = filesplitter.getChunk(i);
					dos.writeInt(buffer.length);

					dos.write(buffer);
					dos.flush();

					System.out.println("UPLOADED : Chunk " + i + " to client " + clientId);

				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {

				try {
					dos.close();
					dis.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	private void initFileSplitter() {

		filesplitter = new FileSplitter(filepath, fileName);

	}

	public static void main(String args[]) {

		ClassLoader clientPath = Server.class.getClassLoader();
		String iniFile = clientPath.getResource("").getPath();

		// Initialize the ini file path
		iniFile += "start.ini";

		clientIDList = new HashSet<Integer>();

		try {
			FileReader inputFile = new FileReader(iniFile);
			BufferedReader inputReader = new BufferedReader(inputFile);
			String line = null;
			while ((line = inputReader.readLine()) != null) {
				String[] tokens = line.split(" ");

				// If there are only two tokens that means this is the Server
				// Port and its Id
				if (tokens.length == 2) {
					SERVER_ID = Integer.parseInt(tokens[0]);
					SERVER_PORT = Integer.parseInt(tokens[1]);

				} else if (tokens.length == 4) {
					// There are four tokens that means this is about the
					// UPLOADER and THE DOWNLOADER
					clientIDList.add(Integer.parseInt(tokens[0]));
				} else {
					inputFile.close();
					inputReader.close();
					throw new IllegalArgumentException(args[0] + " is not a valid format. Correct the format.");
				}
			}
			inputFile.close();
			inputReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Server is running...");
		System.out.println("Please enter the file name");
		Scanner sc = new Scanner(System.in);
		System.out.println("");

		fileName = sc.nextLine();
		URL location = Server.class.getProtectionDomain().getCodeSource().getLocation();
		filepath = location.getPath() + "input/" + fileName;
		System.out.println(filepath);

		Server srvr = new Server();
		srvr.initFileSplitter();

		ServerSocket listener = null;
		int clientNum = 1;

		try {
			listener = new ServerSocket(SERVER_PORT);

			while (true) {
				RequestHandler handler = new RequestHandler(listener.accept(), clientNum);
				handler.start();
				System.out.println("\n\nClient " + clientNum + " is connected on port number : " + SERVER_PORT);
				clientNum++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				listener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
