package edu.gettysburg.hog;


import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    int user_numRolls = 1;
    static HashMap<String, Drawable> drawableMap = new HashMap<String, Drawable>();
    boolean isPlayer = true;
    Random rand = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawableMap.put("die1", ContextCompat.getDrawable(this, R.drawable.die1cross));
        drawableMap.put("die2", ContextCompat.getDrawable(this, R.drawable.die2));
        drawableMap.put("die3", ContextCompat.getDrawable(this, R.drawable.die3));
        drawableMap.put("die4", ContextCompat.getDrawable(this, R.drawable.die4));
        drawableMap.put("die5", ContextCompat.getDrawable(this, R.drawable.die5));
        drawableMap.put("die6", ContextCompat.getDrawable(this, R.drawable.die6));



        Spinner spinner = (Spinner) findViewById(R.id.roll_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roll_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
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
        resetGrid();
        ImageButton rollButton = (ImageButton)findViewById(R.id.rollButton);
        rollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roll();
            }
        });


    }

    private void roll()
    {
        int[] rolls = new int[user_numRolls];
        for(int i = 0; i < user_numRolls; ++i){
            rolls[i] = rand.nextInt(6) + 1;
        }
        fillGrid(rolls);

    }

    private void resetGrid() {
        Display display = getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        double width = p.x;
        double height = p.y;
        int size = (int)Math.min(width, 0.5*height);
        GridLayout imageGrid = (GridLayout) findViewById(R.id.image_grid);
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

    private void fillGrid(int[] outcomes){
        GridLayout imageGrid = (GridLayout) findViewById(R.id.image_grid);
        imageGrid.removeAllViews();
        Display display = getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        double width = p.x;
        double height = p.y;
        int gridWidth = (int)Math.min(width, 0.5*height);
        double sqrt = Math.sqrt(outcomes.length);
        int row = (sqrt == (int) sqrt) ? (int)sqrt : (int)sqrt  + 1;
        imageGrid.setColumnCount(row);
        imageGrid.setRowCount(row);
        for(int i = 0; i < outcomes.length; ++i){
            ImageView img = new ImageView(this);
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.width = gridWidth/row;
            param.height = gridWidth/row;
            img.setImageDrawable(drawableMap.get("die" + outcomes[i]));
            img.setLayoutParams(param);
            img.setScaleType(ImageView.ScaleType.FIT_XY);
            imageGrid.addView(img);
        }
    }


}
