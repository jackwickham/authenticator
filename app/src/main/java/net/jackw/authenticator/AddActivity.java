package net.jackw.authenticator;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class AddActivity extends AppCompatActivity implements AccountAddListener, AccountAddQr.OnFragmentInteractionListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

		setupActionBar();
		setupMainFragment();
	}

	@Override
	public void onFragmentInteraction(Uri uri) {

	}

	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			// Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	private void setupMainFragment () {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		Fragment otherMethodsFragment = new AccountAddQr();

		fragmentTransaction.replace(R.id.account_add_frag, otherMethodsFragment);

		fragmentTransaction.commit();
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
	public void onOtherMethodPress () {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		Fragment otherMethodsFragment = new AccountAddManual();

		fragmentTransaction.replace(R.id.account_add_frag, otherMethodsFragment);

		fragmentTransaction.addToBackStack(null);

		fragmentTransaction.commit();
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
}
