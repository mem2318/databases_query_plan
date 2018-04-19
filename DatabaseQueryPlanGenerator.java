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

    static double computeProb(ArrayList<Double> probs, int mask){
        double prob = 1.0;
        for(int j = 0; j < probs.size(); j++) {
            if(getBit(mask, j) == 1){
                prob *= probs.get(j);
            }
        }
        return prob;
    }
    static String logicalAndString(PlanElement elem, ArrayList<Double> queryProbs){
        String output = "(";
        int bitmap = elem.getIndex();
        int counter = 0;
        int numbits = countSetBits(bitmap);
        for(int j = 0; j < queryProbs.size(); j++){
            int bit = getBit(bitmap, j);
            if(bit == 1){
                counter++;
                output += String.format("t%d[o%d[i]]", j+1, j+1);
                if(counter < numbits){
                    output += " & ";
                }
            }
        }
        output += ")";
        return output;
    }

	public static void main(String[] args) {
        Properties prop = new Properties();
        InputStream configInput = null;
        BufferedReader queryInput = null;

        ArrayList<ArrayList<Double>> queryList = new ArrayList<ArrayList<Double>>();
        int r, t, l, m, a, f;
        r = t = l = m = a = f = 0;
        try {
        	configInput = new FileInputStream(args[1]);

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

        	queryInput = new BufferedReader(new FileReader(args[0]));
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

            int numOfPlans = ((int) Math.pow(2, queryProbs.size()))-1;
            // System.out.println("NumPlans: "+numOfPlans);
            PlanElement[] A = new PlanElement[numOfPlans];
            for(int i = 1; i <= A.length; i++){
                int bits = countSetBits(i);
                double prob = computeProb(queryProbs, i);
                int k = bits;



                A[i-1] = new PlanElement(bits, prob, false, i);

                double q;
                if(prob <= 0.5){
                    q = prob;
                } else {
                    q = 1.0-prob;
                }
                double logicalCost = k*r + (k-1)*l + f*k + t + m*q + prob*a;

                double noBranchCost = k*r + (k-1)*l + f*k + a;

                A[i-1].c = logicalCost > noBranchCost ? noBranchCost : logicalCost;
                A[i-1].b = logicalCost > noBranchCost ? true : false;
                // System.out.println("Cost "+i+": "+logicalCost + " " + noBranchCost);
            }
            
            System.out.println("\n\nPart 2:\n");

            for(int s = 1; s <= A.length; s++){
                int s_all = (~s) & (A.length);
                // System.out.println("\nLoop index: "+s+" "+s_all);
                for(int sp = 0; sp <= A.length; sp++){
                    if((~s_all & sp) == 0 && sp != 0){
                        // System.out.println(sp);
                        int sp_k = countSetBits(sp);
                        double sp_fcost = sp_k*r + (sp_k-1)*l + f*sp_k + t;
                        double sp_p = computeProb(queryProbs, sp);
                        double sp_cmetric = (sp_p-1)/sp_fcost;
                        // System.out.println(A[s-1].printTree());
                        // PlanElement s_leftmost = A[s-1].getLeftmostLogical();
                        PlanElement s_leftmost = A[s-1].getLeftmost();
                        double s_leftmost_fcost = s_leftmost.n*r + (s_leftmost.n-1)*l + f*s_leftmost.n + t;
                        double s_leftmost_p = computeProb(queryProbs, s_leftmost.getIndex());
                        if(s_leftmost_p != s_leftmost.p){
                            System.out.println("non-matching s leftmost prob");
                        }
                        double s_leftmost_cmetric = (s_leftmost_p-1)/s_leftmost_fcost;
                        // System.out.println("Cmetrics: "+ s_leftmost_cmetric+ " " + sp_cmetric+ " "+sp_p);
                        if(s_leftmost_cmetric >= sp_cmetric && s_leftmost_p > sp_p){
                            ArrayList<PlanElement> s_logical_terms = A[s-1].getLogicalTerms();
                            boolean case_2_fail = false;
                            for(PlanElement term : s_logical_terms){
                                if(term == s_leftmost){
                                    // System.out.println("Found leftmost term");
                                } else {
                                    double term_fcost = term.n*r + (term.n-1)*l + f*term.n + t;
                                    if(term.p < sp_p && term_fcost < sp_fcost){
                                        case_2_fail = true;
                                        break;
                                    }
                                }
                            }
                            if(sp_p >= 0.5){
                                case_2_fail = false;
                            }
                            // System.out.println("dmetric result: "+case_2_fail);
                            if(case_2_fail == false){
                                int combined = sp|s;
                                double overall_p = computeProb(queryProbs, sp|s);
                                double overall_q = Math.min(overall_p, 1-overall_p);
                                double combinedCost = sp_fcost + m*overall_q + overall_p*A[s-1].c;
                                // System.out.println("Costs: "+combinedCost+" "+A[combined-1].c);
                                if(combinedCost < A[combined-1].c){
                                    // System.out.println("Updating!!!");
                                    A[combined-1].c = combinedCost;
                                    A[combined-1].L = A[sp-1];
                                    A[combined-1].R = A[s-1];
                                }
                            }
                        }
                    }
                }
            }
            // System.out.println("Finished Algorithm 1 for: "+queryProbs.toString());
            // System.out.println(A[A.length-1].printTree());

            System.out.println("==================================================================");
            System.out.println(queryProbs.toString());
            System.out.println("------------------------------------------------------------------");

            ArrayList<PlanElement> inOrderTraversal = A[A.length-1].inOrderTraversal();
            String output = "if(";
            for(int i = 0; i < inOrderTraversal.size(); i++){
                PlanElement elem = inOrderTraversal.get(i);
                if(i == inOrderTraversal.size()-1){
                    // Last iter
                    if(elem.b == false){
                        output += logicalAndString(elem, queryProbs);
                        output += ") {\n\tanswer[j++] = i;\n}";
                    } else {
                        output += ") {\n\tanswer[j] = i;\n\tj += ";
                        output += logicalAndString(elem, queryProbs);
                        output += ";\n}";
                    }
                } else {
                    // otherwise
                    if(elem.L == null && elem.R == null){
                        // logical and with branch term
                        output += logicalAndString(elem, queryProbs);
                    } else {
                        // && term
                        if(i != inOrderTraversal.size() - 2){
                            output += " && ";
                        }
                    }
                }
            }
            System.out.println(output);
            System.out.println("------------------------------------------------------------------");
            System.out.println(A[A.length-1].c);

        }
        System.out.println("==================================================================");
    }
}












