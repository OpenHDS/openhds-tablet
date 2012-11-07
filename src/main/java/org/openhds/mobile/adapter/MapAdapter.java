package org.openhds.mobile.adapter;

import org.openhds.mobile.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MapAdapter extends ArrayAdapter<AdapterContent> {

	Context context;
	int layoutResourceId;
	AdapterContent data[] = null;
	
	public MapAdapter(Context context, int layoutResourceId, AdapterContent[] data) {
		super(context, layoutResourceId, data);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.data = data;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        AdapterHolder holder = null;
       
        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
           
            holder = new AdapterHolder();
            holder.item1 = (TextView) row.findViewById(R.id.toptext);
            holder.item2 = (TextView) row.findViewById(R.id.bottomtext);
           
            row.setTag(holder);
        }
        else {
            holder = (AdapterHolder)row.getTag();
        }
       
        AdapterContent content = data[position];
        holder.item1.setText(content.getItem1());
        holder.item2.setText(content.getItem2());
       
        return row;
    }
	
	static class AdapterHolder {
	    TextView item1;
	    TextView item2;
	}
}
