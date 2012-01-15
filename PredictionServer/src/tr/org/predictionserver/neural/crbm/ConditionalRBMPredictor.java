package tr.org.predictionserver.neural.crbm;

import tr.org.predictionserver.neural.Prediction;
import tr.org.predictionserver.neural.Predictor;



public class ConditionalRBMPredictor  implements Predictor{
	private final static int EPOCHS=10;
	public final static int DIGIT_SIZE=31;
	public final static double DOUBLE_TO_BINARY_PRESENTATION_INTERVAL=1/Math.pow(2, DIGIT_SIZE);


	int hiddenSize=0;
	int timeWindowWidth;
	ConditionalRBM crbm =null;
	CDConditionalRBMLearner crbmLearner = null;
	
	public ConditionalRBMPredictor(int timeWindowWidth){

		this.hiddenSize=2*timeWindowWidth*DIGIT_SIZE;
		this.timeWindowWidth=timeWindowWidth;
    	LinearLayer visibleLayer = new LinearLayer(timeWindowWidth*DIGIT_SIZE);
    	LinearLayer visibleOutputLayer = new LinearLayer(timeWindowWidth*DIGIT_SIZE);
    	StochasticBinaryLayer hiddenLayer = new StochasticBinaryLayer(hiddenSize);
        crbm = new ConditionalRBM(visibleLayer, visibleOutputLayer, hiddenLayer);
        crbm.learningRate=0.05;
        crbm.momentum=0.4;
        crbmLearner=new CDConditionalRBMLearner(crbm);
	}

	public Prediction predict(double[] inputData){
		Prediction returnValue=new Prediction();
		long startTime=System.currentTimeMillis();
		
		for(int i=0;i<EPOCHS;i++){
			System.out.println("Epoch:"+i);
			for(int j=0;j<inputData.length-timeWindowWidth-timeWindowWidth+1;j++){
				double[] timeWindow=new double[timeWindowWidth];
				System.arraycopy(inputData, j, timeWindow, 0, timeWindowWidth);
				double[] targetWindow=new double[timeWindowWidth];
				System.arraycopy(inputData, j+timeWindowWidth, targetWindow, 0, timeWindowWidth);
				double minError=Double.MAX_VALUE;
				for(int k=0;k<10;k++){
					returnValue.networkError=crbmLearner.Learn(convertRealVectorToBinaryVector(timeWindow), convertRealVectorToBinaryVector(targetWindow));
					if(minError>returnValue.networkError){
						minError=returnValue.networkError;
						crbm.saveWeights();
					}
				}
				crbm.restoreWeights();
				returnValue.networkError=minError;
			}
		}
		

		double[][] seed=new double[timeWindowWidth][timeWindowWidth];
		double[][] batchData=new double[timeWindowWidth][timeWindowWidth];

//		System.out.println("BATCHDATA:");
		for(int i=0;i<timeWindowWidth;i++){
		//	System.out.println(inputData.length+"   "+(inputData.length-timeWindowWidth-2*timeWindowWidth+i+1)+"  "+timeWindowWidth);
			System.arraycopy(inputData, inputData.length-timeWindowWidth-2*timeWindowWidth+i+1,batchData[i], 0, timeWindowWidth);
//			for(int t=0;t<timeWindowWidth;t++)System.out.print(batchData[i][t]+"-");	System.out.println("");
		}
//		System.out.println("SEEDS:");
		for(int i=0;i<timeWindowWidth;i++){
			System.arraycopy(inputData, inputData.length-timeWindowWidth-timeWindowWidth+i+1,seed[i], 0, timeWindowWidth);
//			for(int t=0;t<timeWindowWidth;t++)System.out.print(seed[i][t]+"-");System.out.println("");
		}

		
		
		for(int time=0;time<timeWindowWidth;time++){

			double[][] binaryBatchData=new double[timeWindowWidth][timeWindowWidth*DIGIT_SIZE];
			for(int i=0;i<timeWindowWidth;i++){
				binaryBatchData[i]=convertRealVectorToBinaryVector(batchData[i]);
			}
			double[][] binarySeed=new double[timeWindowWidth][timeWindowWidth*DIGIT_SIZE]; 
			for(int i=0;i<timeWindowWidth;i++){
				binarySeed[i]=convertRealVectorToBinaryVector(seed[i]);
			}

	        double[][] outputHiddenActivities = Matrix.multiply(binarySeed, crbm.outputWeights);
	        double[][] visibleHiddenActivities = Matrix.multiply(binaryBatchData, crbm.weights);
	        double[][] hiddenActivities = crbm.hiddenLayer.getActivationProbabilities(Matrix.add(outputHiddenActivities, visibleHiddenActivities));
	        
	        hiddenActivities = crbm.hiddenLayer.generateData(hiddenActivities);
	        double[][] targetreconstruction = crbm.visibleOutputLayer.getActivationProbabilities(Matrix.multiplyTranspose(hiddenActivities, crbm.outputWeights));
	        
	        for (int gibbs = 0; gibbs <2; gibbs++) {
	            outputHiddenActivities = Matrix.multiply(targetreconstruction, crbm.outputWeights);
	            visibleHiddenActivities = Matrix.multiply(binaryBatchData, crbm.weights);
	            hiddenActivities = crbm.hiddenLayer.getActivationProbabilities(Matrix.add(outputHiddenActivities, visibleHiddenActivities));
	            hiddenActivities = crbm.hiddenLayer.generateData(hiddenActivities);
	            targetreconstruction = crbm.visibleOutputLayer.getActivationProbabilities(Matrix.multiplyTranspose(hiddenActivities, crbm.outputWeights));
	        }
	        
	        
	        returnValue.predictedValues=convertPredictedBinaryToReal(convertActivationToBinaryVector(targetreconstruction[targetreconstruction.length-1]));
	        
			double[][] seedNew=new double[timeWindowWidth][timeWindowWidth];
			double[][] batchDataNew=new double[timeWindowWidth][timeWindowWidth];
			

			for(int i=0;i<timeWindowWidth-1;i++){
				batchDataNew[i]=batchData[i+1];
			}
			for(int i=0;i<timeWindowWidth;i++){
				batchDataNew[batchDataNew.length-1][i]=seed[0][i];
			}

			for(int i=0;i<timeWindowWidth-1;i++){
				seedNew[i]=seed[i+1];
			}
			
			// predicted valuelari seed'de bir asagi aliyoruz (timewindow bir kaydirirken son predicted valuelari , ki bunlarin (timeWindowWidth-time-1)'den sonrakileri predicted, gerikeleni eski veridir.
			for(int i=0;i<timeWindowWidth-1;i++){
				seedNew[seed.length-1][i]= seed[seed.length-1][i+1];
			}
			double predictedValue=returnValue.predictedValues[0];
			seedNew[seed.length-1][timeWindowWidth-1]= predictedValue;


			
			batchData=batchDataNew;
			seed=seedNew;
	        
	        
//			System.out.println("NEW BATCHDATA:");
//			for(int i=0;i<timeWindowWidth;i++){
////				for(int t=0;t<timeWindowWidth;t++)System.out.println(" inputDataIndex="+(inputData.length-inputUnitsSize-2*timeWindowWidth+i+1)+"   batchData["+i+"]["+t+"]="+batchData[i][t]);
//				for(int t=0;t<timeWindowWidth;t++)System.out.print(batchData[i][t]+"-");
//				System.out.println("");
//			}
//			System.out.println("NEW SEEDS:");
//			for(int i=0;i<timeWindowWidth;i++){
////				for(int t=0;t<timeWindowWidth;t++)System.out.println(" inputDataIndex="+(inputData.length-timeWindowWidth-inputUnitsSize+i+2)+"   seed["+i+"]["+t+"]="+seed[i][t]);
//				for(int t=0;t<timeWindowWidth;t++)System.out.print(seed[i][t]+"-");
//				System.out.println("");
//			}
//			System.out.println("Predicted Values");
//			for(int t=0;t<timeWindowWidth;t++)System.out.print(returnValue.predictedValues[t]+"-");
//			System.out.println("");
//			System.out.println("Predicted Value ="+predictedValue);
		}
		
        returnValue.totoalTimeMillis=System.currentTimeMillis()-startTime;
		
		return returnValue;
	}
	
	private double[] convertRealVectorToBinaryVector(double[] realVector){
		double[] binaryVector=new double[realVector.length*DIGIT_SIZE];
		for(int i=0;i<realVector.length;i++){
			double[] binaryRepresentaionOfDoubleValue=convertRealToBinary(realVector[i]);
			for(int j=0;j<binaryRepresentaionOfDoubleValue.length;j++){
				binaryVector[i*DIGIT_SIZE+j]=binaryRepresentaionOfDoubleValue[j];
			}
		}
		return binaryVector;
	}
	
	private double[] convertRealToBinary(double value){
		double[] returnValue=new double[DIGIT_SIZE];
		double realValue=value/DOUBLE_TO_BINARY_PRESENTATION_INTERVAL;
		for(int i=returnValue.length-1;i>=0;i--){
			if(realValue-Math.pow(2, i)>0){
				returnValue[i]=1;
				realValue-=Math.pow(2, i);
			}
		}
		return returnValue;
	}
	

	
	private double[] convertPredictedBinaryToReal(int[] binaryMatrix){
		double[] returnValue=new double[timeWindowWidth];
		
		for(int i=0;i<returnValue.length;i++){
			double realValue=0;
			for(int j=0;j<DIGIT_SIZE;j++){
				if(binaryMatrix[i*DIGIT_SIZE+j]==1)realValue+=Math.pow(2, j);
			}
			returnValue[i]=realValue*DOUBLE_TO_BINARY_PRESENTATION_INTERVAL;
		}
		return returnValue;
	}



	private int[] convertActivationToBinaryVector(double[] activations){
		int[] binaryVector=new int[activations.length];
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
