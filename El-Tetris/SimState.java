
public class SimState extends State {	
	private int[][][] pBottom = State.getpBottom();
	private int[][] pHeight = State.getpHeight();
	private int[][][] pTop = State.getpTop();

	private int[][] field = new int[ROWS][COLS];
	private int[] top = new int[COLS];
	private int turn = 0;
	private int cleared = 0;
	protected int nextPiece;
	
	public void setStateTo(State s) {
		this.nextPiece = s.getNextPiece();
		this.lost = s.hasLost();
		this.cleared = s.getRowsCleared();
		this.turn = s.getTurnNumber();
		this.top = HelperFunctions.make1dDeepCopy(s.getTop());
		this.field = HelperFunctions.make2dDeepCopy(s.getField());
	}
	
	// GETTERS (Override the inherited ones to return the correct values)
	@Override
	public int[][] getField() {
		return this.field;
	}
	
	@Override
	public int[] getTop() {
		return this.top;
	}
	
	@Override
	public int getTurnNumber() {
		return this.turn;
	}
	
	@Override
	public int getRowsCleared() {
		return this.cleared;
	}
	
	@Override
	public int getNextPiece() {
		return this.nextPiece;
	}
	
	public void setNextPiece(int piece) {
		this.nextPiece = piece;
	}
	
	// We don't want the piece to be randomized and "lost" because we want to refer to it for evaluation
	public boolean simulateMove(int move) {
		int orient = legalMoves[nextPiece][move][ORIENT];
		int slot = legalMoves[nextPiece][move][SLOT];
		
		turn++;
		//height if the first column makes contact
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
		}
		
		//check if game ended
		if(height+pHeight[nextPiece][orient] >= ROWS) {
			lost = true;
			return false;
		}

		
		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
			
			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = turn;
			}
		}
		
		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}
		
		int rowsCleared = 0;
		
		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				rowsCleared++;
				cleared++;
				//for each column
				for(int c = 0; c < COLS; c++) {

					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}
		
		//pick a new piece
		//nextPiece = randomPiece();
		
		return true;
	}
	
	
	// Feature Extraction Related Functions
	public int getMaxHeight() {
		for (int r = ROWS-1; r >= 0; r--) {
			for (int c = 0; c < COLS; c++) {
				if (field[r][c] != 0)
					return r+1; // Returns highest row number that is non-empty
			}
		}
		return 0; // Empty board
	}
	
	public int GetTransitionCountForRow( int r ) { // result range: 0..COLS
		int transitionCount = 0;
		int cellA, cellB;

        // check cell and neighbor to right...
        for (int c = 0; c < COLS-1; c++ ) {
            cellA = field[r][c];
            cellB = field[r][c+1];

            // If a transition from occupied to unoccupied, or
            // from unoccupied to occupied, then it's a transition.
            if ((cellA != 0 && cellB == 0) ||
            	(cellA == 0 && cellB != 0)) {
                transitionCount++;
            }
        }

        // check transition between left-exterior and column 1.
        // (Note: Exterior is implicitly "occupied".)
        cellA = field[r][0];
        if (cellA == 0) {
            transitionCount++;
        }

        // check transition between column 'mWidth' and right-exterior.
        // (NOTE: Exterior is implicitly "occupied".)
        cellA = field[r][COLS-1];
        if (cellA == 0) {
            transitionCount++;
        }

        return transitionCount;
    }
	
	public int GetTransitionCountForColumn( int c ) { // result range: 1..(ROWS + 1)
		int transitionCount = 0;
		int cellA, cellB;

        // check cell and neighbor above...
        for (int r = 0; r < State.ROWS-1; r++ ) {
            cellA = field[r][c];
            cellB = field[r+1][c];

            // If a transition from occupied to unoccupied, or
            // from unoccupied to occupied, then it's a transition.
            if ((cellA != 0 && cellB == 0) ||
                (cellA == 0 && cellB != 0)) {
            	transitionCount++;
            }
        }

        // check transition between bottom-exterior and row Y=1.
        // (Note: Bottom exterior is implicitly "occupied".)
        cellA = field[0][c];
        if (cellA == 0) {
            transitionCount++;
        }

        // check transition between column 'mHeight' and above-exterior.
        // (Note: Sky above is implicitly UN-"occupied".)
        cellA = field[ROWS-1][c];
        if (cellA == 0) {
            transitionCount++;
        }
        
        return (transitionCount);
    }
	
	public int GetBuriedHolesForColumn( int c ) { // result range: 0..(ROWS-1)
		int totalHoles = 0;
        int cellValue;
        boolean enable = false;

        for (int r = ROWS-1; r >= 0; r-- ) {
            cellValue = field[r][c];
            if (cellValue != 0) {
                enable = true;
            } else {
                if (enable) {
                    totalHoles++;
                }
            }
        }

        return totalHoles;
    }
	
	public int GetAllWellsForColumn( int c ) { // result range: 0..O(Height*mHeight)
        int wellValue = 0;
        int cellLeft, cellRight;

        for (int r = ROWS-1; r >= 0; r-- ) {
            if ((c - 1) >= 0) {
                cellLeft = field[r][c-1];
            }
            else {
                cellLeft = 1; // Non-empty
            }

            if ((c + 1) <= COLS-1) {
                cellRight = field[r][c+1];
            } else {
                cellRight = 1; //Non-empty
            }

            if (cellLeft != 0 && cellRight != 0) {
                int blanksDown = 0;
                blanksDown = this.GetBlanksDownBeforeBlockedForColumn( c, r );
                wellValue += blanksDown;
            }
        }

        return( wellValue );
    }
	
		public int GetBlanksDownBeforeBlockedForColumn (int c, int topRow) { // result range: 0..topY
			 int totalBlanksBeforeBlocked = 0;
	         int cellValue;
	
	         for (int r = topRow; r >= 0; r-- ) {
	             cellValue = field[r][c];
	
	             if (cellValue != 0) {
	                 return totalBlanksBeforeBlocked;
	             } else {
	                 totalBlanksBeforeBlocked++;
	             }
	         }
	
	         return totalBlanksBeforeBlocked;
	     }
}
