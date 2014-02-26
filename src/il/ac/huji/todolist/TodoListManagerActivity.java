package il.ac.huji.todolist;

import java.util.ArrayList;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class TodoListManagerActivity extends Activity {

	// Name Definitions for the shared preferences mechanism
	public static final String PREFS_NAME = "MyPrefsFile";
	public static final String STRING_SET_NAME = "StringSetNum_";
	public static final String ITEMS_COUNT_NAME = "ItemsCount";

	ArrayList<String> todoList;
	AltColorAdapter todoListAdapter;

	ListView todoListView;
	EditText newItemEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_todo_list_manager);

		// Define the todo list array
		todoList = new ArrayList<String>();

		// Get references to views
		todoListView = (ListView) findViewById(R.id.lstTodoItems);
		newItemEditText = (EditText) findViewById(R.id.edtNewItem);

		// Register the context menu to the list view
		registerForContextMenu(todoListView);

		// Create the list adapter and set it with the list view
		todoListAdapter = new AltColorAdapter(getApplicationContext(),
				R.layout.todo_list_row, todoList);
		todoListView.setAdapter(todoListAdapter);
		getListFromSharedPreferences();
		todoListAdapter.notifyDataSetChanged();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.todo_list_manager, menu);
		return true;
	}

	/**
	 * Called then options item is selected
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (R.id.menuItemAdd):
			String newItemStr = newItemEditText.getText().toString();
			if (newItemStr.length() != 0) {
				addNewItem(newItemStr);
				newItemEditText.setText("");
				((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE))
						.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,
								0);
				showToast(getString(R.string.toast_item_added));
			} else {
				showToast(getString(R.string.toast_no_item));
				return false;
			}
			return true;
		}
		;

		return super.onOptionsItemSelected(item);
	};

	/**
	 * Called when the context menu is created (long pressing the list view)
	 */
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.todo_list_context_menu, menu);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		String titleStr = getItemContent(info.position);
		menu.setHeaderTitle(titleStr);
	}

	/**
	 * Called when a context menu item is selected
	 */
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.menuItemDelete:
			// Get the view item and set its animation
			View curView = info.targetView;
			Animation anim = AnimationUtils.loadAnimation(this, R.animator.slide_out);
			
			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationEnd(Animation animation) {
					//curView.setVisibility(View.GONE);
					removeItem((int) info.position);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {					
				}

				@Override
				public void onAnimationStart(Animation animation) {					
				}
			});
			
			curView.startAnimation(anim);			
			return true;
		}
		

		return super.onContextItemSelected(item);
	};

	/**
	 * Get the todo item content from the array
	 * 
	 * @param position
	 *            The given position index to get
	 * @return The todo item in the given position
	 */
	private String getItemContent(int position) {
		if (position < todoList.size() && position >= 0) {
			return todoList.get(position);
		}
		return "";
	}

	/**
	 * Remove item from the todo array
	 * 
	 * @param position
	 *            the given index to remove
	 * @return true if the item was removed, false otherwise
	 */
	private boolean removeItem(int position) {
		if (position < todoList.size() && position >= 0) {
			todoList.remove(position);
			todoListAdapter.notifyDataSetChanged();
			return true;
		}
		return false;
	}

	/**
	 * Add a new todo item to the array
	 * 
	 * @param newItemStr
	 *            The item to add
	 */
	private void addNewItem(String newItemStr) {
		todoList.add(newItemStr);
		todoListAdapter.notifyDataSetChanged();
	}

	/**
	 * Display a short toast in the screen
	 * 
	 * @param text
	 *            the given text to display in the toast
	 */
	private void showToast(String text) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

	/**
	 * Retrieve the todo items array from the local shared preferences
	 */
	private void getListFromSharedPreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		int listSize = settings.getInt(ITEMS_COUNT_NAME, 0);

		for (int i = 0; i < listSize; i++) {
			todoList.add(settings.getString(STRING_SET_NAME + i, ""));
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Get the shared preferences editor
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		// Put the items count into shared preferences
		editor.putInt(ITEMS_COUNT_NAME, todoList.size());
		
		// Put all the todo items into the shared preferences
		for (int i = 0; i < todoList.size(); i++) {
			editor.putString(STRING_SET_NAME + i, todoList.get(i));
		}

		// Commit the edits!
		editor.commit();
	}
	
	@Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
	           .setMessage(getString(R.string.exit_confirmation_msg))
	           .setCancelable(false)
	           .setPositiveButton(getString(R.string.exit_confirmation_yes), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   TodoListManagerActivity.this.finish();
	               }
	           })
	           .setNegativeButton(getString(R.string.exit_confirmation_no), null)
	           .show();
	}
}
