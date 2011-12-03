package tr.org.predictionserver.util;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
	public static String readFile(String filename) throws IOException {
		   BufferedReader br = new BufferedReader(new FileReader(filename));
		   String nextLine = "";
		   StringBuffer sb = new StringBuffer();
		   while ((nextLine = br.readLine()) != null) {
			   sb.append(nextLine);
		   }
		   return sb.toString();
		}
	public static void writeFile(String filename,String content) throws IOException {
		File file=new File(filename);
		FileWriter fileWriter=new FileWriter(file);
		fileWriter.write(content);
		fileWriter.close();
	}
}
