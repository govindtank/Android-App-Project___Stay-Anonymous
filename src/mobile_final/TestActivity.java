package mobile_final;



import com.google.cloud.backend.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import android.view.View.OnClickListener;
import android.widget.Button;

public class TestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_test);
		
		final VoteHandler voter = new VoteHandler(this);
		
		Button testButton = (Button) findViewById(R.id.testButton);
		testButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
 				voter.raiseAVote(10, "testVote");
			}
		});
	}



}
