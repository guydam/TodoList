package il.ac.huji.todolist;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class TodoItem {
	private String itemStr;
	private Date dueDate;
	private long objectId;
	
	public final static String DUE_DATE_FORMAT = "dd/MM/yyyy";
	public final static String NO_DUE_DATE = "No Due Date";

	public TodoItem (String item, Date dueDate) {
		this.setItemStr(item);
		this.setDueDate(dueDate);
	}
	
	public TodoItem (String item, String dueDate) {
		SimpleDateFormat ft = new SimpleDateFormat(DUE_DATE_FORMAT, Locale.US);
		try {	
			this.setItemStr(item);
			this.setDueDate(ft.parse(dueDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public TodoItem (String item) {
		this.setDueDate(new Date());
		this.setItemStr(item);
	}

	/**
	 * @return the due Date as a String
	 */
	public String getDueDateStr() {
		SimpleDateFormat ft = new SimpleDateFormat (DUE_DATE_FORMAT,Locale.US);
		if (dueDate!=null) {
			return ft.format(dueDate);
		}
		return NO_DUE_DATE;
	}
	/**
	 * @return the due Date
	 */
	public Date getDueDate () {
		return dueDate;
	}
	
	/**
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * @return the item
	 */
	public String getItemStr() {
		return itemStr;
	}

	/**
	 * @param item the item to set
	 */
	public void setItemStr(String item) {
		this.itemStr = item;
	}
	
	/**
	 * Check if this item due date has passed
	 * @return true if the due date is passed, false otherwise
	 */
	public boolean isPassedDueDate () {
		Date curDate = new Date();
		if (dueDate!=null) {
			return dueDate.before(curDate);	
		}
		return false;
	}
	
	/**
	 * Checks if this qualifies as a "call to" item
	 * @param callTag the given call tag string to check
	 * @return true if the call tag presents in the item text and its contains some digits, false otherwise
	 */
	public boolean isCallItem (String callTag) {
		boolean containCallStr = itemStr.toLowerCase(Locale.US).contains(callTag.toLowerCase(Locale.US));
		boolean containDigits = itemStr.matches(".*\\d.*");
		
		return containCallStr && containDigits;
	};
	
	/**
	 * Remove all the non-digit characters from this item's text
	 * @return only the digits of this item text 
	 */
	public String extractPhoneNumber () {
		String numStr = itemStr.replaceAll("[^0-9.]", "");
		Log.d("TAG","phone number "+numStr);
		return numStr;
	}
	
	public void setObjectId (long objectId) {
		this.objectId = objectId;
	}
	
	public long getObjectId () {
		return this.objectId;
	}
	
}
