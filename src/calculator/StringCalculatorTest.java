package calculator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StringCalculatorTest {
	private StringCalculator cal;

	@BeforeEach
	public void setup() {
		cal = new StringCalculator();
	}
	
	@Test 
	public void add_nullOrEmpty() {
		assertEquals(0, cal.add(null));
		assertEquals(0, cal.add(""));
	}
	
	@Test
	public void add_oneNumber() {
		assertEquals(5, cal.add("5"));
	}
	
	@Test
	public void add_default() {
		assertEquals(6, cal.add("1:2:3"));
		assertEquals(6, cal.add("1,2,3"));
		assertEquals(6, cal.add("1,2:3"));
	}
	
	@Test
	public void add_custom() {
		assertEquals(6, cal.add("//;\n1;2;3"));
	}
	
	@Test 
	public void add_negative() {
		Throwable exception = assertThrows(NullPointerException.class, 
				() -> {throw new NullPointerException("negative");});
		System.out.println(exception);
	}
}
