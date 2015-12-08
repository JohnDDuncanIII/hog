package edu.gettysburg.hog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.SynchronousQueue;

public class MainActivity extends AppCompatActivity {

	// Simple Dialog used to show the splash screen
	protected Dialog mSplashDialog;
	// difficulty level buttons for splash screen
	private Button buttonEasy, buttonMedium, buttonHard;
	private Button drawerButtonEasy, drawerButtonMedium, drawerButtonHard;
	// name input for splash screen
	private EditText editTextEnteredText;
	// keep track of the username
	// private String userName="User's";
	// keep track of whether or not the user has passed the splash screen
	private boolean hasComputed = false;
	// hard computer player
	HogSolver hardSolver;
	// easy and medium computer player (we will instantiate with a different win
	// weight)
	RiskAverseHogSolver easyMediumSolver;
	// seekbar for animation delay
	private SeekBar seekBar;
	// hold the dice value (even if we are no longer using the original computer object
	private int[][] dice;
	// stuff for instantiation. will be changed when the difficulty is chosen
	int maxDice = 25;
	int goal = 100;
	double theta = 0;
	double winweight = 0;

	private String nextGameDifficulty;


	/**
	 * Delay between displaying each die roll (Only used for animation)
	 */
	protected static long ANIMATION_DELAY = 100;

	/**
	 * Goal score at or above which the holding player wins
	 */
	private static final int GOAL = 100;

	/**
	 * Number of rolls currently selected by user
	 */
	private int user_numRolls = 1;

	/**
	 * User's score
	 */
	private int userScore = 0;

	/**
	 * Computer's score
	 */
	private int computerScore = 0;

	/**
	 * Total for current turn
	 */
	private int turnTotal = 0;

	/**
	 * Whether or not it is currently the user's turn
	 */
	private boolean isUserTurn = true;

	/**
	 * Whether or not the user starts the game
	 */
	private boolean userStartGame = true;

	/**
	 * list of current rolls
	 */
	private int[] currentRolls;

	/**
	 * current roll position
	 */
	private int currentRollPos;

	/**
	 * Text Views for displaying scores and turn total
	 */
	private TextView txtYourScore, txtComputerScore, txtTurnTotal;

	/**
	 * Grid for displaying die images
	 */
	private GridLayout imageGrid;

	/**
	 * Button for rolling the dice
	 */
	ImageButton rollButton;

	/**
	 * Spinner for selecting number of rolls
	 */

	private Spinner spinner;

	/**
	 * mapping from image strings to drawable resources
	 */
	private HashMap<String, Drawable> drawableMap = new HashMap<String, Drawable>();

	/**
	 * Random number generator for rolling dice
	 */
	Random rand = new Random();

	/**
	 * Difficulty level for the human player. Higher level corresponds to better
	 * computer AI
	 */
	private String computerLevel = "easy";

	/**
	 * Screen width and height in pixels.
	 */
	private int width, height;

	/**
	 * Drawer layout for settings
	 */
	private DrawerLayout drawerLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showSplashScreen();

		//final Context ctx = getApplicationContext();


		setContentView(R.layout.activity_main);

		// Initialize GUI components
		txtYourScore = ((TextView) findViewById(R.id.your_score_value));
		txtComputerScore = ((TextView) findViewById(R.id.computer_score_value));
		txtTurnTotal = ((TextView) findViewById(R.id.turn_total_value));

		rollButton = ((ImageButton) findViewById(R.id.rollButton));

		imageGrid = ((GridLayout) findViewById(R.id.image_grid));
		// TODO: change imageGrid parameter so that the views start showing from
		// left rather than center

		drawerButtonEasy = (Button) findViewById(R.id.drawerButtonEasy);
		drawerButtonMedium = (Button) findViewById(R.id.drawerButtonMedium);
		drawerButtonHard = (Button) findViewById(R.id.drawerButtonHard);

		drawerButtonEasy.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//buttonEasy();
				nextGameDifficulty = "easy";
				shouldRecordStats();
				//TODO
			}
		});

		drawerButtonMedium.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//buttonMedium();
				nextGameDifficulty = "medium";
				shouldRecordStats();

			}
		});

		drawerButtonHard.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//buttonHard();
				nextGameDifficulty = "hard";
				shouldRecordStats();
			}
		});


		spinner = (Spinner) findViewById(R.id.roll_spinner);

		drawableMap.put("die1", ContextCompat.getDrawable(this, R.drawable.die1cross));
		drawableMap.put("die2", ContextCompat.getDrawable(this, R.drawable.die2));
		drawableMap.put("die3", ContextCompat.getDrawable(this, R.drawable.die3));
		drawableMap.put("die4", ContextCompat.getDrawable(this, R.drawable.die4));
		drawableMap.put("die5", ContextCompat.getDrawable(this, R.drawable.die5));
		drawableMap.put("die6", ContextCompat.getDrawable(this, R.drawable.die6));

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.roll_array,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				user_numRolls = position + 1;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		rollButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				roll(user_numRolls);
			}
		});

		Display display = getWindowManager().getDefaultDisplay();
		Point p = new Point();
		display.getSize(p);
		width = p.x;
		height = p.y;

		System.out.println("SupportActionBar height: " + getSupportActionBar().getHeight());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawericon);
		// setup drawer
		drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

		drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {

			}

			@Override
			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(R.string.drawer_title);
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				getSupportActionBar().setTitle(R.string.app_name);
			}

			@Override
			public void onDrawerStateChanged(int newState) {

			}
		});

		seekBar = (SeekBar) this.findViewById(R.id.seekBar1);

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int progress = 0;

			@Override
			public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
				progress = progresValue;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Toast.makeText(getApplicationContext(), "Changing seekbar's
				// progress", Toast.LENGTH_SHORT).show();
				Toast.makeText(getApplicationContext(), "Delay changed to " + String.valueOf(progress),
						Toast.LENGTH_SHORT).show();
				ANIMATION_DELAY = progress;
			}
		});

		// don't let the drawer slide closed when you swipe left to change the
		// seekbar
		// sourced from
		// http://stackoverflow.com/questions/18400910/seekbar-in-a-navigationdrawer
		seekBar.setOnTouchListener(new ListView.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					// Disallow Drawer to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(true);
					break;

				case MotionEvent.ACTION_UP:
					// Allow Drawer to intercept touch events.
					v.getParent().requestDisallowInterceptTouchEvent(false);
					break;
				}

				// Handle seekbar touch events.
				v.onTouchEvent(event);
				return true;
			}
		});

		// drawerLayout.addView(seekBar, 0);

		resetGrid();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (drawerLayout.isDrawerOpen(GravityCompat.START)) {

				drawerLayout.closeDrawers();
			} else {

				drawerLayout.openDrawer(GravityCompat.START);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void roll(int numRolls) {
		setButtonsState(false);
		currentRolls = new int[numRolls];
		for (int i = 0; i < numRolls; ++i) {
			currentRolls[i] = rand.nextInt(6) + 1;
		}
		fillGrid();
	}

	private void setUserScore(final int newScore) {
		userScore = newScore;
		txtYourScore.setText(String.valueOf(newScore));
	}

	private void setComputerScore(final int newScore) {
		computerScore = newScore;
		txtComputerScore.setText(String.valueOf(newScore));
	}

	private void setTurnTotal(final int newTotal) {
		turnTotal = newTotal;
		txtTurnTotal.setText(String.valueOf(newTotal));
	}

	private void changeTurn() {
		if (userScore >= GOAL || computerScore >= GOAL) {
			endGame();
		} else {
			isUserTurn = !isUserTurn;
			setButtonsState(isUserTurn);
			if (!isUserTurn) {
				computerTurn();
			}
		}
	}

	private void computerTurn() {
		new Thread((new Runnable() {
			@Override
			public void run() {
				Thread.yield();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int numRolls = 0;

				if(!hasComputed) {
					numRolls = rand.nextInt((6 - 4) + 1) + 4;
				}
				else if (computerLevel == "hard" && hasComputed) {
					numRolls = advise(userScore, computerScore, dice);
				} else if ((computerLevel == "medium" || computerLevel == "easy") && hasComputed) {
					numRolls = advise(userScore, computerScore, dice);
				}


				final int nRolls = numRolls;

				final SynchronousQueue<Boolean> queue = new SynchronousQueue<Boolean>();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						imageGrid.removeAllViews();
						imageGrid.setColumnCount(1);
						imageGrid.setRowCount(1);

						TextView tv = new TextView(MainActivity.this);
						tv.setText("Computer will \nroll " + nRolls + " Dice.");
						tv.setTextAppearance(MainActivity.this, android.R.style.TextAppearance_Medium);
						imageGrid.addView(tv);
						imageGrid.postInvalidate();
						try {
							queue.take();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});

				try {
					queue.put(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				roll(numRolls);
			}
		})).start();
	}

	private void setButtonsState(boolean state) {
		rollButton.setEnabled(state);
		spinner.setEnabled(state);
	}

	private void endGame() {
		String message;

		if(!nextGameDifficulty.isEmpty()){
			message = "User has chosen to reset the game";
			if(nextGameDifficulty == "easy") {
				buttonEasy();
			}
			else if (nextGameDifficulty == "medium") {
				buttonMedium();
			}
			else if (nextGameDifficulty == "hard"){
				buttonHard();
			}
		}
		else{

			message = (!isUserTurn) ? String.format("Computer won %d to %d.", computerScore, userScore)
					: String.format("You win %d to %d.", userScore, computerScore);
		}


		message += "  Would you like to play again?";
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setCancelable(false)
		.setPositiveButton("New Game", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				setUserScore(0);
				setComputerScore(0);
				setTurnTotal(0);
				userStartGame = !userStartGame;
				isUserTurn = userStartGame;
				setButtonsState(isUserTurn);
				if (!isUserTurn) {
					computerTurn();

				} else {
					resetGrid();
				}
				dialog.cancel();
			}
		}).setNegativeButton("Quit", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				MainActivity.this.finish();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	// protected void onSaveInstanceState(Bundle outState) {
	// super.onSaveInstanceState(outState);
	// outState.putInt("userScore", userScore);
	// outState.putInt("computerScore", computerScore);
	// outState.putInt("turnTotal", turnTotal);
	// outState.putBoolean("userStartGame", userStartGame);
	// outState.putBoolean("isUserTurn", isUserTurn);
	// outState.putString("imageName", imageName);
	// }
	//
	// protected void onRestoreInstanceState(Bundle savedInstanceState) {
	// super.onRestoreInstanceState(savedInstanceState);
	// setUserScore(savedInstanceState.getInt("userScore", 0));
	// setComputerScore(savedInstanceState.getInt("computerScore", 0));
	// setTurnTotal(savedInstanceState.getInt("turnTotal", 0));
	// setImage(savedInstanceState.getString("imageName"));
	// userStartGame = savedInstanceState.getBoolean("userStartGame", true);
	// isUserTurn = savedInstanceState.getBoolean("isUserTurn", true);
	// setButtonsState();
	// if (userScore >= GOAL_SCORE || computerScore >= GOAL_SCORE)
	// endGame();
	// else if (!isUserTurn)
	// computerTurn();
	// }

	private void resetGrid() {

		int size = (int) Math.min(width, 0.5 * height);
		imageGrid.removeAllViews();
		imageGrid.setColumnCount(1);
		imageGrid.setRowCount(1);

		ImageView img = new ImageView(this);
		GridLayout.LayoutParams param = new GridLayout.LayoutParams();
		param.width = size;
		param.height = size;
		img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.selectnrollnew));
		img.setLayoutParams(param);
		img.setScaleType(ImageView.ScaleType.FIT_XY);
		imageGrid.addView(img);
	}

	private void fillGrid() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Thread.yield();
				try {
					Thread.sleep(ANIMATION_DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				final SynchronousQueue<Boolean> queue = new SynchronousQueue<Boolean>();
				final int size = (int) Math.min(width, 0.5 * height);
				double sqrt = Math.sqrt(currentRolls.length);
				final int row = (sqrt == (int) sqrt) ? (int) sqrt : (int) sqrt + 1;

				try {
					Thread.sleep(ANIMATION_DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						imageGrid.removeAllViews();
						imageGrid.setColumnCount(row);
						imageGrid.setRowCount(row);
						setTurnTotal(0);
						imageGrid.postInvalidate();
						try {
							queue.take();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
				try {
					queue.put(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				for (int i = 0; i < currentRolls.length; ++i) {
					try {
						Thread.sleep(ANIMATION_DELAY);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					GridLayout.LayoutParams param = new GridLayout.LayoutParams();
					param.width = size / row;
					param.height = size / row;
					final ImageView img = new ImageView(MainActivity.this);
					img.setImageDrawable(drawableMap.get("die" + currentRolls[i]));
					img.setLayoutParams(param);
					img.setScaleType(ImageView.ScaleType.FIT_XY);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							imageGrid.addView(img);
							try {
								queue.take();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					});
					try {
						queue.put(true);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						int sum = 0;
						for (int i = 0; i < currentRolls.length; ++i) {
							if (currentRolls[i] == 1) {
								sum = 0;
								break;
							}
							sum += currentRolls[i];
						}
						if (sum == 0) {

							setTurnTotal(0);
						} else {
							setTurnTotal(sum);
							if (isUserTurn) {
								setUserScore(userScore + turnTotal);
							} else {
								setComputerScore(computerScore + turnTotal);
							}
						}
						changeTurn();
						try {
							queue.take();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
				try {
					queue.put(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	/******************************************************************************************************************************/

	/**
	 * Shows the splash screen to allow the user to enter their name, reset
	 * stats, and choose difficulty before the game starts
	 **/
	/******************************************************************************************************************************/
	protected void showSplashScreen() {
		mSplashDialog = new Dialog(this, R.style.AppTheme);
		mSplashDialog.setContentView(R.layout.difficulty_main);

		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		buttonEasy = (Button) mSplashDialog.findViewById(R.id.buttonEasy);
		buttonMedium = (Button) mSplashDialog.findViewById(R.id.buttonMedium);
		buttonHard = (Button) mSplashDialog.findViewById(R.id.buttonHard);



		editTextEnteredText = (EditText) mSplashDialog.findViewById(R.id.editTextEnteredText);
		//checkBoxResetStats = (CheckBox) mSplashDialog.findViewById(R.id.checkBoxReset);

		/*
		if(!userName.equals("User's")) {
			editTextEnteredText.setHint("Name Currently Saved As: " + userName.substring(0, userName.length()-2));
		}
		 */


		/*
		checkBoxResetStats.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(checkBoxResetStats.isChecked()) {
					resetStats();
				}
			}
		});
		 */

		buttonEasy.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				buttonEasy();
				removeSplashScreen();
			}
		});

		buttonMedium.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				buttonMedium();
				removeSplashScreen();
			}
		});

		buttonHard.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				buttonHard();
				removeSplashScreen();
			}
		});


		mSplashDialog.setCancelable(false);
		mSplashDialog.show();
	}

	public void buttonEasy() {
		/*String inp=editTextEnteredText.getText().toString();
		if(inp.length()>15) {
			inp=inp.substring(0,15);
		}

		if(!inp.equals("")) {
			setUserName(inp.trim() + "'s");
		}
		 */

		computerLevel="easy";

		maxDice = 30;
		goal = 100;
		theta = 1e-9;
		winweight = .2;

		createComputer("easyPlayer.dat");
		/*
		if(checkBoxResetStats.isChecked()) {
			resetStats();
		}
		 */
	}

	public void buttonMedium() {
		//String inp=editTextEnteredText.getText().toString();
		/*
		if(!inp.equals("")) {
			setUserName(inp.trim() + "'s");
		}
		 */
		computerLevel="medium";

		maxDice = 30;
		goal = 100;
		theta = 1e-9;
		winweight = .9;

		createComputer("mediumPlayer.dat");

		//
		/*
		if(checkBoxResetStats.isChecked()) {
			resetStats();
		}
		 */
	}

	public void buttonHard() {
		//String inp=editTextEnteredText.getText().toString();
		/*
		if(!inp.equals("")) {
			setUserName(inp.trim() + "'s");
		}
		 */
		computerLevel="hard";

		maxDice = 30;
		goal = 100;
		theta = 1e-9;

		createComputer("hardPlayer.dat");



		/*
		if(checkBoxResetStats.isChecked()) {
			resetStats();
		}
		 */ 
	}

	/********************************************************/

	/** Removes the Dialog that displays the splash screen **/
	/********************************************************/
	protected void removeSplashScreen() {
		if (mSplashDialog != null) {
			// mix();
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			mSplashDialog.dismiss();
			mSplashDialog = null;
		}
	}

	public int[][] copy(int[][] input) {
		int[][] toReturn = new int[100][100];

		for(int i=0;i<input.length;i++) {
			for (int j=0; j<input[i].length; j++) {
				toReturn[i][j] = input[i][j];
			}
		}
		return toReturn;
	}

	public int advise(int playerScore, int opponentScore, int[][] die) {
		while (true) {
			try {
				if (playerScore >= 0 && opponentScore >= 0 && playerScore < GOAL && opponentScore < GOAL)
					return die[playerScore][opponentScore];
			}
			catch (Exception e) {
				continue;
			}
		}
	}

	public void createComputer(final String fileName) {
		//final String FILENAME = "data.txt";

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					//String path = ctx.getFilesDir().getPath();
					FileInputStream inFile = openFileInput(fileName);
					if (inFile.available() == 0) {
						deleteFile(fileName);
						IOException e = new IOException();
						throw e;
					}

					Scanner scanner = new Scanner(inFile);
					int[][] inMatrix = new int[100][100];

					for(int i=0;i<inMatrix.length;i++) {
						for (int j=0; j<inMatrix[i].length; j++) {
							inMatrix[i][j] = scanner.nextInt();
						}
					}

					dice = copy(inMatrix);
					hasComputed = true;

				} catch (IOException e) { 
					try {
						if(computerLevel == "hard") {
							hardSolver = new HogSolver(maxDice, goal, theta);
							dice = copy(hardSolver.dice);

						}
						else if(computerLevel == "medium" || computerLevel == "easy") {
							easyMediumSolver = new RiskAverseHogSolver(maxDice, goal, theta, winweight);
							dice = copy(easyMediumSolver.dice);
						}

						hasComputed = true;

						FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
						writer.write("");

						runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(getApplicationContext(), "AI done computing", Toast.LENGTH_SHORT).show();
							}
						});

						for(int i=0;i<dice.length;i++) {
							for (int j=0; j<dice[i].length; j++) {
								writer.write(dice[i][j] + " ");
							}
						}		
						writer.flush();
					} catch (IOException e1) {}
				} 
			}
		}).start();
	}

	public void shouldRecordStats() {
		//final boolean toReturn;

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					endGame();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Would you like to immedietly restart, or wait until your current game is complete?").setPositiveButton("Restart", dialogClickListener)
		.setNegativeButton("Wait", dialogClickListener).show();
		//return toReturn;
	}
}