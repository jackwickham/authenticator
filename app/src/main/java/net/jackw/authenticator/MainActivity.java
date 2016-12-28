package net.jackw.authenticator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.*;
import android.widget.*;

import java.util.*;

public class MainActivity extends AppCompatActivity {
    public static int REFRESH_INTERVAL = 100; // ms

    private List<Account> accounts;
    private RefreshHandler refreshTask = null;
    private AccountListAdapter listAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Init db
        DatabaseHelper dbHelper = DatabaseHelper.init(getApplicationContext());

        accounts = dbHelper.getAccountsDb().getAllAccounts();

        ListView accountList = (ListView) findViewById(R.id.account_list);
        listAdapter = new AccountListAdapter(this, R.layout.account_row, accounts);
        accountList.setAdapter(listAdapter);

        refreshTask = new RefreshHandler();
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

        switch(id) {
            case R.id.action_settings:
                return true;
            case R.id.action_add:
                addAccount();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public void addAccount () {
        Intent intent = new Intent(this, AddActivity.class);

        startActivity(intent);
    }



    private class AccountListAdapter extends ArrayAdapter<Account> {
        public AccountListAdapter(Context context, int userRowId, List<Account> accounts) {
            super(context, userRowId, accounts);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
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

            String code = account.getCodeGenerator().getCodeForDisplay();

            if (account.getCodeGenerator().getType() == CodeGenerator.Type.TOTP) {
                float timeRemaining = ((Totp) account.getCodeGenerator()).getTimeRemainingFraction();
                CountdownIndicator countdown = (CountdownIndicator) row.findViewById(R.id.countdown_indicator);

                countdown.setPhase(timeRemaining);
            }

            // Set the code
            codeView.setText(code);

            // Set the label
            String label = account.getIssuer() + " (" + account.getUsername() + ")";
            Spannable labelSpan = new SpannableString(label);
            labelSpan.setSpan(new ForegroundColorSpan(getColor(R.color.label_username)), account.getIssuer().length(), label.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            labelView.setText(labelSpan, TextView.BufferType.SPANNABLE);

            return row;
        }
    }

    private class RefreshHandler {
        private final Handler h;

        public RefreshHandler () {
            h = new Handler();
            h.postDelayed(new RefreshRunner(), REFRESH_INTERVAL);
        }

        private class RefreshRunner implements Runnable {
            @Override
            public void run () {
                listAdapter.notifyDataSetChanged();

                h.postDelayed(this, REFRESH_INTERVAL);
            }
        }

    }
}
