package mobile_final;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.backend.R;

@SuppressLint("ValidFragment")
public class VoteHandler {

	private String						tag							= "VoteHolder";
	private final String				TITLE_POLLING				= "Starting a poll";
	private final String				TEXT_POLLING				= "How many options?";
	private final String				TITLE_POLLING_DETAIL		= "Customize options";
	private final String				TEXT_POLLING_DETAIL			= "Click on number to customize options!";
	private final String				INPUT_POLLING_HINT			= "Input title here";
	private final String				INPUT_POLLING_DETAIL_HINT	= "Input extra info";
	private final String				DEFAULT_VOTE_TITLE			= "Vote for fun!";
	private Context						context;
	private int[]						voteResult;
	private boolean						resultWaiting;
	private AlertDialog					dialog;
	private RelativeLayout				voteContentLayout;
	private final TextView				descriptionField;
	private final EditText				titleField;
	private final NumberPicker			votePicker;
	private BroadcastReceiver			messageReceiver;
	private int							candidateNum;
	private String						voteTitle;
	private boolean						isHolder					= false;
	private SharedPreferences.Editor	voteHistoryEditor;
	private SharedPreferences			voteHistory;
	private String						currentUID;
	private String						currentAction;
	private String						currentInteractingUser;
	final private VoteDetail			voteDetail;
	private HashMap<String, String>		msgMap;

	private class VoteDetail {
		public NumberPicker	maxNumberPicker;
		public NumberPicker	candidateNumberPicker;
		public int			numPicked;
	}

	public VoteHandler(Context context) {
		currentUID = "dummy";
		currentAction = "dummy";
		currentInteractingUser = "dummy";
		resultWaiting = false;
		voteDetail = new VoteDetail();
		this.context = context;
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		voteContentLayout = (RelativeLayout) inflater.inflate(R.layout.vote_dialog_layout, null);
		descriptionField = (TextView) voteContentLayout.findViewById(R.id.description);
		titleField = (EditText) voteContentLayout.findViewById(R.id.title);
		votePicker = (NumberPicker) voteContentLayout.findViewById(R.id.vote_number);
		voteDetail.maxNumberPicker = votePicker;
		messageReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				receiveMessage((HashMap<String, String>) intent.getExtras().get("map"));
			}
		};
		LocalBroadcastManager.getInstance(context).registerReceiver(messageReceiver,
				new IntentFilter("RECEIVE_MESSAGE_L"));
		voteHistory = (SharedPreferences) ((Activity) context).getPreferences(Context.MODE_PRIVATE);
		voteHistoryEditor = voteHistory.edit();
	}

	public void raiseAVote(int maxVote, final String title) {
		isHolder = true;
		voteTitle = DEFAULT_VOTE_TITLE;
		// every time we raise a vote, we initailize the component
		// first we let the holder pick the number of candidates
		if (voteDetail.candidateNumberPicker != null) {
			NumberPicker candidatPicker = voteDetail.candidateNumberPicker;
			LayoutParams layoutParams = (LayoutParams) candidatPicker.getLayoutParams();
			RelativeLayout parentGroup = (RelativeLayout) candidatPicker.getParent();
			if (parentGroup != null) {
				parentGroup.removeView(candidatPicker);
			}
			votePicker.setValue(1);
			parentGroup.addView(votePicker, layoutParams);

		}

		votePicker.setMaxValue(maxVote);
		votePicker.setMinValue(1);
		votePicker.setDisplayedValues(generateDisplayValue(maxVote));
		votePicker.setWrapSelectorWheel(true);

		descriptionField.setText(TEXT_POLLING);
		titleField.setEnabled(true);
		titleField.setFocusable(true);
		titleField.setClickable(true);
		titleField.setText("");
		titleField.setHint(INPUT_POLLING_HINT);

		voteResult = null;
		if (voteContentLayout.getParent() != null) {
			((ViewGroup) voteContentLayout.getParent()).removeView(voteContentLayout);
		}

		// build the alertDialog
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(voteContentLayout);
		builder.setTitle(TITLE_POLLING);

		// when raise a vote is clicked, raise the vote
		builder.setPositiveButton("Continue", new OnClickListener() {
			@Override
			public void onClick(DialogInterface cdialog, int which) {
			}
		});

		// cancel; if clicked, need to restore all settings.
		builder.setNegativeButton("Cancel", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				descriptionField.setText(TEXT_POLLING);
				currentAction = "dummy";
			}
		});

		dialog = builder.create();
		dialog.show();
		final TextView titleTextView = ((TextView) dialog.findViewById(context.getResources()
				.getIdentifier("alertTitle", "id", "android")));
		final Button positiveButton = (Button) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (positiveButton.getText().equals("Continue")) {
					Log.d(tag, "hello");
					positiveButton.setText("Raise");
					voteDetail.numPicked = votePicker.getValue();
					// use a new number picker to replace the old one
					NumberPicker newNumberPicker = new NumberPicker(context);
					LayoutParams pickerLayoutParams = (LayoutParams) votePicker.getLayoutParams();
					ViewGroup parentOfPicker = (ViewGroup) votePicker.getParent();
					parentOfPicker.removeView(votePicker);
					parentOfPicker.addView(newNumberPicker, pickerLayoutParams);
					voteDetail.candidateNumberPicker = newNumberPicker;
					newNumberPicker.setMaxValue(voteDetail.numPicked);
					newNumberPicker.setMinValue(1);
					newNumberPicker.setDisplayedValues(generateDisplayValue(voteDetail.numPicked));
					newNumberPicker.setWrapSelectorWheel(true);
					// hack the numberPicker so that we can make the displayed
					// text changable immediately
					((EditText) newNumberPicker.getChildAt(0)).setFilters(new InputFilter[] {});
					((EditText) newNumberPicker.getChildAt(0))
							.addTextChangedListener(new TextWatcher() {

								@Override
								public void onTextChanged(CharSequence s, int start, int before,
										int count) {

								}

								@Override
								public void beforeTextChanged(CharSequence s, int start, int count,
										int after) {
								}

								@Override
								public void afterTextChanged(Editable s) {
									changeVoteDescription(s.toString());
								}
							});
					voteTitle = titleField.getText().toString().equals("") ? DEFAULT_VOTE_TITLE
							: titleField.getText().toString();
					titleTextView.setText(TITLE_POLLING_DETAIL);
					titleField.setText("");
					titleField.setHint(INPUT_POLLING_DETAIL_HINT);
					descriptionField.setText(TEXT_POLLING_DETAIL);

				}
				else if (positiveButton.getText().equals("Raise")) {

					NumberPicker numberPicker = voteDetail.candidateNumberPicker;
					int maxNum = voteDetail.numPicked;
					Log.d(tag, " ididid is : " + voteDetail.numPicked);

					HashMap<String, String> message = new HashMap<String, String>();
					message.put("api", "vote");
					message.put("action", "polling");
					currentAction = "polling";
					message.put("num_candidates", Integer.toString(voteDetail.numPicked));

					// message.put("description", descriptionField.getText()
					// .toString());
					message.put("displayed_options",
							descriptionEncorder(numberPicker.getDisplayedValues()));
					message.put("description", titleField.getText().toString());
					message.put("title", voteTitle);
					String creationTime = String.valueOf(System.currentTimeMillis() / 1000);
					currentUID = voteTitle + creationTime;
					message.put("creation_time", creationTime);
					message.put("UID", currentUID);
					candidateNum = numberPicker.getValue();
					// set to true if already handled before.
					// otherwise it's the state of handling
					voteHistoryEditor.putString(currentUID, "holder");
					voteHistoryEditor.commit();
					Log.d(tag, "selected value is : " + numberPicker.getValue()
							+ "\nDescription is : " + message.get("description"));
					msgMap = message;
					sendMessage(message);
					dialog.dismiss();
				}

			}
		});

	}

	private String descriptionEncorder(String[] data) {
		String encodedData = "";
		for (int i = 0; i < data.length; i++) {
			if (i == data.length - 1) {
				encodedData += (data[i]);
			}
			else {
				encodedData += (data[i] + "-");
			}

		}
		return encodedData;
	}

	private String[] descriptionDecoder(String data) {
		String[] decodedData = data.split("-");

		return decodedData;
	}

	private void changeVoteDescription(String description) {

		String[] displayed = voteDetail.candidateNumberPicker.getDisplayedValues();
		int currentValue = voteDetail.candidateNumberPicker.getValue();
		displayed[currentValue - 1] = description;
		voteDetail.candidateNumberPicker.setDisplayedValues(displayed);
		// descriptionField.setText(description + votePicker.getValue());
	}

	private String[] generateDisplayValue(int maxVote) {
		String[] dispalyValues = new String[maxVote];
		for (int i = 0; i < dispalyValues.length; i++) {
			dispalyValues[i] = Integer.toString(i + 1);
		}
		return dispalyValues;
	}

	private void sendMessage(HashMap<String, String> msg) {
		// send message through broadcast
		Intent intent = new Intent("SEND_MESSAGE_L");
		intent.putExtra("map", msg);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		Log.d(tag, "sending Message : " + msg.size());
	}

	// Called by broadcast receiver
	private synchronized void receiveMessage(final HashMap<String, String> msg) {
		// check if this device has already received this vote event before
		try {
			msgMap = msg;
			if (currentUID == null) {
				currentUID = "dummy";
			}

			if (currentAction == null) {
				currentAction = "dummy";
			}

			if (currentUID.equals(msg.get("UID")) && currentAction.equals(msg.get("action"))) { return; }

			currentUID = msg.get("UID");
			// Toast.makeText(context, "cUser ID : " + currentInteractingUser,
			// Toast.LENGTH_LONG)
			// .show();

			String history = voteHistory.getString(currentUID, "new");
			if (history.equals("holder")) {
				isHolder = true;
			}
			String api = msg.get("api");
			if (!history.equals("handled") && api != null) {
				// voter only deal with vote
				if (api.equals("vote")) {
					// Distinguish receiver and holder
					if (msg.get("action").equals("polling") && !isHolder) {
						Log.d(tag, "polling");
						voteHistoryEditor.putString(currentUID, "handled");
						voteHistoryEditor.commit();
						// Toast.makeText(context, "polling",
						// Toast.LENGTH_LONG).show();
						if (voteContentLayout.getParent() != null) {
							((ViewGroup) voteContentLayout.getParent())
									.removeView(voteContentLayout);
						}

						int maxVote = Integer.valueOf(msg.get("num_candidates"));
						votePicker.setMaxValue(maxVote);
						votePicker.setMinValue(1);

						votePicker.setDisplayedValues(descriptionDecoder(msg
								.get("displayed_options")));
						votePicker.setWrapSelectorWheel(true);

						// disable text editing in this mode
						descriptionField.setText("Vote for your favorite");
						titleField.setEnabled(false);
						titleField.setFocusable(false);
						titleField.setClickable(false);
						String desString = msg.get("description").equals("") ? "No details." : msg
								.get("description");
						titleField.setText(desString);
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setTitle(msg.get("title"));
						builder.setPositiveButton("Vote", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								HashMap<String, String> message = new HashMap<String, String>();
								message.put("api", "vote");
								message.put("action", "submit");
								message.put("displayed_options", msg.get("displayed_options"));
								currentAction = "submit";
								message.put("UID", currentUID);
								message.put("Vote_Candidates",
										Integer.toString(votePicker.getValue()));
								voteHistoryEditor.putString(currentUID, "handled");
								voteHistoryEditor.commit();
								titleField.setEnabled(true);
								titleField.setFocusable(true);
								titleField.setClickable(true);
								sendMessage(message);
							}
						});

						builder.setNegativeButton("Waive", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								voteHistoryEditor.putString(currentUID, "handled");
								voteHistoryEditor.commit();
								titleField.setEnabled(true);
								titleField.setFocusable(true);
								titleField.setClickable(true);
							}
						});

						builder.setView(voteContentLayout);
						dialog = builder.create();

						dialog.show();

					}
					else if (msg.get("action").equals("submit") && isHolder
							&& !msg.get("id").equals(currentInteractingUser)) {
						// initialize result if it's not yet initialized
						if (voteResult == null) {
							if (candidateNum == 0) {
								candidateNum = 20;
							}
							voteResult = new int[candidateNum];
						}

						// record votes
						synchronized (voteResult) {
							currentInteractingUser = msg.get("id");
							voteResult[Integer.valueOf(msg.get("Vote_Candidates")) - 1]++;
							resultWaiting = true;
							// Log.d(tag,
							// "candidate is " +
							// Integer.valueOf(msg.get("Vote_Candidates")));
							Toast.makeText(
									context,
									"receive submittion, voting for : "
											+ Integer.valueOf(msg.get("Vote_Candidates")),
									Toast.LENGTH_LONG).show();
						}

					}
					else if (msg.get("action").equals("announce_result")) {

						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						builder.setMessage(msg.get("result"));
						builder.setTitle(msg.get("title"));
						builder.setPositiveButton("I get it!", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								voteHistoryEditor.putString(currentUID, "handled");
								voteHistoryEditor.commit();

							}
						});

						dialog = builder.create();
						try {
							dialog.show();
						}
						catch (Exception e) {
							e.printStackTrace();
						}

					}

				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean hasResult() {
		return resultWaiting;

	}

	public boolean showResult() {
		if (voteResult != null && msgMap != null) {
			synchronized (voteResult) {
				if (voteResult != null) {
					String[] displayedStrings;
					// Display result
					if (msgMap.get("displayed_options") != null) {
						displayedStrings = descriptionDecoder(msgMap.get("displayed_options"));
					}
					else {
						displayedStrings = generateDisplayValue(20);
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					String currentResult = "";
					// compute currentResult
					for (int i = 0; i < voteResult.length; i++) {
						currentResult += ((displayedStrings[i]) + " : " + voteResult[i] + " votes\n");
					}
					final String submitResult = currentResult;

					builder.setMessage(currentResult);
					builder.setTitle("Current Result");
					builder.setPositiveButton("Announce Result", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							HashMap<String, String> message = new HashMap<String, String>();
							message.put("api", "vote");
							message.put("action", "announce_result");
							currentAction = "announce_result";
							message.put("result", submitResult);
							message.put("title", "Result of [ " + voteTitle + " ]");
							voteHistoryEditor.putString(currentUID, "handled");
							voteHistoryEditor.commit();
							isHolder = false;
							sendMessage(message);
							resultWaiting = false;
						}
					});

					builder.setNegativeButton("Cancel Vote", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							isHolder = false;
							resultWaiting = false;
						}
					});

					builder.setNeutralButton("Keep waiting", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});

					dialog = builder.create();
					dialog.show();
					return true;
				}
				else {
					return false;
				}

			}
		}
		else {
			return false;
		}

	}

}
