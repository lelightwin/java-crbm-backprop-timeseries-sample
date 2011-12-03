package tr.org.predictionserver.neural.backprop;

import java.util.HashMap;
import java.util.Iterator;

public class Unit {
	
	private double output;
	private double error;
	private boolean biasUnit; 
    private HashMap<Unit,Double> input=new HashMap<Unit,Double>(); //HashMap(Weight,Unit)
    private HashMap<Unit,Double> savedWeights=new HashMap<Unit,Double>(); //HashMap(Weight,Unit)
    private HashMap<Unit,Double> weightDeltas=new HashMap<Unit,Double>(); //HashMap(Weight,Unit)
    //if (inputUnits.size()==0) then this unit is an input layer (layer 0)  unit.
    
	public Unit(HashMap<Unit,Double> input,boolean biasUnit){
		this.input=input;
		this.biasUnit=biasUnit;
		
	}
	
	// SETTERS AND GETTERS
	
	public double getError() {
		return error;
	}
	public void setError(double error) {
		// This method is only used while setting the errors of the units in the output layer, first instruction of backpropagation.
		this.error = error;
	}

	public double getOutput() {
		if(biasUnit)return Network.BIAS;
		return output;
	}
	
	public void setOutput(double output) {
		// This method is only used while setting the initial inputs of the network, first instruction of propagation.
		this.output = output;
	}
	
	public void saveWeights(){
		Iterator<Unit> iterator=input.keySet().iterator();
		while(iterator.hasNext()){
			Unit inputUnit=iterator.next();
			Double weightAssociatedWithTheUnit=new Double(input.get(inputUnit));// new Double , cunku iput'taki degerler kazara degisirse savedWeight'tekilerin degismesini istemiyorum.
			savedWeights.put(inputUnit, weightAssociatedWithTheUnit);
		}
	}
	
	public void restoreWeights(){
		Iterator<Unit> iterator=savedWeights.keySet().iterator();
		while(iterator.hasNext()){
			Unit inputUnit=iterator.next();
			Double weightAssociatedWithTheUnit=savedWeights.get(inputUnit);
			input.put(inputUnit, weightAssociatedWithTheUnit);
		}
	}
	
	public void computeOutput(){
		double sum=0;
		Iterator<Unit> iterator=input.keySet().iterator();
		while(iterator.hasNext()){
			Unit inputUnit=iterator.next();
			Double weightAssociatedWithTheUnit=input.get(inputUnit);
			sum+=inputUnit.getOutput()*weightAssociatedWithTheUnit.doubleValue();
		}
		output= 1 / (1 + Math.exp(- Network.GAIN_OF_THE_SIGMOID_FUNCTION* sum));
	}
		
	public HashMap<Unit, Double> getInput() {
		return input;
	}

	public void setOutputLayerError(double target){
		error=target-output;
	}
	
	public void resetError(){
		error=0;
	}
	
	public void addErrorComingFromUpperLayer(double weightedError){
		error+=weightedError;
	}
	
	public void distributeErrorToPreviousLayer(){
		Iterator<Unit> iterator=input.keySet().iterator();
		while(iterator.hasNext()){
			Unit inputUnit=iterator.next();
			if(!inputUnit.isBiasUnit()){
				Double weightAssociatedWithTheUnit=input.get(inputUnit);
				inputUnit.addErrorComingFromUpperLayer(error*weightAssociatedWithTheUnit);
			}
		}
	}
	
	public boolean isBiasUnit(){
		return biasUnit;
	}
	
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	public void adjustWeights(){
		// this unit is the j.th unit. i is the index of the input.
		// Zj=sum ( wji * xji)
		// output_j = sigmaF(Zj)
		// E= 1/2 * sum (tatget_j - ouput_j)
		// dE/dWji = dE/dZj * dSum/dWji = dE/dZj * Xji
		//                = - (target_j - ouput_j) * dOuput_j/ dZj * Xji
		//                = - (target_j - ouput_j) * dOuput_j/ dZj * Xji
		//                = - error_j * dOuput_j/ dZj * Xji
		//                = - error_j * (1-output_j) * output_j * Xji
		// DeltaWji= - learningRate * dE/dWji  
		//                = - learningRate * - error_j * (1-output_j) * output_j * Xji  
		//                = learningRate * error_j * (1-output_j) * output_j * Xji  
		// Weight_j=  Weight_j + DeltaWji + Meomentum * DeltaWjiCalculatedOneCycleBefore 
		
		Iterator<Unit> iterator=input.keySet().iterator();
		while(iterator.hasNext()){
			Unit inputUnit=iterator.next();
//			if(!inputUnit.isBiasUnit()){
			double weightAssociatedWithTheUnit=input.get(inputUnit).doubleValue();
			
			double weightDelta= Network.DEFAULT_LEARNING_RATE * error * (1-output) * output * inputUnit.getOutput();
			
			double previousWeightDelta=0;
			
			if(weightDeltas.get(inputUnit)!=null) previousWeightDelta=weightDeltas.get(inputUnit).doubleValue();
			
//			double currentWeight=weightAssociatedWithTheUnit+ weightDelta+ Network.DEFAULT_MOMENTUM* previousWeightDelta;
			double currentWeight=weightAssociatedWithTheUnit+ weightDelta+ Network.DEFAULT_MOMENTUM* previousWeightDelta;
			
			input.put(inputUnit,currentWeight);
			
			weightDeltas.put(inputUnit, weightDelta);
			
//			}
		}
		
	}
		
}
