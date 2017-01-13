package net.jackw.authenticator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.*;

public class MainActivity extends AppCompatActivity {
	public final static int REFRESH_INTERVAL = 100; // ms
	private final static int REQUEST_CODE_ADD_ACCOUNT = 1;

	private List<Account> accounts;
	private RefreshHandler refreshTask = null;
	private AccountListAdapter listAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// Initialise the list
		ListView accountList = (ListView) findViewById(R.id.account_list);
		listAdapter = new AccountListAdapter(this, R.layout.account_row);
		accountList.setAdapter(listAdapter);

		// Then populate the list
		populateList();

		refreshTask = new RefreshHandler();
	}

	private void populateList () {
		// Init db
		DatabaseHelper dbHelper = DatabaseHelper.init(this);
		accounts = dbHelper.getAccountsDb().getAllAccounts();

		listAdapter.clear();
		listAdapter.addAll(accounts);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
			case R.id.action_settings:
				return true;
			case R.id.action_add:
				startAddAccount();
				return true;

		}

		return super.onOptionsItemSelected(item);
	}

	public void startAddAccount() {
		Intent intent = new Intent(this, AddActivity.class);

		startActivityForResult(intent, REQUEST_CODE_ADD_ACCOUNT);
	}

	@Override
	protected void onPause () {
		super.onPause();

		refreshTask.pause();
	}

	@Override
	protected void onResume () {
		super.onResume();

		refreshTask.resume();
	}

	@Override
	public void onSaveInstanceState (Bundle outState) {
		// Actually, no state to persist I think

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState (Bundle savedInstanceState) {
		// Nothing to do here either

		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CODE_ADD_ACCOUNT:
				if (resultCode != RESULT_CANCELED) {
					// Refresh the list
					populateList();
				}
				break;
		}
	}


	private class AccountListAdapter extends ArrayAdapter<Account> {
		public AccountListAdapter(Context context, int userRowId, List<Account> accounts) {
			super(context, userRowId, accounts);
		}
		public AccountListAdapter(Context context, int userRowId) {
			super(context, userRowId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			Account account = getItem(position);

			View row;
			if (convertView != null) {
				// Reuse the existing view
				row = convertView;
			} else {
				// Create new one
				row = inflater.inflate(R.layout.account_row, null);
			}

			TextView codeView = (TextView) row.findViewById(R.id.code);
			TextView labelView = (TextView) row.findViewById(R.id.code_label);
			CountdownIndicator countdown = (CountdownIndicator) row.findViewById(R.id.countdown_indicator);

			String code = account.getCodeGenerator().getCodeForDisplay();

			CodeGenerator.Type type = account.getCodeGenerator().getType();
			if (type == CodeGenerator.Type.TOTP) {
				float timeRemaining = ((Totp) account.getCodeGenerator()).getTimeRemainingFraction();

				countdown.setVisibility(View.VISIBLE);
				countdown.setPhase(timeRemaining);
			} else if (type == CodeGenerator.Type.HOTP) {
				countdown.setVisibility(View.GONE);
			}

			ImageView imageView = (ImageView) row.findViewById(R.id.account_image);
			if (account.hasImage()) {
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageBitmap(account.getImage());
			} else {
				imageView.setVisibility(View.GONE);
			}

			// Set the code
			codeView.setText(code);

			// Set the label
			String label = account.getIssuer();
			Spannable labelSpan;
			if (account.getUsername() != null) {
				label += " (" + account.getUsername() + ")";
				labelSpan = new SpannableString(label);
				labelSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.label_username)), account.getIssuer().length(), label.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			} else {
				labelSpan = new SpannableString(label);
			}

			labelView.setText(labelSpan, TextView.BufferType.SPANNABLE);

			return row;
		}
	}

	private class RefreshHandler {
		private final Handler h;
		private final RefreshRunner callback;
		private boolean isRunning = true;

		public RefreshHandler() {
			h = new Handler();
			callback = new RefreshRunner();
			h.postDelayed(callback, REFRESH_INTERVAL);
		}

		private class RefreshRunner implements Runnable {
			@Override
			public void run() {
				listAdapter.notifyDataSetChanged();

				h.postDelayed(this, REFRESH_INTERVAL);
			}
		}

		public void pause () {
			h.removeCallbacks(callback);
			isRunning = false;
		}

		public void resume () {
			if (!isRunning) {
				// Run immediately
				h.post(callback);
				isRunning = true;
			}
		}
	}
}
