package edu.gettysburg.hog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class InstructionsDialog extends Dialog implements android.view.View.OnClickListener {
	
	  public Activity c;
	  public Dialog d;
	  public Button done;

	  public InstructionsDialog(Activity a) {
	    super(a);
	    // TODO Auto-generated constructor stub
	    this.c = a;
	  }

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    setContentView(R.layout.instructions_main);
	    done = (Button) findViewById(R.id.buttonDone);
	    done.setOnClickListener(this);

	  }

	  @Override
	  public void onClick(View v) {
	    dismiss();
	  }
}
