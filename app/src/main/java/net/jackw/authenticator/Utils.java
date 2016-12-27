package net.jackw.authenticator;

import java.util.*;

public class Utils {
	private static List<Character> base32Chars = Arrays.asList(
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', //  7
		'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', // 15
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', // 23
		'Y', 'Z', '2', '3', '4', '5', '6', '7'  // 31
	);
	private static Map<Character, Character> equivalences = new HashMap<>();
	static {
		// Initialise the characters that should be treated as equivalent so that users can misread things
		equivalences.put('1', 'I');
		equivalences.put('0', 'O');
		equivalences.put('8', 'B');
	}

	/**
	 * Convert a base32 string to a byte array
	 *
	 * @param original The base32 string to decode
	 * @return The decoded byte array
	 * @throws Base32ParseException
	 */
	public static byte[] base32Decode (String original) throws Base32ParseException {
		// Convert the input to upper case, and remove all padding from the end of the string
		original = original.toUpperCase().replaceAll("=+$", "");
		// Figure out how long the decoded value is expected to be (nb integer division so implicit floor)
		int len = original.length() * 5 / 8;
		// Byte array to store the result
		byte[] result = new byte[len];
		// Because there are multiple chars per resulting byte, store the current total that hasn't
		//  yet been added to a byte
		int runningResult = 0;
		// How many bits are filled in runningResult
		int lastPower = 0;
		// The next index to put into the result array
		int resultIndex = 0;

		for (char letter : original.toCharArray()) {
			if (equivalences.containsKey(letter)) {
				// The user has mistyped the letter
				letter = equivalences.get(letter);
			}
			// Figure out what number this char represents
			int index = base32Chars.indexOf(letter);
			if (index == -1) {
				// Not valid base32
				throw new Base32ParseException("Invalid base 32 character: " + letter);
			}
			// Add the new value to the running result, shifting the existing value along
			runningResult <<= 5;
			runningResult += index;
			lastPower += 5;
			if (lastPower >= 8) {
				// Move the highest 8 bits to the result byte array
				// nb. casting to byte will make it signed, but the raw bits are retained so we can
				//  retrieve the information by bitwise anding with 0xFF
				result[resultIndex++] = (byte) ((runningResult >>> (lastPower - 8)) & 0xFF);
				lastPower -= 8;
			}
		}
		if (lastPower != 0 && (runningResult & ((1 << lastPower) - 1)) != 0) {
			throw new Base32ParseException("Remaining data was not 0");
		}

		// Make sure the decoded string was the length we expected
		if (BuildConfig.DEBUG && resultIndex != len) {
			throw new AssertionError("Decoded length didn't match expected length");
		}

		return result;
	}

	public static String base32Encode (byte[] original, boolean pad) {
		String result = "";
		int runningTotal = 0;
		int bitsRemaining = 0;

		for (byte current : original) {
			runningTotal <<= 8;
			runningTotal += current & 0xFF;
			bitsRemaining += 8;

			while (bitsRemaining >= 5) {
				result += String.valueOf(base32Chars.get( (runningTotal >>> (bitsRemaining - 5)) & 0x1F ));
				bitsRemaining -= 5;
			}
		}

		// Add the last bits
		if (bitsRemaining != 0) {
			result += String.valueOf(base32Chars.get( (runningTotal << (5 - bitsRemaining)) & 0x1F));
		}

		while (pad && result.length() % 8 != 0) {
			result += "=";
		}

		return result;
	}
}
