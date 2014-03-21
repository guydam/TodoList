package il.ac.huji.todolist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

	// Defines for Parse DB
	public static final String TODO_ITEM_CLASS_NAME = "TodoItem";
	public static final String TODO_ITEM_TEXT_KEY = "todoText";
	public static final String TODO_ITEM_DUE_DATE_KEY = "dueDate";
	public static final String TODO_ITEM_CREATED_AT_KEY = "createdAt";
	public static final String TODO_ITEM_USER_KEY = "user";
	public static final String TODO_ITEM_OBJECTID_KEY = "objectId";

	// Name Definitions for the shared preferences mechanism
	public static final String PREFS_NAME = "MyPrefsFile";
	public static final String STRING_SET_NAME = "StringSetNum_";
	public static final String DATE_SET_NAME = "DateSetNum_";
	public static final String OBJECTID_SET_NAME = "ObjectIdNum_";
	public static final String ITEMS_COUNT_NAME = "ItemsCount";

	public static final int INTENT_REQUEST_CODE = 11;

	// Reference to the Todo List , Adapter and List View
	private ArrayList<TodoItem> todoList;
	private AltColorAdapter todoListAdapter;
	private ListView todoListView;
	private ProgressBar progressBar;

	private ParseUser curUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Init the Parse DB
		Parse.initialize(this, "RqbP6dRTutucJE7GW8Tib6B8VtzSXB5JbnsMJUCQ", "H68Sthcqwey6GSd0Cmnj6AH8ahccMlpdxOPOH0nj");

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

		curUser = ParseUser.getCurrentUser();
		if (curUser == null) {
			loginUser();
		} else {
			getListFromCloud();
		}
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
			String objectId = todoList.get(position).getObjectId();
			todoList.remove(position);
			todoListAdapter.notifyDataSetChanged();
			removeItemFromCloud(objectId);
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
		todoList.add(newItem);
		todoListAdapter.notifyDataSetChanged();
		addItemToCloud(newItem);
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
	 * Login to Parse as anonymous user
	 */
	private void loginUser() {
		ParseAnonymousUtils.logIn(new LogInCallback() {

			@Override
			public void done(ParseUser user, com.parse.ParseException e) {
				if (e == null) {
					curUser = user;
					showToast(getString(R.string.login_ok));
					getListFromCloud();
				} else {
					showToast(getString(R.string.login_bad));
				}
			}
		});
	}

	/**
	 * Delete an item from the cloud 
	 * @param objectId the given object id to delete
	 */
	private void removeItemFromCloud(String objectId) {
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(TODO_ITEM_CLASS_NAME);

		try {
			ParseObject object = query.get(objectId);
			object.deleteInBackground();
		} catch (com.parse.ParseException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Add the given TodoItem to the cloud
	 * @param item The given item to add
	 */
	private void addItemToCloud(TodoItem item) {
		final int itemLocation = todoList.size() - 1;
		final ParseObject pObject = new ParseObject(TODO_ITEM_CLASS_NAME);

		pObject.put(TODO_ITEM_TEXT_KEY, item.getItemStr());
		pObject.put(TODO_ITEM_DUE_DATE_KEY, item.getDueDate());
		pObject.put(TODO_ITEM_USER_KEY, curUser);

		item.setObjectId(pObject.getObjectId());
		pObject.saveInBackground(new SaveCallback() {

			@Override
			public void done(com.parse.ParseException e) {
				todoList.get(itemLocation).setObjectId(pObject.getObjectId());
			}
		});
	}

	/**
	 * Retrieve the Todo list from the cloud
	 */
	private void getListFromCloud() {
		todoList.clear();

		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(TODO_ITEM_CLASS_NAME);
		query.addAscendingOrder(TODO_ITEM_CREATED_AT_KEY);
		query.whereEqualTo(TODO_ITEM_USER_KEY, curUser);

		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> objects, com.parse.ParseException e) {

				// If everything was ok update the list and notify data change
				if (e == null) {
					for (ParseObject object : objects) {
						TodoItem item = new TodoItem(object.getString(TODO_ITEM_TEXT_KEY), object.getDate(TODO_ITEM_DUE_DATE_KEY));
						item.setObjectId(object.getObjectId());
						todoList.add(item);
					}
					progressBar.setVisibility(View.GONE);
					todoListAdapter.notifyDataSetChanged();

					// Else, take the list from the saved shared
					// preferences
				} else {
					e.printStackTrace();
					getListFromSharedPreferences();
					progressBar.setVisibility(View.GONE);
					todoListAdapter.notifyDataSetChanged();
				}

			}
		});
	}

	/**
	 * Retrieve the todo items array from the local shared preferences
	 */
	private void getListFromSharedPreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

		int listSize = settings.getInt(ITEMS_COUNT_NAME, 0);
		SimpleDateFormat ft = new SimpleDateFormat(TodoItem.DUE_DATE_FORMAT, Locale.US);

		for (int i = 0; i < listSize; i++) {
			String curStr = settings.getString(STRING_SET_NAME + i, "");
			String curDateStr = settings.getString(DATE_SET_NAME + i, "");
			String objectId = settings.getString(OBJECTID_SET_NAME + i, "");
			try {
				TodoItem item = new TodoItem(curStr, ft.parse(curDateStr));
				item.setObjectId(objectId);
				todoList.add(item);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Save the list on shared preferences
	 */
	private void saveListOnSharedPrefereces() {
		// Get the shared preferences editor
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		// Put the items count into shared preferences
		editor.putInt(ITEMS_COUNT_NAME, todoList.size());

		// Put all the todo items into the shared preferences
		for (int i = 0; i < todoList.size(); i++) {
			editor.putString(STRING_SET_NAME + i, todoList.get(i).getItemStr());
			editor.putString(DATE_SET_NAME + i, todoList.get(i).getDueDateStr());
			editor.putString(OBJECTID_SET_NAME + i, todoList.get(i).getObjectId());

		}
		// Commit the edits!
		editor.commit();
	}

	@Override
	protected void onStop() {
		super.onStop();
		saveListOnSharedPrefereces();
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
}
