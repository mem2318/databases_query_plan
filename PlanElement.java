import java.util.ArrayList;

/**
 * This class is holder the information relating to a particular node in the tree.
 * Effectively the algorithm constructs right-deep binary tree so this is the node.
 */
public class PlanElement {
	int n;
	double p;
	boolean b = false;
	double c;
	int Lind = -1;
	int Rind = -1;
	PlanElement L = null;
	PlanElement R = null;
	private int index;

	public PlanElement(int n, double p, boolean b, double c, PlanElement L, PlanElement R, int index) {
		this.n = n;
		this.p = p;
		this.b = b;
		this.c = c;
		this.L = L;
		this.R = R;
		this.index = index;
	}

	public PlanElement(int n, double p, boolean b, double c, int index) {
		this.n = n;
		this.p = p;
		this.b = b;
		this.c = c;
		this.index = index;
	}
	public PlanElement(int n, double p, boolean b, int index) {
		this.n = n;
		this.p = p;
		this.b = b;
		this.index = index;
	}

	public PlanElement getLeftmost(){
		if(this.L == null){
			return this;
		}

		return this.L.getLeftmost();
	}

	public PlanElement getLeftmostLogical(){
		if(this.L == null && this.R != null){
			System.out.println("right without left");
		} else if(this.R == null && this.L != null){
			System.out.println("left without right");
		}

		if(this.L == null){
			if(this.b == false){
				return this;
			} else {
				return null;
			}
		}

		PlanElement left_branch = this.L.getLeftmostLogical();
		if(left_branch != null){
			return left_branch;
		}

		if(this.R != null){
			PlanElement right_branch = this.R.getLeftmostLogical();
			if(right_branch != null){
				return right_branch;
			}
		}
		return null;
	}

	public ArrayList<PlanElement> inOrderTraversal(){
		ArrayList<PlanElement> temp = new ArrayList<PlanElement>();
		if(this.L == null && this.R == null){
			temp.add(this);
		} else if(this.L == null){
			System.out.println("ERROR NULL LEFT VALID RIGHT");
			temp.add(this);
			temp.addAll(this.R.inOrderTraversal());
		} else if(this.R == null){
			System.out.println("ERROR NULL RIGHT VALID LEFT");
			temp.addAll(this.L.inOrderTraversal());
			temp.add(this);
		} else {
			temp.addAll(this.L.inOrderTraversal());
			temp.add(this);
			temp.addAll(this.R.inOrderTraversal());
		}
		return temp;
	}

	public ArrayList<PlanElement> getLogicalTerms(){
		if(this.L == null && this.R != null){
			System.out.println("right without left");
		} else if(this.R == null && this.L != null){
			System.out.println("left without right");
		}

		ArrayList<PlanElement> leftSide = null;
		ArrayList<PlanElement> rightSide = null;
		if(this.L == null && this.R == null){
			// if(this.b == false){
			ArrayList<PlanElement> tempList = new ArrayList<PlanElement>();
			tempList.add(this);
			return tempList;
			// }
		}
		if(this.L != null){
			leftSide = this.L.getLogicalTerms();
		}
		if(this.R != null){
			rightSide = this.R.getLogicalTerms();
		}

		if(leftSide == null && rightSide != null){
			return rightSide;
		} else if(rightSide == null && leftSide != null){
			return leftSide;
		} else if(leftSide != null && rightSide != null){
			leftSide.addAll(rightSide);
			return leftSide;
		}

		System.out.println("left and right but no logical terms");
		return null;
	}

	public int getIndex() {
		return this.index;
	}

	public String printTree() {
		String output = this.toString()+"\n";
		if(this.L != null){
			output += "\t"+this.index+" Left: "+this.L.printTree().replaceAll("\n", "\n\t");
		}
		if(this.R != null){
			output += this.index+" Right: "+this.R.printTree().replaceAll("\n", "\n\t");
		}
		return output;
	}

	public String toString() {
		return String.format("Index: %d, Cost: %f,  Prob: %f, Status: %b ", this.index, this.c, this.p, this.b);
	}
}