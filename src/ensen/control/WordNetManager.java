package ensen.control;

import java.awt.EventQueue;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class WordNetManager {
	WordNetDatabase database;

	public WordNetManager() {

		System.setProperty("wordnet.database.dir", PropertiesManager.getProperty("wordnetPath"));

		database = WordNetDatabase.getFileInstance();

	}

	public String extendText(String in) {
		String res = in;
		String[] words = in.split(" ");
		for (int i = 0; i < words.length; i++) {
			res += extendOneWord(words[i]) + " ";
		}
		return res;
	}

	public String extendTextWithLimit(String in, int Limit) {
		String res = "";
		String[] words = in.split(" ");
		for (int i = 0; i < words.length; i++) {
			res += extendOneWordWithLimit(words[i], Limit) + " ";
		}
		return res;
	}

	public String extendOneWord(String in) {
		String res = " ";
		Synset nounSynset;
		NounSynset[] hyponyms;
		NounSynset[] holonyms;
		NounSynset[] hypernyms;
		Synset[] synsets = database.getSynsets(in);

		for (int i = 0; i < synsets.length; i++) {
			nounSynset = (synsets[i]);
			//res += "\n ----------------synset(" + i + ")----------------------- \n";
			//res += "\n ----------------Definition----------------------- \n";
			//	res += " " + nounSynset.getDefinition();

			//res += "\n ----------------WordForms----------------------- \n";
			for (int j = 0; j < nounSynset.getWordForms().length; j++) {
				res += " " + nounSynset.getWordForms()[j];
			}

			/*
						try {
							//res += "\n ----------------Topics----------------------- \n";
							NounSynset[] topics = ((NounSynset) nounSynset).getTopics();
							for (int j = 0; j < topics.length; j++) {
									res += " " + topics[j].getWordForms()[0];
									res += " " + topics[j].getDefinition();
							}

						} catch (Exception e) {

						}*/
			/*
			try {
				//	res += "\n ----------------hypernyms----------------------- \n";
				hypernyms = ((NounSynset) nounSynset).getHypernyms();
				for (int j = 0; j < hypernyms.length; j++) {
							res += " " + hypernyms[j].getWordForms()[0];
							res += " " + hypernyms[j].getDefinition();
				}

			} catch (Exception e) {

			}
			*/
			/*
						try {
							//	res += "\n ----------------hyponyms----------------------- \n";
							hyponyms = ((NounSynset) nounSynset).getHyponyms();
							for (int j = 0; j < hyponyms.length; j++) {
									res += " " + hyponyms[j].getWordForms()[0];
									res += " " + hyponyms[j].getDefinition();
							}

						} catch (Exception e) {

						}
						*/
			/*
			try {
				//	res += "\n ----------------holonyms----------------------- \n";
				holonyms = ((NounSynset) nounSynset).getMemberHolonyms();
				for (int j = 0; j < holonyms.length; j++) {
						res += " " + holonyms[j].getWordForms()[0];
						res += " " + holonyms[j].getDefinition();
				}
			} catch (Exception e) {

			}*/

		}
		return res;
	}

	public String extendOneWordWithLimit(String in, int limit) {
		String res = " ";
		Synset nounSynset;
		Synset[] synsets = database.getSynsets(in);
		for (int i = 0; i < Math.min(synsets.length, limit); i++) {
			nounSynset = (synsets[i]);
			for (int j = 0; j < nounSynset.getWordForms().length; j++) {
				res += " " + nounSynset.getWordForms()[j];
			}
		}
		return res;
	}

	public String test(String in) {
		String res = "\n" + in;
		Synset nounSynset;
		NounSynset[] hyponyms;
		NounSynset[] holonyms;
		NounSynset[] hypernyms;
		Synset[] synsets = database.getSynsets(in);

		for (int i = 0; i < synsets.length; i++) {
			nounSynset = (synsets[i]);
			res += "\n ----------------synset(" + i + ")----------------------- \n";
			res += "\n ----------------Definition----------------------- \n";
			res += "\n " + nounSynset.getDefinition();

			res += "\n ----------------WordForms----------------------- \n";
			for (int j = 0; j < nounSynset.getWordForms().length; j++) {
				res += nounSynset.getWordForms()[j] + ", ";
			}

			try {
				res += "\n ----------------Topics----------------------- \n";
				NounSynset[] topics = ((NounSynset) nounSynset).getTopics();
				for (int j = 0; j < topics.length; j++) {
					res += "\n " + topics[j].getWordForms()[0];
					res += "\n " + topics[j].getDefinition();
				}

			} catch (Exception e) {

			}
			try {
				res += "\n ----------------hypernyms----------------------- \n";
				hypernyms = ((NounSynset) nounSynset).getHypernyms();
				for (int j = 0; j < hypernyms.length; j++) {
					res += "\n " + hypernyms[j].getWordForms()[0];
					res += "\n " + hypernyms[j].getDefinition();
				}

			} catch (Exception e) {

			}

			try {
				res += "\n ----------------hyponyms----------------------- \n";
				hyponyms = ((NounSynset) nounSynset).getHyponyms();
				for (int j = 0; j < hyponyms.length; j++) {
					res += "\n " + hyponyms[j].getWordForms()[0];
					res += "\n " + hyponyms[j].getDefinition();
				}

			} catch (Exception e) {

			}
			try {
				res += "\n ----------------holonyms----------------------- \n";
				holonyms = ((NounSynset) nounSynset).getMemberHolonyms();
				for (int j = 0; j < holonyms.length; j++) {
					res += "\n " + holonyms[j].getWordForms()[0];
					res += "\n " + holonyms[j].getDefinition();
				}
			} catch (Exception e) {

			}

		}
		return res;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WordNetManager WN = new WordNetManager();
					System.err.println(WN.extendText("information retrieval"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
