public class PlanElement {
	int n;
	double p;
	boolean b = false;
	double c;
	PlanElement L;
	PlanElement R;

	public PlanElement(int n, double p, boolean b, double c, PlanElement L, PlanElement R) {
		this.n = n;
		this.p = p;
		this.b = b;
		this.c = c;
		this.L = L;
		this.R = R;
	}

	public PlanElement(int n, double p, boolean b, double c) {
		this.n = n;
		this.p = p;
		this.b = b;
		this.c = c;
	}
}