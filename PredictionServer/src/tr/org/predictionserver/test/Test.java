package tr.org.predictionserver.test;

import java.io.IOException;
import java.util.ArrayList;

import tr.org.predictionserver.neural.PredictionResult;
import tr.org.predictionserver.neural.backprop.BackPropPredictor;
import tr.org.predictionserver.neural.crbm.ConditionalRBMPredictor;
import tr.org.predictionserver.neural.crbm.Matrix;
import tr.org.predictionserver.reports.DetailReports;
import tr.org.predictionserver.reports.MainSummaryReport;
import tr.org.predictionserver.util.FileUtils;

public class Test {
				public static void main(String[] args){
					
					//      timeWindowWidth=5 ile test et. 
					//      timeWindowWidth=8 ile test et.
					
					
					// Summary               : TekrarNumarası+GirişVeriİstatistikleri+BackPropNetError+BackPropNetTestError+CRBMNetError+CRBMNetTestError
					// sonuc grafiklerinde : InputData-BackPropReporoducedData-CRBMReproducedData
					//                                  Altına TekrarNumarası+GirişVeriİstatistikleri+BackPropNetError+BackPropNetTestError+CRBMNetError+CRBMNetTestError Değerleri
					
					
					// neural networks (BackProp, RBM) will take 5 input and give 1 output.
					// We will make 5 successive predictions in each prediction cycle.

					ArrayList<PredictionResult> predictionResults=new ArrayList<PredictionResult>();
//					DataProvider.readFromDataFile("/home/memo/Download/veriler1.txt");
					
					for(int timeWindowParameter=0;timeWindowParameter<5;timeWindowParameter++){
						int timeWindowWidth=(timeWindowParameter+1)*2;
						System.out.println("\n\n\n\nTIME WINDOW WIDTH="+timeWindowWidth+"\n\n\n");
						DataProvider.prepareDataSets(timeWindowWidth);
						for(int dataSetNo=0;dataSetNo<DataProvider.getDataSets(timeWindowWidth).size(); dataSetNo++){
							for(int tryTheSameDataNumber=0;tryTheSameDataNumber<4;tryTheSameDataNumber++){
								BackPropPredictor backPropPredictor=new BackPropPredictor(timeWindowWidth);
								ConditionalRBMPredictor  conditionalRBMPredictor=new ConditionalRBMPredictor(timeWindowWidth);
								PredictionResult predictionResult=new PredictionResult();
								predictionResult.timeWindowWidth=timeWindowWidth;
								predictionResult.tryNumber=tryTheSameDataNumber;
								double[] sampleData=DataProvider.getDataSets(timeWindowWidth).get(dataSetNo);
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
						}
						
					}
					
					StringBuffer outputFileBuffer=new StringBuffer();


					for(int i=0;i<predictionResults.size();i++){
						outputFileBuffer.append(predictionResults.get(i).toString());
					}
	
					
					try {
						FileUtils.writeFile("/var/log/PredictionServer/server.log", outputFileBuffer.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					// Main Summary Report
					MainSummaryReport mainSummaryReport=new MainSummaryReport();
					mainSummaryReport.generateAndSaveHTMLTableReport(predictionResults);
					mainSummaryReport.generateAndSaveCharts(predictionResults);
					
					DetailReports detailReports=new DetailReports();
					detailReports.generateAndSaveHTMLTableReport(predictionResults);
					detailReports.generateAndSaveCharts(predictionResults);
					

					
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

