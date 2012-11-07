package org.openhds.mobile.activity;

import org.openhds.mobile.R;
import org.openhds.mobile.database.DatabaseAdapter;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class FormListActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String user = getIntent().getExtras().getString(
				AbstractActivity.USERNAME_PARAM);

		DatabaseAdapter store = new DatabaseAdapter(this);

		String[] columns = new String[] { DatabaseAdapter.KEY_FORM_TYPE,
				DatabaseAdapter.KEY_FORMOWNER_ID };
		int[] bindTo = new int[] { android.R.id.text1, android.R.id.text2 };
		Cursor formInstanceCursor = store.getFormsForUsername(user);

		CustomAdapter adapter = new CustomAdapter(this, R.layout.list_item,
				formInstanceCursor, columns, bindTo);
		startManagingCursor(formInstanceCursor);

		setListAdapter(adapter);
	}

	static class CustomAdapter extends SimpleCursorAdapter {

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			super.bindView(view, context, cursor);
			ImageView iv = (ImageView) view.findViewById(R.id.reviewImage);
			if (cursor
					.getInt(cursor.getColumnIndex(DatabaseAdapter.KEY_REVIEW)) == 0) {
				iv.setImageResource(R.drawable.error_icon);
			} else {
				iv.setImageResource(R.drawable.review_icon);
			}
		}

		public CustomAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(getApplicationContext(),
				FormViewActivity.class);
		intent.putExtra(AbstractActivity.FORM_ID, id);
		startActivity(intent);
	}
}
