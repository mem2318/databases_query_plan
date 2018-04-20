import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ArrayList;
import java.io.*;
import java.util.*;

/**
 * This is the wrapper for the main that runs the algorithm.
 * 
 */
public class DatabaseQueryPlanGenerator {

    /**
     * This function counts the number of set bits in an integer.  For positive integers, used as bitmaps,
     * this represents the number of elements in this set/subset.
     * @param n: the integer to be counted.
     * @return int the number of set bits.
     */
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

    /**
     * This gets the value of a certain bit in an integer.
     * @param val This is the number to be searched.
     * @param position This is the position of the bit you want the value of.
     * @return int representing the boolean value.
     */
    static int getBit(int val, int position)
    {
       return (val >> position) & 1;
    }

    /**
     * This gets the joint probability of a given subset from a probability list.
     * @param probs the complete list of probabilities
     * @param mask bits set to one in this will be used as indexes into the probability list to be used.
     * @return double the probability.
     */
    static double computeProb(ArrayList<Double> probs, int mask){
        double prob = 1.0;
        for(int j = 0; j < probs.size(); j++) {
            if(getBit(mask, j) == 1){
                prob *= probs.get(j);
            }
        }
        return prob;
    }

    /**
     * This function just makes a string for logical and for actually putting together the c code.
     * @param elem the plan element we're using
     * @param queryProbs the probabilities list
     * @return string with c code for logical and.
     */
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

    static ArrayList<String> getSetBits(int mask, ArrayList<Double> queryProbs){
        ArrayList<String> output = new ArrayList<String>();
        for(int i = 0; i < queryProbs.size(); i++){
            int bit = getBit(mask, i);
            if(bit == 1){
                String s = "t"+(i+1);
                output.add(s);
            }
        }
        return output;
    }

	public static void main(String[] args) {
        Properties prop = new Properties();
        InputStream configInput = null;
        BufferedReader queryInput = null;

        ArrayList<ArrayList<Double>> queryList = new ArrayList<ArrayList<Double>>();
        int r, t, l, m, a, f;
        r = t = l = m = a = f = 0;

        //store properties from config.txt with java.util.Properties formatting
        try {
        	configInput = new FileInputStream(args[1]);

        	prop.load(configInput);

            r = Integer.parseInt(prop.getProperty("r"));
            t = Integer.parseInt(prop.getProperty("t"));
            l = Integer.parseInt(prop.getProperty("l"));
            m = Integer.parseInt(prop.getProperty("m"));
            a = Integer.parseInt(prop.getProperty("a"));
            f = Integer.parseInt(prop.getProperty("f"));

            //store values from query.txt
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

        //error if either config.txt or query.txt is missing or in incorrect format
        }  catch (IOException ex) {  
            System.out.println("Error in reading and processing the input files, make sure they are as shown in the assignment.");
			// ex.printStackTrace();
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

            //beginning part 1 of the algorithm, generate 2^k - 1 plans 
            int numOfPlans = ((int) Math.pow(2, queryProbs.size()))-1;
            // System.out.println("NumPlans: "+numOfPlans);
           
            //generate array A using only &-terms for each nonempty subset s
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

                //compute logical cost
                double logicalCost = k*r + (k-1)*l + f*k + t + m*q + prob*a;

                //compute no branch cost
                double noBranchCost = k*r + (k-1)*l + f*k + a;

                //if the cost for no-branch algorithm is smaller, replace A[s].c by
                //that cost and set A[s].b = true
                A[i-1].c = logicalCost > noBranchCost ? noBranchCost : logicalCost;
                A[i-1].b = logicalCost > noBranchCost ? true : false;
                // System.out.println("Cost "+i+": "+logicalCost + " " + noBranchCost);
            }
            
            // System.out.println("\n\nPart 2:\n");

            //begin part 2 of the algorithm
            for(int s = 1; s <= A.length; s++){
                // System.out.println(getSetBits(s, queryProbs).toString());
                int s_all = (~s) & (A.length);
                // System.out.println("\nLoop index: "+s+" "+s_all);
                
                //for each nonempty s' in S such that s intersectinon s' = empty set...
                //sp is also equivalent to s' 
                for(int sp = 0; sp <= A.length; sp++){
                    if((~s_all & sp) == 0 && sp != 0){
                        System.out.println("\nJoint: "+getSetBits(s|sp, queryProbs).toString());

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
                        
                        //if the c-metric of s' is dominated by the c-metric of the leftmost & term in s
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
                            
                            //otherwise calculate the cost c for the combined plan (s' && s) using Eq 1
                            if(case_2_fail == false){
                                int combined = sp|s;
                                // double overall_p = computeProb(queryProbs, sp|s);
                                // double overall_q = Math.min(overall_p, 1-overall_p);
                                double sp_p2 = computeProb(queryProbs, sp);
                                double sp_q = Math.min(sp_p2, 1-sp_p2);
                                double combinedCost = sp_fcost + m*sp_q + sp_p2*A[s-1].c;
                                
                                //If c < A[s' union s].c then:
                                // System.out.println("\nJoint: "+getsetBits(combined, queryProbs).toString());
                                // System.out.println("\nJoint: "+getSetBits(combined, queryProbs).toString());

                                if(combinedCost < A[combined-1].c){
                                    System.out.println("Updating!!!");
                                    System.out.println("Costs: "+combinedCost+" "+A[combined-1].c);
                                    System.out.println("S: "+getSetBits(s, queryProbs).toString());
                                    System.out.println("S: "+A[s-1].toString());
                                    System.out.println("Sp: "+getSetBits(sp, queryProbs).toString());
                                    System.out.println("Sp: "+A[sp-1].toString()+" "+sp_fcost);
                                    // System.out.println("")
                                    A[combined-1].c = combinedCost; //replace A[s' union s].c with c
                                    A[combined-1].L = A[sp-1]; //replace A[s' union s].L with s'
                                    A[combined-1].R = A[s-1]; //replace A[s' union s].R with
                                } else {
                                    System.out.println("NOT UPDATING");
                                    System.out.println("Costs: "+combinedCost+" "+A[combined-1].c);
                                    System.out.println("S: "+getSetBits(s, queryProbs).toString());
                                    System.out.println("S: "+A[s-1].toString());
                                    System.out.println("Sp: "+getSetBits(sp, queryProbs).toString());
                                    System.out.println("Sp: "+A[sp-1].toString()+" "+sp_fcost+" "+A[s-1].c+" "+A[sp-1].c);
                                }
                            }
                        }
                        // System.out.println("S: "+getSetBits(s, queryProbs).toString());
                        // System.out.println("S: "+A[s-1].toString());
                        // System.out.println("Sp: "+getSetBits(sp, queryProbs).toString());
                        // System.out.println("Sp: "+A[sp-1].toString());
        }
                }
            }
            // System.out.println("Finished Algorithm 1 for: "+queryProbs.toString());
            // System.out.println(A[A.length-1].printTree());

            System.out.println("==================================================================");
            // System.out.println(queryProbs.toString());
            for(double prob: queryProbs){
                System.out.print(prob+" ");
            }
            System.out.println("\n------------------------------------------------------------------");


            //traverse tree and build up string to generate c program 
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












