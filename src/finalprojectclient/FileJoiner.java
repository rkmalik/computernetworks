package finalprojectclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

class FileJoiner {

	private int clientId;
	private ConcurrentHashMap<Integer, Data> datacache;

	public FileJoiner(int clientId, ConcurrentHashMap<Integer, Data> datacache) {
		this.clientId = clientId;
		this.datacache = datacache;
	}

	public void joinFiles() {

		URL location = Client.class.getProtectionDomain().getCodeSource().getLocation();
		String dirName = location.getPath() + "output/";
		File dir = new File(dirName);

		if (!dir.exists()) {
			try {
				dir.mkdir();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		
		dirName+=String.valueOf(clientId);
		dir = new File(dirName); 

		if (!dir.exists()) {
			try {
				dir.mkdir();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		
		String fileName = dirName + "/targetfile";
		System.out.println("Joining the chunks into file - " + fileName);
		File targetFile = new File(fileName);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetFile, true);

			for (int i = 0; i < datacache.size(); i++) {
				fos.write(datacache.get(i).getData(), 0, datacache.get(i).getData().length);
			}
			
			System.out.println("Created OUPUTFILE with size : " + targetFile.length());
			fos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}