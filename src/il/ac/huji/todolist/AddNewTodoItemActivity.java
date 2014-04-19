package il.ac.huji.todolist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

public class AddNewTodoItemActivity extends Activity {
		
	public static final String EXTRA_KEY_TITLE = "title";
	public static final String EXTRA_KEY_DATE = "dueDate";	
	private EditText newItemEditText;
	
	DatePicker datePicekr;
	private int year;
	private int month;
	private int day;
 
	private Button dialogOkButton;
	private Button dialogCancelButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_dialog_layout);
		setUpDialog();
		setButtonsListeners();
	}
	
	/**
	 * Setup the add dialog with text and date picker
	 */
	private void setUpDialog () {
		datePicekr = (DatePicker) findViewById(R.id.datePicker);
		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		datePicekr.init(year, month, day, null);
		
		// Get reference for the Edit Text
		newItemEditText = (EditText) findViewById(R.id.edtNewItem);
		
		// Handle the buttons
		dialogCancelButton = (Button) findViewById(R.id.btnCancel);
		dialogOkButton = (Button) findViewById(R.id.btnOK);
	}
	
	/**
	 * Set the correct Button Listeners
	 */
	private void setButtonsListeners () {
		
		dialogCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		dialogOkButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Date itemDueDate = getNewItemDate();
				String itemStr = newItemEditText.getText().toString();
				
				// Make sure the item text has some content
				if (itemStr.length() != 0) {
					Intent intent =  getIntent();
					intent.putExtra(EXTRA_KEY_TITLE, itemStr);
					intent.putExtra(EXTRA_KEY_DATE, itemDueDate);
					setResult(RESULT_OK, intent);
					finish();
				} else {
					Context context = getApplicationContext();
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, getString(R.string.toast_no_item), duration);
					toast.show();
				}
			}
		});
	}
	
	/**
	 * Return the new item Date from the date picker
	 * @return The new item Date as it set in the date picker
	 */
	private Date getNewItemDate () {
		int day = datePicekr.getDayOfMonth();
		int year = datePicekr.getYear();
		int month = datePicekr.getMonth()+1;
				
		String dateStr = new String(day+"/"+month+"/"+year);
		
		Date date = null;
		try {
			date = new SimpleDateFormat(TodoItem.DUE_DATE_FORMAT,Locale.ENGLISH).parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
}
