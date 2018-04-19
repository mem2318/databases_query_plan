Repository for COMSW4112 project 2

Database Implementation with Professor Ross
Project 2
Michael Chess msc2209
Megan Massey mem2318

Running instructions:
	make and stage2.sh are used as described in the instructions.  stage2.sh must be executable. make generates a java executable that is run in stage2.sh.  The output of this is redirected into a file called output.txt.

DatabaseQueryPlanGenerator.java:
	This file contains the main function and helper functions.  It contains the main algorithm and actually runs the project.
	We first establish a list of queries in an ArrayList of ArrayLists of Doubles. Each element of this list is an ArrayList of Doubles that represents a single query.  The total list is the input file (eg "query.txt") that is newline delimeted on a query basis and space delimited for the query values.
	We then loop through the outer arraylist in a for loop so that we have one iteration per query.  In each loop we establish an array A of length of the number of possible plans for the query, that is 2^(query length).  This will hold our information in the form of PlanElement objects.  These objects contain information such as a selectivity (p) a cost (c) a switch for whether it is a no-branch (b) and so on (see below for more details).  They also contain functions facilitating tree traversals.  This array correlates to the A array in the original algorithm spec.
	We then run a for loop over all elements of A that computes part 1 of the algorithm for each element.  That is it computes the logical & branching cost and the no-branching cost and stores the better one in the PlanElement.  Please note.  One confusing aspect of the use of A is that it is 0 indexed so the integer representing the bitmap for a given subset indexes to A[i-1] so as to not have an empty element at the beginning. Each plan element is constructed with n which is the number of bits, a joint probability, and the bitmap i.
	Once this is complete we move into part 2 of the algorithm. This part is achieved with nested for loops. The outer one iterates over a variable s representing a bitmap of the set S in the original algorithm. The inner sp loop variable iterates over all of the possible subsets used as the left child for a given S. We then compute the required metrics for sp and for the leftmost child of s (reached using a PlanElement tree traversal function).
	Then the two conditions are checked and if they are passed the left and right children of the PlanElement at the array position of the combined (logical or) sets of s and sp (minus one) is updated. This effectively connects up the tree so that at the end you have a forest of trees in the A array. The tree at the last element (representing a set with all elements) represents the final ordering of the optimal plan.
	The tree of this optimal plan is used to construct the output c code. This functionality utilizes an in-order traversal of the tree starting at the last element of the A array. We then iterate over this in order traversal to construct the c code.  The in order traversal is used because it allows us to concatenatively construct the string for the c code.

PlanElement.java:
	This file contains the class PlanElement.  This class is used as tree element and an element of array A.  It contains the required information for the algorithm and the required binary tree traversal algorithms and information for constructing c code. For example the function getLeftmost is utilized when checking the conditions in part two of the algorithm since we need the metrics of the leftmost element of set S.  Additionally it implements inOrderTraversal which returns an ArrayList<PlanElement> of the in order traversal from that node that is used as described above to construct c code.  It also has nice print functions such as printTree to show how the internal tree structure looks.