package ensen.util;

//------------------------------------------------------------//
//JavaGetUrl.java:                                          //
//------------------------------------------------------------//
//A Java program that demonstrates a procedure that can be  //
//used to download the contents of a specified URL.         //
//------------------------------------------------------------//

import java.io.IOException;

import org.jsoup.Jsoup;

import ensen.control.PropertiesManager;

public final class HTMLreader {
	static String tempTitle;

	public static String load(String uri) {
		String res = "";
		boolean goo = true;
		int i = 0;
		while (goo) {
			try {
				org.jsoup.Connection c = Jsoup.connect(uri);
				c.timeout(Integer.parseInt(PropertiesManager.getProperty("getHtmlTiemout")));
				c.followRedirects(true);
				c.ignoreHttpErrors(true);
				org.jsoup.nodes.Document d = c.get();
				res = d.html();
				goo = false;
				System.out.println("Get Html code (" + uri + "):Try (" + i + ") succes");
			} catch (IOException e1) {
				// e1.printStackTrace();
				System.out.println("Get Html code (" + uri + "): Try (" + i + ") failed");
				if (i == Integer.parseInt(PropertiesManager.getProperty("getHtmlTries"))) {
					goo = false;
				}
				i++;
			}
		}

		return res;

	} // end of main

	// end of class definition
}
