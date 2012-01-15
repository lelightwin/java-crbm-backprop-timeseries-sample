package tr.org.predictionserver.reports;

import java.util.ArrayList;

import tr.org.predictionserver.neural.Prediction;

public class PredictionSummary {
	//PRIMARY KEYS
	public int timeWindowWidth;
	public int dataSetLength;

	// DATA
	public double[] dataToBePredicted;
	public ArrayList<Prediction> backpropPredictions=new ArrayList<Prediction>();
	public ArrayList<Prediction> boltzmannPredictions=new ArrayList<Prediction>();


	
}
