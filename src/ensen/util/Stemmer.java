package ensen.util;

import org.tartarus.snowball.SnowballStemmer;

public class Stemmer {
	SnowballStemmer SBStemmer = null;

	public Stemmer() {
		Class stemClass;
		try {
			System.err.println("Initialize porter stemmer");
			stemClass = Class.forName("org.tartarus.snowball.ext.porterStemmer");
			SBStemmer = (SnowballStemmer) stemClass.newInstance();
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		} catch (InstantiationException e) {
			
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			
			e.printStackTrace();
		}

	}

	public String stem(String word) {
		//System.out.print(w + " --> ");
		SBStemmer.setCurrent(word);
		SBStemmer.stem();
		return SBStemmer.getCurrent();
		//System.out.println(SBStemmer.getCurrent());
	}

}
