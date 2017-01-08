package net.jackw.authenticator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class HotpTest {
	@Test
	public void code_isCorrect() throws Exception {
		byte[] secret = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x90, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x90 };
        HotpGenerator generator = new CounterHotp(secret, HotpGenerator.HashAlgorithm.SHA1, 6, 1);
		assertEquals("803282", generator.generateCode());
    }

	@Test
	public void code_leadingZero () throws Exception {
		byte[] secret = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x90, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x90 };
		HotpGenerator generator = new CounterHotp(secret, HotpGenerator.HashAlgorithm.SHA1, 6, 2);
		assertEquals("039425", generator.generateCode());
	}
}