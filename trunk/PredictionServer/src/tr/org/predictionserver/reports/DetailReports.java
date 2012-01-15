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

public class DetailReports {

	
	public HashMap<String,XYSeriesCollection> generateDataSets(ArrayList<PredictionSummary> predictionSummaries) {
		HashMap<String,XYSeriesCollection> result=new HashMap<String,XYSeriesCollection>();
		
		for(int predictionSummaryNo=0;predictionSummaryNo<predictionSummaries.size();predictionSummaryNo++){
	        XYSeriesCollection dataset = new XYSeriesCollection();
	        result.put(predictionSummaries.get(predictionSummaryNo).timeWindowWidth+"-"+predictionSummaries.get(predictionSummaryNo).dataSetLength,dataset);
	        XYSeries[] series=new XYSeries[1+predictionSummaries.get(predictionSummaryNo).backpropPredictions.size()+predictionSummaries.get(predictionSummaryNo).boltzmannPredictions.size()];
	        series[0] = new XYSeries("Gerçekte Olan Veri");
	        int seriesIndex=1;
	        for(int i=0;i<predictionSummaries.get(predictionSummaryNo).backpropPredictions.size();i++){
	        	series[seriesIndex]=new XYSeries("İleri Yönelimli YSA Tahmin No "+(i+1));
	        	seriesIndex++;
	        }
	        for(int i=0;i<predictionSummaries.get(predictionSummaryNo).boltzmannPredictions.size();i++){
	        	series[seriesIndex]=new XYSeries("Kısıtlanmış Boltzmann Makinesi Tahmin No "+(i+1));
	        	seriesIndex++;
	        }
	        for(int i=0;i<series.length;i++){
	        	dataset.addSeries(series[i]);
	        }
	        
	        for(int i=0;i<predictionSummaries.get(predictionSummaryNo).dataToBePredicted.length;i++){
	        	series[0].add(new Double(i).doubleValue(),predictionSummaries.get(predictionSummaryNo).dataToBePredicted[i]);
	        	 seriesIndex=1;
	        	for(int j=0;j<predictionSummaries.get(predictionSummaryNo).backpropPredictions.size();j++){
		        	series[seriesIndex].add(new Double(i).doubleValue(),predictionSummaries.get(predictionSummaryNo).backpropPredictions.get(j).predictedValues[i]);
		        	seriesIndex++;
		        }
		        for(int j=0;j<predictionSummaries.get(predictionSummaryNo).boltzmannPredictions.size();j++){
		        	series[seriesIndex].add(new Double(i).doubleValue(),predictionSummaries.get(predictionSummaryNo).boltzmannPredictions.get(j).predictedValues[i]);
		        	seriesIndex++;
		        }
	        	
	        }
			
		}
		return result;
	}


	public void generateAndSaveCharts(	ArrayList<PredictionResult> predictionResults) {
		HashMap<String,XYSeriesCollection>  dataSets=generateDataSets(generateReportData(predictionResults));
		 Iterator<String> iterator=dataSets.keySet().iterator();
		 while(iterator.hasNext()){
			 String timeWindowAndDataSetLength=iterator.next();
			XYSeriesCollection dataset =dataSets.get(timeWindowAndDataSetLength);

			  JFreeChart chart = ChartFactory.createXYLineChart("Zaman Aralığı - Veri Kümesi Büyüklüğü : "+timeWindowAndDataSetLength,"Tahmin No", "Gerçek Değer ve Tahmin Değerleri", dataset, PlotOrientation.VERTICAL, true,true,true);
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
			  //viewChart(timeWindowAndDataSetLength,chart);
			  saveChart(timeWindowAndDataSetLength,chart);
		 }
	}

	public synchronized void saveChart(String timeWindowAndDataSetLength,JFreeChart chart){
		try {
			ChartUtilities.saveChartAsJPEG(new File("/var/log/PredictionServer/detail."+timeWindowAndDataSetLength+".jpeg"), chart, 550, 550);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public synchronized void viewChart(String timeWindowAndDataSetLength,JFreeChart chart){
	      ApplicationFrame applicationFrame=new ApplicationFrame("Zaman Aralığı - Veri Kümesi Büyüklüğü "+ timeWindowAndDataSetLength+" için oluşturulan grafik ");
	        ChartPanel panel = new ChartPanel(chart);
	        panel.setFillZoomRectangle(true);
	        panel.setMouseWheelEnabled(true);
	        panel.setPreferredSize(new java.awt.Dimension(550, 350));
	        applicationFrame.setContentPane(panel);
	        applicationFrame.pack();
	        RefineryUtilities.centerFrameOnScreen( applicationFrame);
	        applicationFrame.setVisible(true);
	}
	

	public void generateAndSaveHTMLTableReport(
			ArrayList<PredictionResult> predictionResults) {
		
		ArrayList<PredictionSummary> predictionSummaries =generateReportData(predictionResults);
		for(int i=0;i<predictionSummaries.size();i++){
			StringBuffer genratedHTMLTable=new StringBuffer();
			genratedHTMLTable.append("<html><head></head><body><table>");
			genratedHTMLTable.append("<tr>\n");
			genratedHTMLTable.append("<td colspan=\""+(predictionSummaries.get(i).backpropPredictions.size()+1)+"\"> ZAMAN ARALIĞI ="+predictionSummaries.get(i).timeWindowWidth+"</td>\n");
			genratedHTMLTable.append("<td colspan=\""+predictionSummaries.get(i).boltzmannPredictions.size()+"\"> VERİ KÜMESİ BÜYÜKLÜĞÜ ="+predictionSummaries.get(i).dataSetLength+"</td>\n");
			genratedHTMLTable.append("</tr>\n");
			StringBuffer stringBuffer=new StringBuffer();
			stringBuffer.append("<tr>\n");
			stringBuffer.append("<td>Gerçek Veri</td>\n");
			for(int k=0;k<predictionSummaries.get(i).backpropPredictions.size();k++){
				stringBuffer.append("<td>İleri Yönelimli YSA "+(k+1)+". Tahmin</td>\n");
			}
			for(int k=0;k<predictionSummaries.get(i).boltzmannPredictions.size();k++){
				stringBuffer.append("<td>Boltzmann Makinesi "+(k+1)+". Tahmin</td>\n");
			}
			stringBuffer.append("</tr>\n");
			genratedHTMLTable.append(stringBuffer);
			for(int j=0;j<predictionSummaries.get(i).dataToBePredicted.length;j++){
					stringBuffer=new StringBuffer();
					stringBuffer.append("<tr>\n");
					stringBuffer.append("<td>"+predictionSummaries.get(i).dataToBePredicted[j]+"</td>\n");
					for(int k=0;k<predictionSummaries.get(i).backpropPredictions.size();k++){
						stringBuffer.append("<td>"+predictionSummaries.get(i).backpropPredictions.get(k).predictedValues[j]+"</td>\n");
					}
					for(int k=0;k<predictionSummaries.get(i).boltzmannPredictions.size();k++){
						stringBuffer.append("<td>"+predictionSummaries.get(i).boltzmannPredictions.get(k).predictedValues[j]+"</td>\n");
					}
					stringBuffer.append("</tr>\n");
					genratedHTMLTable.append(stringBuffer);
			}
			genratedHTMLTable.append("</table></body></html>");
			try {
				FileUtils.writeFile("/var/log/PredictionServer/detailreport."+predictionSummaries.get(i).timeWindowWidth+"."+predictionSummaries.get(i).dataSetLength+".html", genratedHTMLTable.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}


	public ArrayList<PredictionSummary> generateReportData(ArrayList<PredictionResult> predictionResults){
		ArrayList<PredictionSummary> result =new ArrayList<PredictionSummary>();
		PredictionSummary predictionSummary=null;
		
		for(int i=0;i<predictionResults.size();i++){
			PredictionResult predictionResult=predictionResults.get(i);
			if(predictionSummary==null || predictionSummary.dataSetLength!=predictionResult.dataSetLength ){
				predictionSummary=new PredictionSummary();
				predictionSummary.dataSetLength=predictionResults.get(i).dataSetLength;
				predictionSummary.timeWindowWidth=predictionResults.get(i).timeWindowWidth;
				predictionSummary.dataToBePredicted=predictionResults.get(i).dataToBePredicted;
				result.add(predictionSummary);
			}
			predictionSummary.backpropPredictions.add(predictionResults.get(i).backPropPrediction);
			predictionSummary.boltzmannPredictions.add(predictionResults.get(i).crbmPrediction);
		}

		System.out.println("result.size="+result.size());

		return result;
	}


		
}


