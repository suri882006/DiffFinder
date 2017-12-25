package in.fourbits.schemadiff;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Responsible to download the file from the specified URL and write the content
 * to revised directory
 * 
 *
 */
public class SchemaDownloader {

	public void downloadSchema(final String fileURL, final String filePath, String fileExtension) {
		try {
			DateFormat formatter = new SimpleDateFormat("ddMMyyy");
			URL url = new URL(fileURL);
			try {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.connect();

				if (connection.getResponseCode() == 200) {
					InputStream is = new BufferedInputStream(connection.getInputStream(), 1024);
					FileOutputStream fos = new FileOutputStream(
							new File(filePath + formatter.format(new Date()) + fileExtension));
					int data = 0;
					while ((data = is.read()) != -1) {
						fos.write((char) data);
					}
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
