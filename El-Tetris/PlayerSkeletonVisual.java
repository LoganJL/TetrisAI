
public class PlayerSkeletonVisual {

	// Final Rating
	/*
	rating = (0.0);
    rating += ((-1.0) * (landingHeight));
    rating += ((1.0) * ((double)(erodedPieceCellsMetric)));
    rating += ((-1.0) * ((double)(boardRowTransitions)));
    rating += ((-1.0) * ((double)(boardColumnTransitions)));
    rating += ((-4.0) * ((double)(boardBuriedHoles)));
    rating += ((-1.0) * ((double)(boardWells)));
    */
	private static int n = 0;
	public static final int LANDINGHEIGHT = n++;
	public static final int ERODEDPIECE = n++;
	public static final int ROWTRANSITIONS = n++;
	public static final int COLTRANSITIONS = n++;
	public static final int BURIEDHOLES = n++;
	public static final int WELLS = n++;
	public static final int NUMFEATURES = n;
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeletonVisual p = new PlayerSkeletonVisual();
		
		int rowsCleared = 0;
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			/*
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//*/
			
			//*
			if (rowsCleared*10000 < s.getRowsCleared()) {
				rowsCleared++;// = s.getRowsCleared();
				System.out.println("You have completed "+ s.getRowsCleared() +" rows.");
			}
			//*/
		}
		System.out.println("Game over!");
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
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
