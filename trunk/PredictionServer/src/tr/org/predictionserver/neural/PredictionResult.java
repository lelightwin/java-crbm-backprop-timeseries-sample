package tr.org.predictionserver.neural;

import tr.org.predictionserver.neural.crbm.Matrix;

public class PredictionResult{
	public int timeWindowWidth=0;
	public int tryNumber;
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
		stringBuffer.append("dataSetNumber="+dataSetNumber+"\t");
		stringBuffer.append("tryNumber="+tryNumber+"\t");
		stringBuffer.append("dataSetLength="+dataSetLength+"\n");
		stringBuffer.append("input data :\n");
		for(int j=0;j<inputData.length;j++){
			stringBuffer.append(inputData[j]+"\t");
		}
		stringBuffer.append("\n");

		double mean=0,variance=0;
		double max=Double.MIN_VALUE,min=Double.MAX_VALUE,maxmindiff=0;
		for(int i=0;i<inputData.length;i++){
			mean+=inputData[i]/inputData.length;
			variance+=inputData[i]*inputData[i]/inputData.length;
			if(max<inputData[i])max=inputData[i];
			if(min>inputData[i])min=inputData[i];
		}
		maxmindiff=max-min;
		variance-=mean*mean;
		
		stringBuffer.append("Giriş Verileri İstatistiki Bilgileri :   Değişke = "+variance+"      Ortalama="+mean+"   Max="+max+"    Min="+min+"    Max-Min="+maxmindiff+"  \n");

		if(backPropPrediction!=null){
			stringBuffer.append("Back Prop Prediction :   NET ERROR="+backPropPrediction.networkError+", TEST ERROR="+backPropPrediction.testError+"  Time = "+(backPropPrediction.totoalTimeMillis/1000)+" seconds:\n");
		}
		if(crbmPrediction!=null){
			stringBuffer.append("CRBM Prediction :   NET ERROR="+crbmPrediction.networkError+", TEST ERROR="+crbmPrediction.testError+"  Time = "+(crbmPrediction.totoalTimeMillis/1000)+" seconds:\n");
		}
		if(backPropPrediction!=null){
			stringBuffer.append("Back Prop Predicted Values :\t");
			for(int j=0;j<backPropPrediction.predictedValues.length;j++){
				stringBuffer.append(backPropPrediction.predictedValues[j]+"\t\t");	
			}
			stringBuffer.append("\n");
		}
		if(crbmPrediction!=null){
			stringBuffer.append("CRBM Predicted Values :\t\t\t");
			for(int j=0;j<backPropPrediction.predictedValues.length;j++){
				stringBuffer.append(crbmPrediction.predictedValues[j]+"\t\t");	
			}
			stringBuffer.append("\n");
		}
		
		stringBuffer.append("Prediction Target :\t\t\t\t\t");
		for(int j=0;j<dataToBePredicted.length;j++){
			stringBuffer.append(dataToBePredicted[j]+"\t\t");	
		}
		stringBuffer.append("\n");
		return stringBuffer.toString();
	}

}