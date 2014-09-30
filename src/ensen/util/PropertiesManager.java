package ensen.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManager {

	public PropertiesManager() {

	}

	public static String getProperty(String name) {

		Properties defaultProps = getPropertiesFromClasspath();
		return defaultProps.getProperty(name);
	}

	public static void setProperty(String name, String value) {

		FileOutputStream out;

		Properties defaultProps = getPropertiesFromClasspath();

		defaultProps.setProperty(name, value);
		try {

			out = new FileOutputStream(PropertiesManager.class.getResource("/ensen/" + getPropertiesName()).getPath());
			defaultProps.store(out, "");
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}

	}

	private static String OS = null;

	public static String getPropertiesName() {
		if (isWindows()) {
			return "ensen.properties";
		}
		return "ensen.linux.properties";
	}

	public static String getOsName() {
		if (OS == null) {
			OS = System.getProperty("os.name");
		}
		return OS;
	}

	public static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}

	public static boolean isUnix() {
		return !getOsName().startsWith("Windows");
	}

	static Properties getPropertiesFromClasspath() {
		Properties props = new Properties();
		try {
			InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(getPropertiesName());

			props.load(stream);
			stream.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}


		return props;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println(PropertiesManager.getProperty("rootPath"));
	}

}
