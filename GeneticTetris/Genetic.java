// to run genetic algorithm

import java.util.*;
import java.text.*;

public class Genetic {

	// 1 List<Double> = 1 DNA
	private static TetrisLogger LOGGER = new TetrisLogger("genetic_algorithm");
  	private static TetrisLogger DATA = new TetrisLogger("performance");
	private static int GENE_COUNT;
	private static Random mutation_pool = new Random();
	public static boolean log_to_screen = false;
	private static DecimalFormat df = new DecimalFormat("#.#####");

	private static List<Individual> solver_population = new ArrayList<Individual>(); //limit to 16

	private static Double selection_cutoff_threshold;
	private static Double mutation_chance;
	private static Double multiple_gene_mutation_chance;  
	private static boolean is_hard;
	private static Double s_and_z_chance;
	private static int instance_per_individual;

	private static Double min_row_requirement;
	private static Double min_ratio_requirement;
	private static int max_generation_no;


	// add a few controlled genes + random genes
	public static void initialize_population() {

		// some controlled individual
		Double[][] controlled_dnas = {
				// max_column_height_penalty - column_height_penalty - hole_count_penalty - weighted_hole_penalty
				// row_cleared_reward - altitude_difference_penalty - max_well_depth_penalty - well_depth_penalty
				// cell_count_penalty - weighted_cell_count_penalty
				// row_transition_penalty - column_transition_penalty
      
		};

		for (int i = 0; i < controlled_dnas.length; i++) {
			solver_population.add( new Individual(Arrays.asList(controlled_dnas[i]), i) );
		}

		// random individuals
		int random_individual_count = 14;


		for (int i = 0; i < random_individual_count; i++) {
			Double[] dna = new Double[GENE_COUNT];
			for (int j = 0; j < GENE_COUNT; j++) {
				dna[j] = Math.random();
			}      
			solver_population.add( new Individual(Arrays.asList(dna), i+3) );
		}

		// initialize mutation pool
		mutation_pool.setSeed(System.currentTimeMillis());
	}

	// return a list of Individuals sorted by increasing fitness
	private static List<Individual> live(List<Individual> population, int generation_id) {

		if (log_to_screen) {
			LOGGER.info( String.format("Generation: %-10s", generation_id) );
			LOGGER.info( String.format("%-30s %-20s %-20s %-20s", "Gene_name", "Avg rows cleared", "Avg ratio", "Fitness") );
		} else {
			LOGGER.debug( String.format("Generation: %-10s", generation_id) );
			LOGGER.debug( String.format("%-30s %-20s %-20s %-20s", "Gene_name", "Avg rows cleared", "Avg ratio", "Fitness") );
		}

		for (Individual cell : population) {
			String gene_name = String.format("generation_%s_dna_%s", generation_id, cell.id);

			DataCollector collector = new DataCollector(gene_name, cell.dna, is_hard, s_and_z_chance, instance_per_individual);
			cell.set_performance( collector.call() );
		}
		Collections.sort( population, new IndividualComparator() );

		for (Individual cell : population)
			if (log_to_screen) {
				String gene_name = String.format("generation_%s_dna_%s", generation_id, cell.id);
				LOGGER.info( String.format("%-30s %-20s %-20s %-20s", gene_name, df.format(cell.average_row_cleared), df.format(cell.average_ratio), df.format(cell.fitness)) );
			} else {
				String gene_name = String.format("generation_%s_dna_%s", generation_id, cell.id);
				LOGGER.debug( String.format("%-30s %-20s %-20s %-20s", gene_name, df.format(cell.average_row_cleared), df.format(cell.average_ratio), df.format(cell.fitness)) );
			}

		return population;
	}

	// determine if the population is good (have cleared ~X no of rows, for example)
	private static boolean is_qualified(List<Individual> population) {

		for (Individual cell : population) {
			if (cell.average_row_cleared < min_row_requirement || cell.average_ratio > min_ratio_requirement)
				return false;
		}

		return true;
	}

	private static Individual random_selection(List<Individual> population) {
		int index = (int)((Math.random()*(1 - selection_cutoff_threshold) + selection_cutoff_threshold)*population.size());

		// if (log_to_screen) {
		//   LOGGER.info( String.format("Selected [%-3s]", index) );
		// } else {
		//   LOGGER.debug( String.format("Selected [%-3s]", index) );
		// }

		return population.get(index);
	}

	private static List<Double> mutate(List<Double> original_dna) {
		boolean will_mutate = Math.random() < mutation_chance;
		if (will_mutate) {

			// copy the dna first
			List<Double> new_dna = new ArrayList<Double>(original_dna);

			// set off a mutation
			boolean mutate_another_gene;
			do {
				int index = (int)(Math.random()*original_dna.size());
				new_dna.set(index, Math.random());
				mutate_another_gene = Math.random() < multiple_gene_mutation_chance;
			} while (mutate_another_gene);

			return new_dna;
		} else {
			return original_dna;
		}
	}

	private static List<Double> breed(Individual mom, Individual dad) {
		int split_index = (int)( Math.random()*mom.dna.size() );
		List<Double> mom_genes = mom.dna.subList( 0, split_index );
		List<Double> dad_genes = dad.dna.subList( split_index, dad.dna.size() );

		List<Double> child_dna = new ArrayList<Double>();
		child_dna.addAll(mom_genes);
		child_dna.addAll(dad_genes);

		child_dna = mutate(child_dna);

		return child_dna;
	}

  	private static List<Double> performance(List<Individual> population) {
    	Double average_row_cleared = 0.0;
    	Double average_ratio = 0.0;

    	for (Individual cell : population) {
      		average_row_cleared += cell.average_row_cleared;
      		average_ratio += cell.average_ratio;
    	}

    	average_row_cleared = average_row_cleared / population.size();
    	average_ratio = average_ratio / population.size();

    	List<Double> result = new ArrayList<Double>();
    	result.add( average_row_cleared );
    	result.add( average_ratio );

    	return result;
  	}

	private static void genetic_algorithm(List<Individual> population) {
		int generation_id = 0;
    	DATA.info( String.format("%-20s %-20s %-20s", "Generation", "Avg rows cleared", "Avg ratio") );

		LOGGER.info( String.format("Generation no. %s", generation_id) );
		LOGGER.info( "---------------------------------------------------------------" );

		population = live(population, generation_id);

    	while (generation_id <= Genetic.max_generation_no && !is_qualified(population)) {

      		List<Double> performance = performance(population);
      		DATA.info( String.format("%-10s %-10s %-10s", generation_id, performance.get(0), performance.get(1)) );

			List<Individual> new_population = new ArrayList<Individual>();      

			for (int i = 0; i < population.size(); i++) {
				Individual mom = random_selection(population);
				Individual dad = random_selection(population);

				List<Double> child_dna = breed(mom, dad);
				Individual child = new Individual(child_dna, i);
				new_population.add(child);
			}

			generation_id++;
			population = live(new_population, generation_id);
			// LOGGER.info( String.format("Generation no. %s", generation_id) );

			if (generation_id%50 == 0)
				for ( Individual cell : population ) {
					LOGGER.debug( cell.dna );
				}

			LOGGER.info( "---------------------------------------------------------------" );

		}

		for ( Individual cell : population ) {
			LOGGER.debug( cell.dna );
		}
	}

	public static void main(String[] args) {

		// args:
		// min_row_requirement | min_ratio_requirement | max_generation_no | s_and_z_chance | selection_cutoff_threshold | mutation_chance | multiple_gene_mutation_chance

		Genetic.min_row_requirement = Double.parseDouble( args[0] ); // 3000.00
		Genetic.min_ratio_requirement = Double.parseDouble( args[1] ); // 2.6
		Genetic.max_generation_no = Integer.parseInt  ( args[2] ); // 500

		Genetic.s_and_z_chance = Double.parseDouble( args[3] ); // 0.8
		if (Genetic.s_and_z_chance > 0)
			Genetic.is_hard = true;
		else
			Genetic.is_hard = false;

		Genetic.selection_cutoff_threshold  = Double.parseDouble( args[4] ); // 0.5
		Genetic.mutation_chance = Double.parseDouble( args[5] ); // 0.2
		Genetic.multiple_gene_mutation_chance = Double.parseDouble( args[6] ); // 0.1

		Genetic.GENE_COUNT = 12;
		Genetic.instance_per_individual = 5;

		initialize_population();
		// LOGGER.info("Initializing population");

		Genetic.log_to_screen = true;
		genetic_algorithm(solver_population);

	}
}

class Individual {  
	
	public List<Double> dna;
	public double average_row_cleared;
	public double average_ratio;
	public double fitness;
	public int id;

	public void set_performance(List<Double> gene_result) {
		average_row_cleared = gene_result.get(0);
		average_ratio = gene_result.get(2);
		fitness = Individual.fitness(average_row_cleared, average_ratio);
	}

	public Individual(List<Double> dna_sequence, int given_id) {
		dna = dna_sequence;
		id = given_id;
	}

	// determine the fitness of an individual
	public static Double fitness(Double average_row_cleared, Double average_ratio) {
		return ( average_row_cleared / ( 1 + average_ratio - 2.5 ) );
	}
}

class IndividualComparator implements Comparator<Individual> {
	public int compare(Individual first, Individual second) {
		return (int) Math.signum(first.fitness - second.fitness);
	}
}
