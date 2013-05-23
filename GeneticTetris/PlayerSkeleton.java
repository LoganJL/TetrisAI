// implements an instance of the game

import java.util.*;
import java.util.concurrent.*;

public class PlayerSkeleton implements Callable<List<Integer>> {
	
	// public TetrisLogger LOGGER;
	private String name; 
	private List<Double> weights;
	private boolean have_GUI;
	private boolean is_hard;
	private double hard_pieces_chance;
	private int move_delay;
	public boolean log_to_screen = false;

	// ------------------------------------------------------------------------------

	// creates a game with fully customizable parameters
	public PlayerSkeleton(String instance_name, List<Double> feature_weights, boolean show_GUI, 
		boolean more_hard_pieces, double s_and_z_chance, int mili_between_moves) {
		name = instance_name;
		weights = feature_weights;
		
		is_hard = more_hard_pieces;
		hard_pieces_chance = s_and_z_chance;
		
		move_delay = mili_between_moves;
//		have_GUI = show_GUI;
		have_GUI = false;
		
		// LOGGER = new TetrisLogger(name);

		assert !instance_name.isEmpty() : "Instance does not have a name";
	}

	// a default game as give in the original source
	public PlayerSkeleton() {
		name = Integer.toString((int) (System.currentTimeMillis() / 1000L)); // use the current POSIX time
		
		Double[] initial_weights = {
			1.0000,
			1.0000,
			10.0000,
			10.0000,
			1.0000,
			1.0000,
			1.0000,
			1.0000,
			1.0000,
			1.0000,
			1.0000,
			1.0000};
		weights = new ArrayList<Double>( Arrays.asList(initial_weights) );
	
		is_hard = false;		
		hard_pieces_chance = 0;

		have_GUI = true;
		move_delay = 500;

		// LOGGER = new TetrisLogger(name);
	}

	// ------------------------------------------------------------------------------
	
	public List<Integer> call() {

		// create a easy / hard game
		State game_state;
		if (is_hard) 
			game_state = new State(true, hard_pieces_chance);
		else
			game_state = new State(); // normal game

		// show GUI or not
		if (have_GUI)
			new TFrame(game_state);

		// main game loop
		while (!game_state.hasLost()) {
			game_state.makeMove( pickMove(game_state, game_state.legalMoves(), weights) );
			
			if (have_GUI) {
				game_state.draw();
				game_state.drawNext(0,0);
			}
			
			try {
				Thread.sleep( move_delay );
			} catch (InterruptedException exception) {
				exception.printStackTrace();
			}
		}
		
		// log and return game result
		int row_cleared = game_state.getRowsCleared();
		int piece_used = game_state.getTurnNumber();
		double ratio = (double)piece_used / (double)row_cleared;

		List<Integer> result = new ArrayList<Integer>();
		result.add(row_cleared);
		result.add(piece_used);

		// if (log_to_screen) {
		// 	LOGGER.info( "GAME OVER --------------------------------------------------------" );
		// 	LOGGER.info( String.format("Cleared: %-10s rows. Used %-10s pieces. Avg piece/row: %.5g", row_cleared, piece_used, ratio) );
		// } else {
		// 	LOGGER.debug( "GAME OVER --------------------------------------------------------" );
		// 	LOGGER.debug( String.format("Cleared: %-10s rows. Used %-10s pieces. Avg piece/row: %.5g", row_cleared, piece_used, ratio) );
		// }

		if (log_to_screen) {
			System.out.println( String.format("Cleared: %-10s rows. Used %-10s pieces. Avg piece/row: %.5g", row_cleared, piece_used, ratio) );
		}

		return result;
	}

	// ------------------------------------------------------------------------------

	public int pickMove(State state, int[][] legalMoves, List<Double> feature_weights) {		
		// int move = 0;
		int move = Features.pickMove(state, legalMoves, feature_weights);
		// int move = Features.pickMove(state, legalMoves, feature_weights, LOGGER);
		// AlgoByKhiem.LOGGER = LOGGER;

		return move;	
	}

	// ------------------------------------------------------------------------------

	// if we run this file
	public static void main(String[] args) {
		// PlayerSkeleton game_instance = new PlayerSkeleton();

		// custom game test
		Double[] initial_weights = {0.013742836901574277, 0.0882621028770333, 0.15764946761124013, 0.1738448121582822, -0.013004248503644967, -0.0036198758916475278, 0.005278774194090465, 0.057177520748422334, -0.02776233278781331, 0.011612128898547241, 0.058475136177431296, 0.10879338563872724};
		//Double[] initial_weights = {0.09204108605386596, 0.8599773043552206, 0.9039575584835701, 0.653294590912058, -0.20401970693255644, -0.07448660666070372, 0.024528513321928958, 0.3083654764249424, 0.10700965487148106, 0.4293759904937482, 0.44651725435459566, 0.9751951338366429};
		//Double[] initial_weights = {21.539335131606947, 1026.7623146707917, 650.0985909996524, 1864.8171309579059, 1675.5379939835739, 18.716353267202642, 44.86745937085246, 278.17959023057443, 533.2318298257198, 1818.9823784855855, 796.4315105698482, 1789.4387175273848};
		List<Double> starting_weights = new ArrayList<Double>( Arrays.asList(initial_weights) );
		PlayerSkeleton game_instance = new PlayerSkeleton("tetris", starting_weights, true, false, 0.8, 0);

		game_instance.log_to_screen = true;
		game_instance.call();

		return;
	}

}