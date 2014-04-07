package org.openhds.mobile.utilities;

import org.openhds.mobile.R;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class LayoutUtils {

	public static Button makeNewGenericButton(Activity activity, String description, String buttonName,
			Object buttonTag, OnClickListener listener, ViewGroup container) {

		View v = activity.getLayoutInflater().inflate(R.layout.generic_textview_button, null);
		container.addView(v);
		Button b = (Button) v.findViewById(R.id.generic_button);
		TextView t = (TextView) v.findViewById(R.id.generic_button_description);

		if (null == description) {
			t.setVisibility(View.GONE);
			
		}else{
			t.setText(description);	
		}

		b.setText(buttonName);
		b.setTag(buttonTag);
		b.setOnClickListener(listener);

		return b;
	}

}
