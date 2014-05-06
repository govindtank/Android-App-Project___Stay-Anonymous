package mobile_final;

/*
 * Some parts of the following code uses some code from google's demo.
 */

/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.backend.R;
import com.google.cloud.backend.core.CloudBackendFragment;
import com.google.cloud.backend.core.CloudBackendFragment.OnListener;
import com.google.cloud.backend.core.CloudCallbackHandler;
import com.google.cloud.backend.core.CloudEntity;
import com.google.cloud.backend.core.CloudQuery.Order;
import com.google.cloud.backend.core.CloudQuery.Scope;
import com.google.cloud.backend.core.Consts;

/**
 * Sample Guestbook app with Mobile Backend Starter.
 */
public class ChattingRoomActivity extends Activity implements OnListener {

	private static final String		BROADCAST_PROP_DURATION	= "duration";
	private static final String		BROADCAST_PROP_MESSAGE	= "message";
	private static final String		debugTag				= "ChattingRoomActivity";

	private static final String		PROCESSING_FRAGMENT_TAG	= "BACKEND_FRAGMENT";
	// private static final String SPLASH_FRAGMENT_TAG = "SPLASH_FRAGMENT";

	// public static final String GUESTBOOK_SHARED_PREFS =
	// "GUESTBOOK_SHARED_PREFS";
	public static final String		SCOPE_PREFS_KEY			= "SCOPE_PREFS_KEY";

	/*
	 * For chatting
	 */
	private ListView				messageListView;
	private TextView				stubView;
	private EditText				textField;
	private ImageView				sendButton;
	// private TextView mAnnounceTxt;

	private FragmentManager			fragmentManager;
	private CloudBackendFragment	processFragment;
	// private SplashFragment mSplashFragment;
	private List<CloudEntity>		allPosts				= new LinkedList<CloudEntity>();

	/*
	 * For Vote
	 */

	private VoteHandler				voteHandler;
	private BroadcastReceiver		messageReceiver;
	private Context					context;

	/*
	 * For general room property
	 */
	private boolean					isRoomHolder;
	private String					roomTitle;
	private ProgressDialog			pDialog;

	/**
	 * Override Activity lifecycle method.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		isRoomHolder = false;
		roomTitle = "defaultRoom";
		pDialog = new ProgressDialog(this);
		pDialog.setMessage("Refreshing...");

		// Extract intent
		Intent receivingIntent = getIntent();
		if (receivingIntent != null && receivingIntent.getExtras() != null) {

			roomTitle = receivingIntent.getExtras().getString("joinTitle") != null ? receivingIntent
					.getExtras().getString("joinTitle") : "defaultRoom";
			String isInstructor = receivingIntent.getExtras().getString("isInstructor") != null ? receivingIntent
					.getExtras().getString("isInstructor") : "no";
			isRoomHolder = isInstructor.equals("yes") ? true : false;
		}

		// Create voteHandler
		voteHandler = new VoteHandler(this);
		messageReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d("messagetest", "Receiving msg");
				sendMessage((HashMap<String, String>) intent.getExtras().get("map"));
			}
		};
		LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
				new IntentFilter("SEND_MESSAGE_L"));

		// Create the view
		messageListView = (ListView) findViewById(R.id.posts_list);
		stubView = (TextView) findViewById(R.id.no_messages);
		textField = (EditText) findViewById(R.id.message);
		textField.setHint("Type message");
		textField.setEnabled(false);
		sendButton = (ImageView) findViewById(R.id.send_btn);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSendButtonPressed(v);
			}
		});
		sendButton.setEnabled(false);
		// mAnnounceTxt = (TextView) findViewById(R.id.announce_text);

		fragmentManager = getFragmentManager();

		initiateFragments();
	}

	/**
	 * Override Activity lifecycle method.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chatting_room, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Override Activity lifecycle method.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem loginItem = menu.findItem(R.id.switch_account);
		loginItem.setVisible(Consts.IS_AUTH_ENABLED);
		MenuItem voteItem = menu.findItem(R.id.vote);
		voteItem.setVisible(true);
		MenuItem voteResultItem = menu.findItem(R.id.voteResult);
		return true;
	}

	/**
	 * Override Activity lifecycle method.
	 * <p>
	 * To add more option menu items in your client, add the item to
	 * menu/activity_main.xml, and provide additional case statements in this
	 * method.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.switch_account:
				processFragment.signInAndSubscribe(true);
				return true;
			case R.id.vote:
				voteHandler.raiseAVote(10, "testVote");
				return true;
			case R.id.voteResult:
				voteHandler.showResult();
				return true;
			case R.id.refresh:
				refresh();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * Method called via OnListener in {@link CloudBackendFragment}.
	 */
	@Override
	public void onCreateFinished() {
		Log.d(debugTag, "list post");
		listPosts();
	}

	/**
	 * Method called via OnListener in {@link CloudBackendFragment}.
	 */
	@Override
	public void onBroadcastMessageReceived(List<CloudEntity> l) {
		for (CloudEntity e : l) {
			String message = (String) e.get(BROADCAST_PROP_MESSAGE);
			int duration = Integer.parseInt((String) e.get(BROADCAST_PROP_DURATION));
			Toast.makeText(this, message, duration).show();
			Log.i(Consts.TAG, "A message was recieved with content: " + message);
		}
	}

	private void initiateFragments() {
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		// Check to see if we have retained the fragment which handles
		// asynchronous backend calls
		// processFragment = (CloudBackendFragment) fragmentManager
		// .findFragmentByTag(PROCESSING_FRAGMENT_TAG);
		// If not retained (or first time running), create a new one
		if (processFragment == null) {
			processFragment = new CloudBackendFragment();
			processFragment.setRetainInstance(true);
			// fragmentTransaction.add(processFragment,
			// PROCESSING_FRAGMENT_TAG);
		}

		// // Add the splash screen fragment
		// mSplashFragment = new SplashFragment();
		// fragmentTransaction.add(R.id.activity_main, mSplashFragment,
		// SPLASH_FRAGMENT_TAG);
		fragmentTransaction.commit();
	}

	/*
	 * sendMessage for general use : vote etc
	 */
	private void sendMessage(HashMap<String, String> message) {
		// create a CloudEntity with the new post
		CloudEntity newPost = new CloudEntity("Guestbook");

		// see what we have in the message
		Set<Entry<String, String>> set = message.entrySet();

		Iterator<Entry<String, String>> setIterator = set.iterator();
		while (setIterator.hasNext()) {
			Entry<String, String> mapEntry = setIterator.next();
			newPost.put(mapEntry.getKey(), mapEntry.getValue());
		}

		newPost.put("room_title", roomTitle);

		CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
			@Override
			public void onComplete(final CloudEntity result) {
				Log.d("test", "it's completed : " + result.toString());
			}

			@Override
			public void onError(final IOException exception) {
				handleEndpointException(exception);
			}
		};

		processFragment.getCloudBackend().insert(newPost, handler);

	}

	/**
	 * onClick method.
	 */
	public void onSendButtonPressed(View view) {

		// create a CloudEntity with the new post
		CloudEntity newPost = new CloudEntity("Guestbook");
		newPost.put("api", "chat");
		newPost.put("room_title", roomTitle);
		newPost.put("message", textField.getText().toString());
		// create a response handler that will receive the result or an error
		CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
			@Override
			public void onComplete(final CloudEntity result) {
				allPosts.add(0, result);
				updateChatRoomView();
				textField.setText("");
				textField.setEnabled(true);
				sendButton.setEnabled(true);
			}

			@Override
			public void onError(final IOException exception) {
				handleEndpointException(exception);
			}
		};

		// execute the insertion with the handler
		processFragment.getCloudBackend().insert(newPost, handler);
		textField.setEnabled(false);
		sendButton.setEnabled(false);
	}

	/*
	 * Refresh request
	 */

	private void refresh() {
		request(Scope.FUTURE_AND_PAST);
	}

	private void request(Scope s) {

		// create a response handler that will receive the result or an error
		CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
			@Override
			public void onComplete(List<CloudEntity> results) {
				// First we need to distinguish the chat messages and other APIs
				// such as vote etc.
				pDialog.dismiss();
				if (!results.isEmpty()) {
					LinkedList<CloudEntity> chatMsgs = new LinkedList<CloudEntity>();
					// mAnnounceTxt.setText(R.string.announce_success);
					for (CloudEntity ce : results) {
						if ((ce.get("api")) != null) {

							if ((ce.get("api")).equals("chat")) {
								// seperating rooms
								if (ce.get("room_title") != null) {
									if (ce.get("room_title").equals(roomTitle)) {
										chatMsgs.add(ce);
									}
								}

							}
						}

					}

					allPosts = chatMsgs;
					// animateArrival();
					updateChatRoomView();

					// send broadcast to voter

					CloudEntity latestMsg = results.get(0);
					Toast.makeText(context, "newPost : " + latestMsg.getId(), Toast.LENGTH_SHORT)
							.show();

					Map<String, Object> resultMap = latestMsg.getProperties();
					Set<Entry<String, Object>> resultSet = resultMap.entrySet();
					Iterator<Entry<String, Object>> st = resultSet.iterator();
					HashMap<String, String> map = new HashMap<String, String>();
					while (st.hasNext()) {
						Entry<String, Object> currentEntry = st.next();
						map.put(currentEntry.getKey(), currentEntry.getValue().toString());
					}
					if (map.get("room_title") != null) {
						// Toast.makeText(context, "roomTitle"
						// +map.get("room_title"), Toast.LENGTH_SHORT).show();

						if (map.get("room_title").equals(roomTitle)) {
							map.put("id", latestMsg.getId());
							Intent intent = new Intent("RECEIVE_MESSAGE_L");
							intent.putExtra("map", map);
							LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
						}
					}
					else {
						Toast.makeText(context, "roomTitle is null", Toast.LENGTH_SHORT).show();
					}

				}
				else {
					// animateArrival();
					updateChatRoomView();
				}

			}

			@Override
			public void onError(IOException exception) {
				// mAnnounceTxt.setText(R.string.announce_fail);
				// animateArrival();
				handleEndpointException(exception);
			}
		};
		// execute the query with the handler
		pDialog.show();
		processFragment.getCloudBackend().listByKind("Guestbook", CloudEntity.PROP_CREATED_AT,
				Order.DESC, 50, Scope.FUTURE_AND_PAST, handler);

	}

	/**
	 * Retrieves the list of all posts from the backend and updates the UI. For
	 * demonstration in this sample, the query that is executed is:
	 * "SELECT * FROM Guestbook ORDER BY _createdAt DESC LIMIT 50" This query
	 * will be re-executed when matching entity is updated.
	 */
	private void listPosts() {

		request(Scope.FUTURE_AND_PAST);
	}

	// private boolean firstArrival = true;
	// private void animateArrival() {
	// FragmentTransaction fragmentTransaction =
	// fragmentManager.beginTransaction();
	// mSplashFragment = (SplashFragment) fragmentManager.findFragmentByTag(
	// SPLASH_FRAGMENT_TAG);
	// if (mSplashFragment != null) {
	// fragmentTransaction.remove(mSplashFragment);
	// fragmentTransaction.commitAllowingStateLoss();
	// }
	//
	// // if (firstArrival) {
	// // mAnnounceTxt.setVisibility(View.VISIBLE);
	// // Animation anim = AnimationUtils.loadAnimation(this,
	// R.anim.translate_progress);
	// // anim.setAnimationListener(new Animation.AnimationListener() {
	// //
	// // @Override
	// // public void onAnimationStart(Animation animation) {
	// // }
	// //
	// // @Override
	// // public void onAnimationRepeat(Animation animation) {
	// // }
	// //
	// // @Override
	// // public void onAnimationEnd(Animation animation) {
	// // mAnnounceTxt.setVisibility(View.GONE);
	// // }
	// // });
	// //// mAnnounceTxt.startAnimation(anim);
	// // firstArrival = false;
	// // }
	// }

	private void updateChatRoomView() {
		textField.setEnabled(true);
		sendButton.setEnabled(true);
		if (!allPosts.isEmpty()) {
			stubView.setVisibility(View.GONE);
			messageListView.setVisibility(View.VISIBLE);
			messageListView.setAdapter(new PostAdapter(this, android.R.layout.simple_list_item_1,
					allPosts));
		}
		else {
			stubView.setVisibility(View.VISIBLE);
			messageListView.setVisibility(View.GONE);
		}
	}

	private void handleEndpointException(IOException e) {
		Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		sendButton.setEnabled(true);
	}

}
