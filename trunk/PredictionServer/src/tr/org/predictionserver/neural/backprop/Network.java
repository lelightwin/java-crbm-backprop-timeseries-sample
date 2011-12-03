package tr.org.predictionserver.neural.backprop;

import java.util.ArrayList;
import java.util.Iterator;




public class Network {
		public  static double DEFAULT_MOMENTUM=1.2;//Alpha 0.5
		public  static double DEFAULT_LEARNING_RATE=0.1;//Eta 00.5
		public  static double GAIN_OF_THE_SIGMOID_FUNCTION=0.7;//Gain=1 varsayilan degeri
		public  static double BIAS=1.2;//1
		ArrayList<Layer> layers=new ArrayList<Layer>();
		public double savedError=0;
		// layers[0]==inputlayer
		// layers[N]==outputlayer

		public Network(int[] numberOfUnitsInEachLayer){
			Layer previousLayer=null;
			for(int i=0;i<numberOfUnitsInEachLayer.length;i++){
				boolean layerHasBiasUnit=true;
				if(i==numberOfUnitsInEachLayer.length-1)layerHasBiasUnit=false;
				Layer newLayer=new Layer(previousLayer, numberOfUnitsInEachLayer[i],layerHasBiasUnit);
				layers.add(newLayer);
				previousLayer=newLayer;
			}
		}

		public void setInput(double[] input) {
			Layer inputLayer=layers.get(0);
			for(int i=1;i<inputLayer.getUnits().size();i++){//i==0 bias unit, onu atliyoruz.
				inputLayer.getUnits().get(i).setOutput(input[i-1]);
			}
		}
		
		
		public void saveWeights(){
			//ilk kusak giris kusagi oldugu icin 1'den basliyoruz. Cunku ilk seviyenin inputu = disaridan verilen output.
			for(int i=1;i<layers.size();i++){
				for(int j=1;j<layers.get(i).getUnits().size();j++){ //ilk unit bias unit, onu atliyoruz.
					layers.get(i).getUnits().get(j).saveWeights();
				}
			}
		}
		public void restoreWeights(){
			//ilk kusak giris kusagi oldugu icin 1'den basliyoruz. Cunku ilk seviyenin inputu = disaridan verilen output.
			for(int i=1;i<layers.size();i++){
				for(int j=1;j<layers.get(i).getUnits().size();j++){//ilk unit bias unit, onu atliyoruz.
					layers.get(i).getUnits().get(j).restoreWeights();
				}
			}
		}
		public void propagate(){
			for(int i=1;i<layers.size();i++){
				layers.get(i).propagate();
			}
		}
		
		public  double computeOutputError(double[] target){
			double totalNetworkError=0;
			Layer outputLayer=layers.get(layers.size()-1);// enson layer output layer'dir.
			for(int i=0;i<outputLayer.getUnits().size();i++){
				double outputOfTheUnit=outputLayer.getUnits().get(i).getOutput();
				double deviation=target[i]-outputOfTheUnit;
				totalNetworkError+=0.5*(deviation*deviation);
			}
			return totalNetworkError;
		}
		
		public void backPropagate(double[] target){
			Layer lastLayer=layers.get(layers.size()-1);
			lastLayer.setOutputLayerError(target);
			for(int i=(layers.size()-1);1<i;i--){
				layers.get(i).backPropagate();
			}
		}
		

		public double[] getOutput(){
			double[] returnValue=new double[layers.get(layers.size()-1).getUnits().size()];
			
			for(int i=0;i<returnValue.length;i++){
				returnValue[i]=layers.get(layers.size()-1).getUnits().get(i).getOutput();
			}
			
			return returnValue;

		}
		

		
}
