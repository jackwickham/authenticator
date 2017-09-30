package net.jackw.authenticator;

import android.support.annotation.Nullable;

import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class QRDecoder {
	private Reader barcodeReader;

	public QRDecoder() {
		barcodeReader = new QRCodeReader();
	}

	@Nullable
	public Account detectAccount(byte[] data, int width, int height) {
		// See com.google.zxing.client.android.DecodeHandler

		// Not sure what the args are supposed to be for this - hopefully this works
		PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);

		// convert to the image format required for zxing
		BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));

		// Parse the image and URL
		// Spec: https://github.com/google/google-authenticator/wiki/Key-Uri-Format
		try {
			Result result = barcodeReader.decode(bitmap);
			URI uri = new URI(result.getText());

			if (!uri.getScheme().equalsIgnoreCase("otpauth")) {
				return null;
			}

			Map<String, String> queryString = decodeQueryString(uri.getQuery());

			if (!queryString.containsKey("secret")) {
				return null;
			}
			byte[] secret = Utils.base32Decode(queryString.get("secret"));

			HotpGenerator generator;

			switch (uri.getAuthority().toLowerCase()) {
				case "totp":
					Totp totpGen = new Totp(secret);

					if (queryString.containsKey("period")) {
						try {
							int period = Integer.parseInt(queryString.get("period"), 10);
							if (period > 0) {
								totpGen.setInterval(period);
							}
						} catch (NumberFormatException e) {
							// ignore it
						}
					}

					generator = totpGen;

					break;
				case "hotp":
					CounterHotp hotpGen = new CounterHotp(secret);

					if (queryString.containsKey("counter")) {
						try {
							long counter = Long.parseLong(queryString.get("counter"), 10);
							if (counter >= 0) {
								hotpGen.setCounter(counter);
							} else {
								// Mandatory argument
								return null;
							}
						} catch (NumberFormatException e) {
							return null;
						}
					} else {
						return null;
					}

					generator = hotpGen;
					break;
				default:
					return null;
			}

			if (queryString.containsKey("algorithm")) {
				switch (queryString.get("algorithm").toUpperCase()) {
					// SHA1 is default
					case "SHA256":
						generator.setAlgorithm(HotpGenerator.HashAlgorithm.SHA256);
						break;
					case "SHA512":
						generator.setAlgorithm(HotpGenerator.HashAlgorithm.SHA512);
						break;
				}
			}

			// Trim leading /, then split the path into issuer and username
			// Note that this is not compliant with the latest version of Google Authenticator,
			// because this URL decodes first before splitting, in compliance with the spec
			String[] pathParts = uri.getPath().substring(1).split(":", 1);

			String username = null;
			String issuer = null;

			if (pathParts.length == 1) {
				username = pathParts[0];
			} else {
				issuer = pathParts[0];
				username = pathParts[1];
			}

			if (queryString.containsKey("issuer")) {
				// Query parameter takes precedence
				issuer = queryString.get("issuer");
			}

			Account account = new Account(generator, username, issuer);

			return account;
		} catch (ReaderException e) {
			return null;
		} catch (URISyntaxException e) {
			return null;
		} catch (Base32ParseException e) {
			return null;
		}
	}

	// https://stackoverflow.com/a/13592567/2826188
	private static Map<String, String> decodeQueryString (String query) {
		HashMap<String, String> queryPairs = new HashMap<>();
		String[] pairs = query.split("&");
		try {
			for (String pair : pairs) {
				int idx = pair.indexOf("=");
				queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return queryPairs;
	}
}
