package net.jackw.authenticator.ui;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import net.jackw.authenticator.Account;
import net.jackw.authenticator.CounterHotp;
import net.jackw.authenticator.HotpGenerator;
import net.jackw.authenticator.R;
import net.jackw.authenticator.Totp;

import java.util.HashMap;

public class AddActivity extends AppCompatActivity implements AccountAddListener {
	private HashMap<AddMethod, Fragment> fragmentCache = new HashMap<>(2);

	private final AddMethod DEFAULT_METHOD = AddMethod.Manual;

	private Button switchMethodButton = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

		switchMethodButton = (Button) findViewById(R.id.add_other_button);
		switchMethodButton.setOnClickListener(new View.OnClickListener() {
			public void onClick (View v) {
				AccountAddFragment fragment = (AccountAddFragment) getSupportFragmentManager().findFragmentByTag("accountMethod");
				AddMethod method;
				if (fragment instanceof AccountAddQr) {
					method = AddMethod.Manual;
				} else if (fragment instanceof AccountAddManual){
					method = AddMethod.QR;
				} else {
					throw new IllegalStateException("Unknown fragment type");
				}

				setupMethodFragment(method, true);
			}
		});

		setupActionBar();

		// Try to restore state
		if (savedInstanceState == null) {
			setupMethodFragment(DEFAULT_METHOD, false);
		} else {
			setSwitchButtonText();
		}

		// Add listener to change the button text when the back stack changes
		getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				setSwitchButtonText();
			}
		});
	}

	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			// Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	private void setupMethodFragment (AddMethod type, boolean addToBackStack) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		AccountAddFragment methodFragment = null;
		switch (type) {
			case QR:
				methodFragment = new AccountAddQr();
				break;
			case Manual:
				methodFragment = new AccountAddManual();
				break;
		}
		fragmentTransaction.replace(R.id.account_add_frag, methodFragment, "accountMethod");

		if (addToBackStack) {
			fragmentTransaction.addToBackStack(null);
		}

		fragmentTransaction.commit();

		// Change the button text
		setSwitchButtonText(type);
	}

	private void setSwitchButtonText (AddMethod currentType) {
		int stringId = 0;
		switch(currentType) {
			case Manual:
				stringId = R.string.add_method_qr;
				break;
			case QR:
				stringId = R.string.add_method_manual;
				break;
		}
		switchMethodButton.setText(stringId);
	}

	private void setSwitchButtonText () {
		AccountAddFragment fragment = (AccountAddFragment) getSupportFragmentManager().findFragmentByTag("accountMethod");
		AddMethod method;
		if (fragment instanceof AccountAddQr) {
			method = AddMethod.QR;
		} else if (fragment instanceof AccountAddManual){
			method = AddMethod.Manual;
		} else {
			throw new IllegalStateException("Unknown fragment type");
		}

		setSwitchButtonText(method);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch(id) {
			case R.id.home:
				// TODO: If in the manual fragment, switch back to QR fragment
				// Use default handler
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void addTotpAccount (byte[] secret, String issuer, @Nullable String username, @Nullable Integer digits, @Nullable Integer period, @Nullable HotpGenerator.HashAlgorithm algorithm) {
		// Construct the code generator
		Totp generator = new Totp(secret);
		// Initialise optional arguments
		if (digits != null) {
			generator.setLength(digits);
		}
		if (period != null) {
			generator.setInterval(period);
		}
		if (algorithm != null) {
			generator.setAlgorithm(algorithm);
		}

		// Construct the account
		Account account = new Account(generator, username, issuer);

		account.save();

		// Inform the main activity that we added an account, and return to it
		setResult(RESULT_OK);
		finish();
	}

	@Override
	public void addHotpAccount (byte[]  secret, String issuer, @Nullable String username, @Nullable Integer digits, @Nullable Long counter, @Nullable HotpGenerator.HashAlgorithm algorithm) {
		// Construct the code generator
		CounterHotp generator = new CounterHotp(secret);
		// Initialise optional arguments
		if (digits != null) {
			generator.setLength(digits);
		}
		if (counter != null) {
			generator.setCounter(counter);
		}
		if (algorithm != null) {
			generator.setAlgorithm(algorithm);
		}

		// Construct the account
		Account account = new Account(generator, username, issuer);

		account.save();

		// Inform the main activity that we added an account, and return to it
		setResult(RESULT_OK);
		finish();
	}

	@Override
	public void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState (Bundle savedInstance) {
		// Everything has to be handled in onCreate instead
		super.onRestoreInstanceState(savedInstance);
	}


	public enum AddMethod {
		QR (1, "qr"),
		Manual (2, "manual");

		public final int value;
		public final String name;
		private AddMethod(int value, String name) {
			this.value = value;
			this.name = name;
		}

		public static AddMethod get (int i) {
			for (AddMethod type : AddMethod.values()) {
				if (type.value == i) {
					return type;
				}
			}
			return null;
		}
	}

}
