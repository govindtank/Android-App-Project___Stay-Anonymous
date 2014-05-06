package mobile_final;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.cloud.backend.R;
import com.google.cloud.backend.core.CloudEntity;

public class EventListAdaptor extends BaseAdapter {

	private EditText		_nameEditText;
	private EditText		_detailsEditText;
	private EditText		_placeEditText;
	private EditText		_dateEditText;
	private EditText		_timeEditText;
	// private EditText _noOfFrndsEditText;

	private LayoutInflater	mInflater;
	private Activity		_activity;
	private List			_list;

	public EventListAdaptor(Activity eventListActivity, List list) {
		this._activity = eventListActivity;
		this._list = list;
		mInflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public int getCount() {
		return _list.size();
	}

	@Override
	public Object getItem(int position) {
		return _list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;

		final CloudEntity ce = (CloudEntity) getItem(position);

		String id = ce.getId();

		final Map<String, Object> eventValues = ce.getProperties();

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.event_row, null);

			holder._event_listrow = (LinearLayout) convertView.findViewById(R.id.row);
			holder._nameText = (TextView) convertView.findViewById(R.id.nameTextView);
			holder._placeText = (TextView) convertView.findViewById(R.id.placeTextView);
			holder._dateText = (TextView) convertView.findViewById(R.id.dateTextView);

			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();

		}

		holder.name = eventValues.get("name") == null ? " " : eventValues.get("name").toString();
		holder.place = eventValues.get("place") == null ? " " : eventValues.get("place").toString();
		holder.date = eventValues.get("date") == null ? " " : eventValues.get("date").toString();
		holder.details = eventValues.get("details") == null ? " " : eventValues.get("details")
				.toString();
		// noOfFriends = eventValues.get("nooffrnds").toString();
		holder.propertyNameString = eventValues.get("propertyName") == null ? " " : eventValues
				.get("propertyName").toString();
		holder.timeString = eventValues.get("time") == null ? " " : eventValues.get("time")
				.toString();

		holder._nameText.setText(holder.name);
		holder._placeText.setText(holder.place);
		holder._dateText.setText(holder.date);
		// if (eventValues.containsKey("name") &&
		// eventValues.containsKey("date")
		// && eventValues.containsKey("place")) {

		// }

		holder._event_listrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// Intent intent = new Intent(_activity,
				// ShowEventActivity.class);
				//
				//
				// Bundle bundle = new Bundle();
				// bundle.putSerializable("eventInfo", (Serializable)
				// eventValues);
				//
				// intent.putExtras(bundle);
				//
				// _activity.startActivity(intent);

				// 1. Instantiate an AlertDialog.Builder with its constructor

				AlertDialog.Builder builder = new AlertDialog.Builder(((Context) _activity));

				// 2. Chain together various setter methods to set the dialog
				// characteristics
				builder.setTitle("Partner Finding..");

				// 3. setView in the middle of dialog
				LayoutInflater inflater = (_activity).getLayoutInflater();
				final View view = inflater.inflate(R.layout.showevent, null);

				_nameEditText = (EditText) view.findViewById(R.id.nameEditText);
				_detailsEditText = (EditText) view.findViewById(R.id.detailsEditText);
				_placeEditText = (EditText) view.findViewById(R.id.placeEditText);
				_dateEditText = (EditText) view.findViewById(R.id.dateEditText);
				_timeEditText = (EditText) view.findViewById(R.id.timeEditText);
				// _noOfFrndsEditText= (EditText)
				// view.findViewById(R.id.nofFrndsEditText);

				// HashMap<String, Object> properties = (HashMap<String,
				// Object>) bundle.getSerializable("eventInfo");
				//
				//
				// String name = (String) properties.get("name");
				// String details = (String) properties.get("details");
				// String place = (String) properties.get("place");
				// String date = (String) properties.get("date");
				// String time = (String) properties.get("time");
				// String noOfFriends = (String) properties.get("nooffrnds");
				// String propertyName = (String)
				// properties.get("propertyName");

				Log.d("****", "Dialog: " + holder.name + holder.details + holder.place
						+ holder.date + holder.timeString);
				_nameEditText.setText(holder.name);
				_detailsEditText.setText(holder.details);
				_placeEditText.setText(holder.place);
				_dateEditText.setText(holder.date);
				_timeEditText.setText(holder.timeString);
				// _noOfFrndsEditText.setText(noOfFriends);

				builder.setView(view);
				// 4. Add the buttons
				// builder.setPositiveButton("Create", new
				// DialogInterface.OnClickListener() {
				// public void onClick(DialogInterface dialog, int id) {
				// // User clicked CREATE button
				//
				// // return to background
				// dialog.dismiss();
				// }
				// });
				builder.setNegativeButton("Return", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
						dialog.dismiss();
					}
				});
				// 5. Get the AlertDialog from create()
				AlertDialog dialog = builder.create();
				// 6. put this to where ever you want to show this dialog
				dialog.show();

			}
		});

		return convertView;

	}

	static class ViewHolder {
		LinearLayout	_event_listrow;
		TextView		_nameText;
		TextView		_dateText;
		TextView		_placeText;
		String			name;
		String			place;
		String			date;
		String			details;
		String			noOfFriends;
		String			propertyNameString;
		String			timeString;

	}

}
