public class UnitTestByKhiem {

  public static int ROWS = State.ROWS;
  public static int COLUMNS = State.COLS;
  public static TetrisLogger LOGGER = new TetrisLogger("unit_test");

  public static int[][] grid1() {
    int[][] test_grid = new int[ROWS][COLUMNS];

    for (int i = 0; i < ROWS; i++)
      for (int j = 0; j < COLUMNS; j++)
        test_grid[i][j] = 0;

    for (int i = 0; i < 4 ; i++)
      for (int j = 0; j < COLUMNS; j++)
        test_grid[i][j] = 1;
    test_grid[4][3] = 1; test_grid[5][3] = 1; test_grid[6][3] = 1;
    test_grid[4][8] = 1; test_grid[5][8] = 1;

    return test_grid;
  }

  public static int[][] grid2() {
    int[][] test_grid = new int[ROWS][COLUMNS];

    for (int i = 0; i < ROWS; i++)
      for (int j = 0; j < COLUMNS; j++)
        test_grid[i][j] = 0;

    for (int i = 0; i < 4 ; i++)
      for (int j = 0; j < COLUMNS; j++)
        test_grid[i][j] = 1;
    test_grid[1][0] = 0;
    test_grid[4][3] = 0; test_grid[5][3] = 1; test_grid[6][3] = 1;
    test_grid[4][8] = 0; test_grid[5][8] = 1;
    test_grid[1][2] = 1;
    test_grid[0][9] = 0; test_grid[1][9] = 0; test_grid[2][9] = 0; test_grid[3][9] = 0;

    return test_grid;
  }

  public static int[][] grid3() {
    int[][] test_grid = new int[ROWS][COLUMNS];

    for (int i = 0; i < ROWS; i++)
      for (int j = 0; j < COLUMNS; j++)
        test_grid[i][j] = 0;

    for (int i = 0; i < 2 ; i++)
      for (int j = 0; j < COLUMNS; j++)
        test_grid[i][j] = 1;
    test_grid[2][3] = 1; test_grid[3][3] = 1; test_grid[4][3] = 1;
    test_grid[2][8] = 1; test_grid[3][8] = 1;

    return test_grid;
  }

  public static int[][] grid4() {
    int[][] test_grid = new int[ROWS][COLUMNS];

    for (int i = 0; i < ROWS; i++)
      for (int j = 0; j < COLUMNS; j++)
        test_grid[i][j] = 0;

    for (int i = 0; i < 4 ; i++)
      for (int j = 0; j < COLUMNS; j++)
        test_grid[i][j] = 1;
    test_grid[0][2] = 0; test_grid[1][2] = 0; test_grid[2][2] = 0; test_grid[3][2] = 0;
    test_grid[4][4] = 1; test_grid[5][4] = 1; test_grid[6][4] = 1; test_grid[7][4] = 1;
    test_grid[4][6] = 1; test_grid[5][6] = 1;
    test_grid[3][1] = 0;

    return test_grid;
  }

  public static void main(String[] args) {
    
    // randomization test
    // for (int i=0; i<30; i++)
    //   LOGGER.info( String.format("%s", (int)(Math.random() * 5)) );

    int[][] grid = grid2();
    int[][] another_grid = grid3();

    LOGGER.info(grid);
    // LOGGER.info(another_grid);

    // Features count: 12

    int max_column_height_p = Features.max_column_height_penalty(grid);
    LOGGER.info( String.format("Max column height P: %s", max_column_height_p) );

    int column_height_p = Features.column_height_penalty(grid);
    LOGGER.info( String.format("Column height P: %s", column_height_p) );

    int hole_count_p = Features.hole_count_penalty(grid);
    LOGGER.info( String.format("Hole count P: %s", hole_count_p) );

    int weighted_hole_p = Features.weighted_hole_penalty(grid);
    LOGGER.info( String.format("Weighted hole P: %s", weighted_hole_p) );

    int row_cleared_r = Features.row_cleared_reward(grid, another_grid);
    LOGGER.info( String.format("Row cleared P: %s", row_cleared_r) );

    int altitude_difference_p = Features.altitude_difference_penalty(grid);
    LOGGER.info( String.format("Altitude difference P: %s", altitude_difference_p) );

    int max_well_depth_p = Features.max_well_depth_penalty(grid);
    LOGGER.info( String.format("Max well depth P: %s", max_well_depth_p) );

    int well_depth_p = Features.well_depth_penalty(grid);
    LOGGER.info( String.format("Well depth P: %s", well_depth_p) );

    int cell_count_p = Features.cell_count_penalty(grid);
    LOGGER.info( String.format("Cell count P: %s", cell_count_p) );

    int weighted_cell_count_p = Features.weighted_cell_count_penalty(grid);
    LOGGER.info( String.format("Weighted cell count P: %s", weighted_cell_count_p) );

    int row_transition_p = Features.row_transition_penalty(grid);
    LOGGER.info( String.format("Row transition P: %s", row_transition_p) );

    int column_transition_p = Features.column_transition_penalty(grid);
    LOGGER.info( String.format("Column transition P: %s", column_transition_p) );

  }
}