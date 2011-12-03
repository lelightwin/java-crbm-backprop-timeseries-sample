package tr.org.predictionserver.neural.crbm;
public class LinearLayer  {
	private static final long serialVersionUID = 8904012071023195346L;
	
    public static double DEFAULT_BIAS_LEARNING_RATE = 0.05;
    public static double DEFAULT_BIAS_MOMENTUM = 0.5;
    public static double DEFAULT_BIAS_WEIGHTCOST = 0.00002 * DEFAULT_BIAS_LEARNING_RATE;
    public double[] biases;
    public double[] biasIncrement;
    public double learningRate;
    public double weightCost;
    public double momentum;
    public int numUnits;

	public LinearLayer(int numUnits) {
		this.numUnits=numUnits;
        this.biases = new double[numUnits];
        this.biasIncrement = new double[numUnits];
        this.momentum=DEFAULT_BIAS_MOMENTUM;
        this.learningRate=DEFAULT_BIAS_LEARNING_RATE;
        this.weightCost=DEFAULT_BIAS_WEIGHTCOST;
    }
    
	public double[][] getActivationProbabilities(double[][] productSum) {
        double[][] activation_probabilities = productSum;
        for (int ithDataVector = 0; ithDataVector < activation_probabilities.length; ithDataVector++) {  
        	// for each data vector
            for (int j = 0; j < this.biases.length; j++) {
                activation_probabilities[ithDataVector][j] += this.biases[j];
            }
        }
        return activation_probabilities;
    }
    
    public void updateBiases(double[][] positivePhaseData, double[][] negativePhaseData) {
        // Calculating how much to change the hidden-unit biases by
        double[] avgPositivePhaseData = Matrix.sumRows(Matrix.multiply(positivePhaseData, 1.0/positivePhaseData.length));
        double[] avgNegativePhaseData = Matrix.sumRows(Matrix.multiply(negativePhaseData, 1.0/negativePhaseData.length));
        
        for (int i = 0; i < this.biases.length; i++) {
            this.biasIncrement[i] = this.momentum * this.biasIncrement[i] +
                    this.learningRate * ((avgPositivePhaseData[i] - avgNegativePhaseData[i]) -
                    this.weightCost * this.biases[i]);
            
            // Updating the hidden-unit biases
            this.biases[i] += this.biasIncrement[i];
        }
    }
}