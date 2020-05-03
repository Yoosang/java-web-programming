package calculator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringCalculator {
	private static final String CUSTOM_PATTERN = "//(.)\n(.*)";
	private static final String PLUS_TOKEN = ":|,"; 
	
	public int add(String input) {
		if(isEmptyStr(input)) {
			return 0;
		}
		int result = 0;
		String[] numbers = split(input);
		
		for(String number : numbers) {
			result += positiveNum(number);
		}
		return result;
	}
	
	private int positiveNum(String num) {
		int number = Integer.parseInt(num);
		if(number < 0) {
			throw new RuntimeException();
		}
		return number;
	}

	private boolean isEmptyStr(String str) {
		if(str == null || str.isEmpty()) {
			return true;
		}
		return false;
	}
	
	private String[] split(String str) {
		Matcher m = Pattern.compile(CUSTOM_PATTERN).matcher(str);
		if(m.find()) {
			String cust_token = m.group(1);
			return m.group(2).split(cust_token);
		}
		return str.split(PLUS_TOKEN);
	}
	
}
