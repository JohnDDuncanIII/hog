package edu.gettysburg.hog;


import android.app.AlertDialog;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

public class MainActivity extends AppCompatActivity {

    /**
     * Delay between displaying each die roll (Only used for animation)
     */
    protected static final long ANIMATION_DELAY = 100;

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
     * Difficulty level for the human player. Higher level corresponds to better computer AI
     */
    private String difficulLevel = "Easy";

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
        setContentView(R.layout.activity_main);


        //Initialize GUI components
        txtYourScore = ((TextView) findViewById(R.id.your_score_value));
        txtComputerScore = ((TextView) findViewById(R.id.computer_score_value));
        txtTurnTotal = ((TextView) findViewById(R.id.turn_total_value));

        rollButton = ((ImageButton) findViewById(R.id.rollButton));

        imageGrid = ((GridLayout) findViewById(R.id.image_grid));
        //TODO: change imageGrid parameter so that the views start showing from left rather than center

        spinner = (Spinner) findViewById(R.id.roll_spinner);



        drawableMap.put("die1", ContextCompat.getDrawable(this, R.drawable.die1cross));
        drawableMap.put("die2", ContextCompat.getDrawable(this, R.drawable.die2));
        drawableMap.put("die3", ContextCompat.getDrawable(this, R.drawable.die3));
        drawableMap.put("die4", ContextCompat.getDrawable(this, R.drawable.die4));
        drawableMap.put("die5", ContextCompat.getDrawable(this, R.drawable.die5));
        drawableMap.put("die6", ContextCompat.getDrawable(this, R.drawable.die6));


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roll_array, android.R.layout.simple_spinner_item);
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

        resetGrid();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            if(drawerLayout.isDrawerOpen(GravityCompat.START)) {

                drawerLayout.closeDrawers();
            }
            else {

                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void roll(int numRolls)
    {
        setButtonsState(false);
        currentRolls = new int[numRolls];
        for(int i = 0; i < numRolls; ++i){
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
        if(userScore >= GOAL || computerScore >= GOAL)
        {
            endGame();
        }
        else
        {
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
                final int numRolls = rand.nextInt(25) + 1;
                final SynchronousQueue<Boolean> queue = new SynchronousQueue<Boolean>();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageGrid.removeAllViews();
                        imageGrid.setColumnCount(1);
                        imageGrid.setRowCount(1);

                        TextView tv = new TextView(MainActivity.this);
                        tv.setText("Computer will \nroll " + numRolls + " Dice.");
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
        String message = (!isUserTurn)
                ? String.format("Computer won %d to %d.", computerScore, userScore)
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
                        setButtonsState(isUserTurn);
                        if(!isUserTurn) {
                            computerTurn();

                        }
                        else{
                            resetGrid();
                        }
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

//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putInt("userScore", userScore);
//        outState.putInt("computerScore", computerScore);
//        outState.putInt("turnTotal", turnTotal);
//        outState.putBoolean("userStartGame", userStartGame);
//        outState.putBoolean("isUserTurn", isUserTurn);
//        outState.putString("imageName", imageName);
//    }
//
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        setUserScore(savedInstanceState.getInt("userScore", 0));
//        setComputerScore(savedInstanceState.getInt("computerScore", 0));
//        setTurnTotal(savedInstanceState.getInt("turnTotal", 0));
//        setImage(savedInstanceState.getString("imageName"));
//        userStartGame = savedInstanceState.getBoolean("userStartGame", true);
//        isUserTurn = savedInstanceState.getBoolean("isUserTurn", true);
//        setButtonsState();
//        if (userScore >= GOAL_SCORE || computerScore >= GOAL_SCORE)
//            endGame();
//        else if (!isUserTurn)
//            computerTurn();
//    }

    private void resetGrid() {

        int size = (int)Math.min(width, 0.5 * height);
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


    private void fillGrid(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.yield();
                try { Thread.sleep(ANIMATION_DELAY); }
                catch (InterruptedException e) { e.printStackTrace(); }
                final SynchronousQueue<Boolean> queue = new SynchronousQueue<Boolean>();
                final int size = (int)Math.min(width, 0.5 * height);
                double sqrt = Math.sqrt(currentRolls.length);
                final int row = (sqrt == (int) sqrt) ? (int)sqrt : (int)sqrt  + 1;

                try { Thread.sleep(ANIMATION_DELAY); }
                catch (InterruptedException e) { e.printStackTrace(); }
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

                for(int i = 0; i < currentRolls.length; ++i) {
                    try {
                        Thread.sleep(ANIMATION_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                    param.width = size/row;
                    param.height = size/row;
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
                        for(int i = 0; i < currentRolls.length; ++i){
                            if(currentRolls[i] == 1){
                                sum = 0;
                                break;
                            }
                            sum += currentRolls[i];
                        }
                        if(sum == 0){

                            setTurnTotal(0);
                        }
                        else {
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



}