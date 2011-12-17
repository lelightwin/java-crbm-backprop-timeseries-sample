package tr.org.predictionserver.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class DataProvider {

	private static double min=Double.MAX_VALUE;
	private static double max=Double.MIN_VALUE;
	private static double HIGH=0.9;
	private static double LOW=0.1;	
	private static int numberOfDataSets=10;
	private  static ArrayList<double[]> normalizedDataSets=new ArrayList<double[]>();
	private  static double[] normalizedData=null;
	private  static double[] allData=new double[]{
			 0.0262,  0.0575,  0.0837,  0.1203,  0.1883,  0.3033,
           0.1517,  0.1046,  0.0523,  0.0418,  0.0157,  0.0000,
           0.0000,  0.0105,  0.0575,  0.1412,  0.2458,  0.3295,
           0.3138,  0.2040,  0.1464,  0.1360,  0.1151,  0.0575,
           0.1098,  0.2092,  0.4079,  0.6381,  0.5387,  0.3818,
           0.2458,  0.1831,  0.0575,  0.0262,  0.0837,  0.1778,
           0.3661,  0.4236,  0.5805,  0.5282,  0.3818,  0.2092,
           0.1046,  0.0837,  0.0262,  0.0575,  0.1151,  0.2092,
           0.3138,  0.4231,  0.4362,  0.2495,  0.2500,  0.1606,
           0.0638,  0.0502,  0.0534,  0.1700,  0.2489,  0.2824,
           0.3290,  0.4493,  0.3201,  0.2359,  0.1904,  0.1093,
           0.0596,  0.1977,  0.3651,  0.5549,  0.5272,  0.4268,
           0.3478,  0.1820,  0.1600,  0.0366,  0.1036,  0.4838,
           0.8075,  0.6585,  0.4435,  0.3562,  0.2014,  0.1192,
           0.0534,  0.1260,  0.4336,  0.6904,  0.6846,  0.6177,
           0.4702,  0.3483,  0.3138,  0.2453,  0.2144,  0.1114,
           0.0837,  0.0335,  0.0214,  0.0356,  0.0758,  0.1778,
           0.2354,  0.2254,  0.2484,  0.2207,  0.1470,  0.0528,
           0.0424,  0.0131,  0.0000,  0.0073,  0.0262,  0.0638,
           0.0727,  0.1851,  0.2395,  0.2150,  0.1574,  0.1250,
           0.0816,  0.0345,  0.0209,  0.0094,  0.0445,  0.0868,
           0.1898,  0.2594,  0.3358,  0.3504,  0.3708,  0.2500,
           0.1438,  0.0445,  0.0690,  0.2976,  0.6354,  0.7233,
           0.5397,  0.4482,  0.3379,  0.1919,  0.1266,  0.0560,
           0.0785,  0.2097,  0.3216,  0.5152,  0.6522,  0.5036,
           0.3483,  0.3373,  0.2829,  0.2040,  0.1077,  0.0350,
           0.0225,  0.1187,  0.2866,  0.4906,  0.5010,  0.4038,
           0.3091,  0.2301,  0.2458,  0.1595,  0.0853,  0.0382,
           0.1966,  0.3870,  0.7270,  0.5816,  0.5314,  0.3462,
           0.2338,  0.0889,  0.0591,  0.0649,  0.0178,  0.0314,
           0.1689,  0.2840,  0.3122,  0.3332,  0.3321,  0.2730,
           0.1328,  0.0685,  0.0356,  0.0330,  0.0371,  0.1862,
           0.3818,  0.4451,  0.4079,  0.3347,  0.2186,  0.1370,
           0.1396,  0.0633,  0.0497,  0.0141,  0.0262,  0.1276,
           0.2197,  0.3321,  0.2814,  0.3243,  0.2537,  0.2296,
           0.0973,  0.0298,  0.0188,  0.0073,  0.0502,  0.2479,
           0.2986,  0.5434,  0.4215,  0.3326,  0.1966,  0.1365,
           0.0743,  0.0303,  0.0873,  0.2317,  0.3342,  0.3609,
           0.4069,  0.3394,  0.1867,  0.1109,  0.0581,  0.0298,
           0.0455,  0.1888,  0.4168,  0.5983,  0.5732,  0.4644,
           0.3546,  0.2484,  0.1600,  0.0853,  0.0502,  0.1736,
           0.4843,  0.7929,  0.7128,  0.7045,  0.4388,  0.3630,
           0.1647,  0.0727,  0.0230,  0.1987,  0.7411,  0.9947,
           0.9665,  0.8316,  0.5873,  0.2819,  0.1961,  0.1459,
           0.0534,  0.0790,  0.2458,  0.4906,  0.5539,  0.5518,
           0.5465,  0.3483,  0.3603,  0.1987,  0.1804,  0.0811,
           0.0659,  0.1428,  0.4838,  0.8127
	};


	public static void prepareDataSets(int timeWindowWidth){
		if(normalizedData==null){
			for(int i=0;i<allData.length;i++){
				if(min>allData[i])min=allData[i];
				else if (max<allData[i])max=allData[i];
			}
			normalizedData=new double[allData.length];
			for(int i=0;i<allData.length;i++){
				normalizedData[i]=(((allData[i]-min)/(max-min))*(HIGH-LOW)+LOW);
			}
		}
		//normalizedData=allData;
		for(int i=0;i<numberOfDataSets;i++){
			int length=(i+4)*timeWindowWidth;
			int startIndex=(int)(Math.random()*50);
			double [] dataSet=new double[length];
			System.arraycopy(normalizedData, startIndex, dataSet, 0, length);
			normalizedDataSets.add(dataSet);
		}
	}
	
	public static ArrayList<double[]> getDataSets(int timeWindowWidth){
		if(normalizedDataSets==null || normalizedDataSets.size()==0 || normalizedDataSets.get(0)==null){
			prepareDataSets(timeWindowWidth);
		}
		return normalizedDataSets;
	}
	public static void readFromDataFile(String fileName){
try{
BufferedReader inputData = new BufferedReader(new FileReader(fileName));
String line = null;
int dataLength = count(fileName);
allData = new double[dataLength];
int i = 0;
while ((line = inputData.readLine()) != null) {
	line = line.trim();
	if (line.length() > 0) {
		allData[i] = Double.parseDouble(line);
		i++;
	}
}
}catch(NumberFormatException nfe){
	nfe.printStackTrace();
} catch (FileNotFoundException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
}

public static int count(String filename) throws IOException {
InputStream is = new BufferedInputStream(new FileInputStream(filename));
try {
	byte[] c = new byte[1024];
	int count = 0;
	int readChars = 0;
	while ((readChars = is.read(c)) != -1) {
		for (int i = 0; i < readChars; ++i) {
			if (c[i] == '\n')
				++count;
		}
	}
	return count;
} finally {
	is.close();
}
}
}
