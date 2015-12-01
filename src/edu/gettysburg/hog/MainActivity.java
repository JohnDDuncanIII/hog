package edu.gettysburg.hog;

import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
//import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

//public class MainActivity extends ActionBarActivity {
public class MainActivity extends Activity {

	// COMPUTER_DELAY - delay between computer rolls in milliseconds
	protected static final long COMPUTER_DELAY = 1000; 
	// GOAL_SCORE - goal score at or above which the holding player wins
	private static final int GOAL_SCORE = 100; 
	// Game state variables: 
	private int userScore = 0, computerScore = 0, turnTotal = 0; 
	// userStartGame - whether or not the user starts the current game 
	private boolean userStartGame = true; 
	// isUserTurn - whether or not it is currently the user's turn
	private boolean isUserTurn = true; 
	// imageName - name of the current displayed image
	private String imageName = "roll"; 

	// GUI views
	private TextView textViewYourScore, textViewMyScore, textViewTurnTotal;
	private ImageView imageView;
	// GUI buttons
	private Button buttonRoll, buttonHold;

	// mapping from image strings to Drawable resources
	private HashMap<String, Drawable> drawableMap = new HashMap<String, Drawable>(); 
	// random - random number generator for rolling dice
	private Random random;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textViewYourScore = (TextView) findViewById(R.id.textViewYourScore);
		textViewMyScore = (TextView) findViewById(R.id.textViewMyScore);
		textViewTurnTotal = (TextView) findViewById(R.id.textViewTurnTotal);
		buttonRoll = (Button) findViewById(R.id.buttonRoll);
		buttonHold = (Button) findViewById(R.id.buttonHold);
		imageView = (ImageView) findViewById(R.id.imageView);
		drawableMap.put("roll", ContextCompat.getDrawable(this, R.drawable.roll));
		drawableMap.put("hold", ContextCompat.getDrawable(this, R.drawable.hold));
		drawableMap.put("die1", ContextCompat.getDrawable(this, R.drawable.die1));
		drawableMap.put("die2", ContextCompat.getDrawable(this, R.drawable.die2));
		drawableMap.put("die3", ContextCompat.getDrawable(this, R.drawable.die3));
		drawableMap.put("die4", ContextCompat.getDrawable(this, R.drawable.die4));
		drawableMap.put("die5", ContextCompat.getDrawable(this, R.drawable.die5));
		drawableMap.put("die6", ContextCompat.getDrawable(this, R.drawable.die6));
		random = new Random();

		buttonRoll.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				roll();
			}
		});
		buttonHold.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				hold();
			}
		});
	}

	private void roll() {
		int roll = random.nextInt(6) + 1;

		setImage("die" + roll);
		if (roll == 1) {
			setTurnTotal(0);
			changeTurn();
		}
		else {
			setTurnTotal(turnTotal + roll);
		}
	}

	private void hold() {
		setImage("hold");
		if (isUserTurn) 
			setUserScore(userScore + turnTotal);
		else 
			setComputerScore(computerScore + turnTotal);
		setTurnTotal(0);
		if (userScore >= GOAL_SCORE || computerScore >= GOAL_SCORE)
			endGame();
		else
			changeTurn();	
	}

	private void computerTurn() {
		new Thread(new Runnable() {
			public void run() {
				Thread.yield();
				try { Thread.sleep(COMPUTER_DELAY); } 
				catch (InterruptedException e) { e.printStackTrace(); }
				while (!isUserTurn) {
					int holdValue = 21 + (int) Math.round((userScore - computerScore)/8.0);


					if (!(computerScore + turnTotal >= GOAL_SCORE) && 
							(userScore >= 71 || computerScore >= 71 || turnTotal < holdValue))
						runOnUiThread(new Runnable() {public void run() {roll();}});
					else {
						runOnUiThread(new Runnable() {public void run() {hold();}});
						break;
					}
					Thread.yield();
					try { Thread.sleep(COMPUTER_DELAY); } 
					catch (InterruptedException e) { e.printStackTrace(); }
				}
			}
		}).start();
	}
	
	private void endGame() {
		String message = (!isUserTurn) 
			? String.format("I win %d to %d.", computerScore, userScore)
			: String.format("You win %d to %d.", userScore, computerScore);
		message += "  Would you like to play again?";
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)       

		       .setCancelable(false)
		       .setPositiveButton("New Game", new DialogInterface.OnClickListener() {
		    	   public void onClick(DialogInterface dialog, int id) {
		    		   setUserScore(0);
		    		   setComputerScore(0);
		    		   setTurnTotal(0);
		    		   userStartGame = !userStartGame;
		    		   isUserTurn = userStartGame;
		    		   setButtonsState();
		    		   if (isUserTurn)
		    			   setImage("roll");
		    		   else
		    			   computerTurn();
		    		   dialog.cancel();             
		    	   }
		       })
		       .setNegativeButton("Quit", new DialogInterface.OnClickListener() {           
		    	   public void onClick(DialogInterface dialog, int id) {                
		    		   MainActivity.this.finish();
		    	   }       
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void setButtonsState() {
		buttonHold.setEnabled(isUserTurn);
		buttonRoll.setEnabled(isUserTurn);
	}

	private void setUserScore(final int newScore) {
		userScore = newScore;
		textViewYourScore.setText(String.valueOf(newScore));
	}
	
	private void changeTurn() {
		isUserTurn = !isUserTurn;
		setButtonsState();
		if (!isUserTurn)
			computerTurn();
	}

	private void setComputerScore(final int newScore) {
		computerScore = newScore;
		textViewMyScore.setText(String.valueOf(newScore));
	}

	private void setTurnTotal(final int newTotal) {
		turnTotal = newTotal;
		textViewTurnTotal.setText(String.valueOf(newTotal));
	}

	private void setImage(final String newImageName) {
		imageName = newImageName;
		imageView.setImageDrawable(drawableMap.get(imageName));
	}
	
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("userScore", userScore);
		outState.putInt("computerScore", computerScore);
		outState.putInt("turnTotal", turnTotal);
		outState.putBoolean("userStartGame", userStartGame);
		outState.putBoolean("isUserTurn", isUserTurn);
		outState.putString("imageName", imageName);
	}
	
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		setUserScore(savedInstanceState.getInt("userScore", 0));
		setComputerScore(savedInstanceState.getInt("computerScore", 0));
		setTurnTotal(savedInstanceState.getInt("turnTotal", 0));
		setImage(savedInstanceState.getString("imageName"));
		userStartGame = savedInstanceState.getBoolean("userStartGame", true);
		isUserTurn = savedInstanceState.getBoolean("isUserTurn", true);
		setButtonsState();
		if (userScore >= GOAL_SCORE || computerScore >= GOAL_SCORE)
			endGame();
		else if (!isUserTurn)
			computerTurn();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
