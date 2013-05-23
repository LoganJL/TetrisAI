
class AlgoByTho {

  public static int pickMove(State gameState, int[][] legalMoves, int method) {
    int[][] nextField = new int[gameState.ROWS][gameState.COLS]; //field == game grid
    double weight = -1000000000;
    int action = 0;

    // for each legal move of this state
    for (int m=0; m<legalMoves.length; m++) {
      // create the corresponding next state's field
      nextField = updateNextField(gameState, legalMoves, m);
      
      //check if game ends 
      if (nextField != null) {
        // evaluate the weight of the next state
        double thisWeight = evaluateState(nextField, gameState.ROWS, gameState.COLS, method);
        
        //System.out.printf("%d\n", thisWeight);
        // choose action base on the weight of the next state
        if (weight<=thisWeight) {
          weight = thisWeight;
          action = m;
        }
      } else 
        return 0;
    }

    return action;
  }
  
  // whole code taken from State.makeMove(int, int)
  private static int[][] updateNextField(State s, int[][] legalMoves, int move) {
    // TODO Auto-generated method stub
    int[][] tempField = new int[s.ROWS][s.COLS];
    int curOrient = legalMoves[move][0];
    int curSlot = legalMoves[move][1];
    int curTurn = s.getTurnNumber()+1;
    int[] curTop = new int[s.COLS];
    
    // update the current state's field and top
    for (int i=0; i<s.ROWS; i++)
      for (int j=0; j<s.COLS; j++) 
        tempField[i][j] = s.getField()[i][j];
    
    for (int i=0; i<s.COLS; i++) {
      curTop[i] = s.getTop()[i];
    }
    
    //height if the first column makes contact
    int height = s.getTop()[curSlot]-s.getpBottom()[s.getNextPiece()][curOrient][0];
    //for each column beyond the first in the piece
    for(int c = 1; c < s.getpWidth()[s.getNextPiece()][curOrient];c++) {
      height = Math.max(height, s.getTop()[curSlot+c]
          -s.getpBottom()[s.getNextPiece()][curOrient][c]);
    }
    //check if game ends
    if (height+s.getpHeight()[s.getNextPiece()][curOrient]>=s.ROWS)
      return null;
    
    //for each column in the piece - fill in the appropriate blocks
    for(int i = 0; i < s.getpWidth()[s.getNextPiece()][curOrient]; i++) {
          
      //from bottom to top of brick
      for(int h = height+s.getpBottom()[s.getNextPiece()][curOrient][i]; 
          h < height+s.getpTop()[s.getNextPiece()][curOrient][i]; h++) {
        tempField[h][i+curSlot] = curTurn;
      }
    }
        
    //adjust top
    for(int c = 0; c < s.getpWidth()[s.getNextPiece()][curOrient]; c++) {
      curTop[curSlot+c]=height+s.getpTop()[s.getNextPiece()][curOrient][c];
    }
    
    //check for full rows - starting at the top
    for(int r = height+s.getpHeight()[s.getNextPiece()][curOrient]-1;
        r >= height; r--) {
      //check all columns in the row
      boolean full = true;
      for(int c = 0; c < s.COLS; c++) {
        if(tempField[r][c] == 0) {
          full = false;
          break;
        }
      }
      //if the row was full - remove it and slide above stuff down
      if(full) {
        //for each column
        for(int c = 0; c < s.COLS; c++) {
          //slide down all bricks
          for(int i = r; i < curTop[c]; i++) {
            tempField[i][c] = tempField[i+1][c];
          }
        }
      }   
    }
    
    return tempField;
  }
  
  private static double evaluateState(int[][] nextField, int rows, int cols, int method) {
    // TODO Auto-generated method stub
    double stateWeight = 0;
    int[] top = new int[cols];
    //printState(nextField, rows, cols);
    for (int i=0; i<cols; i++) {
      for (int j=rows-1; j>=0; j--) {
        if (nextField[j][i]!=0 && top[i]<j) 
          top[i] = j+1;
      }
    }
    
    for (int i=0; i<rows; i++) {
      for (int j=0; j<cols; j++) {
        switch(method) {
          case 1:
            stateWeight += evaluateBlock1(nextField, i, j, rows, cols, top);
            break;
          case 2:
            stateWeight += evaluateBlock2(nextField, i, j, rows, cols, top);
            break;
          case 3:
            stateWeight += evaluateBlock3(nextField, i, j, rows, cols, top);
            break;
        }
      }
    }
  
    return stateWeight;
  }

  private static double evaluateBlock1(int[][] nextField, int i, int j, int rows, int cols, int[] top) {
    // TODO Auto-generated method stub
    double blockWeight = 0;
    
    int maxTop = 0;
    for (int c=0; c<cols; c++) {
      maxTop = Math.max(top[c], maxTop);
    }
    
    // get penalty for height
    double heightPenalty = 0;
    if (nextField[i][j]!=0) {
      if (i<4) {
        heightPenalty = (-20) * Math.pow(i+1, 1);
      } else if (i<8){
        heightPenalty = (-20) * Math.pow(i+1, 1);
      } else {
        heightPenalty = (-40) * Math.pow(i+1, 1);
      }
      int highestHole= 0;
      for (int c=0; c<i; c++) {
        if (nextField[c][j]==0) { // have block any unoccupied block
          highestHole = c;
        }
      }
      //heightPenalty -=  (int)Math.pow(i-highestHole, 4);
    } else {
      if (i<top[j]) 
        heightPenalty = (-10) * Math.pow((i+1), 4);
    } 
    
    // get penalty for surface pattern
    double patternPenalty = 0;
    if (nextField[i][j]!=0){
      if (i==top[j]-1) { // on surface 
        if (j!=0) { // not right-most column
          if (Math.abs(top[j-1]-i-1)> 2 && Math.abs(top[j-1]-i-1)<4) {
            patternPenalty -= 20 * Math.pow(top[j-1]-i-1, 2);
          } else if (Math.abs(top[j-1]-i-1)>=4) {
            patternPenalty -= 40 * Math.pow(Math.abs(top[j-1]-i-1), 4);
          }
        } 
        
        if (j!=9){ // not left-most column
          if (Math.abs(top[j+1]-i-1)> 2 && Math.abs(top[j+1]-i-1)<4) {
            patternPenalty -= 20 * Math.pow(top[j+1]-i-1, 2);
          } else if (Math.abs(top[j+1]-i-1)>=4) {
            patternPenalty -= 40 * Math.pow(Math.abs(top[j+1]-i-1), 4);
          }
        }
      }
    }
    blockWeight += heightPenalty + patternPenalty;
    
    return blockWeight;
  }

  private static int evaluateBlock2(int[][] nextField, int i, int j, int rows, int cols, int[] top) { 
    int blockWeight = 0;
    
    int heightPenalty= 0; // penalty for height
    int holePenalty = 0; // penalty for hole
    int blockagePenalty = 0; // penalty for blocking any unoccupied block

    int x = -20;
    int y = -2;
    int z = -100;
    int w = 4;
    
    int maxTop = 0;
    for (int c=0; c<cols; c++) {
      maxTop = Math.max(top[c], maxTop);
    }
    
    if (nextField[i][j]!=0) { // occupied block
      // calculate penalty for height
      heightPenalty = x * (int)Math.pow(i+1, 1);
      
      // calculate penalty for blockage
      for (int c=0; c<i; c++) {
        if (nextField[c][j]==0) {
          // punish for each unoccupied block under this block
          blockagePenalty += y * (int)Math.pow(c+1, 1); 
        }
      }
      
      // calculate penalty for hole (compare to 2 columns next to this column) 
      int depth = maxTop-i-1;
      if (depth>0) {
        holePenalty = z * depth; 
      }
    } else { // unoccupied block
      if (i<top[j]) { // be under another occupied block
        // calculate penalty for height
        heightPenalty = x * (i+1);
        
        // calculate penalty for hole
        // the higher the hole is, the more it will be punished
        holePenalty = z * (int)Math.pow(i+1, 1);
        // the more occupied blocks are over it, the more it will be punished
        for (int c=top[j]; c>i; c--) {
          if (nextField[c][j]!=0) {
            holePenalty += z * Math.pow(c+1, 2);
          }
        }
      } else { // free block
        if (i>maxTop-1) {
        // the lower it is, the higher reward it gets
          //heightPenalty = w * (rows-i);
        } else {
          heightPenalty = x * (int)Math.pow(maxTop-i-1, 1); 
        }
      }
      
    }
    
    blockWeight = heightPenalty + holePenalty + blockagePenalty;
    
    return blockWeight;
  }
  
  
  // this method calculate block weight using the side of the cell
  // Concisely, it only counts the side of an occupied cell with another unoccupied
  private static int evaluateBlock3(int[][] nextField, int i, int j, int rows, int cols, int[] top) { 
    // TODO Auto-generated method stub
    int blockWeight = 0;
    int downPenalty = -10;
    int rightPenalty = -1;
    int leftPenalty = -1;
    
    // if this cell is occupied
    if (nextField[i][j]!=0) {
      // if this cell is not in the bottom
      if (i>0) {
        // if there is unoccupied cell under it
        if (nextField[i-1][j]==0) {
          blockWeight += downPenalty; 
        }
      }
      // if this cell is not next to the right border
      if (j<9) {
        if (nextField[i][j+1]==0) {
          blockWeight += rightPenalty;
        }
      }
      // if this cell is not next to the left border
      if (j>0) {
        if (nextField[i][j-1]==0) {
          blockWeight += leftPenalty;
        }
      }
    }
    
    return blockWeight;
  }
}