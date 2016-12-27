package net.jackw.authenticator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UtilTest {
	@Test
	public void base32Decode_valid() throws Exception {
		String testCase = "JBSWY3DPEHPK3PXP";
		byte[] expected = { 'H', 'e', 'l', 'l', 'o', '!', (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF };
		byte[] result = Utils.base32Decode(testCase);
		assertArrayEquals(expected, result);
	}

	@Test
	public void base32Decode_invalid() throws Exception {
		String testCase = "JBSWY3DP=EHPK3PXP";
		try {
			byte[] result = Utils.base32Decode(testCase);
		} catch (Base32ParseException e) {
			assertTrue(true);
			return;
		}
		fail("Exception not thrown");
	}

	@Test
	public void base32Decode_padded() throws Exception {
		String testCase = "MZXW6YQ=";
		byte[] expected = { (byte) 0x66, (byte) 0x6F, (byte) 0x6F, (byte) 0x62 };

		byte[] result = Utils.base32Decode(testCase);
		assertArrayEquals(expected, result);
	}

	@Test
	public void base32Decode_equiv() throws Exception {
		String testCase = "1B0AA";
		String shouldBeEquivalentTo = "IBOAA";
		byte[] expected = Utils.base32Decode(shouldBeEquivalentTo);
		byte[] result = Utils.base32Decode(testCase);
		assertArrayEquals(expected, result);
	}



	@Test
	public void base32Encode_valid() throws Exception {
		String expected = "JBSWY3DPEHPK3PXP";
		byte[] testCase = { 'H', 'e', 'l', 'l', 'o', '!', (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF };
		String result = Utils.base32Encode(testCase, false);
		assertEquals(expected, result);
	}

	@Test
	public void base32Encode_noPadding () throws Exception {
		String expected = "MZXW6YQ";
		byte[] testCase = { (byte) 0x66, (byte) 0x6F, (byte) 0x6F, (byte) 0x62 };
		String result = Utils.base32Encode(testCase, false);
		assertEquals(expected, result);
	}

	@Test
	public void base32Encode_withPadding () throws Exception {
		String expected = "MZXW6YQ=";
		byte[] testCase = { (byte) 0x66, (byte) 0x6F, (byte) 0x6F, (byte) 0x62 };
		String result = Utils.base32Encode(testCase, true);
		assertEquals(expected, result);
	}
}
