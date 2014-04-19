package il.ac.huji.todolist;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TodoItemSQLiteHelper extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "ItemsDB";
	
	private static final String TABLE_NAME = "items";
	private static final String COLUMN_ID_NAME = "id";
	private static final String COLUMN_TEXT_NAME = "text";
	private static final String COLUMN_DATE_NAME = "dueDate";
	
    public TodoItemSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		
        String CREATE_ITEMS_TABLE = "CREATE TABLE "+TABLE_NAME+" ( " +
                COLUMN_ID_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                COLUMN_TEXT_NAME + " TEXT, "+
                COLUMN_DATE_NAME + " TEXT )";
        
        db.execSQL(CREATE_ITEMS_TABLE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
		this.onCreate(db);
	}
	
	/**
	 * Add the given item to the SQL data base
	 * @param item The given item to add
	 * @return the added item object id
	 */
	public long addItem (TodoItem item) {
		long id;
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_TEXT_NAME, item.getItemStr());
		values.put(COLUMN_DATE_NAME, item.getDueDateStr());
		id = db.insert(TABLE_NAME, null, values);
		db.close();
//		Log.d(TodoListManagerActivity.LOG_TAG, "Adding new Item "+item.getItemStr()+" Id = "+id);
		
		return id;
	}
	
	/**
	 * Return all the Todo Items from the SQL database
	 * @return All the Todo Items as a List
	 */
	public ArrayList<TodoItem> getAllItems () {
		Log.d(TodoListManagerActivity.LOG_TAG, "Getting all items ");

		ArrayList<TodoItem> items = new ArrayList<TodoItem>();
		
		String query = "SELECT * FROM " + TABLE_NAME;
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(query,null);
		
		TodoItem item = null;
		if (cursor.moveToFirst()) {
			do {
				item = new TodoItem(cursor.getString(1),cursor.getString(2));
				item.setObjectId(cursor.getLong(0));
				items.add(item);
//				Log.d(TodoListManagerActivity.LOG_TAG, "got item "+item.getItemStr()+ " id = "+item.getObjectId());
			} while (cursor.moveToNext());
		}
		
		return items;
	}
	
	/**
	 * Remove the given item from the SQL database
	 * @param item The given item to remove
	 * @return true if an item has been removed
	 */
	public boolean removeItem (TodoItem item) {
//		Log.d(TodoListManagerActivity.LOG_TAG, "remove item "+item.getItemStr()+ " id = "+item.getObjectId());
		SQLiteDatabase db = this.getWritableDatabase();
		
		return db.delete(TABLE_NAME,
				COLUMN_ID_NAME +"='"+item.getObjectId()+"'", 
				null) > 0;
	}
	
	/**
	 * Clears the SQL database
	 */
	public void clearTable () {
		SQLiteDatabase db = this.getWritableDatabase();

		db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
		this.onCreate(db);
	}
	
}
