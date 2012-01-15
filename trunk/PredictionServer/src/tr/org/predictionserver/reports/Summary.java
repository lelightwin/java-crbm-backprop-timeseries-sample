package tr.org.predictionserver.reports;

public class Summary{
	public int timeWindowWidth;
	public int dataSetLength;
	public double  meanBackPropTestError;
	public double  meanBackPropNetError;
	public double  meanBackPropTimeElapsed;
	public double meanBoltzmannTestError;
	public double meanBoltzmannNetError;
	public double meanBoltzmannTimeElapsed;
	public double inputData[];
	
	public double getMeanOfInputData(){
		double mean=0;
		for(int i=0;i<inputData.length;i++){
			mean+=inputData[i]/inputData.length;
		}
		return mean;
	}
	public double getVarianceOfInputData(){
		double mean=0,variance=0;
		for(int i=0;i<inputData.length;i++){
			mean+=inputData[i]/inputData.length;
			variance+=inputData[i]*inputData[i]/inputData.length;
		}
		variance-=mean*mean;
		return variance;
	}
}
