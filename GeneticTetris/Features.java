import java.util.*;

public class Features {
    // public static TetrisLogger LOGGER = PlayerSkeleton.LOGGER;
    public static int ROWS = State.ROWS;
    public static int COLUMNS = State.COLS;

    public static int pickMove(State gameState, int[][] legal_moves, List<Double> weights) {
    // public static int pickMove(State gameState, int[][] legal_moves, List<Double> weights, TetrisLogger logger) {
    
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
            // Double next_grid_value = evaluate(i, gameState, legal_moves, weights, logger);
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

    public static double evaluate(int move, State gameState, int[][] legal_moves, List<Double> weights) {
    // public static double evaluate(int move, State gameState, int[][] legal_moves, List<Double> weights, TetrisLogger logger) {
        double move_value = 0;

        // make a copy of the old grid
        int[][] old_grid = new int[ROWS][COLUMNS];
        for (int row=0; row<ROWS; row++)
            for (int column=0; column<COLUMNS; column++) 
                old_grid[row][column] = gameState.getField()[row][column];

        // find the result after making a move
        int[][] next_grid = grid_after_move(move, gameState, legal_moves);
        if (next_grid == null) {
            move_value = Double.NEGATIVE_INFINITY; // large penalty for losing
            // LOGGER.debug("GAME OVER");
        } else {
            for (int i=0; i < weights.size(); i++) {
                move_value += getFeatureValue(old_grid, next_grid, i, weights.get(i));
            }
            //LOGGER.debug( String.format("No.%-4s | Height_P: %-10s, Hole_P: %-10s, Max_col_P: %-10s, Total: %-10s", );
        }
        return move_value;
    }

    // -------------------------------------------------------------------------------------

    // pile height: the row of the highest cell on the board
    // by Khiem
    public static int max_column_height_penalty(int[][] grid) {
        return ( -1 * max_grid_height(grid) );
    }

    // pile height: the row of the highest cell on the board
    // by Khiem
    public static int column_height_penalty(int[][] grid) {
        int penalty = 0;

        for (int column = 0; column < COLUMNS; column++ ) {
            penalty -= height_of_column(grid, column);
        }

        return penalty;
    }

    // count the no of holes (empty cells with at least one filled cell above)
    // by Khiem
    public static int hole_count_penalty(int[][] grid) {
        int count = 0;

        for (int column = 0; column < COLUMNS; column++ ) {
            int top_row = height_of_column(grid, column); // the row of the highest filled cell
            for (int row = top_row-2; row >= 0; row--) { // start from the cell below that -> bottom
                if (is_empty(grid[row][column])) {
                    count += 1;
                }
            }
        }

        return ( -1 * count );
    }

    // each holes (see definition above) receive a penalty = how many cells () above it
    // by Khiem
    public static int weighted_hole_penalty(int[][] grid) {
        int penalty = 0;

        for (int column = 0; column < COLUMNS; column++ ) {
            int top_row = height_of_column(grid, column); // the row of the highest filled cell
            for (int row = top_row-2; row >= 0; row--) { // start from the cell below that -> bottom
                if (is_empty(grid[row][column])) {
                    penalty -= top_row - row - 1;
                }     
            }
        }

        return penalty;
    }

    // compare the old & new grid and see how many lines was cleared, based on the max grid height
    // by Khiem
    public static int row_cleared_reward(int[][] old_grid, int[][] new_grid) {
        int old_grid_max_height = max_grid_height(old_grid);
        int new_grid_max_height = max_grid_height(new_grid);
        
        if (new_grid_max_height < old_grid_max_height)
            return ( old_grid_max_height - new_grid_max_height );
        else
            return 0;
    }

    // the difference between the highest filled cell and the lowest empty celll
    // by Khiem
    public static int altitude_difference_penalty(int[][] grid) {
        int penalty = 0;
        int highest_filled_height = 0, lowest_empty_height = ROWS;

        for (int column = 0; column < COLUMNS; column++ ) {
            int top_row = height_of_column(grid, column); // the row of the highest filled cell
            
            if (top_row > highest_filled_height)
                highest_filled_height = top_row;
            
            if (top_row + 1 < lowest_empty_height)
                lowest_empty_height = top_row + 1;
        }

        return -1 * (highest_filled_height - lowest_empty_height + 1);
    }

    // the height of the deepest well in the grid
    // by Khiem
    public static int max_well_depth_penalty(int[][] grid) {
        return ( -1 * max_well_depth(grid) );
    }

    // penalize each well (using its height)
    // by Khiem
    public static int well_depth_penalty(int[][] grid) {
        int penalty = 0;

        for (int column = 0; column < COLUMNS; column++ ) {
            penalty -= well_depth_of_column(grid, column);
        }

        return penalty;
    }

    // count the no of filled cells
    // by Khiem
    public static int cell_count_penalty(int[][] grid) {
        int count = 0;

        for (int column = 0; column < COLUMNS; column++ ) {
            int top_row = height_of_column(grid, column); // the row of the highest filled cell
            for (int row = top_row-1; row >= 0; row--) {
                if (!is_empty(grid[row][column]))
                    count += 1;
            }
        }

        return -1 * count;
    }

    // each filled cell now has the weight of the row it's in
    // by Khiem
    public static int weighted_cell_count_penalty(int[][] grid) {
        int penalty = 0;

        for (int column = 0; column < COLUMNS; column++ ) {
            int top_row = height_of_column(grid, column); // the row of the highest filled cell
            for (int row = top_row-1; row >= 0; row--) {
                if (!is_empty(grid[row][column]))
                    penalty -= row+1;
            }
        }

        return penalty;
    }

    // sum of all transition from filled -> empty on the rows
    // by Khiem
    public static int row_transition_penalty(int[][] grid) {
        int penalty = 0;

        for (int row = 0; row < ROWS; row++) {
            boolean is_filled = true;
            for (int column = 0; column < COLUMNS; column++ ) {
                if (!is_empty(grid[row][column])) {
                    if (!is_filled) {
                        // System.out.println( String.format("[%s, %s]", row, column) );
                        penalty -= 1;
                    }

                    is_filled = true;
                }
                else { // to the left, there was a filled cell -> transition
                    if (is_filled) {
                        // System.out.println( String.format("[%s, %s]", row, column) );
                        penalty -= 1;
                    }

                    is_filled = false;
                }
            }
        }

        return penalty;
    }

    // sum of all transition from filled -> empty on the columns
    // by Khiem
    public static int column_transition_penalty(int[][] grid) {
        int penalty = 0;

        for (int column = 0; column < COLUMNS; column++ ) {
            boolean is_filled = true;
            for (int row = 0; row < ROWS; row++) {
                if (!is_empty(grid[row][column])) {
                    if (!is_filled) {
                        // System.out.println( String.format("[%s, %s]", row, column) );
                        penalty -= 1;
                    }

                    is_filled = true;
                }
                else { // to the left, there was a filled cell -> transition
                    if (is_filled) {
                        // System.out.println( String.format("[%s, %s]", row, column) );
                        penalty -= 1;
                    }

                    is_filled = false;
                }
            }
        }

        return penalty;
    }

    // -------------------------------------------------------------------------------------

    private static double getFeatureValue(int[][] old_grid, int[][] next_grid, int feature_index, Double weight) {
        double value = 0;

        switch(feature_index) {
            case(0):
                value = (double) (max_column_height_penalty(next_grid)) * weight/21;
                break;
            case(1):
                value = (double) column_height_penalty(next_grid) * weight/210; 
                break;
            case(2):
                value = (double) hole_count_penalty(next_grid) * weight/180;
                break;
            case(3):
                value = (double) weighted_hole_penalty(next_grid) * weight/1890;
                break;
            case(4):
                value = (double) row_cleared_reward(old_grid, next_grid) * weight/4;
                break;
            case(5):
                value = (double) altitude_difference_penalty(next_grid) * weight/21;
                break;
            case(6):
                value = (double) max_well_depth_penalty(next_grid) * weight/21;
                break;
            case(7):
                value = (double) well_depth_penalty(next_grid) * weight/105;
                break;
            case(8):
                value = (double) cell_count_penalty(next_grid) * weight/210;
                break;
            case(9):
                value = (double) weighted_cell_count_penalty(next_grid) * weight/2310;
                break;
            case(10):
                value = (double) row_transition_penalty(next_grid) * weight/200;
                break;
            case(11):
                value = (double) column_transition_penalty(next_grid) * weight/205;
                break;
            default:
                value = 0;
        }
        
        return value;
    }

    // // penalize high grid
    // public static int height_penalty_by_Khiem(int[][] grid) {
    //     int height_value = 0;

    //     for (int row = 0; row < ROWS; row++)
    //         for (int column = 0; column < COLUMNS; column++)
    //             if ( !is_empty(grid[row][column]) )
    //                 height_value -= primes[row + 1];

    //     return height_value;
    // }

    

    //     return penalty;
    // }

    // public static int max_column_height_penalty_by_Khiem(int[][] grid) {
    //     int penalty = 0;

    //     for (int column = 0; column < COLUMNS; column++)
    //         penalty -= height_of_column(grid, column);

    //     return penalty;
    // }

    // public static int rough_surface_penalty_by_Khiem(int[][] grid) {
    //     int rough_surface_penalty = 0;

    //     int[] column_height = new int[COLUMNS];
    //     for( int column = 0; column < COLUMNS; column++ ) {
    //         column_height[column] = height_of_column(grid, column);
    //         // LOGGER.debug( String.format("Column %s, height = %s", column, column_height[column]) );
    //     }

    //     for( int column = 1; column < COLUMNS; column++ ) {
    //         int difference = Math.abs(column_height[column] - column_height[column-1]);
    //         if (difference > 2)
    //             rough_surface_penalty -= difference;
    //     }

    //     return rough_surface_penalty;
    // }

    

    // public static int max_grid_height_penalty_by_Khiem(int[][] grid) {
    //     int max_grid_height = height_of_column(grid, 0); //take col no.0 to be highest by default
    //     for( int column = 1; column < COLUMNS; column++ ) {
    //         int column_height = height_of_column(grid, column);
    //         if (column_height > max_grid_height)
    //             max_grid_height = column_height;
    //     }
    //     // max_grid_height shows the higest column now

    //     return max_grid_height;
    // }

    // public static int height_penalty_by_Tho(int[][] grid) {
    //     int penalty = 0;

    //     for (int row = 0; row < ROWS; row++)
    //         for (int column = 0; column < COLUMNS; column++)
    //             if ( !is_empty(grid[row][column]) ) {
    //                 if (row<8) {
    //                     penalty = (-1) * (row+1);
    //                 } else {
    //                     penalty = (-2) * (row+1);
    //                 }
    //             }
    //     return penalty;
    // }

    // public static int hole_penalty_by_Tho(int[][] grid) {
    //     int penalty = 0;

    //     for (int column=0; column<COLUMNS; column++) {
    //         int top_row = height_of_column(grid, column);
    //         for (int row = top_row-2; row >= 0; row--) {        
    //             if (is_empty(grid[row][column])) {
    //                 penalty -= (-1) * (int)(Math.pow((row+1), 4));
    //             }     
    //         }
    //     }
    //     return penalty;
    // }

    // public static int pattern_penalty_by_Tho(int[][] grid) {
    //     int penalty = 0;

    //     int[] top = new int[COLUMNS];
    //     for (int column=0; column<COLUMNS; column++) {
    //         top[column] = height_of_column(grid, column);
    //     }   

    //     for (int column=0; column<COLUMNS; column++) {
    //         if (column!=0) {
    //             if (Math.abs(top[column]-top[column-1])> 2 
    //                 && Math.abs(top[column]-top[column-1])<4) {
    //                 penalty -= (int) Math.pow(Math.abs(top[column]-top[column-1]), 2);
    //             } else if (Math.abs(top[column]-top[column-1])>=4) {
    //                 penalty -= (int)Math.pow(Math.abs(top[column]-top[column-1]), 4);
    //             }
    //         }  

    //         if (column!=COLUMNS-1) {
    //             if (Math.abs(top[column]-top[column+1])> 2 
    //                 && Math.abs(top[column]-top[column+1])<4) {
    //                 penalty -= (int) Math.pow(Math.abs(top[column]-top[column+1]), 2);
    //             } else if (Math.abs(top[column]-top[column+1])>=4) {
    //                 penalty -= (int)Math.pow(Math.abs(top[column]-top[column+1]), 4);
    //             }
    //         }        
    //     }

    //     return penalty;   
    // }

    // public static int cell_relationship_penalty_by_Tho(int[][] grid) {
    //     int penalty = 0;

    //     for (int row=0; row<ROWS; row++) {
    //         for (int column=0; column<COLUMNS; column++) {
    //             if (!is_empty(grid[row][column])) {
    //                 if (row>0) {
    //                     if (is_empty(grid[row-1][column])) {
    //                         penalty -=10;
    //                     }
    //                 }
    //                 if (column>0) {
    //                     if (is_empty(grid[row][column-1])) {
    //                         penalty -= 1;
    //                     }
    //                 }
    //                 if (column<COLUMNS-1) {
    //                     if (is_empty(grid[row][column+1])) {
    //                         penalty -= 1;
    //                     }
    //                 }
    //             }
    //         }
    //     }

    //     return penalty;
    // }

    // -----------------------------------------------------------------------------
    // Support functions

    public static int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79};

    private static boolean is_empty(int cell_value) {    
        return (cell_value == 0);
    }

    private static int height_of_column(int[][] grid, int column) {
        int highest_row = ROWS-1;
    
        while ( highest_row >= 0 && is_empty(grid[highest_row][column]) ){
            highest_row--; //start with ROWS-1
        }

        if ( highest_row >= 0 )
            return ( highest_row + 1 );
        else
        return 0;
    }

    private static int well_depth_of_column(int[][] grid, int column) {
        int left_side_height, right_side_height, column_height;

        if (column == 0) {
            left_side_height = ROWS;
            right_side_height = height_of_column(grid, column+1);
        } else if (column == COLUMNS-1) {
            left_side_height = height_of_column(grid, column-1);
            right_side_height = ROWS;
        } else {
            left_side_height = height_of_column(grid, column-1);
            right_side_height = height_of_column(grid, column+1);
        }

        column_height = height_of_column(grid, column);
        if (column_height < Math.min(left_side_height, right_side_height))
            return Math.min(left_side_height, right_side_height) - column_height;
        else
            return 0;
    }

    private static int max_grid_height(int[][] grid) {
        int max_grid_height = height_of_column(grid, 0); //take col no.0 to be highest by default
        for( int column = 1; column < COLUMNS; column++ ) {
            int column_height = height_of_column(grid, column);
            if (column_height > max_grid_height)
                max_grid_height = column_height;
        }
        // max_grid_height = the row of the highest cell

        return max_grid_height;
    }

    private static int max_well_depth(int[][] grid) {
        int max_well_depth = well_depth_of_column(grid, 0); //take col no.0 to be highest by default
        for( int column = 1; column < COLUMNS; column++ ) {
            int well_depth = well_depth_of_column(grid, column);
            if (well_depth > max_well_depth)
                max_well_depth = well_depth;
        }

        return max_well_depth;
    }

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
                //from bottom to top of cell
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
                //slide down all cells
                    for(int i = r; i < curTop[c]; i++) {
                    tempField[i][c] = tempField[i+1][c];
                    }
                }
            }   
        }
        return tempField;
    }
}