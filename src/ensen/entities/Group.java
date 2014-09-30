package ensen.entities;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import ensen.controler.RDFManager;

public class Group {
	static Logger log = Logger.getLogger(Group.class.getName());
	public String name;
	public TreeMap<String, Double> predicates;
	public TreeMap<String, Concept> auths;
	public TreeMap<String, Concept> hubs;
	Document doc;
	public String title = "";
	public String description = "";
	public boolean noSnippet;

	public Group(String n, Document doci) {
		name = n;
		doc = doci;
	}

	public void buildTheSnippet() {
		if (isGood()) {

			buildDesc();
			noSnippet = false;
		} else
			noSnippet = true;
	}

	private boolean isGood() {
		if (predicates != null && predicates.size() > 0) {
			TreeMap<String, Double> ps = new TreeMap<String, Double>();
			for (Entry<String, Double> p : predicates.entrySet()) {
				if (!p.getKey().contains("http://ensen.org/data#virtualProperty"))
					ps.put(p.getKey(), p.getValue());
			}
			return (ps.size() > 0) && (auths.size() > 0) && (hubs.size() > 0);
		} else
			return false;
	}

	private void buildDesc() {
		description = "";
		Model m = RDFManager.createRDFModel();
		for (String auth : auths.keySet()) {
			for (String p : predicates.keySet()) {
				StmtIterator sts = doc.fullGraph.listStatements(null, m.createProperty(p), m.createResource(auth));
				while (sts.hasNext()) {
					Statement st = sts.next();
					description += st.getSubject().getLocalName().replace("_", " ") + " " + st.getPredicate().getLocalName().replace("_", " ") + " " + st.getObject().asResource().getLocalName().replace("_", " ") + ", ";
				}
			}

		}

		for (String hub : hubs.keySet()) {
			for (String p : predicates.keySet()) {
				StmtIterator sts = doc.fullGraph.listStatements(m.createResource(hub), m.createProperty(p), (RDFNode) null);
				while (sts.hasNext()) {
					Statement st = sts.next();
					if (st.getObject().isResource())
						description += st.getSubject().getLocalName().replace("_", " ") + " " + st.getPredicate().getLocalName().replace("_", " ") + " " + st.getObject().asResource().getLocalName().replace("_", " ") + ", ";
					else
						description += st.getSubject().getLocalName().replace("_", " ") + " " + st.getPredicate().getLocalName().replace("_", " ") + " " + st.getObject().asNode().toString().substring(0, Math.min(st.getObject().asNode().toString().length(), 50)) + ", ";

				}
			}
		}
	}

	public String toString() {
		String out = "";
		out += name + "\n";
		out += "Predicates: " + "\n";
		for (Entry<String, Double> p : predicates.entrySet()) {
			out += p.getKey() + "==>" + p.getValue() + "\n";
		}
		out += "Auths: " + "\n";
		for (Entry<String, Concept> p : auths.entrySet()) {
			out += p.getKey() + "==>" + p.getValue().score + "\n";
		}
		out += "Hubs: " + "\n";
		for (Entry<String, Concept> p : hubs.entrySet()) {
			out += p.getKey() + "==>" + p.getValue().score + "\n";
		}

		return out;
	}

	public ArrayList<String> getTriples(Document d) {
		ArrayList<String> triples = new ArrayList<>();

		return triples;
	}

}
