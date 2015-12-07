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

	public int advise(int userScore, int computerScore) {
		while (true) {
			try {
				if (userScore >= 0 && computerScore >= 0 && userScore < goal && computerScore < goal)
					return dice[userScore][computerScore];
			}
			catch (Exception e) {
				continue;
			}
		}
	}
} // RiskAverseHogSolver
