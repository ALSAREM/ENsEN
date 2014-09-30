package ensen.ml;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SerializationHelper;
import ensen.util.PropertiesManager;

public class MlControler {

	public double[] evaluateForMainSentence(String[] values, String algo) {

		double[] out = null;
		Instances dataUnlabeled = new Instances("TestInstances", mainSentenceMl.getAtts(), 10);
		dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);
		DenseInstance instance = createMainInstance(values, dataUnlabeled);

		try {
			switch (algo) {
			case "libsvm":
				LibSVM svm = (LibSVM) SerializationHelper.read(PropertiesManager.getProperty("mlModels") + "/libsvm.main.model");
				svm.setProbabilityEstimates(true);
				out = svm.distributionForInstance(instance);
				System.out.println("LibSVM: false " + out[0] + " : true " + out[1]);
				break;
			case "logistic":
				Logistic logistic = (Logistic) SerializationHelper.read(PropertiesManager.getProperty("mlModels") + "/logistic.main.model");
				out = logistic.distributionForInstance(instance);
				System.out.println("Logistic: false " + out[0] + " : true " + out[1]);
				break;
			case "j48":
				J48 smo = (J48) SerializationHelper.read(PropertiesManager.getProperty("mlModels") + "/j48.main.model");
				out = smo.distributionForInstance(instance);
				System.out.println("j48: false " + out[0] + " : true " + out[1]);
				break;
			case "bayes":
				NaiveBayes bayes = (NaiveBayes) SerializationHelper.read(PropertiesManager.getProperty("mlModels") + "/naivebayes.main.model");
				out = bayes.distributionForInstance(instance);
				System.out.println("bayes: false " + out[0] + " : true " + out[1]);
				break;

			default:
				break;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return out;
	}

	public double[] evaluateForConceptSentence(String[] values, String algo) {

		double[] out = null;
		Instances dataUnlabeled = new Instances("TestInstances", conceptSentenceMl.getAtts(), 10);
		dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);
		DenseInstance instance = createConceptInstance(values, dataUnlabeled);

		try {
			switch (algo) {
			case "libsvm":
				LibSVM svm = (LibSVM) SerializationHelper.read(PropertiesManager.getProperty("mlModels") + "/libsvm.concepts.model");
				svm.setProbabilityEstimates(true);
				out = svm.distributionForInstance(instance);
				System.out.println("LibSVM: false " + out[0] + " : true " + out[1]);
				break;
			case "logistic":
				Logistic logistic = (Logistic) SerializationHelper.read(PropertiesManager.getProperty("mlModels") + "/logistic.concepts.model");
				out = logistic.distributionForInstance(instance);
				System.out.println("Logistic: false " + out[0] + " : true " + out[1]);
				break;
			case "j48":
				J48 smo = (J48) SerializationHelper.read(PropertiesManager.getProperty("mlModels") + "/j48.concepts.model");
				out = smo.distributionForInstance(instance);
				System.out.println("j48: false " + out[0] + " : true " + out[1]);
				break;
			case "bayes":
				NaiveBayes bayes = (NaiveBayes) SerializationHelper.read(PropertiesManager.getProperty("mlModels") + "/naivebayes.concepts.model");
				out = bayes.distributionForInstance(instance);
				System.out.println("bayes: false " + out[0] + " : true " + out[1]);
				break;


			default:
				break;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return out;
	}

	private DenseInstance createMainInstance(String[] values, Instances dataUnlabeled) {
		//qt,qr,qmr,mr,ar,cns,arl,mrl,pt,as,len,tt,sss,cs,nch,d,php,fph,lph
		DenseInstance instance = new DenseInstance(dataUnlabeled.numAttributes());
		instance.setDataset(dataUnlabeled);
		//System.out.println(Arrays.toString(values));
		for (int i = 1; i < values.length - 1; i++) {
			//System.out.println(i + ": set value for /" + dataUnlabeled.attribute(i - 1).name() + "/ with value " + values[i]);

			double value = -1;
			try {
				instance.setValue(i - 1, Double.parseDouble(values[i]));
			} catch (Exception e) {
				instance.setValue(i - 1, values[i]);
			}

		}
		return instance;

	}

	private DenseInstance createConceptInstance(String[] values, Instances dataUnlabeled) {
		//qt,qr,qmr,mr,ar,cns,arl,mrl,pt,as,len,tt,sss,cs,nch,d,php,fph,lph
		DenseInstance instance = new DenseInstance(dataUnlabeled.numAttributes());
		instance.setDataset(dataUnlabeled);
		//System.out.println(Arrays.toString(values));
		for (int i = 1; i < values.length - 1; i++) {
			//System.out.println(i + ": set value for /" + dataUnlabeled.attribute(i - 1).name() + "/ with value " + values[i]);

			double value = -1;
			try {
				instance.setValue(i - 1, Double.parseDouble(values[i]));
			} catch (Exception e) {
				instance.setValue(i - 1, values[i]);
			}

		}
		return instance;

	}

}

class mainSentenceMl {

	public static ArrayList<Attribute> getAtts() {
		List booleanValues = new ArrayList();
		booleanValues.add("false");
		booleanValues.add("true");

		ArrayList<Attribute> atts = new ArrayList<Attribute>();

		atts.add(new Attribute("qt")); //query's terms
		atts.add(new Attribute("qr")); //query's resources
		atts.add(new Attribute("qmr")); //Query's main resources
		atts.add(new Attribute("mr")); //main resources (top-K)
		atts.add(new Attribute("ar")); //annotated resources
		atts.add(new Attribute("cns")); //couple of neighbors (in the Graph)
		atts.add(new Attribute("arl")); //terms in the resources' labels
		atts.add(new Attribute("mrl")); //terms in the main resources' labels
		atts.add(new Attribute("pt")); //predicates' terms (from top predicates in each group)
		atts.add(new Attribute("as")); //Spotlight annotation score (AVG)
		atts.add(new Attribute("len")); //length
		atts.add(new Attribute("tt")); //terms shared with page's title
		atts.add(new Attribute("se", (FastVector) null));//se Imp: Sentence's end
		atts.add(new Attribute("ss", (FastVector) null));//ss Sentence's start: char, number, other
		atts.add(new Attribute("hl", booleanValues));//hl http links
		atts.add(new Attribute("sss")); //small sub-sentence (separated by , and length < 3)
		atts.add(new Attribute("cs")); //~ sum of resources' scores (1/rank) or AVG
		atts.add(new Attribute("nch"));//nch % of not a-z chars
		atts.add(new Attribute("d", booleanValues));//d has date in it
		atts.add(new Attribute("php"));//php Paragraph position (rank/all)
		atts.add(new Attribute("fph", booleanValues));//fph First Sentence 
		atts.add(new Attribute("lph", booleanValues));//lph Last Sentence 
		atts.add(new Attribute("selected", booleanValues)); //class

		return atts;
	}

	/*public static Instance buildNewInstance(double[] values) {
		Instances dataUnlabeled = new Instances("TestInstances", getAtts(), 1);
		dataUnlabeled.add(new DenseInstance(0.0, values));
		dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);
		return dataUnlabeled.firstInstance();
	}*/
}

class conceptSentenceMl {

	public static ArrayList<Attribute> getAtts() {
		List booleanValues = new ArrayList();
		booleanValues.add("false");
		booleanValues.add("true");

		ArrayList<Attribute> atts = new ArrayList<Attribute>();

		atts.add(new Attribute("tct")); //This resource label's terms
		atts.add(new Attribute("cb")); //resource's brothers (in the same group)
		atts.add(new Attribute("cn")); //resource's neighbors (graph)
		atts.add(new Attribute("cbl")); //terms in the resource's brothers labels
		atts.add(new Attribute("cnl")); //terms in the resource's neighbors labels
		atts.add(new Attribute("ca")); //shared terms with resource's abstract
		atts.add(new Attribute("cp")); //resource's position (%)
		atts.add(new Attribute("qt")); //terms of the query (with limitation: term.length>3)
		atts.add(new Attribute("qr")); //query's annotated resources
		atts.add(new Attribute("mr")); //main resources (top-k)
		atts.add(new Attribute("ar")); //annotated resources
		atts.add(new Attribute("arl")); //terms in the resources' labels
		atts.add(new Attribute("mrl"));//terms in the main resources' labels
		atts.add(new Attribute("pt"));//predicates' terms (from top predicates in each group)
		atts.add(new Attribute("cas"));//this resource Spotlight annotation score (AVG)
		atts.add(new Attribute("as")); //Spotlight annotation score (AVG)
		atts.add(new Attribute("len")); //length
		atts.add(new Attribute("tt"));//terms shared with page's title
		atts.add(new Attribute("se", (FastVector) null));//Imp: Sentence's end 
		atts.add(new Attribute("ss", (FastVector) null));//Sentence's start: char, number, other
		atts.add(new Attribute("hl", booleanValues));//http links
		atts.add(new Attribute("sss"));//small sub-sentence (separated by , and length < 3)
		atts.add(new Attribute("csc"));//~ resource' scores (1/rank)
		atts.add(new Attribute("cs"));//~ sum of resources' scores (1/rank) or AVG
		atts.add(new Attribute("nch"));//% of not a-z chars
		atts.add(new Attribute("d", booleanValues));//d has date in it
		atts.add(new Attribute("php"));//php Paragraph position (rank/all)
		atts.add(new Attribute("fph", booleanValues));//fph First Sentence 
		atts.add(new Attribute("lph", booleanValues));//lph Last Sentence 
		atts.add(new Attribute("selected", booleanValues)); //class

		return atts;
	}

	/*public static Instance buildNewInstance(double[] values) {
		Instances dataUnlabeled = new Instances("TestInstances", getAtts(), 1);
		dataUnlabeled.add(new DenseInstance(0.0, values));
		dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);
		return dataUnlabeled.firstInstance();
	}*/
}
