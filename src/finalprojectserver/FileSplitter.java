package finalprojectserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * FileSplitter will split the file into equal chunks of 100 KB except the last
 * chunk.
 * 
 * Functionality of this Class. 1. Constructor opens the file. 2. Once opened it
 * will read the file and split the file in chunks. 3. Once asked by the server
 * it will return the random chunk to the server. Which will be sent to the
 * client.
 * 
 * 
 * 
 */
public class FileSplitter {

	private String filePath;
	private File file = null;

	private FileInputStream fis;
	private BufferedInputStream bis;

	private final int CHUNK_SIZE = 100000;
	private byte[][] cache = null;
	
	private long filesize = 0;
	private long chunkcount = 0;
	
	
	public long getFileSize () {
		return filesize;
	}
	
	public long getChunkCount () {
		return chunkcount;
	}
	
	public byte [] getChunk (int chunkIndex) {
		return cache [chunkIndex];
	}

	public FileSplitter(String filePath, String fileName) {
		this.filePath = filePath;
		file = new File(filePath);

		filesize = file.length();
		chunkcount = (long) Math.ceil((double) filesize / (double) CHUNK_SIZE);

		System.out.println("File Name : " + fileName);
		System.out.println("File size : " + filesize);
		System.out.println("File chunks : " + chunkcount);

		cache = new byte[(int) chunkcount][];

		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);

			int bytesread = 0;
			int id = 0;

			do {

				byte[] bytearray = new byte[CHUNK_SIZE];
				bytesread = bis.read(bytearray, 0, CHUNK_SIZE);
				
				if (bytesread < 0)	
					break;
				
				if (bytesread < CHUNK_SIZE) {
					
					byte [] smallerdata = new byte [bytesread];
					System.arraycopy(bytearray, 0, smallerdata, 0, bytesread);
					bytearray = smallerdata;
				}
				
				if (bytesread >0)
				cache[id++] = bytearray;
				
			} while (bytesread > 0);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				fis.close();
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
