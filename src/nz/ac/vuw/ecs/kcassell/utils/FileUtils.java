package nz.ac.vuw.ecs.kcassell.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class FileUtils {

	public static URL toURL(String fileName) throws MalformedURLException {
		File file = new File(fileName);
		URL url = file.toURI().toURL();
		return url;
	}

	public static String toURLString(String fileName) throws MalformedURLException {
		File file = new File(fileName);
		URL url = file.toURI().toURL();
		return url.toExternalForm();
	}
}
