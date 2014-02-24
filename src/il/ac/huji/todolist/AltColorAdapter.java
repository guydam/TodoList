package il.ac.huji.todolist;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AltColorAdapter extends ArrayAdapter<String> {

	public AltColorAdapter(Context context, int resource, List<String> objects) {
		super(context, resource, objects);
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		
		String itemStr = getItem(pos);
		
		
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.todo_list_row, null);
		}
		
		TextView todoItemTv = (TextView) convertView.findViewById(R.id.todoItemTextView);
		
		if (pos %2 == 0) {
			todoItemTv.setTextColor(Color.RED);	
		} else {
			todoItemTv.setTextColor(Color.BLUE);
		}
		todoItemTv.setText(itemStr);
				
		return convertView;

	}
}
