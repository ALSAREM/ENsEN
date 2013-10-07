package ensen.entities;

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Model;

import ensen.control.RDFManager;

public class BipartiteGraph {

	public Model rdfGraph;
	public ArrayList<String> v1;
	public ArrayList<String> v2;
	public int[][] M;
	public int Mt[][];
	public int Ma[][];
	public double[][] Pa;
	public int triplesCounter;
	public int entitesSize;
	public boolean indexed;

	public BipartiteGraph() {
		rdfGraph = RDFManager.createRDFModel();
		v1 = new ArrayList<String>();
		v2 = new ArrayList<String>();
	}
}
