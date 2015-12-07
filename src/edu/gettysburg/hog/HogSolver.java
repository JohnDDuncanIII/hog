package edu.gettysburg.hog;
import java.util.Scanner;

/**
 * HogSolver.java - demonstrates the computation of Hog win
 * probabilities using value iteration.
 *
 * @author Todd Neller */

public class HogSolver {

	int maxDice;

	/**
	 * variable <code>goal</code> - goal score for Pig
	 */
	int goal;

	/**
	 * variable <code>p</code> - probability estimates
	 */
	double[][] p;

	/**
	 * variable <code>dice</code> - best number of dice to roll
	 */
	int[][] dice;


	/**
	 * variable <code>expOutcomes</code> - probability of outcome
	 * indexed by the number of dice and the roll total (HOG being 0).
	 */
	double[][] expOutcomes;


	/**
	 * Creates a new <code>HogSolver</code> instance.
	 *
	 * @param goal an <code>int</code> value - goal value
	 * @param theta a <code>double</code> value - convergence
	 * condition for value iteration
	 */
	HogSolver(int maxDice, int goal, double theta) 
	{
		this.maxDice = maxDice;
		this.goal = goal;
		init();
		while (valueIterate() >= theta);	
	}

	HogSolver() {
		dice = new int[100][100];
	}
	
	public void init() 
	{
		p = new double[goal][goal];
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



	public double pWin(int myScore, int otherScore)
	{ // probability of a win from a given game state
		if (myScore >= goal)
			return 1.0;
		else if (otherScore >= goal)
			return 0.0;
		else return p[myScore][otherScore];
	}

	public double pWinWithDice(int myScore, int otherScore, int dice) 
	{
		double winProb = 0.0;
		for (int total = 0; total < expOutcomes[dice].length; total++)
			winProb += expOutcomes[dice][total] * (1 - pWin(otherScore, myScore + total));
		return winProb;
	}

	/**
	 * <code>valueIterate</code> - perform one iteration of value
	 * iteration and return the maximum change to a probability
	 * estimate.
	 *
	 * @return a <code>double</code> value - the maximum change to a
	 * probability estimate */
	public double valueIterate()
	{
		double maxChange = 0.0;

		// Compute new probabilities.
		for (int i = 0; i < goal; i++) // for all i
			for (int j = 0; j < goal; j++) { // for all j
				double oldProb = p[i][j];
				int newBestDice = 1;
				double newBestProb = pWinWithDice(i, j, 1);
				for (int dice = 2; dice <= maxDice; dice++) {
					double pWinWDice = pWinWithDice(i, j, dice);
					if (pWinWDice > newBestProb) {
						newBestDice = dice;
						newBestProb = pWinWDice;
					}
				}
				p[i][j] = newBestProb;
				dice[i][j] = newBestDice;
				double change = Math.abs(p[i][j] - oldProb);
				maxChange = Math.max(maxChange, change);
			}
		return maxChange;
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

	public int advise(int playerScore, int opponentScore) {
		while (true) {
			try {
				if (playerScore >= 0 && opponentScore >= 0 && playerScore < goal && opponentScore < goal)
					return dice[playerScore][opponentScore];
			}
			catch (Exception e) {
				continue;
			}
		}
	}
	
} // HogSolver
