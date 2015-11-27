package finalprojectclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

class FileJoiner {

	private int clientId;
	private HashMap<Integer, Data> datacache;

	public FileJoiner(int clientId, HashMap<Integer, Data> datacache) {
		this.clientId = clientId;
		this.datacache = datacache;
	}

	public void joinFiles() {

		String dirName = "/home/rkmalik/work/allworkspaces/semester3/networks/outputfile/outputfile/"
				+ String.valueOf(clientId) + "/";
		File dir = new File(dirName);

		if (!dir.exists()) {

			try {
				dir.mkdir();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}

		String fileName = dirName + "targetfile.pdf";
		File targetFile = new File(fileName);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetFile, true);

			for (int i = 0; i < datacache.size(); i++) {
				fos.write(datacache.get(i).getData(), 0, datacache.get(i).getData().length);
			}
			
			System.out.println("Created OUPUTFILE with size : " + targetFile.length());

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}