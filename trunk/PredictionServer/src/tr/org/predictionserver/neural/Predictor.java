package tr.org.predictionserver.neural;

public interface Predictor {
	public Prediction predict(double[] inputData);
}
