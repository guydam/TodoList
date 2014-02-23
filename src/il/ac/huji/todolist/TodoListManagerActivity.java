package il.ac.huji.todolist;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class TodoListManagerActivity extends Activity {

	ArrayList<String> todoList;
	AltColorAdapter todoListAdapter;

	ListView todoListView;
	EditText newItemEditText;

	ImageButton addItemButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_todo_list_manager);

		todoList = new ArrayList<String>();


		todoListView = (ListView) findViewById(R.id.lstTodoItems);
		newItemEditText = (EditText) findViewById(R.id.edtNewItem);
		addItemButton = (ImageButton) findViewById(R.id.addItemImageButton);

		registerForContextMenu(todoListView);

		
		todoListAdapter = new AltColorAdapter(this,android.R.layout.simple_list_item_1, todoList);
		todoListView.setAdapter(todoListAdapter);
		todoListAdapter.notifyDataSetChanged();

		addNewItem("Guy");
		addNewItem("Guy1");		
		addNewItem("Guy2");
		addNewItem("Guy3");
		
		addItemButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String newItemStr = newItemEditText.getText().toString();
				if (newItemStr.length()!=0) {
					addNewItem(newItemStr);
					newItemEditText.setText("");
					((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
				};
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.todo_list_manager, menu);
		return true;
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.todo_list_context_menu, menu);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		String titleStr = getItemContent(info.position);
		menu.setHeaderTitle(titleStr);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch (item.getItemId()) {
		case R.id.contextMenuDeleteItem: 			
			removeItem((int) info.position);
			return true;
		};

		return super.onContextItemSelected(item); 
	};

	private String getItemContent (int position) {
		if (position < todoList.size() && position >=0) {
			return todoList.get(position);
		}
		return "";
	}
	
	private boolean removeItem (int position) {
		if (position < todoList.size() && position >=0) {
			todoList.remove(position);
			todoListAdapter.notifyDataSetChanged();
			return true;
		}
		return false; 
	}
	
	private void addNewItem(String newItemStr) {
		todoList.add(newItemStr);
		todoListAdapter.notifyDataSetChanged();
	}
	
}
