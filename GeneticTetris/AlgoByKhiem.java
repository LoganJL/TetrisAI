import java.util.*;

public class AlgoByKhiem {

  // public static TetrisLogger LOGGER = PlayerSkeleton.LOGGER;
  public static int ROWS = State.ROWS;
  public static int COLUMNS = State.COLS;

  // ---------------------------------------------------------------------

  // whole code taken from State.makeMove(int, int)
  private static int[][] grid_after_move(int move, State s, int[][] legal_moves) {
    // TODO Auto-generated method stub
    int[][] tempField = new int[s.ROWS][s.COLS];
    int curOrient = legal_moves[move][0];
    int curSlot = legal_moves[move][1];
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

  public static int[][] clone_array(int[][] source) {
    int length = source.length;
    int[][] target = new int[length][source[0].length];
    for (int i = 0; i < length; i++) {
        System.arraycopy(source[i], 0, target[i], 0, source[i].length);
    }
    return target;
  }

  // a wrapper to be able to write meaningful code
  public static boolean is_empty(int cell_value) {    
    return (cell_value == 0);
  }

  // ---------------------------------------------------------------------

  public static int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79};

  // ---------------------------------------------------------------------

  // the height of the column (0 if there's no brick)
  public static int height_of_column(int[][] grid, int column) {
    int highest_row = ROWS-1;
    
    while ( highest_row >= 0 && is_empty(grid[highest_row][column]) ){
      highest_row--; //start with ROWS-1
    }

    if ( highest_row >= 0 )
      return ( highest_row + 1 );
    else
      return 0;
  }

  // highest column's height
  public static int max_grid_height(int[][] grid) {
    int max_grid_height = height_of_column(grid, 0); //take col no.0 to be highest by default
    for( int column = 1; column < COLUMNS; column++ ) {
      int column_height = height_of_column(grid, column);
      if (column_height > max_grid_height)
        max_grid_height = column_height;
    }
    // max_grid_height shows the higest column now

    return max_grid_height;
  }

  // ---------------------------------------------------------------------

  public static int max_column_height_penalty(int[][] grid) {
    int penalty = 0;

    for (int column = 0; column < COLUMNS; column++)
      penalty -= height_of_column(grid, column);

    return penalty;
  }

  // ---------------------------------------------------------------------

  // penalize high grid
  public static int height_penalty(int[][] grid) {
    int height_value = 0;

    for (int row = 0; row < ROWS; row++)
      for (int column = 0; column < COLUMNS; column++)
        if ( !is_empty(grid[row][column]) )
          height_value -= primes[row + 1];

    return height_value;
  }

  // ---------------------------------------------------------------------

  // reward when a row is cleared
  public static int row_cleared_reward(int[][] old_grid, int[][] new_grid) {
    int old_grid_max_height = max_grid_height(old_grid);
    int new_grid_max_height = max_grid_height(new_grid);
    
    if (new_grid_max_height < old_grid_max_height)
      return ( old_grid_max_height - new_grid_max_height );
    else
      return 0;
  }

  // ---------------------------------------------------------------------

  // penalize wholes in the grid
  public static int hole_penalty(int[][] grid) {
    int penalty = 0;

    for (int column = 0; column < COLUMNS; column++ ) {
      int top_row = height_of_column(grid, column);
      for (int row = top_row-2; row >= 0; row--) {        
        if (is_empty(grid[row][column])) {
          penalty -= top_row - (row + 1);
        }
      }
    }

    return penalty;
  }

  // ---------------------------------------------------------------------

  
  public static int rough_surface_penalty(int[][] grid) {
    int rough_surface_penalty = 0;

    int[] column_height = new int[COLUMNS];
    for( int column = 0; column < COLUMNS; column++ ) {
      column_height[column] = height_of_column(grid, column);
      // LOGGER.debug( String.format("Column %s, height = %s", column, column_height[column]) );
    }

    for( int column = 1; column < COLUMNS; column++ ) {
      int difference = Math.abs(column_height[column] - column_height[column-1]);
      if (difference > 2)
        rough_surface_penalty -= difference;
    }

    return rough_surface_penalty;
  }

  // ---------------------------------------------------------------------
  public static Double evaluate(int move, State gameState, int[][] legal_moves, List<Double> dna) {
    Double move_value;

    int[][] next_grid = grid_after_move(move, gameState, legal_moves);
    if (next_grid == null) {
      move_value = Double.NEGATIVE_INFINITY; // large penalty for losing
      // LOGGER.debug("GAME OVER");
    }
    else {
        // System.out.println( String.format("Height P: %s", (double)height_penalty(next_grid)) );
        // System.out.println( String.format("Hole P: %s", (double)hole_penalty(next_grid)) );
        // System.out.println( String.format("Max grid P: %s", (double)max_column_height_penalty(next_grid)) );
        // for( Double dna_item : dna) {
        //   System.out.println( String.format("DNA: %s", dna_item) );
        // }
        move_value = (double)height_penalty(next_grid) * dna.get(0)
                      + (double)hole_penalty(next_grid) * dna.get(1)
                      + (double)max_column_height_penalty(next_grid) * dna.get(2);
                      
        // LOGGER.debug( String.format("No.%-4s | Height_P: %-10s, Hole_P: %-10s, Max_col_P: %-10s, Total: %-10s", move, height_penalty(next_grid), hole_penalty(next_grid)*10, max_column_height_penalty(next_grid), move_value) );

      // move_value = dna.get(0)*height_penalty(next_grid) + dna.get(1)*row_cleared_reward(gameState.getField(), next_grid) + dna.get(2)*hole_penalty(next_grid) + dna.get(3)*rough_surface_penalty(next_grid);
      // LOGGER.debug(next_grid);
      // if (max_grid_height(next_grid) < 10) {
      //   move_value = (double)height_penalty(next_grid) + (double)hole_penalty(next_grid)*10 + (double)max_column_height_penalty(next_grid);
      //   LOGGER.debug( String.format("No.%-4s | Height_P: %-10s, Hole_P: %-10s, Max_col_P: %-10s, Total: %-10s", move, height_penalty(next_grid), hole_penalty(next_grid)*10, max_column_height_penalty(next_grid), move_value) );
      // }
      // else {
      //   move_value = (double)height_penalty(next_grid)*5 + (double)hole_penalty(next_grid) + (double)max_column_height_penalty(next_grid)*5;
      //   LOGGER.debug( String.format("No.%-4s | Height_P: %-10s, Hole_P: %-10s, Max_col_P: %-10s, Total: %-10s", move, height_penalty(next_grid)*5, hole_penalty(next_grid), max_column_height_penalty(next_grid)*5, move_value) );
      // }
    }

    // LOGGER.debug("Possible move no." +  Integer.toString(move) + ", Value= " + Double.toString(move_value));

    return move_value;
  }

  public static int pickMove(State gameState, int[][] legal_moves, List<Double> weights) {
    
    // LOGGER.debug(gameState.getField());
    // LOGGER.debug("-------------------------------------------------------------------");

    int no_of_legal_moves = legal_moves.length;
    
    // pick move 0 by default
    int picked_move = 0;
    Double picked_value = Double.NEGATIVE_INFINITY;
    // evaluate(0, gameState, legal_moves, weights);

    // pick other moves if it's better
    for( int i = 0; i < no_of_legal_moves; i++ ) {
      Double next_grid_value = evaluate(i, gameState, legal_moves, weights);
      // LOGGER.debug("\n");

      if ( next_grid_value > picked_value ) {
        picked_value = next_grid_value;
        picked_move = i;
      }
    }


    // LOGGER.info("Picked move no." + Integer.toString( picked_move ));
    // LOGGER.debug("*******************************************************************");
    // LOGGER.debug("*******************************************************************");

    return picked_move;
  }

}