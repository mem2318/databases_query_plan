import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ArrayList;
import java.io.*;
import java.util.*;

public class DatabaseQueryPlanGenerator {

    static int countSetBits(int n)
    {
        int count = 0;
        while (n > 0)
        {
            n &= (n - 1) ;
            count++;
        }
        return count;
    }

    static int getBit(int val, int position)
    {
       return (val >> position) & 1;
    }

	public static void main(String[] args) {
        Properties prop = new Properties();
        InputStream configInput = null;
        BufferedReader queryInput = null;

        ArrayList<ArrayList<Double>> queryList = new ArrayList<ArrayList<Double>>();
        int r, t, l, m, a, f;
        r = t = l = m = a = f = 0;
        try {
        	configInput = new FileInputStream("config.txt");

        	prop.load(configInput);
        	// System.out.println(prop.getProperty("r"));
        	// System.out.println(prop.getProperty("t"));
        	// System.out.println(prop.getProperty("l"));
        	// System.out.println(prop.getProperty("m"));
        	// System.out.println(prop.getProperty("a"));
        	// System.out.println(prop.getProperty("f"));

            r = Integer.parseInt(prop.getProperty("r"));
            t = Integer.parseInt(prop.getProperty("t"));
            l = Integer.parseInt(prop.getProperty("l"));
            m = Integer.parseInt(prop.getProperty("m"));
            a = Integer.parseInt(prop.getProperty("a"));
            f = Integer.parseInt(prop.getProperty("f"));

        	queryInput = new BufferedReader(new FileReader("1_query.txt"));
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

        for(ArrayList<Double> queryProbs : queryList){

            int numOfPlans = (int) Math.pow(2, queryProbs.size());
            System.out.println("NumPlans: "+numOfPlans);
            PlanElement[] A = new PlanElement[numOfPlans];
            for(int i = 1; i < A.length; i++){
                int bits = countSetBits(i);
                double prob = 1.0;
                int k = bits;
                double sumProb = 0.0;
                for(int j = 0; j < queryProbs.size(); j++) {
                    if(getBit(i, j) == 1){
                        prob *= queryProbs.get(j);
                        sumProb += queryProbs.get(j);
                    }
                }

                A[i-1] = new PlanElement(bits, prob, false);
                System.out.println("Params "+i+": "+ bits+" "+prob);

                double q;
                if(prob <= 0.5){
                    q = prob;
                } else {
                    q = 1.0-prob;
                }
                double logicalCost = k*r + (k-1)*l + f*k + t + m*q + prob*a;

                double noBranchCost = 0.0;

                for(int j = queryProbs.size()-1; j >= 0; j--){
                    if(getBit(i, j) == 1){
                        double p_n = queryProbs.get(j);
                        double q_n = p_n <= 0.5 ? p_n : 1.0-p_n; 
                        noBranchCost = r + t + f + m*q_n + p_n*noBranchCost;
                    }
                }

                A[i-1].c = logicalCost > noBranchCost ? noBranchCost : logicalCost;
                System.out.println("Cost "+i+": "+logicalCost + " " + noBranchCost);
            }
            
            System.out.println();
        }
    }
}