// This is a helper class for logging

// import java.util.logging.*;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.io.IOException;
import java.util.*;
import java.text.*;

public class TetrisLogger {

  private DecimalFormat df = new DecimalFormat("#.#####");

  private FileHandler file_handler;
  private java.util.logging.Logger the_logger;

  public TetrisLogger(String name) {    
    the_logger = Logger.getLogger(name);
    the_logger.setLevel(Level.FINE);

    boolean created_log_file = false;
    do {
      try {
        file_handler = new FileHandler("../src/" + name + ".log");
        created_log_file = true;
      } catch (IOException exception) {
        exception.printStackTrace();
        System.out.println("Trying again");
      }
    } while (!created_log_file);
    file_handler.setFormatter(new LogFormatter());


    the_logger.addHandler(file_handler);
  }

  // ---------------------------------------------------------------

  // wrapper to make it more convinient to log
  public void info(String message) {
    log(Level.INFO, message);
  }

  public void debug(String message) {
    log(Level.FINE, message);
  }

  public void info(int[][] grid) {
    log(Level.INFO, grid);
  }

  public void debug(int[][] grid) {
   log(Level.FINE, grid);
  }

  public void info(int turn, int piece, int move) {
    log(Level.INFO, turn, piece, move);
  }

  public void debug(int turn, int piece, int move) {
    log(Level.FINE, turn, piece, move);
  }

  public void info(List<Double> dna) {
    log(Level.INFO, dna);
  }

  public void debug(List<Double> dna) {
    log(Level.FINE, dna);
  }

  // ---------------------------------------------------------------

  private void log(Level logLevel, int turn, int piece, int move) {
    String step = String.format("T-%-10s Piece: %-5s | Move: %-5s", turn, piece, move);
    the_logger.log( logLevel, step );
  }

  private void log(Level logLevel, List<Double> dna) {
    String dna_string = "\n---------------------------------\n";
    for (Double gene : dna) {
      dna_string += String.format(" %-10s\n", gene);
    }
    dna_string += "---------------------------------";
    the_logger.log(logLevel, dna_string);
  }

  private void log(Level logLevel, int[][] grid) {
    int row_no = grid.length-1;
    int column_no = grid[0].length;
    String printed_grid = "\n"; //so that the grid starts below the log

    int max_turn = 0;
    for (int row = 0; row < grid.length; row++ )
      for (int column = 0; column < column_no; column++ )
        if (grid[row][column] > max_turn)
          max_turn = grid[row][column];

    for( int row = row_no; row >= 0; row-- ) {
      for( int column = 0; column < column_no; column++) {
        if (grid[row][column] == 0)
          printed_grid += String.format( "%-3s", '.' );
          // the_logger.log( logLevel, String.format( "%-3s", '.' ) );
        else if (grid[row][column] == max_turn)
          printed_grid += String.format( "%-3s", 'o' );
        else
          printed_grid += String.format( "%-3s", 'x' );
          // the_logger.log( logLevel, String.format( "%-3s", 'x' ) );
      }
      printed_grid +=  "\n";
      // the_logger.log( logLevel, '\n' ) );
    }

    the_logger.log( logLevel, printed_grid );
  }

  private void log(Level logLevel, String message) {
    the_logger.log(logLevel, message);
  }

}