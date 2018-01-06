package in.fourbits.schemadiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

	public static void sendMail() {
		Properties properties = new Properties();
		FileInputStream fis;
		final String username = "<username>";
		final String password = "<password>";
		String fromAddress = "";
		String toAddress = "";
		try {
			fis = new FileInputStream("config.properties");
			properties.load(fis);
			fromAddress = properties.getProperty("mailFrom");
			toAddress = properties.getProperty("mailTo"); // suraj.trivikram.acharya@sap.com
			Properties props = new Properties();
			
			props.put("mail.smtp.auth", properties.getProperty("mailSmtpAuth"));
			props.put("mail.smtp.starttls.enable", properties.getProperty("mailSmtpTlsEnabled"));
			props.put("mail.smtp.host", properties.getProperty("mailSmtpHost"));
			props.put("mail.smtp.port", properties.getProperty("mailSmtpPort"));
			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

			try {
				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(fromAddress));
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
				message.setSubject(properties.getProperty("mailSubject"));
				message.setText(properties.getProperty("mailBody"));

				Transport.send(message);

				System.out.println("Schema difference Notification sent to the team");

			} catch (MessagingException e) {
				System.out.println("Schema difference Notification to team failed");
				throw new RuntimeException(e);
			}

		} catch (IOException e1) {
			System.out.println("Schema difference Notification to team failed");
			e1.printStackTrace();
		}
	}

	/**
	 * Main method
	 * 
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
		boolean hasChanges = false, hasInsertion = false, hasDeletion = false;

		SchemaComparator comparator = new SchemaComparator(original, revised);
		try {
			System.out.println("\n============ Changes ===============\n");
			List<LineDiff> diffList = comparator.getChangesFromOriginal();
			int size = 0;
			for (LineDiff diff : diffList) {
				hasChanges = (size = diff.getBefore().getLines().size()) > 0 ? true : false;
				for (int i = 0; i < size; i++) {
					System.out.println("[Pos: " + diff.getBefore().getPosition() + "]" + getData(diff.getBefore(), i)
							+ " --> " + getData(diff.getAfter(), i));
				}
			}
			System.out.println("\n============ Inserts ===============\n");
			diffList = comparator.getInsertsFromOriginal();
			for (LineDiff diff : diffList) {
				hasInsertion = (size = diff.getBefore().getLines().size()) > 0 ? true : false;
				for (int i = 0; i < size; i++) {
					System.out.println("[Pos: " + diff.getBefore().getPosition() + "]" + getData(diff.getBefore(), i)
							+ " --> " + getData(diff.getAfter(), i));
				}
			}

			System.out.println("\n============ Deletion ===============\n");
			diffList = comparator.getDeletionFromOriginal();
			for (LineDiff diff : diffList) {
				hasDeletion = (size = diff.getBefore().getLines().size()) > 0 ? true : false;
				for (int i = 0; i < size; i++) {
					System.out.println("[Pos: " + diff.getBefore().getPosition() + "]" + getData(diff.getBefore(), i)
							+ " --> " + getData(diff.getAfter(), i));
				}
			}

			if (hasChanges || hasInsertion || hasDeletion) {
				sendMail();
			} else {
				System.out.println("No Schema difference identified");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Get differences in lines
	 * 
	 * @param data
	 *            can be before/after
	 * @param i
	 *            arrayList index
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
