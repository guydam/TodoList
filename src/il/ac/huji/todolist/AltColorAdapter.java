package il.ac.huji.todolist;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AltColorAdapter extends ArrayAdapter<TodoItem> {

	Context mContext;
	
	public AltColorAdapter(Context context, int resource, List<TodoItem> objects) {
		super(context, resource, objects);
		mContext = context;
	}
	
	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		
		TodoItem item = getItem(pos);
		
		
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.todo_list_row, null);
		}
		
		TextView todoItemTv = (TextView) convertView.findViewById(R.id.txtTodoTitle);
		TextView dueDateTv = (TextView) convertView.findViewById(R.id.txtTodoDueDate);
		
		if (item.isPassedDueDate()) {
			todoItemTv.setTextColor(Color.RED);
			dueDateTv.setTextColor(Color.RED);
		} else {
			todoItemTv.setTextColor(Color.BLACK);
			dueDateTv.setTextColor(Color.BLACK);	
		}
		
		todoItemTv.setText(item.getItemStr());
		dueDateTv.setText(item.getDueDate());
		
		return convertView;

	}
}
