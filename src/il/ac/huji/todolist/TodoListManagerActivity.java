package il.ac.huji.todolist;

import java.util.ArrayList;
import java.util.Date;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class TodoListManagerActivity extends Activity {

	public static final String LOG_TAG = "TODO";

	public static final int INTENT_REQUEST_CODE = 11;

	// Reference to the Todo List , Adapter and List View
	private ArrayList<TodoItem> todoList;
	private AltColorAdapter todoListAdapter;
	private ListView todoListView;
	private ProgressBar progressBar;

	private TodoItemSQLiteHelper itemsDb;
	private TodoListAsyncTask todoListTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_todo_list_manager);

		progressBar = (ProgressBar) findViewById(R.id.listProgressBar);

		// Define the Todo list array
		todoList = new ArrayList<TodoItem>();

		// Get references to views
		todoListView = (ListView) findViewById(R.id.lstTodoItems);

		// Register the context menu to the list view
		registerForContextMenu(todoListView);

		// Create the list adapter and set it with the list view
		todoListAdapter = new AltColorAdapter(getApplicationContext(), R.layout.todo_list_row, todoList);
		todoListView.setAdapter(todoListAdapter);

		// Create a new SQLite Helper
		itemsDb = new TodoItemSQLiteHelper(getApplicationContext());

		// Get the list from the DB (this will refresh the listView)
		todoListTask = new TodoListAsyncTask();
		todoListTask.execute();
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
			Intent addDialogIntent = new Intent(getApplicationContext(), AddNewTodoItemActivity.class);
			startActivityForResult(addDialogIntent, INTENT_REQUEST_CODE);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INTENT_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				if (data.hasExtra(AddNewTodoItemActivity.EXTRA_KEY_TITLE) && data.hasExtra(AddNewTodoItemActivity.EXTRA_KEY_DATE)) {
					String newItemStr = data.getExtras().getString(AddNewTodoItemActivity.EXTRA_KEY_TITLE);
					Date newItemDate = (Date) data.getExtras().getSerializable(AddNewTodoItemActivity.EXTRA_KEY_DATE);

					if (newItemStr.length() != 0) {
						TodoItem newItem = new TodoItem(newItemStr, newItemDate);
						addNewItem(newItem);
						showToast(getString(R.string.toast_item_added));
					} else {
						showToast(getString(R.string.toast_no_item));
					}
				}
			}
		}
	};

	/**
	 * Called when the context menu is created (long pressing the list view)
	 */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.todo_list_context_menu, menu);

		// Get the selected item
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		TodoItem itemSelected = getItemContent(info.position);

		// Set the menu title
		String titleStr = getItemContent(info.position).getItemStr();
		menu.setHeaderTitle(titleStr);

		MenuItem callItem = menu.findItem(R.id.menuItemCall);
		callItem.setVisible(false);

		// If this is a call item, add the relevant item
		if (itemSelected.isCallItem(getString(R.string.todo_call_tag))) {
			String callStr = getString(R.string.todo_call_tag) + " " + itemSelected.extractPhoneNumber();
			callItem.setTitle(callStr);
			callItem.setVisible(true);
		}
	}

	/**
	 * Called when a context menu item is selected
	 */
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.menuItemDelete:
			// Get the view item and set its animation
			View curView = info.targetView;
			Animation anim = AnimationUtils.loadAnimation(this, R.animator.slide_out);

			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationEnd(Animation animation) {
					// curView.setVisibility(View.GONE);
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

		case R.id.menuItemCall:
			String numStr = item.getTitle().toString();
			numStr = numStr.replaceAll("[^0-9.]", "");
			Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numStr));
			startActivity(dial);
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
	private TodoItem getItemContent(int position) {
		if (position < todoList.size() && position >= 0) {
			return todoList.get(position);
		}
		return null;
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

			// Remove the item from the local DB
			itemsDb.removeItem(todoList.get(position));

			// Remove the item from the list and view
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
	private void addNewItem(TodoItem newItem) {
		// Add the item to the DB (get its object id)
		long id;
		id = itemsDb.addItem(newItem);

		if (id != -1) {
			// Add the item to the list and view
			newItem.setObjectId(id);
			todoList.add(newItem);
			todoListAdapter.notifyDataSetChanged();			
		}
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

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setMessage(getString(R.string.exit_confirmation_msg)).setCancelable(false)
				.setPositiveButton(getString(R.string.exit_confirmation_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						TodoListManagerActivity.this.finish();
					}
				}).setNegativeButton(getString(R.string.exit_confirmation_no), null).show();
	}

	private class TodoListAsyncTask extends AsyncTask<String, TodoItem, String> {

		@Override
		protected String doInBackground(String... arg0) {
			progressBar.setVisibility(View.VISIBLE);
			todoList.clear();
			ArrayList<TodoItem> list = itemsDb.getAllItems();
			Log.d(LOG_TAG, "Got from DB total " + list.size());

			for (TodoItem item : list) {
				try {
					Thread.sleep(1000);
					publishProgress(item);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		protected void onProgressUpdate(TodoItem... a) {
			todoList.add(a[0]);
			todoListAdapter.notifyDataSetChanged();
		}
		
	    protected void onPostExecute(String result) {
			Log.d(LOG_TAG, "Async Task Done");
			progressBar.setVisibility(View.GONE);
	    }

	}

}
