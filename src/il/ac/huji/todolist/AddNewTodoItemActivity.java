package il.ac.huji.todolist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AddNewTodoItemActivity extends Activity {
	
	public static final int SPINNER_START_YEAR = 1954;
	public static final int SPINNER_END_YEAR = 2020;
	
	public static final String EXTRA_KEY_TITLE = "title";
	public static final String EXTRA_KEY_DATE = "dueDate";	
	private EditText newItemEditText;
	
	private Spinner dateDaySpinner;
	private Spinner dateMonthSpinner;
	private Spinner dateYearSpinner;
	
	private Button dialogOkButton;
	private Button dialogCancelButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_dialog_layout);
		setUpDialog();
		setSpinnersCurrentDate();
		setButtonsListeners();
	}
	
	private void setUpDialog () {
		dateDaySpinner = (Spinner) findViewById(R.id.dateDaySpinner);
		dateMonthSpinner = (Spinner) findViewById(R.id.dateMonthSpinner);
		dateYearSpinner = (Spinner) findViewById(R.id.dateYearSpinner);
		
		// Create the days Spinner
		ArrayList<Integer> days = new ArrayList<Integer>();
		for (int i = 1 ; i <= 31 ; i++) {
			days.add(i);
		}
		ArrayAdapter<Integer> daysAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item,days);
		daysAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
		dateDaySpinner.setAdapter(daysAdapter);

		
		// Create the Years Spinner
		ArrayList<Integer> years = new ArrayList<Integer>();
		for (int i = SPINNER_START_YEAR ; i <= SPINNER_END_YEAR ; i++) {
			years.add(i);			
		}
		ArrayAdapter<Integer> yearsAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item,years);
		yearsAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
		dateYearSpinner.setAdapter(yearsAdapter);

		ArrayAdapter<String> monthAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.due_date_months));
		monthAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
		dateMonthSpinner.setAdapter(monthAdapter);
		
		
		
		// Get reference for the Edit Text
		newItemEditText = (EditText) findViewById(R.id.edtNewItem);
		
		// Handle the buttons
		dialogCancelButton = (Button) findViewById(R.id.dialogCancelButton);
		dialogOkButton = (Button) findViewById(R.id.dialogOkButton);
	}
	
	/**
	 * Make sure the spinners are selecting the current date by default 
	 */
	private void setSpinnersCurrentDate () {
		try {
			Date curDate = new Date();
			SimpleDateFormat ft = new SimpleDateFormat(TodoItem.DUE_DATE_FORMAT,Locale.US);
			String curDateStr = ft.format(curDate);
			String[] dateArr = curDateStr.split("\\/");

			int curYearPos = Integer.parseInt(dateArr[2]) - SPINNER_START_YEAR;
			
			dateDaySpinner.setSelection(Integer.parseInt(dateArr[0])-1);
			dateMonthSpinner.setSelection(Integer.parseInt(dateArr[1])-1);
			dateYearSpinner.setSelection(curYearPos);
		} catch (Exception e) {
			Log.e("TAG", "Got an issue when tried to set spinners to current date"+e.getMessage());
		}
	}
	
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
	
	private Date getNewItemDate () {
		int day = (Integer) dateDaySpinner.getSelectedItem();
		int year = (Integer) dateYearSpinner.getSelectedItem();
		int month = dateMonthSpinner.getSelectedItemPosition()+1;
				
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
