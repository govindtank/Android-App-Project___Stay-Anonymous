package mobile_final;

import java.io.IOException;
import java.util.*;

import com.google.cloud.backend.*;
import com.google.cloud.backend.core.CloudBackend;
import com.google.cloud.backend.core.CloudEntity;
import com.google.cloud.backend.core.CloudQuery;
import com.google.cloud.backend.core.CloudQuery.Scope;
import com.google.cloud.backend.core.Filter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SecondActivity extends Activity {

	private EditText numPswd; //
	private TextView titleText; //
	private EditText joinTitle; // id: editText1


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);

		// attaching id to components
		joinTitle = (EditText) findViewById(R.id.editText1);
		Button addButton = (Button) findViewById(R.id.button1);
		Button enterButton = (Button) findViewById(R.id.button2);
		
		// enter a room (NON-instructor)
		enterButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v)	{

				// execute the Cloud Query to check if the room exists or not
				// Cloud Query
				boolean isValidRoom = false;
				
				CloudQuery cq = new CloudQuery("Guestbook");
				String titleString = joinTitle.getText().toString();
				
						
				cq.setFilter(Filter.eq("room_title", titleString));
				cq.setScope(Scope.FUTURE_AND_PAST);
				
				CloudBackend cb = new CloudBackend();
				try {
					List<CloudEntity> results = cb.list(cq);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				// if the room exist, then send the intent
				if(isValidRoom)	{
				
					Intent intent = new Intent(
							SecondActivity.this,
							ChattingRoomActivity.class);
					intent.putExtra("joinTitle", joinTitle
							.getText().toString());
					intent.putExtra("isInstructor", "no");
		
					startActivity(intent);
				}	else {
					
					AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
					LayoutInflater inflater = SecondActivity.this.getLayoutInflater();
					View contentView = inflater.inflate(R.layout.noroom, null);
					builder.setView(contentView).setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// I got it!
						}
					});	
					builder.create();
					builder.show();
				}
				
				
			}
		});
		
		
		// add a new room (for instructor)
		addButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(
						SecondActivity.this);

				// Get the layout inflater
				LayoutInflater inflater = SecondActivity.this
						.getLayoutInflater();
				final View contentView = inflater.inflate(R.layout.dialog_chatroom, null);
				builder.setView(contentView)
						.setPositiveButton("create",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,int which) {

										// if press "confirm", go to the room
										// with this name

	
										Intent intent = new Intent(
												SecondActivity.this,
												ChattingRoomActivity.class);
										
										// dffdfdfdf
										EditText editPassword = (EditText) contentView.findViewById(R.id.password);
										EditText editTitle = (EditText) contentView.findViewById(R.id.title);
										
										intent.putExtra("password", Integer.parseInt(editPassword.getText().toString()));
										intent.putExtra("joinTitle", joinTitle
												.getText().toString());

										intent.putExtra("isInstructor", "yes");
										startActivity(intent);

									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								});

				builder.create();
				builder.show();
			}

		});

		// 1. Instantiate an AlertDialog.Builder with its constructor
		// AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//
		// // 2. Chain together various setter methods to set the dialog
		// characteristics
		// builder.setMessage("abc")
		// .setTitle("def");
		//
		// // 3. Get the AlertDialog from create()
		// AlertDialog dialog = builder.create();
		// dialog.show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.second, menu);
		return true;
	}

}
