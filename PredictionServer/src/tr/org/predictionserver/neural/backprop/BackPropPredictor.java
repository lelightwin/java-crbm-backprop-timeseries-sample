package tr.org.predictionserver.neural.backprop;


import tr.org.predictionserver.neural.Prediction;
import tr.org.predictionserver.neural.Predictor;

public class BackPropPredictor implements Predictor{
	private final static int EPOCHS=400;
	int inputUnitsSize;
	int outputUnitsSize;
	int timeWindowWidth;
	Network network=null;
	
	public BackPropPredictor(int inputUnitsSize,int outputUnitsSize,int timeWindowWidth){
		this.inputUnitsSize=inputUnitsSize;
		this.outputUnitsSize=outputUnitsSize;
		this.timeWindowWidth=timeWindowWidth;
		int[] numberOfUnitsInEachLayer=new int[]{inputUnitsSize+1,7*inputUnitsSize+1,outputUnitsSize};
		this.network=new Network(numberOfUnitsInEachLayer);
	}
	
	public Prediction predict(double[] inputData){
		Prediction returnValue=new Prediction();
		long startTime=System.currentTimeMillis();
		double minimumNetError=Double.MAX_VALUE;
		for(int i=0;i<EPOCHS;i++){
			for(int j=0;j<inputData.length-timeWindowWidth-outputUnitsSize;j++){
				double[] timeWindow=new double[timeWindowWidth];
				System.arraycopy(inputData, j, timeWindow, 0, timeWindowWidth);
				double[] targetWindow=new double[outputUnitsSize];
				System.arraycopy(inputData, j+timeWindowWidth, targetWindow, 0, outputUnitsSize);
				for(int k=0;k<10;k++){
					network.setInput(timeWindow);
					network.propagate();
					network.backPropagate(targetWindow);
					returnValue.networkError=network.computeOutputError(targetWindow);
					if(minimumNetError>returnValue.networkError){
						minimumNetError=returnValue.networkError;
						network.savedError=returnValue.networkError;
						network.saveWeights();
					}
				}
			}
		}
		
		double[] lastTimeWindow=new double[timeWindowWidth];
		System.arraycopy(inputData, inputData.length-timeWindowWidth, lastTimeWindow, 0, timeWindowWidth);
		network.restoreWeights();
		returnValue.networkError=network.savedError;
		network.setInput(lastTimeWindow);
		network.propagate();
		returnValue.networkError=network.computeOutputError(lastTimeWindow);
		returnValue.predictedValues=network.getOutput();
		returnValue.totoalTimeMillis=System.currentTimeMillis()-startTime;
		return returnValue;
	}

}