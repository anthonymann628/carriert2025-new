package com.agilegeodata.carriertrack.android.utils;

import com.agilegeodata.carriertrack.android.constants.GlobalConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/*
 * Generic utils for manipulating dates and calendar.
 */
public class DateUtil{

	public static long calendarStartTimestamp(Calendar sDate, int numDays){
		//logger.debug("NumDays: " + numDays +" StartDate: " + calcDateFromTime(sDate.getTimeInMillis(), "MM/dd/yyyy" ));
		sDate.add(Calendar.DAY_OF_YEAR, numDays);

		sDate.set(Calendar.MINUTE, 0);
		sDate.set(Calendar.HOUR, 0);
		sDate.set(Calendar.SECOND, 0);
		//logger.debug(" New StartDate: " + calcDateFromTime(sDate.getTimeInMillis(), "MM/dd/yyyy" ));
		return sDate.getTimeInMillis();
	}

	public static boolean isSameDate(Calendar sDate, Calendar otherday){
		//logger.debug("Comparing: " + sDate.get(Calendar.MONTH)+"/" +sDate.get(Calendar.DAY_OF_MONTH)+
		//"/" +sDate.get(Calendar.YEAR) + " to "+ otherday.get(Calendar.MONTH)+"/" +otherday.get(Calendar.DAY_OF_MONTH)+
		//"/" +otherday.get(Calendar.YEAR));
		return otherday.get(Calendar.YEAR) == sDate.get(Calendar.YEAR)
			   && otherday.get(Calendar.MONTH) == sDate.get(Calendar.MONTH)
			   && otherday.get(Calendar.DAY_OF_MONTH) == sDate.get(Calendar.DAY_OF_MONTH);
	}

	public static boolean isAfterOrSameDate(Calendar sDate, Calendar otherday){
		Calendar newStartCal = Calendar.getInstance();
		Calendar newOtherCal = Calendar.getInstance();
		newStartCal.set(Calendar.YEAR, sDate.get(Calendar.YEAR));
		newStartCal.set(Calendar.MONTH, sDate.get(Calendar.MONTH));
		newStartCal.set(Calendar.DAY_OF_MONTH, sDate.get(Calendar.DAY_OF_MONTH));
		newOtherCal.set(Calendar.YEAR, otherday.get(Calendar.YEAR));
		newOtherCal.set(Calendar.MONTH, otherday.get(Calendar.MONTH));
		newOtherCal.set(Calendar.DAY_OF_MONTH, otherday.get(Calendar.DAY_OF_MONTH));

		//logger.debug("Comparing: " + newOtherCal.getTimeInMillis() + " " +  newStartCal.getTimeInMillis());
		return newOtherCal.getTimeInMillis() <= newStartCal.getTimeInMillis();
	}

	public static boolean isBeforeOrSameDate(Calendar sDate, Calendar otherday){
		Calendar newStartCal = Calendar.getInstance();
		Calendar newOtherCal = Calendar.getInstance();
		newStartCal.set(Calendar.YEAR, sDate.get(Calendar.YEAR));
		newStartCal.set(Calendar.MONTH, sDate.get(Calendar.MONTH));
		newStartCal.set(Calendar.DAY_OF_MONTH, sDate.get(Calendar.DAY_OF_MONTH));
		newOtherCal.set(Calendar.YEAR, otherday.get(Calendar.YEAR));
		newOtherCal.set(Calendar.MONTH, otherday.get(Calendar.MONTH));
		newOtherCal.set(Calendar.DAY_OF_MONTH, otherday.get(Calendar.DAY_OF_MONTH));

		//logger.debug("Comparing: " + newOtherCal.getTimeInMillis() + " " +  newStartCal.getTimeInMillis());
		return newOtherCal.getTimeInMillis() >= newStartCal.getTimeInMillis();
	}

	public static int compareCalendars(Calendar sDate, Calendar otherday){
		return sDate.compareTo(otherday);
	}

	public static final String calcDateFromTime(long milliseconds, String format){
		//logger.debug("Time: "+ milliseconds + "format" + format);
		if(format == null){
			format = GlobalConstants.DEFAULT_DATE_FORMAT;
		}
		Date date = new Date(milliseconds);

		String strDate = "";
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		strDate = formatter.format(date);
		return strDate;
	}

	public static final String formatStringAsDate(String date, String entryFormat, String exitFormat){
		String newDate = null;
		try{

			//logger.debug("entry format:" + date);
			SimpleDateFormat formatter = new SimpleDateFormat(entryFormat);
			Date theDate = formatter.parse(date.trim());
			formatter = new SimpleDateFormat(exitFormat);
			newDate = formatter.format(theDate);
		}
		catch(Exception e){
			//logger.debug("Exception e"+ e);
		}
		return newDate;
	}

	public static final Date convertStringToDate(String date, String entryFormat){
		Date theDate = null;
		try{
			//SimpleDateFormat formatter  = new SimpleDateFormat( "MMMM d, yyyy");"yyyy-mm-dd"
			SimpleDateFormat formatter = new SimpleDateFormat(entryFormat);
			theDate = formatter.parse(date);

		}
		catch(Exception e){

		}
		return theDate;
	}

	public static final Calendar calculateDayOfTheWeek(Date curDate, int amountToMove){
		Calendar cal = Calendar.getInstance();
		cal.setTime(curDate);
		cal.add(Calendar.DAY_OF_WEEK, amountToMove);
		return cal;
	}

	public static boolean overrideDST(String state){
		return state.equals("AZ");
	}

	public static String getUserTimeZone(long timeInMillis, int style, Locale locale){
		Calendar curCal = Calendar.getInstance();
		curCal.setTimeInMillis(timeInMillis);
		TimeZone tz = curCal.getTimeZone();
		Date d = new Date();
		d.setTime(curCal.getTimeInMillis());
		boolean isDST = tz.inDaylightTime(d);
		return tz.getDisplayName(isDST, style, locale);

	}

	public static boolean isUserTimeZoneInDST(long timeInMillis){
		Calendar curCal = Calendar.getInstance();
		curCal.setTimeInMillis(timeInMillis);
		TimeZone tz = curCal.getTimeZone();
		Date d = new Date();
		d.setTime(curCal.getTimeInMillis());
		boolean isDST = tz.inDaylightTime(d);
		return isDST;

	}

	public static int getUserTimeZoneOffset(long timeInMillis){
		Calendar curCal = Calendar.getInstance();
		curCal.setTimeInMillis(timeInMillis);
		TimeZone tz = curCal.getTimeZone();
		int timeOffSet = tz.getOffset(curCal.getTimeInMillis());

		return timeOffSet;
	}
}