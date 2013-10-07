package ensen.entities;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class Triplet {
	public Statement statement;
	public Resource subject;
	public Resource predicat;
	public RDFNode object;
	public double score;
	int type; // 1 annotation , 2 generated , 3 

}
