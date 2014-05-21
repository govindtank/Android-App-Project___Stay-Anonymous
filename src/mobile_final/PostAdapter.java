package mobile_final;

/*
 * 
 */

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.cloud.backend.R;
import com.google.cloud.backend.core.CloudEntity;

/**
 * This ArrayAdapter uses CloudEntities as items and displays them as a post in
 * the guestbook. Layout uses row.xml.
 * 
 */
public class PostAdapter extends ArrayAdapter<CloudEntity> {

	private static final SimpleDateFormat	SDF	= new SimpleDateFormat("HH:mm:ss ", Locale.US);

	private LayoutInflater					mInflater;

	/**
	 * Creates a new instance of this adapter.
	 * 
	 * @param context
	 * @param textViewResourceId
	 * @param objects
	 */
	public PostAdapter(Context context, int textViewResourceId, List<CloudEntity> objects) {
		super(context, textViewResourceId, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView != null ? convertView : mInflater.inflate(R.layout.row_post,
				parent, false);

		CloudEntity ce = getItem(position);

		if (ce != null) {
			TextView message = (TextView) view.findViewById(R.id.messageContent);
			TextView signature = (TextView) view.findViewById(R.id.signature);
			ImageView leftEndImage = (ImageView) view.findViewById(R.id.imageView1);

			if (message != null) {
				message.setText(ce.get("message").toString());
				String mString = ce.get("message").toString().length() == 0 ? " " : ce.get(
						"message").toString();

				if (mString.charAt(0) == 'a' || mString.charAt(0) == 'A') {
					leftEndImage.setBackgroundResource(R.drawable.a);
				}
				else if (mString.charAt(0) == 'b' || mString.charAt(0) == 'B') {
					leftEndImage.setBackgroundResource(R.drawable.b);
				}
				else if (mString.charAt(0) == 'c' || mString.charAt(0) == 'C') {
					leftEndImage.setBackgroundResource(R.drawable.c);
				}
				else if (mString.charAt(0) == 'd' || mString.charAt(0) == 'D') {
					leftEndImage.setBackgroundResource(R.drawable.d);
				}
				else if (mString.charAt(0) == 'e' || mString.charAt(0) == 'E') {
					leftEndImage.setBackgroundResource(R.drawable.e);
				}
				else if (mString.charAt(0) == 'f' || mString.charAt(0) == 'F') {
					leftEndImage.setBackgroundResource(R.drawable.f);
				}
				else if (mString.charAt(0) == 'g' || mString.charAt(0) == 'G') {
					leftEndImage.setBackgroundResource(R.drawable.g);
				}
				else if (mString.charAt(0) == 'h' || mString.charAt(0) == 'H') {
					leftEndImage.setBackgroundResource(R.drawable.h);
				}
				else if (mString.charAt(0) == 'i' || mString.charAt(0) == 'I') {
					leftEndImage.setBackgroundResource(R.drawable.i);
				}
				else if (mString.charAt(0) == 'j' || mString.charAt(0) == 'J') {
					leftEndImage.setBackgroundResource(R.drawable.j);
				}
				else if (mString.charAt(0) == 'k' || mString.charAt(0) == 'K') {
					leftEndImage.setBackgroundResource(R.drawable.k);
				}
				else if (mString.charAt(0) == 'l' || mString.charAt(0) == 'L') {
					leftEndImage.setBackgroundResource(R.drawable.l);
				}
				else if (mString.charAt(0) == 'm' || mString.charAt(0) == 'M') {
					leftEndImage.setBackgroundResource(R.drawable.m);
				}
				else if (mString.charAt(0) == 'n' || mString.charAt(0) == 'N') {
					leftEndImage.setBackgroundResource(R.drawable.n);
				}
				else if (mString.charAt(0) == 'o' || mString.charAt(0) == 'O') {
					leftEndImage.setBackgroundResource(R.drawable.o);
				}
				else if (mString.charAt(0) == 'p' || mString.charAt(0) == 'P') {
					leftEndImage.setBackgroundResource(R.drawable.p);
				}
				else if (mString.charAt(0) == 'q' || mString.charAt(0) == 'Q') {
					leftEndImage.setBackgroundResource(R.drawable.q);
				}
				else if (mString.charAt(0) == 'r' || mString.charAt(0) == 'R') {
					leftEndImage.setBackgroundResource(R.drawable.r);
				}
				else if (mString.charAt(0) == 's' || mString.charAt(0) == 'S') {
					leftEndImage.setBackgroundResource(R.drawable.s);
				}
				else if (mString.charAt(0) == 't' || mString.charAt(0) == 'T') {
					leftEndImage.setBackgroundResource(R.drawable.t);
				}
				else if (mString.charAt(0) == 'u' || mString.charAt(0) == 'U') {
					leftEndImage.setBackgroundResource(R.drawable.u);
				}
				else if (mString.charAt(0) == 'v' || mString.charAt(0) == 'V') {
					leftEndImage.setBackgroundResource(R.drawable.v);
				}
				else if (mString.charAt(0) == 'w' || mString.charAt(0) == 'W') {
					leftEndImage.setBackgroundResource(R.drawable.w);
				}
				else if (mString.charAt(0) == 'x' || mString.charAt(0) == 'X') {
					leftEndImage.setBackgroundResource(R.drawable.x);
				}
				else if (mString.charAt(0) == 'y' || mString.charAt(0) == 'Y') {
					leftEndImage.setBackgroundResource(R.drawable.y);
				}
				else if (mString.charAt(0) == 'z' || mString.charAt(0) == 'Z') {
					leftEndImage.setBackgroundResource(R.drawable.z);
				}
				else {
					leftEndImage.setBackgroundResource(R.drawable.blue_background);
				}

			}
			if (signature != null) {
				signature.setText("<anonymous>" + " " + SDF.format(ce.getCreatedAt()));
			}
		}

		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return this.getView(position, convertView, parent);
	}

}
