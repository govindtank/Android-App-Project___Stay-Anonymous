package mobile_final;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.cloud.backend.R;
import com.google.cloud.backend.core.CloudBackend;
import com.google.cloud.backend.core.CloudBackendAsync;
import com.google.cloud.backend.core.CloudBackendFragment;
import com.google.cloud.backend.core.CloudBackendFragment.OnListener;
import com.google.cloud.backend.core.CloudBackendMessaging;
import com.google.cloud.backend.core.CloudCallbackHandler;
import com.google.cloud.backend.core.CloudEntity;
import com.google.cloud.backend.core.CloudQuery;
import com.google.cloud.backend.core.CloudQuery.Order;
import com.google.cloud.backend.core.CloudQuery.Scope;
import com.google.cloud.backend.core.Consts;
import com.google.cloud.backend.core.Filter;

public class StartingActivity extends FragmentActivity implements OnListener {
	private static ViewPager		mViewPager;
	private static ViewPagerAdapter	mViewPagerAdapter;
	protected static boolean		isRoomHolder;
	protected static String			roomTitle;
	protected static String			roomDescription;
	protected static Context		mainContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_starting);
		getActionBar().hide();
		mainContext = this;
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		mViewPagerAdapter.switchFragmentSet(ViewPagerAdapter.ENTER_CREATE_SET);
		mViewPager.setAdapter(mViewPagerAdapter);
		mViewPager.setCurrentItem(2);
	}

	@Override
	public void onBackPressed() {
		if (ViewPagerAdapter.fragmentSet == ViewPagerAdapter.CHAT_VOTE_SET) {
			switchFragment(ViewPagerAdapter.ENTER_CREATE_SET);
		}
		else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.starting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) { return true; }
		if (id == R.id.action_enter) {
			switchFragment(mViewPagerAdapter.ENTER_CREATE_SET);
			return true;
		}
		if (id == R.id.action_chat) {
			switchFragment(mViewPagerAdapter.CHAT_VOTE_SET);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	protected static void switchFragment(int switchTo) {
		mViewPagerAdapter.switchFragmentSet(switchTo);
		mViewPager.setAdapter(mViewPagerAdapter);
	}

	/*
	 * Adapter of viewpager
	 */
	public static class ViewPagerAdapter extends FragmentStatePagerAdapter {
		private static int			fragmentSet;
		private static final int	ENTER_CREATE_SET	= -1;
		private static final int	CHAT_VOTE_SET		= -2;

		public ViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public void switchFragmentSet(int choice) {
			fragmentSet = choice;
		}

		@Override
		public android.support.v4.app.Fragment getItem(int choice) {
			android.support.v4.app.Fragment item = null;
			Activity main = ((Activity) mainContext);
			switch (choice) {
				case 0:
					if (fragmentSet == ENTER_CREATE_SET) {
						item = CreateEventFragment.newInstance(0);

					}
					else {
						item = ChattingRoomFragment.newInstance(ChattingRoomFragment.CHAT_ROOM);
					}

				break;
				case 1:
					if (fragmentSet == ENTER_CREATE_SET) {
						item = EventListFragment.newInstance();

					}

				break;
				case 2:
					// staring page
					if (fragmentSet == ENTER_CREATE_SET) {
						item = ViewPagerFragment.newInstance(ViewPagerFragment.STARTING_PAGE);

					}

				break;
				case 3:
					// chat & vote enter
					if (fragmentSet == ENTER_CREATE_SET) {
						item = ViewPagerFragment.newInstance(ViewPagerFragment.ENTER_A_ROOM);

					}

				break;
				case 4:
					// chat & vote create
					if (fragmentSet == ENTER_CREATE_SET) {
						item = ViewPagerFragment.newInstance(ViewPagerFragment.CREATE_A_ROOM);
					}

				break;
				default:
				break;
			}
			return item;
		}

		@Override
		public int getCount() {
			if (fragmentSet == ENTER_CREATE_SET) {
				return 5;
			}
			else {
				return 1;
			}

		}

	}

	/*
	 * Fragment of view pager
	 */
	public static class ViewPagerFragment extends android.support.v4.app.Fragment {

		private EditText					joinTitle;					// id:
																		// editText1

		private TextView					titleText;					//
		private EditText					editDescription;			// id:
																		// description

		static boolean						isValidRoom		= false;
		private SharedPreferences.Editor	roomistoryEditor;
		private SharedPreferences			roomHistory;

		public static final int				ENTER_A_ROOM	= -1;
		public static final int				CREATE_A_ROOM	= -2;
		public static final int				STARTING_PAGE	= -3;
		private ProgressDialog				pDialog;

		// private static int layoutChoice;

		// private int layoutChoice = 0;

		public static ViewPagerFragment newInstance(int mLayoutChoice) {
			Bundle extras = new Bundle();
			extras.putInt("choice", mLayoutChoice);
			ViewPagerFragment newInstance = new ViewPagerFragment();
			newInstance.setArguments(extras);
			// layoutChoice = mLayoutChoice;
			return newInstance;
		}

		private class QueryThread extends AsyncTask<Integer, Integer, String> {
			@Override
			protected String doInBackground(Integer... params) {

				CloudQuery cq = new CloudQuery("Guestbook");

				String titleString = joinTitle.getText().toString();

				cq.setFilter(Filter.eq("room_title", titleString));
				cq.setScope(Scope.PAST);

				CloudBackend cb = new CloudBackend();

				Log.i("Haha", "the thread starts here !!!!");
				String result = "false"; // set the result to true at first

				try {

					List<CloudEntity> results;

					results = cb.list(cq);
					if (results.size() != 0) {
						result = "true";
					}

					StringBuilder sb = new StringBuilder();

					sb.append(results.size());
					Log.i("Haha", "this is the size");
					Log.i("Haha", sb.toString());

				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Log.i("haha", "outside try-catch");

				return result;
			}
		}

		private class QueryThread2 extends AsyncTask<Integer, Integer, String> {

			// this so-called doInBackground function is empty here, I will do
			// it later
			@Override
			protected String doInBackground(Integer... params) {
				return null;

			}
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			int layoutChoice = getArguments().getInt("choice");
			// int layoutChoice = ENTER_A_ROOM;
			pDialog = new ProgressDialog(getActivity());
			View fragmentView = null;
			roomHistory = getActivity().getPreferences(Context.MODE_PRIVATE);
			roomistoryEditor = roomHistory.edit();
			// attaching id to components

			if (layoutChoice == ENTER_A_ROOM) {
				fragmentView = inflater.inflate(R.layout.fragment_enter_a_room, container, false);

				// textfield_room_id
				ImageButton enterButton = (ImageButton) fragmentView
						.findViewById(R.id.button_enter_room);
				joinTitle = (EditText) fragmentView.findViewById(R.id.textfield_room_id);

				// enter a room (NON-instructor)
				enterButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {

						// execute the Cloud Query to check if the room exists
						// or not
						// Cloud Query

						// new a Query thread
						QueryThread qThread = new QueryThread() {
							@Override
							protected void onPostExecute(String result) {
								// TODO Auto-generated method stub
								pDialog.dismiss();
								super.onPostExecute(result);

								if (result == "true") {
									isValidRoom = true;
								}
								else {
									isValidRoom = false;
								}

								// if the room exist, then send the intent
								if (isValidRoom) {

									// Intent intent = new Intent(getActivity(),
									// ChattingRoomActivity.class);
									// intent.putExtra("joinTitle",
									// joinTitle.getText().toString());
									// intent.putExtra("isInstructor", "no");
									//
									// startActivity(intent);

									roomTitle = joinTitle.getText().toString();
									if (roomHistory.getString(roomTitle, "fk").equals("yes")) {
										isRoomHolder = true;
									}
									else {
										isRoomHolder = false;
									}

									switchFragment(ViewPagerAdapter.CHAT_VOTE_SET);
								}
								else {

									AlertDialog.Builder builder = new AlertDialog.Builder(
											getActivity());
									LayoutInflater inflater = getActivity().getLayoutInflater();
									View contentView = inflater.inflate(R.layout.noroom, null);
									builder.setView(contentView).setPositiveButton("Got it!",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(DialogInterface dialog,
														int which) {
													// I got it!
												}
											});
									builder.create();
									builder.show();
								}

							}
						};
						pDialog.setMessage("Loading...");
						pDialog.show();
						qThread.execute(11);

					}
				});

			}
			else if (layoutChoice == CREATE_A_ROOM) {

				// binding id !!!!!
				fragmentView = inflater.inflate(R.layout.fragment_create_a_room, container, false);

				ImageButton addButton = (ImageButton) fragmentView
						.findViewById(R.id.button_create_room);
				final EditText editTitle = (EditText) fragmentView
						.findViewById(R.id.textfield_room_title); // id: title
				final EditText editDescription = (EditText) fragmentView
						.findViewById(R.id.textfield_room_description); // id:
				// description

				// add a new room (for instructor)
				addButton.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {

						/**
						 * Allen
						 * 
						 * in this block, these 2 final variables can be used in
						 * qThread2 because qThread2 is executed inside this
						 * block but these 2 variables must be set final because
						 * of multi-threading protection mechanism
						 * 
						 */

						// new a Query thread
						QueryThread2 qThread2 = new QueryThread2() {

							@Override
							protected String doInBackground(Integer... params) {

								CloudQuery cq = new CloudQuery("Guestbook");

								String editTitleString = editTitle.getText().toString();

								cq.setFilter(Filter.eq("room_title", editTitleString));
								cq.setScope(Scope.PAST);

								CloudBackend cb = new CloudBackend();

								Log.i("Haha", "HOLY SHIT");
								String result = "false"; // set the result to
								// true at first

								try {

									List<CloudEntity> results;

									results = cb.list(cq);
									if (results.size() != 0) {
										result = "true";
									}

								}
								catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								return result;
							}

							@Override
							protected void onPostExecute(String result) {
								pDialog.dismiss();
								super.onPostExecute(result);

								Log.i("haha", result); // alwasys false

								if (result == "true") {
									isValidRoom = true;
								}
								else {
									isValidRoom = false;
								}

								// if the room exist, then send the intent
								if (!isValidRoom) {
									// if no such room, then you are good to
									// create the room
									Log.i("haha", "I can create the room now !!!");
									// Intent intent = new Intent(getActivity(),
									// ChattingRoomActivity.class);
									//
									// intent.putExtra("joinTitle",
									// editTitle.getText().toString());
									// intent.putExtra("description",
									// editDescription.getText()
									// .toString());
									//
									// intent.putExtra("isInstructor", "yes");
									// startActivity(intent);

									roomTitle = editTitle.getText().toString();
									roomDescription = editDescription.getText().toString();
									isRoomHolder = true;
									roomistoryEditor.putString(roomTitle, "yes").commit();
									// HashMap<String, String> map = new
									// HashMap<String, String>();
									// map.put("api", "chat");
									// map.put("message",
									// "Welcome to the room!");
									// map.put("room_title", roomTitle);

									switchFragment(ViewPagerAdapter.CHAT_VOTE_SET);
								}
								else {
									Log.i("haha", "I cannot create the room !!!");
									// error message
									// using the same builder but different
									// contentView2

									AlertDialog.Builder builder = new AlertDialog.Builder(
											getActivity());
									View contentView2 = inflater.inflate(R.layout.hasroom, null);
									builder.setView(contentView2).setPositiveButton("Got it!",
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(DialogInterface dialog,
														int which) {
													// I got it!
												}
											});
									AlertDialog dialog2 = builder.create();
									dialog2.show();
								}

							}
						};
						pDialog.setMessage("Loading...");
						pDialog.show();
						qThread2.execute(12);
					}

				});
			}
			else if (layoutChoice == STARTING_PAGE) {

				fragmentView = inflater.inflate(R.layout.fragment_starting_page, container, false);
			}

			return fragmentView;
		}
	}

	/*
	 * Fragment of chattingroom
	 */

	public static class ChattingRoomFragment extends android.support.v4.app.Fragment {

		public static final int			CHAT_ROOM				= -1;
		public static final int			VOTE					= -2;

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
		private static ImageView		sendButton;
		// private TextView mAnnounceTxt;

		private FragmentManager			fragmentManager;
		// private fragmentManager;

		private CloudBackendFragment	processFragment;
		// private SplashFragment mSplashFragment;
		private List<CloudEntity>		allPosts				= new LinkedList<CloudEntity>();

		/*
		 * For Vote
		 */

		private VoteHandler				voteHandler;
		private BroadcastReceiver		messageReceiver;
		private Context					context;
		private ImageButton				btnRaise;

		/*
		 * For general room property
		 */

		private ProgressDialog			pDialog;
		private View					fragmentView;
		private ImageButton				btnRefresh;

		// private static int layoutChoice;

		// private int layoutChoice = 0;

		public static ChattingRoomFragment newInstance(int mLayoutChoice) {
			Bundle extras = new Bundle();
			extras.putInt("choice", mLayoutChoice);
			ChattingRoomFragment newInstance = new ChattingRoomFragment();
			newInstance.setArguments(extras);
			// layoutChoice = mLayoutChoice;
			return newInstance;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			context = getActivity();
			fragmentView = null;
			// fragmentManager = getChildFragmentManager();
			// initiateFragments();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			int layoutChoice = getArguments().getInt("choice");
			// int layoutChoice = ENTER_A_ROOM;

			if (layoutChoice == CHAT_ROOM) {
				fragmentView = inflater.inflate(R.layout.fragment_chat_room, container, false);
				// setContentView(R.layout.activity_main);

				// isRoomHolder = false;
				// roomTitle = "defaultRoom";

				// Extract intent
				// Intent receivingIntent = getIntent();
				// if (receivingIntent != null && receivingIntent.getExtras() !=
				// null) {
				//
				// roomTitle =
				// receivingIntent.getExtras().getString("joinTitle") !=
				// null ? receivingIntent
				// .getExtras().getString("joinTitle") : "defaultRoom";
				// String isInstructor =
				// receivingIntent.getExtras().getString("isInstructor") != null
				// ?
				// receivingIntent
				// .getExtras().getString("isInstructor") : "no";
				// isRoomHolder = isInstructor.equals("yes") ? true : false;
				// }

				messageReceiver = new BroadcastReceiver() {

					@Override
					public void onReceive(Context context, Intent intent) {
						Log.d("messagetest", "Receiving msg");
						sendMessage((HashMap<String, String>) intent.getExtras().get("map"));
					}
				};
				LocalBroadcastManager.getInstance(context).registerReceiver(messageReceiver,
						new IntentFilter("SEND_MESSAGE_L"));

				// Create the view
				messageListView = (ListView) fragmentView.findViewById(R.id.posts_list);
				stubView = (TextView) fragmentView.findViewById(R.id.no_messages);
				textField = (EditText) fragmentView.findViewById(R.id.message);
				textField.setHint("Type message");
				textField.setEnabled(false);
				sendButton = (ImageView) fragmentView.findViewById(R.id.send_btn);
				sendButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onSendButtonPressed(v);
					}
				});
				sendButton.setEnabled(false);
				// mAnnounceTxt = (TextView) findViewById(R.id.announce_text);
				fragmentManager = getChildFragmentManager();
				initiateFragments();

				// Create voteHandler
				voteHandler = new VoteHandler(context);
				btnRaise = (ImageButton) fragmentView.findViewById(R.id.btn_vote);
				if (!isRoomHolder) {
					btnRaise.setVisibility(View.GONE);
				}
				btnRaise.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (voteHandler.hasResult()) {
							voteHandler.showResult();
							Toast.makeText(context, "fkkk", Toast.LENGTH_SHORT).show();
						}
						else {
							voteHandler.raiseAVote(20, "Happy");
						}
					}
				});

				btnRefresh = (ImageButton) fragmentView.findViewById(R.id.btn_refresh);
				btnRefresh.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						refresh();
					}
				});
			}

			return fragmentView;
		}

		/**
		 * Method called via OnListener in {@link CloudBackendFragment}.
		 */
		public void onCreateFinished() {
			Log.d(debugTag, "list post");
			listPosts();
		}

		//
		/**
		 * Method called via OnListener in {@link CloudBackendFragment}.
		 */
		public void onBroadcastMessageReceived(List<CloudEntity> l) {
			for (CloudEntity e : l) {
				String message = (String) e.get(BROADCAST_PROP_MESSAGE);
				int duration = Integer.parseInt((String) e.get(BROADCAST_PROP_DURATION));
				// Toast.makeText(context, message, duration).show();
				Log.i(Consts.TAG, "A message was recieved with content: " + message);
			}
		}

		/**
		 * Retrieves the list of all posts from the backend and updates the UI.
		 * For demonstration in this sample, the query that is executed is:
		 * "SELECT * FROM Guestbook ORDER BY _createdAt DESC LIMIT 50" This
		 * query will be re-executed when matching entity is updated.
		 */
		private void listPosts() {

			request(Scope.FUTURE_AND_PAST);
		}

		/*
		 * Refresh request
		 */

		private void refresh() {
			request(Scope.FUTURE_AND_PAST);
		}

		private void request(Scope s) {

			// create a response handler that will receive the result or an
			// error
			CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
				@Override
				public void onComplete(List<CloudEntity> results) {
					// First we need to distinguish the chat messages and other
					// APIs
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
						// Toast.makeText(context, "newPost : " +
						// latestMsg.getId(),
						// Toast.LENGTH_SHORT).show();

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
							// +map.get("room_title"),
							// Toast.LENGTH_SHORT).show();

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
			pDialog = new ProgressDialog(context);
			pDialog.setMessage("Refreshing...");
			pDialog.show();
			initiateFragments();
			processFragment.getCloudBackend().listByKind("Guestbook", CloudEntity.PROP_CREATED_AT,
					Order.DESC, 50, Scope.FUTURE_AND_PAST, handler);

		}

		/*
		 * sendMessage for general use : vote etc
		 */
		public void sendMessage(HashMap<String, String> message) {
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
			// create a response handler that will receive the result or an
			// error
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

		private void initiateFragments() {
			if (fragmentManager == null) {
				fragmentManager = getChildFragmentManager();
			}

			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

			// Check to see if we have retained the fragment which handles
			// asynchronous backend calls
			// processFragment = (CloudBackendFragment)
			// fragmentManager.findFragmentById(1);
			processFragment = (CloudBackendFragment) fragmentManager
					.findFragmentByTag(PROCESSING_FRAGMENT_TAG);
			// .findFragmentByTag();
			// If not retained (or first time running), create a new one
			if (processFragment == null) {
				processFragment = new CloudBackendFragment();
				processFragment.setRetainInstance(true);
				fragmentTransaction.add(processFragment, PROCESSING_FRAGMENT_TAG);
			}

			// // Add the splash screen fragment
			// mSplashFragment = new SplashFragment();
			// fragmentTransaction.add(R.id.activity_main, mSplashFragment,
			// SPLASH_FRAGMENT_TAG);
			fragmentTransaction.commit();
		}

		private void updateChatRoomView() {
			textField.setEnabled(true);
			sendButton.setEnabled(true);
			if (!allPosts.isEmpty()) {
				stubView.setVisibility(View.GONE);
				messageListView.setVisibility(View.VISIBLE);
				messageListView.setAdapter(new PostAdapter(context,
						android.R.layout.simple_list_item_1, allPosts));
			}
			else {
				stubView.setVisibility(View.VISIBLE);
				messageListView.setVisibility(View.GONE);
			}
		}

		private void handleEndpointException(IOException e) {
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
			sendButton.setEnabled(true);
		}

	}

	/**
	 * Allen's new ViewPagerFragment
	 * 
	 * @author Allen
	 * 
	 */
	public static class CreateEventFragment extends android.support.v4.app.Fragment implements
			OnClickListener, OnItemSelectedListener {

		private EditText			_nameEditText;
		private EditText			_detailsEditText;
		private EditText			_placeEditText;

		private Button				_datePicker;
		private Button				_timePicker;

		private Button				_createButton;
		private Button				_cancelButton;
		private Spinner				spinner;

		private Calendar			cal;
		private int					day;
		private int					month;
		private int					year;
		private int					hour;
		private int					min;

		private ProgressDialog		progressDialog;

		String						items[]		= new String[] { "1", "2", "3", "4", "5", "6", "7",
														"8", "9", "10", "11", "12", "13", "14",
														"15", "16", "17", "18", "19", "20" };
		protected String			timeString;
		protected String			dateString;
		protected String			noOfFriends	= "1";
		private CloudBackend		cloudBackend;
		private List<CloudEntity>	mPosts;

		public static CreateEventFragment newInstance(int mLayoutChoice) {
			Bundle extras = new Bundle();
			extras.putInt("choice", mLayoutChoice);
			CreateEventFragment newInstance = new CreateEventFragment();
			newInstance.setArguments(extras);
			// layoutChoice = mLayoutChoice;
			return newInstance;
		}

		public static void ShowAlertDialog(final String title, String message,
				final Context context, final boolean redirectToPreviousScreen) {
			try {

				AlertDialog.Builder builder = new AlertDialog.Builder(
						((Activity) context).getParent());
				builder.setMessage(message);
				builder.setTitle(title);
				builder.setCancelable(false);
				builder.setInverseBackgroundForced(true);
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
			catch (Exception e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}

		public void onClick(View v) {

			switch (v.getId()) {
				case R.id.createButton:

					progressDialog = ProgressDialog.show(getActivity(), "", "Please wait..");
					progressDialog.setCancelable(true);

					String name = _nameEditText.getText().toString();
					String details = _detailsEditText.getText().toString();
					String place = _placeEditText.getText().toString();
					String date = dateString;
					String time = timeString;
					String noOfFrnds = noOfFriends;

					if (name.length() == 0) {
						ShowAlertDialog("", "Please enter name of the event", getActivity(), false);
					}
					else if (details.length() == 0) {
						ShowAlertDialog("", "Please enter details of the event", getActivity(),
								false);
					}
					if (place.length() == 0) {
						ShowAlertDialog("", "Please enter place of the event", getActivity(), false);
					}
					if (date.length() == 0) {
						ShowAlertDialog("", "Please enter date of the event", getActivity(), false);
					}
					if (time.length() == 0) {
						ShowAlertDialog("", "Please enter time of the event", getActivity(), false);
					}
					if (noOfFrnds.length() == 0) {
						ShowAlertDialog("", "Please enter number of friends for the event",
								getActivity(), false);
					}
					else {
						cloudBackend = new CloudBackend();

						final CloudBackendMessaging cloudBackendMessaging = new CloudBackendMessaging(
								getActivity());
						cloudBackendMessaging.subscribeToCloudMessage("eventapp",
								new CloudCallbackHandler<List<CloudEntity>>() {

									@Override
									public void onComplete(List<CloudEntity> results) {
										// mAnnounceTxt.setText(R.string.announce_success);
										mPosts = results;

										// animateArrival();
										// updateGuestbookView();
									}

									@Override
									public void onError(IOException exception) {
										// mAnnounceTxt.setText(R.string.announce_fail);
										// animateArrival();
										// handleEndpointException(exception);
									}
								});

						CloudEntity cloudEntity = cloudBackendMessaging
								.createCloudMessage("eventapp");// new
																// CloudEntity("Guestbook");
						cloudEntity.put("name", name);
						cloudEntity.put("details", details);
						cloudEntity.put("place", place);
						cloudEntity.put("date", date);
						cloudEntity.put("time", time);
						cloudEntity.put("nooffrnds", noOfFrnds);
						cloudEntity.put("propertyName", "event");
						cloudBackendMessaging.sendCloudMessage(cloudEntity);

						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									cloudBackendMessaging.deleteAll(
											CloudBackendMessaging.KIND_NAME_CLOUD_MESSAGES, mPosts);// delete(CloudBackendMessaging.KIND_NAME_CLOUD_MESSAGES,
																									// "CE:6e7b1ba2-57a6-4d7e-a921-0b1890824273");
								}
								catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});

						progressDialog.dismiss();

						Toast.makeText(getActivity(), "Event created successfully!",
								Toast.LENGTH_LONG).show();
						//
						// Intent intent = new Intent(getActivity(),
						// SelectionActivity.class);
						// startActivity(intent);
					}
				break;

			// case R.id.cancelButton:
			//
			// finish();
			// break;
			// //
			// case R.id.datePickrer:
			//
			// showDialog(0);
			// break;
			//
			// case R.id.timePicker:
			// showDialog(1);
			// break;
			}

		}

		@Deprecated
		protected Dialog onCreateDialog(int id) {
			if (id == 0) {
				return new DatePickerDialog(getActivity(), datePickerListener, year, month, day);
			}
			else if (id == 1) { return new TimePickerDialog(getActivity(), timePickerListener,
					hour, min, false); }
			return new DatePickerDialog(getActivity(), datePickerListener, year, month, day);
		}

		private DatePickerDialog.OnDateSetListener	datePickerListener	= new DatePickerDialog.OnDateSetListener() {
																			public void onDateSet(
																					DatePicker view,
																					int selectedYear,
																					int selectedMonth,
																					int selectedDay) {
																				dateString = (selectedMonth + 1)
																						+ " / "
																						+ selectedDay
																						+ " / "
																						+ selectedYear;
																				_datePicker
																						.setText(dateString);
																			}
																		};
		private TimePickerDialog.OnTimeSetListener	timePickerListener	= new TimePickerDialog.OnTimeSetListener() {
																			@Override
																			public void onTimeSet(
																					TimePicker view,
																					int hourOfDay,
																					int minute) {
																				int hour;
																				String am_pm;
																				if (hourOfDay > 12) {
																					hour = hourOfDay - 12;
																					am_pm = "PM";
																				}
																				else {
																					hour = hourOfDay;
																					am_pm = "AM";
																				}
																				timeString = hour
																						+ " : "
																						+ minute;
																				_timePicker
																						.setText(hour
																								+ " : "
																								+ minute
																								+ " "
																								+ am_pm);
																			}
																		};

		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			int layoutChoice = getArguments().getInt("choice");
			// int layoutChoice = ENTER_A_ROOM;
			View fragmentView = null;

			fragmentView = inflater.inflate(R.layout.createevents, container, false);

			_nameEditText = (EditText) fragmentView.findViewById(R.id.nameEditText);
			_detailsEditText = (EditText) fragmentView.findViewById(R.id.detailsEditText);
			_placeEditText = (EditText) fragmentView.findViewById(R.id.placeEditText);
			_datePicker = (Button) fragmentView.findViewById(R.id.datePickrer);

			// ...
			_datePicker.setOnClickListener((OnClickListener) this);
			_timePicker = (Button) fragmentView.findViewById(R.id.timePicker);
			// ...
			_timePicker.setOnClickListener((OnClickListener) this);

			SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yyyy");

			dateString = sdf.format(new Date());
			_datePicker.setText(dateString);

			timeString = "07:00";
			_timePicker.setText(timeString);

			_createButton = (Button) fragmentView.findViewById(R.id.createButton);
			//
			_createButton.setOnClickListener((OnClickListener) this);

			return fragmentView;
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			noOfFriends = parent.getItemAtPosition(position).toString();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class EventListFragment extends Fragment {

		EventListAdaptor				eventListAdaptor;
		private CloudBackendAsync		cloudBackend;
		private CloudBackendFragment	mProcessingFragment;
		private List<CloudEntity>		mPosts;
		private ListView				listView;
		private ProgressDialog			progressDialog;

		public EventListFragment() {
		}

		public static EventListFragment newInstance() {
			return new EventListFragment();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View rootView = inflater.inflate(R.layout.eventlist_screen, container, false);

			listView = (ListView) rootView.findViewById(R.id.events_listview);

			progressDialog = ProgressDialog.show(getActivity(), "", "Please wait...");

			cloudBackend = new CloudBackendAsync(getActivity());

			CloudQuery cq = new CloudQuery(CloudBackendMessaging.KIND_NAME_CLOUD_MESSAGES);
			cq.setScope(Scope.FUTURE_AND_PAST);
			cq.setSort(CloudEntity.PROP_CREATED_AT, Order.DESC);

			cloudBackend.list(cq, new CloudCallbackHandler<List<CloudEntity>>() {

				@Override
				public void onComplete(List<CloudEntity> results) {
					mPosts = results;

					EventListAdaptor eventListAdaptor = new EventListAdaptor(getActivity(), mPosts);
					listView.setAdapter(eventListAdaptor);
					eventListAdaptor.notifyDataSetChanged();
					listView.invalidate();
					progressDialog.dismiss();
					final CloudBackendMessaging cloudBackendMessaging = new CloudBackendMessaging(
							getActivity());

					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								cloudBackendMessaging.deleteAll(
										CloudBackendMessaging.KIND_NAME_CLOUD_MESSAGES, mPosts);
							}
							catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					});
				}
			});
			return rootView;
		}
	}

	@Override
	public void onCreateFinished() {
		if (mViewPagerAdapter.instantiateItem(mViewPager, 0).getClass()
				.equals(ChattingRoomFragment.class)) {
			((ChattingRoomFragment) mViewPagerAdapter.instantiateItem(mViewPager, 0))
					.onCreateFinished();
		}

	}

	@Override
	public void onBroadcastMessageReceived(List<CloudEntity> message) {
		if (mViewPagerAdapter.instantiateItem(mViewPager, 0).getClass()
				.equals(ChattingRoomFragment.class)) {
			((ChattingRoomFragment) mViewPagerAdapter.instantiateItem(mViewPager, 0))
					.onBroadcastMessageReceived(message);
		}

	}

	/**
	 * Override Activity lifecycle method.
	 */
	// @Override
	// protected void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	//
	// }

	/**
	 * Override Activity lifecycle method.
	 */
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// MenuInflater inflater = getMenuInflater();
	// inflater.inflate(R.menu.chatting_room, menu);
	// return super.onCreateOptionsMenu(menu);
	// }

	/**
	 * Override Activity lifecycle method.
	 */
	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu) {
	// MenuItem loginItem = menu.findItem(R.id.switch_account);
	// loginItem.setVisible(Consts.IS_AUTH_ENABLED);
	// MenuItem voteItem = menu.findItem(R.id.vote);
	// voteItem.setVisible(true);
	// MenuItem voteResultItem = menu.findItem(R.id.voteResult);
	// return true;
	// }

	/**
	 * Override Activity lifecycle method.
	 * <p>
	 * To add more option menu items in your client, add the item to
	 * menu/activity_main.xml, and provide additional case statements in this
	 * method.
	 */
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case R.id.switch_account:
	// processFragment.signInAndSubscribe(true);
	// return true;
	// case R.id.vote:
	// voteHandler.raiseAVote(10, "testVote");
	// return true;
	// case R.id.voteResult:
	// voteHandler.showResult();
	// return true;
	// case R.id.refresh:
	// refresh();
	// return true;
	// default:
	// return super.onOptionsItemSelected(item);
	// }
	// }
	//
	// @Override
	// public void onConfigurationChanged(Configuration newConfig) {
	// super.onConfigurationChanged(newConfig);
	// }
	//

}
