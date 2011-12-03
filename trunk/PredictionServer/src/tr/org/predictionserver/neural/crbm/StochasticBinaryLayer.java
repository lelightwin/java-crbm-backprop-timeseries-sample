package tr.org.predictionserver.neural.crbm;
import java.util.Random;
public class StochasticBinaryLayer  {
	private static final long serialVersionUID = -3492359725755848023L;
	
    public static double DEFAULT_BIAS_LEARNING_RATE = 0.05;
    public static double DEFAULT_BIAS_MOMENTUM = 0.5;
    public static double DEFAULT_BIAS_WEIGHTCOST = 0.00002 * DEFAULT_BIAS_LEARNING_RATE;
    public double[] biases;
    public double[] biasIncrement;
    public double learningRate;
    public double weightCost;
    public double momentum;
    public int numUnits;
    
	private Random rnd= new Random(1234L);

	public StochasticBinaryLayer(int numUnits) {
		this.numUnits=numUnits;
        this.biases = new double[numUnits];
        this.biasIncrement = new double[numUnits];
        this.momentum=DEFAULT_BIAS_MOMENTUM;
        this.learningRate=DEFAULT_BIAS_LEARNING_RATE;
        this.weightCost=DEFAULT_BIAS_WEIGHTCOST;
    }
    
    public double[][] generateData(double[][] activation_probabilities) {
    	// number of units == biases.length
        double[][] generatedData = new double[activation_probabilities.length][this.biases.length];
        for (int iData = 0; iData < generatedData.length; iData++) {
            for (int i = 0; i < this.biases.length; i++) {
                if (activation_probabilities[iData][i] > this.rnd.nextDouble())
                    generatedData[iData][i] = 1.;
                else
                    generatedData[iData][i] = 0.;
            }
        }
        return generatedData;
    }
    
    public double[][] getActivationProbabilities(double[][] sw_sum) {
    	// number of units == biases.length
        double[][] activation_probabilities = sw_sum;
        for (int ithDataVector = 0; ithDataVector < sw_sum.length; ithDataVector++) {  // for each data vector
            for (int j = 0; j < this.biases.length; j++) {
                activation_probabilities[ithDataVector][j] = 1.0 / (1 + Math.exp(-sw_sum[ithDataVector][j] - this.biases[j]));
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
