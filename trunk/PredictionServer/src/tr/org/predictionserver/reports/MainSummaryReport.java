package tr.org.predictionserver.reports;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

import tr.org.predictionserver.neural.PredictionResult;
import tr.org.predictionserver.util.FileUtils;

public class MainSummaryReport {

	
	public HashMap<Integer,XYSeriesCollection> generateDataSets(ArrayList<Summary> summaries) {
		HashMap<Integer,XYSeriesCollection> result=new HashMap<Integer,XYSeriesCollection>();
        int timeWindowWidth=summaries.get(0).timeWindowWidth;
        XYSeriesCollection dataset = new XYSeriesCollection();
        result.put(timeWindowWidth,dataset);
	    XYSeries s1 = new XYSeries("İleri Yönelimli YSA Test Hata");
	    XYSeries s2 = new XYSeries("İleri Yönelimli YSA Net Hata");
	    XYSeries s3 = new XYSeries("Boltzmann Makinesi Test Hata");
	    XYSeries s4 = new XYSeries("Boltzmann Makinesi Net Hata");
	    XYSeries s5 = new XYSeries("Girdi Değerleri Ortalaması");
	    XYSeries s6 = new XYSeries("Girdi Değerleri Değişkesi");
	     dataset.addSeries(s1);
	     dataset.addSeries(s2);
	     dataset.addSeries(s3);
	     dataset.addSeries(s4);
	     dataset.addSeries(s5);
	     dataset.addSeries(s6);
	     
	     
		for(int i=0;i<summaries.size();i++){
			if(timeWindowWidth!=summaries.get(i).timeWindowWidth){
			      timeWindowWidth=summaries.get(i).timeWindowWidth;
			      dataset = new XYSeriesCollection();
			      result.put(timeWindowWidth,dataset);
				  s1 = new XYSeries("İleri Yönelimli YSA Test Hata");
				  s2 = new XYSeries("İleri Yönelimli YSA Net Hata");
				  s3 = new XYSeries("Boltzmann Makinesi Test Hata");
				  s4 = new XYSeries("Boltzmann Makinesi Net Hata");
				  s5 = new XYSeries("Girdi Değerleri Ortalaması");
				  s6 = new XYSeries("Girdi Değerleri Değişkesi");
			     dataset.addSeries(s1);
			     dataset.addSeries(s2);
			     dataset.addSeries(s3);
			     dataset.addSeries(s4);
			     dataset.addSeries(s5);
			     dataset.addSeries(s6);
			}

			s1.add(new Double(summaries.get(i).dataSetLength).doubleValue(), summaries.get(i).meanBackPropTestError);
			s2.add(new Double(summaries.get(i).dataSetLength).doubleValue(), summaries.get(i).meanBackPropNetError);
			s3.add(new Double(summaries.get(i).dataSetLength).doubleValue(), summaries.get(i).meanBoltzmannTestError);
			s4.add(new Double(summaries.get(i).dataSetLength).doubleValue(), summaries.get(i).meanBoltzmannNetError);
			s5.add(new Double(summaries.get(i).dataSetLength).doubleValue(), summaries.get(i).getMeanOfInputData());
			s6.add(new Double(summaries.get(i).dataSetLength).doubleValue(), summaries.get(i).getVarianceOfInputData());

		}

		return result;
	}


	public void generateAndSaveCharts(	ArrayList<PredictionResult> predictionResults) {
		HashMap<Integer,XYSeriesCollection>  dataSets=generateDataSets(generateSummaryReportData(predictionResults));
		 Iterator<Integer> iterator=dataSets.keySet().iterator();
		 while(iterator.hasNext()){
			int timeWindowWidth=iterator.next();
			XYSeriesCollection dataset =dataSets.get(timeWindowWidth);

			  JFreeChart chart = ChartFactory.createXYLineChart("Zaman Aralığı ="+timeWindowWidth,"Veri Kümesi Büyüklüğü", "Ortalama Hata Değerleri", dataset, PlotOrientation.VERTICAL, true,true,true);
		      chart.setBackgroundPaint(Color.white);

		      XYPlot plot = (XYPlot) chart.getPlot();
		      plot.setBackgroundPaint(Color.lightGray);
		      plot.setDomainGridlinePaint(Color.white);
		      plot.setRangeGridlinePaint(Color.white);
		      plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		      plot.setDomainCrosshairVisible(true);
		      plot.setRangeCrosshairVisible(true);

		      XYItemRenderer r = plot.getRenderer();
		      if (r instanceof XYLineAndShapeRenderer) {
	            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
	            renderer.setBaseShapesVisible(true);
	            renderer.setBaseShapesFilled(true);
	            renderer.setDrawSeriesLineAsPath(true);
		      }
			  //viewChart(timeWindowWidth,chart);
			  saveChart(timeWindowWidth,chart);
		 }
	}

	public synchronized void saveChart(int timeWindowWidth,JFreeChart chart){
		try {
			ChartUtilities.saveChartAsJPEG(new File("/var/log/PredictionServer/summary."+timeWindowWidth+".jpeg"), chart, 550, 550);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public synchronized void viewChart(int timeWindowWidth,JFreeChart chart){
	      ApplicationFrame applicationFrame=new ApplicationFrame("Zaman Aralığı ="+ timeWindowWidth+" için oluşturulan grafik ");
	        ChartPanel panel = new ChartPanel(chart);
	        panel.setFillZoomRectangle(true);
	        panel.setMouseWheelEnabled(true);
	        panel.setPreferredSize(new java.awt.Dimension(550, 350));
	        applicationFrame.setContentPane(panel);
	        applicationFrame.pack();
	        RefineryUtilities.centerFrameOnScreen( applicationFrame);
	        applicationFrame.setVisible(true);
	}
	

	public StringBuffer generateAndSaveHTMLTableReport(
		ArrayList<PredictionResult> predictionResults) {
		StringBuffer genratedHTMLTable=new StringBuffer();
		genratedHTMLTable.append("<html><head></head><body>Açıklama: Her  Zaman Aralığı ve her veri kümesi için, ileri yönelimli YSA ve boltzmann makinesi ile birden fazla tahmin yapılır. Aşağıdaki tabloda bu tahminlerin hata değerlerinin ve işlem zamanlarının ortalaması  yazılmaktadır.<br/><table>");
		StringBuffer stringBuffer=new StringBuffer();
		stringBuffer.append("<tr>\n");
		stringBuffer.append("<td>Zaman Aralığı</td>\n");
		stringBuffer.append("<td>Veri Kümesi Büyüklüğü</td>\n");
		stringBuffer.append("<td>Veri Kümesi Değerleri Ortalaması </td>\n");
		stringBuffer.append("<td>Veri Kümesi Değerleri Varyansı</td>\n");
		stringBuffer.append("<td>İleri Yönelimli YSA Ortalama Test Hata Değeri</td>\n");
		stringBuffer.append("<td>İleri Yönelimli YSA Ortalama Öğrenme Hata Değeri</td>\n");
		stringBuffer.append("<td>İleri Yönelimli YSA Ortalama İşlem Zamanı</td>\n");
		stringBuffer.append("<td>Boltzmann Makinesi Ortalama Test Hata Değeri</td>\n");
		stringBuffer.append("<td>Boltzmann Makinesi Ortalama Öğrenme Hata Değeri</td>\n");
		stringBuffer.append("<td>Boltzmann Makinesi Ortalama İşlem Zamanı</td>\n");
		stringBuffer.append("</tr>\n");
		genratedHTMLTable.append(stringBuffer);
        ArrayList<Summary> summaries=generateSummaryReportData(predictionResults);
		for(int i=0;i<summaries.size();i++){
				stringBuffer=new StringBuffer();
				stringBuffer.append("<tr>\n");
				stringBuffer.append("<td>"+summaries.get(i).timeWindowWidth+"</td>\n");
				stringBuffer.append("<td>"+summaries.get(i).dataSetLength+"</td>\n");
				stringBuffer.append("<td>"+summaries.get(i).getMeanOfInputData()+"</td>\n");
				stringBuffer.append("<td>"+summaries.get(i).getVarianceOfInputData()+"</td>\n");
				stringBuffer.append("<td>"+summaries.get(i).meanBackPropTestError+"</td>\n");
				stringBuffer.append("<td>"+summaries.get(i).meanBackPropNetError+"</td>\n");
				stringBuffer.append("<td>"+summaries.get(i).meanBackPropTimeElapsed+"</td>\n");
				stringBuffer.append("<td>"+summaries.get(i).meanBoltzmannTestError+"</td>\n");
				stringBuffer.append("<td>"+summaries.get(i).meanBoltzmannNetError+"</td>\n");
				stringBuffer.append("<td>"+summaries.get(i).meanBoltzmannTimeElapsed+"</td>\n");
				stringBuffer.append("</tr>\n");
				genratedHTMLTable.append(stringBuffer);
		}
		genratedHTMLTable.append("</table></body></html>");
		try {
			FileUtils.writeFile("/var/log/PredictionServer/mainSummaryReport.html", genratedHTMLTable.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return genratedHTMLTable;
		
	}


	public ArrayList<Summary> generateSummaryReportData(ArrayList<PredictionResult> predictionResults){
		ArrayList<Summary> result =new ArrayList<Summary>();
		int 		tryCount=1;
		Summary summary=new Summary();
		summary.timeWindowWidth=predictionResults.get(0).timeWindowWidth;
		summary.inputData=predictionResults.get(0).inputData;
		summary.dataSetLength=predictionResults.get(0).dataSetLength;
		summary.meanBackPropTestError=predictionResults.get(0).backPropPrediction.testError;
		summary.meanBackPropNetError=predictionResults.get(0).backPropPrediction.networkError;
		summary.meanBackPropTimeElapsed=predictionResults.get(0).backPropPrediction.totoalTimeMillis;
		summary.meanBoltzmannTestError=predictionResults.get(0).crbmPrediction.testError;
		summary.meanBoltzmannNetError=predictionResults.get(0).crbmPrediction.networkError;
		summary.meanBoltzmannTimeElapsed=predictionResults.get(0).crbmPrediction.totoalTimeMillis;
		result.add(summary);
		System.out.println("Added "+summary.dataSetLength+" for time window "+summary.timeWindowWidth);
		
		for(int i=0;i<predictionResults.size();i++){
			PredictionResult predictionResult=predictionResults.get(i);
			if(summary.dataSetLength!=predictionResult.dataSetLength){
				summary.meanBackPropTestError=summary.meanBackPropTestError/tryCount;
				summary.meanBackPropNetError=summary.meanBackPropNetError/tryCount;
				summary.meanBackPropTimeElapsed=summary.meanBackPropTimeElapsed/tryCount;
				summary.meanBoltzmannTestError=summary.meanBoltzmannTestError/tryCount;
				summary.meanBoltzmannNetError=summary.meanBoltzmannNetError/tryCount;
				summary.meanBoltzmannTimeElapsed=summary.meanBoltzmannTimeElapsed/tryCount;


				tryCount=1;
				summary=new Summary();
				summary.timeWindowWidth=predictionResult.timeWindowWidth;
				summary.inputData=predictionResult.inputData;
				summary.dataSetLength=predictionResult.dataSetLength;
				summary.meanBackPropTestError=predictionResult.backPropPrediction.testError;
				summary.meanBackPropNetError=predictionResult.backPropPrediction.networkError;
				summary.meanBackPropTimeElapsed=predictionResult.backPropPrediction.totoalTimeMillis;
				summary.meanBoltzmannTestError=predictionResult.crbmPrediction.testError;
				summary.meanBoltzmannNetError=predictionResult.crbmPrediction.networkError;
				summary.meanBoltzmannTimeElapsed=predictionResult.crbmPrediction.totoalTimeMillis;
				result.add(summary);
				System.out.println("Added "+summary.dataSetLength+" for time window "+summary.timeWindowWidth);
			}else{
				summary.meanBackPropTestError+=predictionResult.backPropPrediction.testError;
				summary.meanBackPropNetError+=predictionResult.backPropPrediction.networkError;
				summary.meanBackPropTimeElapsed+=predictionResult.backPropPrediction.totoalTimeMillis;
				summary.meanBoltzmannTestError+=predictionResult.crbmPrediction.testError;
				summary.meanBoltzmannNetError+=predictionResult.crbmPrediction.networkError;
				summary.meanBoltzmannTimeElapsed+=predictionResult.crbmPrediction.totoalTimeMillis;
				tryCount++;
			}
		}

		System.out.println("result.size="+result.size());

		return result;
	}


		
}


