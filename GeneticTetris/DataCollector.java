import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.text.*;

public class DataCollector implements Callable<List<Double>> {

  private String name;
  private int instance_no;
  private TetrisLogger LOGGER;
  private List<Double> weights;
  private boolean is_hard;
  private double hard_pieces_chance;
  public boolean log_to_screen = false;

  public DataCollector(String collector_name, List<Double> feature_weights, boolean more_hard_pieces, double s_and_z_chance, int number_of_games) {
    name = collector_name;
    instance_no = number_of_games;
    weights = feature_weights;
    is_hard = more_hard_pieces;
    hard_pieces_chance =  s_and_z_chance;
    //LOGGER = new TetrisLogger(name);
  }

  public DataCollector() {
    // use the current POSIX time
    name = "co_" + Integer.toString((int) (System.currentTimeMillis() / 1000L));
    instance_no = 20;

    Double[] initial_weights = {0.034280649, 0.976944555, 0.774470806, 0.159450884, 0.620445567, 0.000894, 0.124175118, 0.765678864, 0.422427394, 0.235410631, 0.48789771, 0.931874068};
    weights = new ArrayList<Double>( Arrays.asList(initial_weights) );

    is_hard = false;
    hard_pieces_chance = 0;

    //LOGGER = new TetrisLogger(name);
  }

  // -------------------------------------------------------------------------------------

  public List<Double> find_average_result(List<List<Integer>> all_game_results) {
    DecimalFormat df = new DecimalFormat("#.#####");
    long total_row_cleared = 0;
    long total_piece_used = 0;
    long max_row_cleared = 0;

    try {
      /*
      if (log_to_screen) {
        LOGGER.info( weights );
        LOGGER.info( String.format("%-20s %-20s %-20s", "Cleared rows", "Pieces used", "Ratio") );
      }
      else {
        LOGGER.debug( weights );
        LOGGER.debug( String.format("%-20s %-20s %-20s", "Cleared rows", "Pieces used", "Ratio") );
      }
      */
      for(List<Integer> one_game_result : all_game_results) {
        int row_cleared = one_game_result.get(0);
        int piece_used = one_game_result.get(1);

        total_row_cleared += row_cleared;
        total_piece_used += piece_used;

        if (row_cleared > max_row_cleared)
          max_row_cleared = row_cleared;

        if (row_cleared > 0) {
          double ratio = (double)piece_used / (double)row_cleared;
        /*  
          if (log_to_screen)
            LOGGER.info( String.format("%-20s %-20s %-20s", row_cleared, piece_used, df.format(ratio)) );
          else
            LOGGER.debug( String.format("%-20s %-20s %-20s", row_cleared, piece_used, df.format(ratio)) );
        } else {
          if (log_to_screen)
            LOGGER.info( String.format("%-20s %-20s %-20s", 0, piece_used, 0) );
          else
            LOGGER.debug( String.format("%-20s %-20s %-20s", 0, piece_used, 0) );
        }
        */
        }
      }
    } catch (Exception exception) {
    //  exception.printStackTrace();
    }

    double average_row_cleared = (double)total_row_cleared/(double)instance_no;
    double average_piece_used = (double)total_piece_used/(double)instance_no;
    double average_ratio = (double)average_piece_used/(double)average_row_cleared;
    /*
    if (log_to_screen) {
      LOGGER.info( String.format("%-20s %-20s %-20s %-20s", "Avg rows", "Max rows", "Avg pieces", "Avg ratio") );
      LOGGER.info( String.format("%-20s %-20s %-20s %-20s", df.format(average_row_cleared), max_row_cleared, df.format(average_piece_used), df.format(average_ratio)) );
    } else {
      LOGGER.debug( String.format("%-20s %-20s %-20s %-20s", "Avg rows", "Max rows", "Avg pieces", "Avg ratio") );
      LOGGER.debug( String.format("%-20s %-20s %-20s %-20s", df.format(average_row_cleared), max_row_cleared, df.format(average_piece_used), df.format(average_ratio)) );
    }

    if (log_to_screen) {
      System.out.println( String.format("%-10s %-10s %-10s %-10s", "Avg rows", "Max rows", "Avg pieces", "Avg ratio") );
      System.out.println( String.format("%-10s %-10s %-10s %-10s", df.format(average_row_cleared), max_row_cleared, df.format(average_piece_used), df.format(average_ratio)) );
    }
    */

    List<Double> result = new ArrayList<Double>();
    // result.add((Double)max_row_cleared);
    result.add(average_row_cleared);
    result.add(average_piece_used);
    result.add(average_ratio);
    return result;
  }

  public List<Double> call() {

    List<List<Integer>> all_game_data = new ArrayList<List<Integer>>();
    
    ExecutorService workers = Executors.newCachedThreadPool();
    List<PlayerSkeleton> tasks = new ArrayList<PlayerSkeleton>();

    for (int i = 0; i < instance_no; i++) {
      String game_name = name + "_g" + Integer.toString(i);

      if (is_hard) {
        tasks.add( new PlayerSkeleton(game_name, weights, false, true, hard_pieces_chance, 0) );
      } else {
        tasks.add( new PlayerSkeleton(game_name, weights, false, false, 0, 0) );
      }
    }

    try {
      List<Future<List<Integer>>> data = workers.invokeAll(tasks);
      for (Future<List<Integer>> one_game_data : data) {
        List<Integer> game_result = one_game_data.get();
        all_game_data.add(game_result);
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    
    workers.shutdown();
    return find_average_result(all_game_data);
  }

  public static void main(String[] args) {
    // DataCollector collector = new DataCollector();

    // custom data collector 
    Double[] initial_weights = {0.09204108605386596, 0.8599773043552206, 0.9039575584835701, 0.653294590912058, -0.20401970693255644, -0.07448660666070372, 0.024528513321928958, 0.34324261679129464, 0.10700965487148106, 0.830634595579863, 0.44651725435459566, 0.9751951338366429};
    // {0.09204108605386596, 0.8599773043552206, 0.9039575584835701, 0.653294590912058, -0.20401970693255644, -0.07448660666070372, 0.024528513321928958, 0.34324261679129464, 0.10700965487148106, 0.830634595579863, 0.44651725435459566, 0.9751951338366429};
    // {0.09204108605386596, 0.8599773043552206, 0.9039575584835701, 0.653294590912058, -0.20401970693255644, -0.07448660666070372, 0.024528513321928958, 0.34324261679129464, 0.10700965487148106, 0.830634595579863, 0.44651725435459566, 0.9751951338366429};
    // {0.09204108605386596, 0.8599773043552206, 0.9039575584835701, 0.653294590912058, -0.20401970693255644, -0.07448660666070372, 0.024528513321928958, 0.3083654764249424, 0.10700965487148106, 0.4293759904937482, 0.44651725435459566, 0.9751951338366429};
    // {0.09204108605386596, 0.8599773043552206, 0.9039575584835701, 0.653294590912058, -0.20401970693255644, -0.07448660666070372, 0.024528513321928958, 0.3083654764249424, 0.10700965487148106, 0.4293759904937482, 0.44651725435459566, 0.9751951338366429};
    //{0.034280649, 0.976944555, 0.774470806, 0.159450884, 0.620445567, 0.000894, 0.124175118, 0.765678864, 0.422427394, 0.235410631, 0.48789771, 0.931874068};
    List<Double> starting_weights = new ArrayList<Double>( Arrays.asList(initial_weights) );
    DataCollector collector = new DataCollector("games", starting_weights, false, 0.8, 100);

    collector.log_to_screen = true;
    collector.call();
  }
}