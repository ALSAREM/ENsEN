package ensen.entities;

import org.dbpedia.spotlight.model.DBpediaResource;
import org.dbpedia.spotlight.model.OntologyType;

import scala.collection.immutable.List;

public class EnsenDBpediaResource extends DBpediaResource {
	public int offset;
	public String originalText;
	public Double similarityScore;
	public Double percentageOfSecondRank;
	public String types;

	public EnsenDBpediaResource(String uri) {
		super(uri);
		// TODO Auto-generated constructor stub
	}

	public EnsenDBpediaResource(String uri, int support, int offset, String originalText, Double similarityScore, String types, Double percentageOfSecondRank) {
		super(uri, support);
		this.offset = offset;
		this.originalText = originalText;
		this.similarityScore = similarityScore;
		this.types = types;
		this.percentageOfSecondRank = percentageOfSecondRank;

	}

	public DBpediaResource getSuper() {

		return this;
	}

	public EnsenDBpediaResource(String uri, int support) {
		super(uri, support);
		// TODO Auto-generated constructor stub
	}

	public EnsenDBpediaResource(String uri, int support, double prior) {
		super(uri, support, prior);
		// TODO Auto-generated constructor stub
	}

	public EnsenDBpediaResource(String uri, int support, double prior, List<OntologyType> types) {
		super(uri, support, prior, types);
		// TODO Auto-generated constructor stub
	}

}
