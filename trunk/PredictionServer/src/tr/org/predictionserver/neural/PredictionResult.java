package tr.org.predictionserver.neural;

import tr.org.predictionserver.neural.crbm.Matrix;

public class PredictionResult{
	public int dataSetNumber;
	public int dataSetLength;
	public double[] inputData;
	public Prediction crbmPrediction;
	public Prediction backPropPrediction;
	public double[] dataToBePredicted;
	
	//            System.out.println("Epoch: " + e + " Error " + Matrix.getMeanSquaredError(batchtarget, targetreconstruction));

	public String toString(){
		StringBuffer stringBuffer=new StringBuffer();
		stringBuffer.append("\n\n\n");
		stringBuffer.append("dataSetNumber="+dataSetNumber+"\n");
		stringBuffer.append("dataSetLength="+dataSetLength+"\n");
		stringBuffer.append("input data :\t");
		for(int j=0;j<inputData.length;j++){
			stringBuffer.append(inputData[j]+"\t\t");	
		}
		stringBuffer.append("\n");

		if(backPropPrediction!=null){
			stringBuffer.append("Back Prop Prediction : NET ERROR="+backPropPrediction.networkError+", TEST ERROR="+backPropPrediction.testError+"  Time = "+(backPropPrediction.totoalTimeMillis/1000)+" seconds:\n");
			for(int j=0;j<backPropPrediction.predictedValues.length;j++){
				stringBuffer.append(backPropPrediction.predictedValues[j]+"\t\t");	
			}
			stringBuffer.append("\n");
		}
		if(crbmPrediction!=null){
			stringBuffer.append("CRBM Prediction : NET ERROR="+crbmPrediction.networkError+", TEST ERROR="+crbmPrediction.testError+"  Time = "+(crbmPrediction.totoalTimeMillis/1000)+" seconds:\n");
			for(int j=0;j<backPropPrediction.predictedValues.length;j++){
				stringBuffer.append(crbmPrediction.predictedValues[j]+"\t\t");	
			}
			stringBuffer.append("\n");
		}
		
		stringBuffer.append("Prediction Target :\t");
		for(int j=0;j<dataToBePredicted.length;j++){
			stringBuffer.append(dataToBePredicted[j]+"\t\t");	
		}
		stringBuffer.append("\n");
		return stringBuffer.toString();
	}
}