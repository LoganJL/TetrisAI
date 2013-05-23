public class PlayerSkeleton {
	public static final int MIN = -2147483648;

	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		
		double best_eval = PlayerSkeleton.MIN;
		int best_move = 0;
		
		//iterate through the moves
		for(int i = 0; i < legalMoves.length; i++){
			double evaluation = evaluateMove(s, legalMoves[i][0],legalMoves[i][1]);

			//Figure out the best move
			if(evaluation>best_eval){
				best_eval = evaluation;
				best_move = i;
			}
		}
		return best_move;
	}
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
		s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}
	
	public double evaluateMove(State board, int orient, int slot){
		
		StateSim temp;
		temp = new StateSim(board);
		temp.makeMove(orient,slot);
		if (temp.hasLost)
			return PlayerSkeleton.MIN;
		
		double clearedRows_Weight = 3.2139831;
		double landingHeight_Weight = -4.7329915;
		double rowTransitions_Weight = -3.0148912;
		double columnTransitions_Weight = -8.5341247;
		double holes_Weight = -7.1114569;
		double wellSums_Weight = -3.2390876;

		double clearedRows = temp.getRowsCleared()-board.getRowsCleared();
		double landingHeight = getLandingHeight(board,temp,orient,slot);
		double rowTransitions = getRowTransitions(temp);
		double columnTransitions = getColumnTransitions(temp);
		double holes = getHoles(temp);
		double wellSums = getWellSums(temp);

		
		double evaluation = clearedRows*clearedRows_Weight
				+landingHeight*landingHeight_Weight
				+rowTransitions*rowTransitions_Weight
				+columnTransitions*columnTransitions_Weight
				+holes*holes_Weight
				+wellSums*wellSums_Weight;
		
	return evaluation;	
	}

	public double getWellSums(StateSim temp){
		double wellSums=0;
		int[][] field = temp.getField();
        int cellLeft, cellRight;
        for (int c = 0; c<10;c++){
	        for (int r = 20; r >= 0; r-- ) {
	            if ((c - 1) >= 0) {
	                cellLeft = field[r][c-1];
	            }
	            else {
	                cellLeft = 1; // Non-empty
	            }
	
	            if ((c + 1) <= 9) {
	                cellRight = field[r][c+1];
	            } else {
	                cellRight = 1; //Non-empty
	            }
	
	            if (cellLeft != 0 && cellRight != 0) {
	                int blanksDown = 0;
	                blanksDown = GetBlanksDownBeforeBlockedForColumn( c, r ,field);
	                wellSums += blanksDown;
	            }
	        }
        }
		
		return wellSums;
	}
	
	public int GetBlanksDownBeforeBlockedForColumn (int c, int topRow,int[][] field) {
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
	
	public double getLandingHeight(State board, StateSim temp, int orient, int slot){
		
		double landingHeight=0;
		int pieceTurn = board.getTurnNumber()+1;
		int [][] currentField = temp.getField();
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
        landingHeight = 0.5 * (double)( pieceMinY + pieceMaxY );
		return landingHeight;
	}
		
	public double getHoles(StateSim board){
		int[][] field = board.getField();
		int row = 20;
		int col = 0;
		double holes = 0;
		boolean cover = false;
		
		for (col=0;col<10;col++){
			cover = false;
			for (row=20;row>-1;row--){
				if ((field[row][col]!=0)==true)
					cover = true;
				if (cover && field[row][col]==0)
					holes++;
			}
		}
		return holes;
	}
	
	public double getColumnTransitions(StateSim board){
		double colTransitions=0;
		boolean last_bit = true;
		int row;
		int col;
		int field[][]=board.getField();
		
		for (col=0;col<10;col++){
			last_bit = true;
			for (row=0;row<field.length;row++){
				if ((field[row][col]!=0)!=last_bit)
					colTransitions++;
				last_bit =(field[row][col]!=0);
			}
		}
		boolean next_bit=false;
		for (col=0;col<10;col++){
			if ((field[20][col]!=0)!=next_bit)
				colTransitions++;
		}
		return colTransitions;
	}
	
	public double getRowTransitions(StateSim board){
		double rowTransitions=0;
		boolean last_bit = true;
		int row;
		int col;
		int[][]field = board.getField();
		
		for (row=0;row<=20;row++){
			last_bit=true;
			for (col=0;col<10;col++){
				if ((field[row][col]!=0)!=last_bit)
					rowTransitions++;
				last_bit=(field[row][col]!=0);
			}
		}
		boolean next_bit=true;
		for (row=0;row<=20;row++){
			if ((field[row][9]!=0)!=next_bit)
				rowTransitions++;
		}
		return rowTransitions;
	}
	
	private class StateSim {
		public final int COLS = 10;
		public final int ROWS = 21;
		public final int N_PIECES = 7;
		public final int ORIENT = 0;
		public final int SLOT = 1;

		private int turn = 0;
		private int cleared = 0;
		protected int nextPiece;

		private int[][] field = new int[ROWS][COLS];
		private int[] top = new int[COLS];
		private boolean hasLost = false;

		protected int[][][] legalMoves = new int[N_PIECES][][];
		protected int[] pOrients = {1,2,4,4,4,2,2};
		protected int[][] pWidth = {
				{2},
				{1,4},
				{2,3,2,3},
				{2,3,2,3},
				{2,3,2,3},
				{3,2},
				{3,2}
		};

		private int[][] pHeight = {
				{2},
				{4,1},
				{3,2,3,2},
				{3,2,3,2},
				{3,2,3,2},
				{2,3},
				{2,3}
		};
		private int[][][] pBottom = {
			{{0,0}},
			{{0},{0,0,0,0}},
			{{0,0},{0,1,1},{2,0},{0,0,0}},
			{{0,0},{0,0,0},{0,2},{1,1,0}},
			{{0,1},{1,0,1},{1,0},{0,0,0}},
			{{0,0,1},{1,0}},
			{{1,0,0},{0,1}}
		};
		private int[][][] pTop = {
			{{2,2}},
			{{4},{1,1,1,1}},
			{{3,1},{2,2,2},{3,3},{1,1,2}},
			{{1,3},{2,1,1},{3,3},{2,2,2}},
			{{3,2},{2,2,2},{2,3},{1,2,1}},
			{{1,2,2},{3,2}},
			{{2,2,1},{2,3}}
		};

		public StateSim(State s){
			
			this.turn=s.getTurnNumber();
			
			for(int i = 0; i < N_PIECES; i++) {
				int n = 0;
				for(int j = 0; j < pOrients[i]; j++) {
					n += COLS+1-pWidth[i][j];
				}
				legalMoves[i] = new int[n][2];
				n = 0;
				for(int j = 0; j < pOrients[i]; j++) {
					for(int k = 0; k < COLS+1-pWidth[i][j];k++) {
						legalMoves[i][n][ORIENT] = j;
						legalMoves[i][n][SLOT] = k;
						n++;
					}
				}
			}

			nextPiece = s.getNextPiece();

			int currentLegalMoves[][] = s.legalMoves();
			for(int i = 0; i < this.legalMoves[nextPiece].length;i++)
				for(int j = 0; j < this.legalMoves[nextPiece][i].length;j++)
					this.legalMoves[nextPiece][i][j] = currentLegalMoves[i][j];

			int currentField[][] = s.getField();
			for(int i = 0; i < ROWS;i++)
				for(int j = 0; j < COLS;j++)
					this.field[i][j] = currentField[i][j];

			int currentTop[] = s.getTop();
			for(int i = 0; i  < currentTop.length; i++)
				this.top[i] = currentTop[i];
		}

		public int[][] getField() {
			return field;
		}

		public int[] getTop() {
			return top;
		}

		public int getRowsCleared() {
			return cleared;
		}

		public void makeMove(int[] move) {
			makeMove(move[ORIENT],move[SLOT]);
		}

		public void makeMove(int orient, int slot) {
			turn++;
			int height = top[slot]-pBottom[nextPiece][orient][0];
			for(int c = 1; c < pWidth[nextPiece][orient];c++) {
				height = Math.max(height,top[slot+c]-pBottom[nextPiece][orient][c]);
			}

			if(height+pHeight[nextPiece][orient] >= ROWS){
				hasLost = true;
				return;
			}


			for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
				for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
					field[h][i+slot] = turn;
				}
			}

			for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
				top[slot+c]=height+pTop[nextPiece][orient][c];
			}

			int rowsCleared = 0;

			for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
				boolean full = true;
				for(int c = 0; c < COLS; c++) {
					if(field[r][c] == 0) {
						full = false;
						break;
					}
				}

				if(full) {
					rowsCleared++;
					cleared++;
					for(int c = 0; c < COLS; c++) {
						for(int i = r; i < top[c]; i++)
							field[i][c] = field[i+1][c];
						top[c]--;
						while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
					}
				}
			}
		}		
	}	
}
	
