package in.fourbits.schemadiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import difflib.Chunk;

/**
 * App to get the latest schema from the configured URL and then find the
 * differences with the existing file
 * 
 *
 */
public class App {

	private static String currentFilePath;
	private static String revisedFilePath;
	private static String currentFileName;
	private static String revisedFileExtension;
	private static String revisedFileURL;
	
	/**
	 * Initialize fields by reading from external config file
	 */
	public static void initConfig() {
		Properties properties = new Properties();
		FileInputStream fis;
		try {
			System.out.println("Initializing!!");
			fis = new FileInputStream("config.properties");
			properties.load(fis);
			currentFilePath = properties.getProperty("currentFilePath");
			revisedFilePath = properties.getProperty("revisedFilePath");
			currentFileName = properties.getProperty("currentFileName");
			revisedFileExtension = properties.getProperty("revisedFileExtension");
			revisedFileURL = properties.getProperty("revisedFileURL");
		} catch (IOException e) {
			System.out.println("No config file found!! Exiting now");
			System.exit(0);
		}
	}

	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {

		initConfig();

		new SchemaDownloader().downloadSchema(revisedFileURL, revisedFilePath, revisedFileExtension);
		System.out.println("Downloading revised file from : " + revisedFileURL);
		
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		DateFormat formatter = new SimpleDateFormat("ddMMyyy");
		File original = new File(currentFilePath + currentFileName);
		File revised = new File(revisedFilePath + formatter.format(new Date()) + revisedFileExtension);
		
		SchemaComparator comparator = new SchemaComparator(original, revised);
		try {
			System.out.println("\n============ Changes ===============\n");
			List<LineDiff> diffList = comparator.getChangesFromOriginal();
			for (LineDiff diff : diffList) {
				for (int i = 0; i < diff.getBefore().getLines().size(); i++) {
					System.out.println("[Pos: " + diff.getBefore().getPosition() + "]" + getData(diff.getBefore(), i)
							+ " --> " + getData(diff.getAfter(), i));
				}
			}
			System.out.println("\n============ Inserts ===============\n");
			diffList = comparator.getInsertsFromOriginal();
			for (LineDiff diff : diffList) {
				for (int i = 0; i < diff.getBefore().getLines().size(); i++) {
					System.out.println("[Pos: " + diff.getBefore().getPosition() + "]" + getData(diff.getBefore(), i)
							+ " --> " + getData(diff.getAfter(), i));
				}
			}

			System.out.println("\n============ Deletion ===============\n");
			diffList = comparator.getDeletionFromOriginal();
			for (LineDiff diff : diffList) {
				for (int i = 0; i < diff.getBefore().getLines().size(); i++) {
					System.out.println("[Pos: " + diff.getBefore().getPosition() + "]" + getData(diff.getBefore(), i)
							+ " --> " + getData(diff.getAfter(), i));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Get differences in lines
	 * @param data can be before/after
	 * @param i arrayList index
	 * @return Object
	 */
	public static Object getData(Chunk data, int i) {
		try {
			return data.getLines().get(i);
		} catch (IndexOutOfBoundsException ie) {
			return "";
		}
	}
}
