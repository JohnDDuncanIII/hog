package edu.gettysburg.hog;
import java.util.Scanner;

/**
 * HogSolver.java - demonstrates the computation of Hog win
 * probabilities using value iteration.
 *
 * @author Todd Neller */

public class RiskAverseHogSolver {

	int maxDice;

	/**
	 * variable <code>goal</code> - goal score for Pig
	 */
	int goal;

	/**
	 * variable <code>dice</code> - best number of dice to roll
	 */
	int[][] dice;


	/**
	 * variable <code>expOutcomes</code> - probability of outcome
	 * indexed by the number of dice and the roll total (HOG being 0).
	 */
	double[][] expOutcomes;

	HogSolver solver;

	double winWeight; // how much the player values winning versus not rolling a 1 on the turn.
	
	/**
	 * Creates a new <code>RiskAverseHogSolver</code> instance.
	 *
	 * @param goal an <code>int</code> value - goal value
	 * @param theta a <code>double</code> value - convergence
	 * @param winWeight a <code>double</code> value between 0 and 1 - how much the player's action value varies between 0 (scoring something this turn) and 1 (maximizing the probability of winning)
	 * condition for value iteration
	 */
	RiskAverseHogSolver(int maxDice, int goal, double theta, double winWeight) 
	{	
		solver = new HogSolver(maxDice, goal, theta);
		this.maxDice = maxDice;
		this.goal = goal;
		this.winWeight = winWeight;
		init();
		computePolicy();
	}
	
	public void computePolicy() {
		for (int i = 0; i < goal; i++)
			for (int j = 0; j < goal; j++) {
				int bestDice = 1;
				double bestUtil = Double.NEGATIVE_INFINITY;
				for (int dice = 1; dice < maxDice; dice++) {
					double util = winWeight * solver.pWinWithDice(i, j, dice) // win utility
							- (1 - winWeight) * expOutcomes[dice][0]; // 1 roll penalty
					if (util > bestUtil) {
						bestUtil = util;
						bestDice = dice;
					}
				}
				dice[i][j] = bestDice;
			}
	}

	public void init() 
	{
		dice = new int[goal][goal];
		expOutcomes = new double[maxDice + 1][6 * maxDice + 1];
		// Enter expectations for the roll of a single die
		for (int total = 0; total <= 6; total++)
			if (total != 1)
				expOutcomes[1][total] = 1.0/6.0;
		// Compute expectations for rolls of more dice
		for (int dice = 2; dice <= maxDice; dice++)
			for (int prevTotal = 0; prevTotal < expOutcomes[dice].length; prevTotal++) {
				if (expOutcomes[dice - 1][prevTotal] > 0) 
					if (prevTotal == 0)
						expOutcomes[dice][prevTotal] += expOutcomes[dice - 1][prevTotal];
					else
						for (int roll = 1; roll <= 6; roll++)
							if (roll == 1)
								expOutcomes[dice][0] += expOutcomes[dice - 1][prevTotal] / 6;
							else
								expOutcomes[dice][prevTotal + roll] += expOutcomes[dice - 1][prevTotal] / 6;
			}
	}



	
	/**
	 * <code>outputPolicy</code> - output optimal policy table
	 * showing dice to roll for each given i (down) and j (across). */
	public void outputPolicy()
	{
//		System.out.println("util[0][0] = " + util[0][0] + "\n");
//		for (int i = 0; i < goal; i++) {
//			for (int j = 0; j < goal; j++)
//				System.out.print(dice[i][j] + " ");
//			System.out.println();
//		}
		
		System.out.print("{");
		for (int i = 0; i < goal; i++) {
			System.out.print("{");
			for (int j = 0; j < goal; j++) {
				System.out.print(dice[i][j]);
				System.out.print((j < goal - 1) ? ", " : "}");
			}
			System.out.println((i < goal - 1) ? "," : "}");
		}
	}

	public void advise() {
		Scanner in = new Scanner(System.in);
		System.out.println("Starting Hog advice mode.  Enter nothing to terminate.");
		while (true) {
			try {
				System.out.print("Please enter your score and your opponent score, separated by a space: ");
				String line = in.nextLine().trim();
				if (line.equals(""))
					break;
				Scanner lineIn = new Scanner(line);
				int i = lineIn.nextInt();
				int j = lineIn.nextInt();
				if (i >= 0 && j >= 0 && i < goal && j < goal)
					System.out.printf("Roll %d dice.\n", dice[i][j]);
			}
			catch (Exception e) {
				continue;
			}
		}
		System.out.println("Exiting Hog advice mode.");
	}
	

	public static void main(String[] args){
		int maxDice = 30;
		int goal = 100;
		double theta = 1e-9;
		double winWeight = .9;
		try {
			if (args.length > 4) 
				throw new IllegalArgumentException("Too many arguments");
			if (args.length > 0) maxDice = Integer.parseInt(args[0]);
			if (args.length > 1) goal = Integer.parseInt(args[1]);
			if (args.length > 2) theta = Double.parseDouble(args[2]);
			if (args.length > 3) winWeight = Double.parseDouble(args[2]);
		}
		catch (Exception e) {
			System.out.println(e);
			System.out.println("Usage: java HogSolver [int maxDice [int goal [double theta]]]");
			System.exit(1);
		}
		RiskAverseHogSolver solver = new RiskAverseHogSolver(maxDice, goal, theta, winWeight);
		solver.outputPolicy();
		solver.advise();
	}
} // RiskAverseHogSolver
