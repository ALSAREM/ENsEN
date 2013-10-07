package ensen.threads;

import ensen.entities.Document;
import ensen.entities.Triplet;

public class TriplesScoreThread extends Thread {
	Triplet entry;
	Document D;
	public double score;
	public String URI;

	public TriplesScoreThread(Triplet entry, Document D) {
		super(); // Store the thread name
		this.entry = entry;
		this.D = D;
		start();
	}

	public String toString() {
		return "entry" + this.getState().toString();
	}

	public void run() {

		D.TripletsScoreCounter++;
	}

}