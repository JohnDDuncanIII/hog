package edu.gettysburg.hog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class CreditsDialog extends Dialog implements android.view.View.OnClickListener {
	
	  public Activity c;
	  public Dialog d;
	  public Button close;

	  public CreditsDialog(Activity a) {
	    super(a);
	    // TODO Auto-generated constructor stub
	    this.c = a;
	  }

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
//	    this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.string.credits );
//	    this.setTitle(R.string.credits);
//	    requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    setContentView(R.layout.credits_main);
	    
	    this.getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
	    this.getWindow().getAttributes().height = WindowManager.LayoutParams.MATCH_PARENT;
	    
	    close = (Button) findViewById(R.id.buttonCloseCredits);
	    close.setOnClickListener(this);

	  }

	  @Override
	  public void onClick(View v) {
	    dismiss();
	  }
}
