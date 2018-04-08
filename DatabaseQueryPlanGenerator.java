import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ArrayList;
import java.io.*;
import java.util.*;

public class DatabaseQueryPlanGenerator {
	public static void main(String[] args) {
        Properties prop = new Properties();
        InputStream configInput = null;
        BufferedReader queryInput = null;

        try {
        	configInput = new FileInputStream("config.txt");

        	prop.load(configInput);
        	System.out.println(prop.getProperty("r"));
        	System.out.println(prop.getProperty("t"));
        	System.out.println(prop.getProperty("l"));
        	System.out.println(prop.getProperty("m"));
        	System.out.println(prop.getProperty("a"));
        	System.out.println(prop.getProperty("f"));

        	queryInput = new BufferedReader(new FileReader("query.txt"));
        	ArrayList<ArrayList<Double>> queryList = new ArrayList<ArrayList<Double>>();
        	String lineRead = queryInput.readLine();
        	while(lineRead != null){
        		String[] splitted = lineRead.split("\\s+");
        		ArrayList<Double> tempQuery = new ArrayList<Double>(); 
        		for(int i = 0; i < splitted.length; i++){
        			tempQuery.add(Double.valueOf(splitted[i]));
        		}
        		queryList.add(tempQuery);
        		lineRead = queryInput.readLine();

        	}

        	for(ArrayList<Double> i : queryList){
        		for(Double d : i){
        			System.out.print(Double.toString(d) + " ");
        		}
        		System.out.print("\n");
        	}


        }  catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (configInput != null) {
				try {
					configInput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }
}