#ENsEN (Enhanced Search Engine)

A software system that enhances a SERP with semantic snippets. 
Given the query, we obtain the SERP (we used Google for our experiments). For each result of the SERP, we use DBpedia Spotlight to obtain a set of DBpedia entities. In the same way, we find entities from the terms of the query. From this set of entities and through queries to a DBpedia SPARQL endpoint, we obtain a graph by finding all the relationships between the entities. 
To each entity, we associate a text obtained by merging its DBpedia’s abstract and windows of text from the webpage centered on the surface forms associated with the entity. With as input the graph, its associated text, and the entities extracted from the query, we execute LDRANK and we obtain a ranking of the entities. The top-ranked entities (viz. “main-entities”) are displayed on the snippet. 
From a DBpedia SPARQL endpoint, we do a 1-hop extension of the main-entities in order to increase the number of triples among which we will then search for the more important ones in terms of a link analysis of the graph. To do this, we build a 3-way tensor from the extended graph: each predicate corresponds to an horizontal slice that represents the adjacency matrix for the restriction of the graph to this predicate. We compute the PARAFAC decomposition of the tensor into a sum of factors (rank-one three-way tensors): for each main-entity, we select the factors to which it contributes the most (as a subject or as an object), and for each of these factors we select the triples with the best ranked predicates. Thus, we associate to each main-entity a set of triples that will appear within its description. 
Finally, we used a machine learning approach to select short excerpts of the webpage to be part of the description of each main-entity. We designed a number of features based on the query, the text of the webpage, and the ranked entities. For the feature selection process, we used an infogain metric to select a small set of features then used as a starter-set for a wrapper method with a forward selection approach. We observed that the features derived from the LDRANK ranking remained in the set of used features, which stands as a supplementary, although indirect, element in favor of the usefulness of LDRANK.

LDRANK (Linked Data Ranking Algorithm)
We introduce LDRANK (Linked Data Ranking Algorithm), a quey-biased algorithm for ranking the entities of a RDF graph. LDRANK is well adapted to sparse graphs for which textual data can be associated with the nodes. Indeed, LDRANK uses both the explicit structure of the graph and the implicit relationships that can be inferred from the text of the nodes.

- source code under GPL licence: LDRANK v0.9 http://liris.cnrs.fr/drim/projects/ensen/data/ldrank_09.zip




For more information: 

- Website: http://liris.cnrs.fr/drim/projects/ensen

- Youtube: https://www.youtube.com/watch?v=eU72ubPiEqc



Mazen ALSAREM
