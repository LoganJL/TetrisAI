
public class PlayerSkeleton {

	// Final Rating
	private static int n = 0;
	public static final int LANDINGHEIGHT = n++;
	public static final int ERODEDPIECE = n++;
	public static final int ROWTRANSITIONS = n++;
	public static final int COLTRANSITIONS = n++;
	public static final int BURIEDHOLES = n++;
	public static final int WELLS = n++;
	public static final int NUMFEATURES = n;

	public static double weights[];

	private State s;

	public PlayerSkeleton(double[] weights) {
		if (weights == null) {
			this.weights = new double[n];
			this.weights[LANDINGHEIGHT] = -4.5;
			this.weights[ERODEDPIECE] = 3.4;
			this.weights[ROWTRANSITIONS] = -3.2;
			this.weights[COLTRANSITIONS] = -9.3;
			this.weights[BURIEDHOLES] = -7.9;
			this.weights[WELLS] = -3.4;
		} else {
			this.weights = new double[weights.length];
			System.arraycopy(weights, 0, this.weights, 0, weights.length);
		}

		s = new State();
		
		// int rowsCleared = 0;
		while(!s.hasLost()) {
			s.makeMove(pickMove(s,s.legalMoves()));
			// if (rowsCleared*10000 < s.getRowsCleared()) {
			// 	rowsCleared++;
			// 	System.out.println("You have completed "+ s.getRowsCleared() +" rows.");
			// }
		}
		//System.out.println("Game over!");
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}

	public PlayerSkeleton() {
		this(null);
	}

	public int getRowsCleared() {
		return s.getRowsCleared();
	}

	public static void main(String[] args) {
		PlayerSkeleton p = new PlayerSkeleton();
		System.out.println(p.getRowsCleared());
	}
	
	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		double[] score = new double[legalMoves.length];
		double[] priority = new double[legalMoves.length];
		
		SimState sim = new SimState();
		sim.setStateTo(s);
		
		SimState exploreMove = new SimState();
		for (int m = 0; m < legalMoves.length; m++) {
			exploreMove.setStateTo(sim);
			
			int piecePlayed = exploreMove.getNextPiece();
			int rowsClearedSoFar = exploreMove.getRowsCleared();
			exploreMove.simulateMove(m);
			score[m] = HelperFunctions.PierreRateThisMove(exploreMove, m, piecePlayed, rowsClearedSoFar);
			priority[m] = HelperFunctions.PierrePriorityThisMove(exploreMove, m, piecePlayed);
		}
		
		int maxIndex = 0;
		double maxScore = score[0];
		for (int i = 1; i < score.length; i++) {
			if ((score[i] > maxScore) ||
				(score[i] == maxScore && priority[i] > priority[maxIndex])) {
				maxScore = score[i];
				maxIndex = i;
			}
		}
		
		return maxIndex;
	}
}
