package ensen.test;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;

import ensen.control.DBpediaSpotlightClient;
import ensen.control.QueryHandler;
import ensen.control.RDFManager;
import ensen.entities.Query;

public class TestQ {
	private QueryHandler QHandler;
	private Query Q;

	public TestQ(String in) {
		System.out.println("Step 3: Query Analyses");
		DBpediaSpotlightClient c = new DBpediaSpotlightClient();
		//c.local = true;
		QHandler = new QueryHandler();
		Q = QHandler.createQuery(in, true, true, 3, c);

		int dataSize = 0;

		if (Q.graph != null) {
			ResIterator L = Q.graph.listSubjects();
			dataSize += L.toList().size();
		}

		if (Q.graph != null) {
			ResIterator L = Q.graph.listSubjects();
			int j = 0;
			while (L.hasNext()) {
				Resource res = L.next();
				Selector selector = new SimpleSelector(null, null, res);
				System.out.println("Label: " + res.getLocalName() + " , URI: " + res.getURI() + " , Object:" + Q.graph.listStatements(selector).toList().size() + " , Subject:" + res.listProperties().toList().size());

				j++;

			}
		}

		int objects = Q.graph.listObjects().toList().size();
		int trips = Q.graph.listStatements().toList().size();
		System.out.println("Statistics: Triples (" + trips + "),Entities(" + (objects + dataSize) + ") ,Subjects(" + dataSize + ") ,Objects(" + objects + ")");

		QHandler.Qexpansion(Q);

		objects = Q.ExtendedGraph.listObjects().toList().size();
		trips = Q.ExtendedGraph.listStatements().toList().size();
		dataSize = 0;

		if (Q.ExtendedGraph != null) {
			ResIterator L = Q.ExtendedGraph.listSubjects();
			dataSize += L.toList().size();
		}
		System.out.println("Statistics: Triples (" + trips + "),Entities(" + (objects + dataSize) + ") ,Subjects(" + dataSize + ") ,Objects(" + objects + ")");

		RDFManager.createRDFfile("QGraph", Q.graph);
		RDFManager.createRDFfile("QEGraph", Q.ExtendedGraph);
		RDFManager.createRDFfile("DiffGraph", Q.ExtendedGraph.difference(Q.graph));

		/*GraphViewer V = new GraphViewer();
		V.layoutType = 0;
		V.showGraph(Q.ExtendedGraph.difference(Q.graph), true, true, true);
		V.show();*/
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestQ TQ = new TestQ("Information Retrieval");

	}

}
