/************* License relating to this Java implementation **********
 * This is a Java implementation of a Restricted Boltzmann Machine.
 * The GPL license applies to this Java implementation only.
 *
 * Copyright (C) 2008 Benjamin Chung   [benchung AT NO $PAM gmail DOT com]
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 ********************************************************************* */

package tr.org.predictionserver.neural.crbm;

public class CDConditionalRBMLearner {

    private ConditionalRBM crbm;
    private double[][] prevVisibleWeightUpdates;
    private double[][] prevVisibleOutputWeightUpdates;
    
    public CDConditionalRBMLearner(ConditionalRBM conditionalRBM){
    	this.crbm=conditionalRBM;
            
        this.prevVisibleWeightUpdates = Matrix.clone(crbm.weights);
        Matrix.zero(this.prevVisibleWeightUpdates);
            
        this.prevVisibleOutputWeightUpdates = Matrix.clone(crbm.outputWeights);
        Matrix.zero(this.prevVisibleOutputWeightUpdates);
    }
    public ConditionalRBM getCRBM() {
        return this.crbm;
    }
    
    public double Learn(double[] visibleData, double[] visibleOutputData) {
        double[][] batchVisibleData = {visibleData};
        double[][] batchVisibleOutputData = {visibleOutputData};
        return Learn(batchVisibleData, batchVisibleOutputData);
    }
    public double Learn(double[][] visibleData, double[][] visibleOutputData) {
//    	System.out.println("\nVisible Input Data : ");
//    	for(int i=0;i<visibleData[0].length;i++){
//    		System.out.print(visibleData[0][i]+"-");
//    	}
//    	System.out.println("\nVisible Output Data : ");
//    	for(int i=0;i<visibleOutputData[0].length;i++){
//    		System.out.print(visibleOutputData[0][i]+"-");
//    	}
    	
        // positive phase, where the visible units' states are clamped to a particular binary state vector sampled from the training set 
        double[][] outputHiddenActivities = Matrix.multiply(visibleOutputData, crbm.outputWeights);
        double[][] visibleHiddenActivities = Matrix.multiply(visibleData, crbm.weights);
        double[][] hiddenActivities = crbm.hiddenLayer.getActivationProbabilities(Matrix.add(outputHiddenActivities, visibleHiddenActivities));
        double[][] hiddenData = crbm.hiddenLayer.generateData(hiddenActivities);
        
        // negative phase , where the network is allowed to run freely, i.e. no units have their state determined by external data
        double[][] negPhaseVisibleOutputActivities =crbm.visibleOutputLayer.getActivationProbabilities(Matrix.multiplyTranspose(hiddenData, crbm.outputWeights));
        double[][] visibleActivities = crbm.visibleLayer.getActivationProbabilities(Matrix.multiplyTranspose(hiddenData, crbm.weights));
        double[][] negativeOutputHiddenActivities = Matrix.multiply(negPhaseVisibleOutputActivities, crbm.outputWeights);
        double[][] negativeVisibleHiddenActivities = Matrix.multiply(visibleData, crbm.weights);
        double[][] negativePhaseHiddenActivities = crbm.hiddenLayer.getActivationProbabilities(Matrix.add(negativeOutputHiddenActivities, negativeVisibleHiddenActivities));
        
        // Update weights
        double[][] visibleOutputWeightUpdates = getConnectionWeightUpdates(crbm.outputWeights, visibleOutputData, hiddenActivities, negPhaseVisibleOutputActivities, negativePhaseHiddenActivities);
        double[][] visibleWeightUpdates = getConnectionWeightUpdates(crbm.weights,visibleData, hiddenActivities, visibleActivities, negativePhaseHiddenActivities);
        updateVisibleWeights(visibleOutputWeightUpdates,crbm.outputWeights,prevVisibleOutputWeightUpdates);
        prevVisibleOutputWeightUpdates = visibleOutputWeightUpdates;
        updateVisibleWeights(visibleWeightUpdates,crbm.weights,prevVisibleWeightUpdates) ;
        prevVisibleWeightUpdates = visibleWeightUpdates;
        crbm.hiddenLayer.updateBiases(hiddenData, negativePhaseHiddenActivities);
        crbm.visibleOutputLayer.updateBiases(visibleOutputData, negPhaseVisibleOutputActivities);
        
        //Calculate Error
        outputHiddenActivities = Matrix.multiply(visibleOutputData, crbm.outputWeights);
        visibleHiddenActivities = Matrix.multiply(visibleData, crbm.weights);
        hiddenActivities = crbm.hiddenLayer.getActivationProbabilities(Matrix.add(outputHiddenActivities, visibleHiddenActivities));
        visibleActivities =crbm.visibleLayer.getActivationProbabilities(Matrix.multiplyTranspose(hiddenData, crbm.weights));
        
        double error=0;
        for(int i=0;i<visibleOutputData.length;i++){
             error+=Matrix.getSquaredError(convertPredictedBinaryToReal(visibleOutputData[i],visibleOutputData.length), convertPredictedBinaryToReal(convertActivationToBinaryVector(visibleActivities[i]),visibleOutputData.length));
        }
        return Math.sqrt(error);
    }
    
    public static double[][] getConnectionWeightUpdates(double[][] connectionWeights, 
                                                        double[][] data, double[][] hiddenActivities, 
                                                        double[][] generatedData, double[][] negativePhaseHiddenActivities) {
                                                        
        final int numVisibleUnits = connectionWeights.length;
        final int numHiddenUnits = connectionWeights[0].length;
        double[][] weightUpdates = new double[numVisibleUnits][numHiddenUnits];

        final double[][] positivePhaseProduct = Matrix.transposeMultiply(data, hiddenActivities);
        final double[][] negativePhaseProduct = Matrix.transposeMultiply(generatedData, negativePhaseHiddenActivities);
        
        for (int v = 0; v < numVisibleUnits; v++) {
            for (int h = 0; h < numHiddenUnits; h++) {
                weightUpdates[v][h] = (positivePhaseProduct[v][h] - negativePhaseProduct[v][h]) / data.length;
            }
        }
        return weightUpdates;
    }

    public void updateVisibleWeights(double[][] weightUpdates,double[][] weights,double[][] prevWeightUpdates) {
        final double momentum = this.crbm.momentum;
        final double learningRate = this.crbm.learningRate;
        final double weightCost = this.crbm.weightCost;
        for (int v = 0; v < weightUpdates.length; v++) {
            for (int h = 0; h < weightUpdates[v].length; h++) {
                weightUpdates[v][h] = momentum * prevWeightUpdates[v][h] +
                                      learningRate * weightUpdates[v][h] - 
                                      weightCost * weights[v][h];
                weights[v][h] += weightUpdates[v][h];
            }
        }
    }
    
    

	
	private double[] convertPredictedBinaryToReal(double[] binaryMatrix,int outputUnitsSize){
		double[] returnValue=new double[outputUnitsSize];
		
		for(int i=0;i<returnValue.length;i++){
			double realValue=0;
			for(int j=0;j<ConditionalRBMPredictor.DIGIT_SIZE;j++){
				if(binaryMatrix[i*ConditionalRBMPredictor.DIGIT_SIZE+j]==1)realValue+=Math.pow(2, j);
			}
			returnValue[i]=realValue*ConditionalRBMPredictor.DOUBLE_TO_BINARY_PRESENTATION_INTERVAL;
		}
		return returnValue;
	}



	private double[] convertActivationToBinaryVector(double[] activations){
		double[] binaryVector=new double[activations.length];
		double sum=0;
		double max=Double.MIN_VALUE;
		double min=Double.MAX_VALUE;
		double mean=0;
		for(int i=0;i<activations.length;i++){
			sum+=activations[i];
			if(activations[i]<min)min=activations[i];
			if(activations[i]>max)max=activations[i];
		}
		//mean=sum/activations.length;
		//mean=((max+min)/2)*(sum/activations.length);
		mean=(max+min)/2;
		for(int i=0;i<activations.length;i++){
				if(activations[i]>mean){
					binaryVector[i]=1;
				}
		}
		return binaryVector;
	}
}