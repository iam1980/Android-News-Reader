package com.suredigit.naftemporikihd;

import java.text.ParseException;

public class GreekDatesHelper {

	public static String getGreekDay(String day) throws ParseException{
		if (day.equalsIgnoreCase("Monday")) return "�������";
		if (day.equalsIgnoreCase("Tuesday")) return "�����";
		if (day.equalsIgnoreCase("Wednesday")) return "�������";
		if (day.equalsIgnoreCase("Thursday")) return "������";
		if (day.equalsIgnoreCase("Friday")) return "���������";
		if (day.equalsIgnoreCase("Saturday")) return "�������";
		if (day.equalsIgnoreCase("Sunday")) return "�������";
		throw new ParseException("Could not tranlsate input date:" + day,0);
	}
	
	public static String getGreekMonth(String month,boolean geniki) throws ParseException{
		if (month.equalsIgnoreCase("January")) return geniki ? "����������" : "����������";
		if (month.equalsIgnoreCase("February")) return geniki ? "�����������" : "�����������";
		if (month.equalsIgnoreCase("March")) return geniki ? "�������" : "�������";
		if (month.equalsIgnoreCase("April")) return geniki ? "��������" : "��������";
		if (month.equalsIgnoreCase("May")) return geniki ? "�����" : "�����";
		if (month.equalsIgnoreCase("June")) return geniki ? "�������" : "�������";
		if (month.equalsIgnoreCase("July")) return geniki ? "�������" : "�������";
		if (month.equalsIgnoreCase("August")) return geniki ? "���������" : "���������";
		if (month.equalsIgnoreCase("September")) return geniki ? "�����������" : "�����������";
		if (month.equalsIgnoreCase("October")) return geniki ? "���������" : "���������";
		if (month.equalsIgnoreCase("November")) return geniki ? "���������" : "���������";
		if (month.equalsIgnoreCase("December")) return geniki ? "����������" : "����������";		
		throw new ParseException("Could not tranlsate input month:" + month,0);
	}	
}
