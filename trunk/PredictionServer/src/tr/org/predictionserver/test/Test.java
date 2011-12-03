package tr.org.predictionserver.test;

import java.io.IOException;
import java.util.ArrayList;

import tr.org.predictionserver.neural.PredictionResult;
import tr.org.predictionserver.neural.backprop.BackPropPredictor;
import tr.org.predictionserver.neural.crbm.ConditionalRBMPredictor;
import tr.org.predictionserver.neural.crbm.Matrix;
import tr.org.predictionserver.util.FileUtils;

public class Test {
				public static void main(String[] args){
					// neural networks (BackProp, RBM) will take 5 input and give 1 output.
					// We will make 5 successive predictions in each prediction cycle.
					
					int neuralNetworkInputUnitsSize=5;
					int neuralNetworkOutputUnitsSize=5;
					int timeWindowWidth=neuralNetworkInputUnitsSize;
					
					ArrayList<PredictionResult> predictionResults=new ArrayList<PredictionResult>();
					
					for(int dataSetNo=0;dataSetNo<DataProvider.getDataSets().size(); dataSetNo++){
						// 70 tane veri olsun, bunda enson 54'e  kadar gideriz cunku
						//                 54'te  5 tanesi input ve 1 tanesi test icin etti 60.
						//                 60'ta : 5 input veriyoruz 60-65 arası.
						//                              son 5 gerçek karşılaştırma deferi
					    //                              5 tane de neural net predict eder, bu son ikisinin farkını error olarak predictionresult'a yazılır. 
						// 70 tane veri= 1 kaynaktan 70 ölçüm yapıyoruz,
						// t=0 0-5 arasını neural network'e veriyoruz, , t=2'de 1-6 arasını eğitiyoruz.
						
						// son 5 tanenin tahmini su sekilde olacak : 
						// 66. tahmin edilir, 
						//			          time window 1 kaydırılır, bilinen input 5'li sinin son degeri 66. tahmin edilen noktadır, 67. tahmin edilir, 
						//			          time window 1 kaydırılır, bilinen input 5'li sinin sondan bir onceki degeri 666. tahmin , son degeri 67. tahmin edilen noktadır, 68. tahmin edilir
						// ....., yani ilk tahnminden sonraki tahminlere , daha önceki tahminler input data olur..

						BackPropPredictor backPropPredictor=new BackPropPredictor(neuralNetworkInputUnitsSize,neuralNetworkOutputUnitsSize,timeWindowWidth);
						ConditionalRBMPredictor  conditionalRBMPredictor=new ConditionalRBMPredictor(neuralNetworkInputUnitsSize,neuralNetworkOutputUnitsSize,timeWindowWidth);
						PredictionResult predictionResult=new PredictionResult();
						double[] sampleData=DataProvider.getDataSets().get(dataSetNo);
						predictionResult.dataSetLength=sampleData.length;
						predictionResult.inputData=new double[sampleData.length-timeWindowWidth];
						 System.arraycopy(sampleData, 0, predictionResult.inputData, 0, sampleData.length-timeWindowWidth);
						 predictionResult.dataToBePredicted=new double[timeWindowWidth];
					     System.arraycopy(predictionResult.inputData, predictionResult.inputData.length-timeWindowWidth, predictionResult.dataToBePredicted, 0, timeWindowWidth);
					     predictionResult.dataSetNumber=dataSetNo;
					  // inputData'nın son 5 tanesi sanki hic yokmus gibi  CRBM ve BackProp tarafından tahmin edilecektir.
					     // daha sonra da tahmin degerleri asagidaki  gercek son 5 degerle karsilastirilmak uzere PredictionResult'in icine konacaktir.
					     predictionResult.crbmPrediction=conditionalRBMPredictor.predict( predictionResult.inputData);
					     predictionResult.crbmPrediction.testError=calculateError(predictionResult.crbmPrediction.predictedValues, predictionResult.dataToBePredicted);
					     predictionResult.backPropPrediction=backPropPredictor.predict( predictionResult.inputData);
					     predictionResult.backPropPrediction.testError=calculateError(predictionResult.backPropPrediction.predictedValues, predictionResult.dataToBePredicted);
					     predictionResults.add(predictionResult);
					     System.out.println(predictionResult.toString());
					}
					StringBuffer outputFileBuffer=new StringBuffer();
					for(int i=0;i<predictionResults.size();i++){
						outputFileBuffer.append(predictionResults.get(i).toString());
					}
					
					try {
						FileUtils.writeFile("/var/log/PredictionServer.log", outputFileBuffer.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					
					// graflari ciz.
					
					Matrix.es.shutdown();
				}
				
				private static double calculateError(double[] outputData,double[] targetData){
					double error=0;
					for(int i=0;i<outputData.length;i++){
						error+=(outputData[i]-targetData[i])*(outputData[i]-targetData[i]);
					}
					return Math.sqrt(error);
				}

}

