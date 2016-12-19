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
		byte[] secret = {};
        HotpGenerator generator = new CounterHotp(secret, HotpGenerator.HashAlgorithm.sha1, 6, 1);
		assertEquals("", generator.generateCode());
    }
}