
public class HelperFunctions {
	public static int lastRowHeight = 0;

	public static int[] make1dDeepCopy(int[] source) {
		int[] dest = new int[source.length];
		for (int i = 0; i < source.length; i++) {
			dest[i] = source[i];
		}
		return dest;
	}
	
	public static int[][] make2dDeepCopy(int[][] source) {
		int[][] dest = new int[source.length][];
		for (int i = 0; i < source.length; i++) {
			dest[i] = new int[source[i].length];
			for (int j = 0; j < source[i].length; j++) {
				dest[i][j] = source[i][j];
			}
		}
		return dest;
	}
	
	// Implementation of Pierre Dellacherie (without his tie-breaking "priority")
	public static double PierreRateThisMove(SimState s, int moveUsed, int piecePlayed, int rowsClearedSoFar) {
		//int piecePlayed = s.getNextPiece();
		//int pieceOrientation = State.legalMoves[piecePlayed][moveUsed][State.ORIENT];
		int pieceTurn = s.getTurnNumber();// - 1; // The turn the piece was played. Note: turn++ when makeMove is called
		int[][] currentField = s.getField();
		
		/*
		int[] pieceTops = s.getpTop()[piecePlayed][pieceOrientation];
		int[] pieceBtms = s.getpBottom()[piecePlayed][pieceOrientation];
		int pieceMaxY = pieceTops[0];
		int pieceMinY = pieceBtms[0];
		for (int i = 0; i < pieceTops.length; i++) {
			pieceMaxY = Math.max(pieceMaxY, pieceTops[i]);
			pieceMinY = Math.min(pieceMinY, pieceBtms[i]);
		}*/
		
		int pieceMaxY = 0;
		int pieceMinY = State.ROWS;
		
		for (int r = 0; r < State.ROWS; r++) {
			for (int c = 0; c < State.COLS; c++) {
				if (currentField[r][c] == pieceTurn) {
					pieceMaxY = Math.max(pieceMaxY, r);
					pieceMinY = Math.min(pieceMinY, r);
				}
			}
		}
		
		// Landing Height (vertical midpoint)
        double landingHeight = 0.0;
        landingHeight = 0.5 * (double)( pieceMinY + pieceMaxY );
        
        int completedRows = 0;
        completedRows = s.getRowsCleared() - rowsClearedSoFar;
        
        // Count piece cells eroded by completed rows before doing collapse on pile.
        int erodedPieceCellsMetric = 0;
        if (completedRows > 0)
        {
        	int pieceCellsEliminated = 4; // All pieces have 4 cells
        	for (int r = 0; r < State.ROWS; r++) {
        		for (int c = 0; c < State.COLS; c++) {
        			if (currentField[r][c] == pieceTurn) {
        				pieceCellsEliminated--; // This cell is not eliminated
        			}
        		}
        	}
        	
        	// Weight eroded cells by completed rows
            erodedPieceCellsMetric = (completedRows * pieceCellsEliminated);
        }
        
        int pileHeight = 0;
        pileHeight = s.getMaxHeight();
        
        
        
        
        // Each empty row (above pile height) has two (2) "transitions"
        // (We could call ref_Board.GetTransitionCountForRow( y ) for
        // these unoccupied rows, but this is an optimization.)
        int boardRowTransitions = 0;
        boardRowTransitions = 2 * (State.ROWS - pileHeight);

        // Only go up to the pile height, and later we'll account for the
        // remaining rows transitions (2 per empty row).
        for (int y = 0; y < pileHeight; y++ ) {
            boardRowTransitions += (s.GetTransitionCountForRow( y ));
        }

        int boardColumnTransitions = 0;
        int boardBuriedHoles = 0;
        int boardWells = 0;
        for (int x = 0; x < State.COLS; x++ ) {
            boardColumnTransitions += s.GetTransitionCountForColumn( x );
            boardBuriedHoles += s.GetBuriedHolesForColumn( x );
            boardWells += s.GetAllWellsForColumn( x );
        }
        
        
        double rating = 0;
        rating += PlayerSkeleton.weights[PlayerSkeleton.LANDINGHEIGHT] * (landingHeight);
        rating += PlayerSkeleton.weights[PlayerSkeleton.ERODEDPIECE] * ((double)(erodedPieceCellsMetric));
        rating += PlayerSkeleton.weights[PlayerSkeleton.ROWTRANSITIONS] * ((double)(boardRowTransitions));
        rating += PlayerSkeleton.weights[PlayerSkeleton.COLTRANSITIONS] * ((double)(boardColumnTransitions));
        rating += PlayerSkeleton.weights[PlayerSkeleton.BURIEDHOLES] * ((double)(boardBuriedHoles));
        rating += PlayerSkeleton.weights[PlayerSkeleton.WELLS] * ((double)(boardWells));

//        System.out.println(rating + " || " + landingHeight + " | " + erodedPieceCellsMetric + " | " +
//        					boardRowTransitions + " | " + boardColumnTransitions + " | " +
//        					boardBuriedHoles + " | " + boardWells);
        // EXPLANATION:
        //   [1] Punish landing height
        //   [2] Reward eroded piece cells
        //   [3] Punish row    transitions
        //   [4] Punish column transitions
        //   [5] Punish buried holes (cellars)
        //   [6] Punish wells
        
        return rating;
	}
	
	// Priority for tie-breaking
	public static double PierrePriorityThisMove(SimState exploreMove, int moveUsed, int piecePlayed) {
		int pieceMaxX = 0;
		int pieceMinX = State.COLS;
		int pieceTurn = exploreMove.getTurnNumber();
		int[][] currentField = exploreMove.getField();
					
		for (int r = 0; r < State.ROWS; r++) {
			for (int c = 0; c < State.COLS; c++) {
				if (currentField[r][c] == pieceTurn) {
					pieceMaxX = Math.max(r, pieceMaxX);
					pieceMinX = Math.min(r, pieceMinX);
				}
			}
		}
		
		/*
		int width = 1 + (State.getpWidth()[piecePlayed][State.legalMoves[piecePlayed][moveUsed][State.ORIENT]])/2;
		int absoluteDistanceX = 0;
		absoluteDistanceX = ((pieceMaxX + pieceMinX)/2 - width);
		if (absoluteDistanceX < 0) {
			absoluteDistanceX = (-(absoluteDistanceX));
		}
		*/
		
		int priority = 0;
		//priority += (100 * absoluteDistanceX);
		priority += 100 * ((pieceMaxX + pieceMinX + 1)/2 - 6);
		if ((pieceMaxX + pieceMinX + 1)/2 < 6) {
			priority += 10;
		}
			        
		int totalOrientations = State.getpOrients()[piecePlayed]-1;
		int pieceOrientation;
			        
		if (totalOrientations == 0) {
			pieceOrientation = 0;
		} else {
			pieceOrientation = 1 + ((((State.legalMoves[piecePlayed][moveUsed][State.ORIENT] - 1) % totalOrientations) 
		                        + totalOrientations) % totalOrientations);
		}

		priority -= (pieceOrientation - 1);
		
		return priority;
	}
}
