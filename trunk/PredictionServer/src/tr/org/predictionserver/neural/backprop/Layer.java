package tr.org.predictionserver.neural.backprop;

import java.util.ArrayList;
import java.util.HashMap;


public class Layer {
	private ArrayList<Unit> units=new ArrayList<Unit>();
    private Layer previousLayer=null;


	public Layer(Layer previousLayer,int numberOfUnits,boolean hasBiasUnit){
		this.previousLayer=previousLayer;
		for(int i=0;i<numberOfUnits;i++){
			boolean isBiasUnit=false;
			if(i==0 && hasBiasUnit) isBiasUnit=true;
			HashMap<Unit,Double> input=new HashMap<Unit,Double>();
			if(previousLayer!=null){
				input=new HashMap<Unit,Double>();
//             Weights will be rearranged by  Network.randomizeWeightsUsingNguyenWidrowMethod();
				for(int j=0;j<previousLayer.getUnits().size();j++){
					//May be new Random(1234L);
					input.put(previousLayer.getUnits().get(j), new Double(2*Math.random()-1));
				}
			}
			units.add(new Unit(input,isBiasUnit));
		}
	}
	
	public void propagate(){
		for(int i=0;i<units.size();i++){
			units.get(i).computeOutput();
		}
	}
	
	public void setOutputLayerError(double[] target){
		for(int i=0;i<units.size();i++){
			units.get(i).setOutputLayerError(target[i]);
		}
	}
	
	
	public void backPropagate(){// error is already set for this layer. and set the error values of the previousLayer while leaving the method.
		if(previousLayer==null)return;//no need to back-propagate for the input layer
		
		// 1. distribute errors to previous layer before adjusting weights, 
		//     because  error of each  unit's error in this layer will be added to the error of the previousLayer's unit by multiplying with the connection weight. 
		
		// 1.1 first reset the errors
		for(int i=1;i<previousLayer.getUnits().size();i++){
			previousLayer.getUnits().get(i).resetError();
		}
		
		// 1.2 distribute the errors by weights
		for(int i=1;i<units.size();i++){
			units.get(i).distributeErrorToPreviousLayer();
		}
		
		
		// 2. adjust weights
		for(int i=0;i<units.size();i++){
			units.get(i).adjustWeights();
		}
	}

	public ArrayList<Unit> getUnits() {
		return units;
	}

}
