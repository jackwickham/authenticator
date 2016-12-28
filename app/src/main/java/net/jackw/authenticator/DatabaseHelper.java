package net.jackw.authenticator;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.provider.BaseColumns;

import java.util.*;

public class DatabaseHelper extends SQLiteOpenHelper  {
	/**
	 * Database data
	 */
	public static final String DATABASE_NAME = "Accounts.db";
	public static final int DATABASE_VERSION = 1;

	/**
	 * Instances of the table helpers
	 */
	private TableHelper[] tables;
	private AccountsDb accountsDb;

	/**
	 * Private constructor for singleton use
	 */
	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);

		tables = new TableHelper[1];
		tables[0] = accountsDb = new AccountsDb(this);
	}

	/**
	 * Singleton methods
	 */
	private static DatabaseHelper instance;
	public static DatabaseHelper instance () {
		if (instance == null) {
			throw new RuntimeException("Database hasn't been initialised yet");
		}
		return instance;
	}
	public static DatabaseHelper init (Context context) {
		if (instance == null) {
			instance = new DatabaseHelper(context);
		}
		return instance;
	}

	/**
	 * Table getters
	 */
	public AccountsDb getAccountsDb () {
		return accountsDb;
	}


	/**
	 * Table helper class
	 */
	private static abstract class TableHelper {
		protected abstract void onCreate (SQLiteDatabase db);
		protected void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) { }
		protected void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) { }
	}

	/**
	 * Accounts table helper
	 */
	public static class AccountsDb extends TableHelper implements BaseColumns {
		/**
		 * Table data
		 */
		public static final String TABLE_NAME = "accounts";

		public static final String COLUMN_NAME_ISSUER = "isser";
		public static final String COLUMN_NAME_USERNAME = "username";
		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_EXTRA = "extra"; // Store the secret, or anything else needed by the method

		private static final String SQL_CREATE_TABLE = String.format(
				"CREATE TABLE IF NOT EXISTS %s" +
				"(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s INTEGER, %s TEXT)",
			TABLE_NAME, _ID, COLUMN_NAME_ISSUER, COLUMN_NAME_USERNAME, COLUMN_NAME_TYPE, COLUMN_NAME_EXTRA
		);
		private static final String SQL_DESTROY_TABLE = String.format(
				"DROP TABLE IF EXISTS %s",
				TABLE_NAME
		);

		/**
		 * Instance of the outer class, so we can make SQL queries
		 */
		private DatabaseHelper dbHelper;

		private AccountsDb (DatabaseHelper dbHelper) {
			this.dbHelper = dbHelper;
		}

		/**
		 * Now for actually useful methods
		 */
		private Account constructAccount (int id, String issuer, String username, CodeGenerator.Type type, String extra) throws CodeGeneratorConstructionException {
			CodeGenerator gen;
			switch (type) {
				case TOTP:
					gen = new Totp(extra);
					break;
				case HOTP:
					gen = new CounterHotp(extra);
					break;
				default:
					throw new CodeGeneratorConstructionException("Invalid type");
			}
			Account account = new Account(gen, username, issuer, id);

			return account;
		}
		/**
		 * Load all accounts from the DB
		 *
		 * @return List of accounts from the DB
		 */
		public List<Account> getAllAccounts () {
			// Construct the query
			String[] cols = {_ID, COLUMN_NAME_ISSUER, COLUMN_NAME_USERNAME, COLUMN_NAME_TYPE, COLUMN_NAME_EXTRA};
			Cursor cursor = this.dbHelper.getReadableDatabase().query(
					TABLE_NAME,
					cols,
					null,
					null,
					null,
					null,
					_ID + " DESC"
			);
			// Run it and process the results
			List<Account> accounts = new ArrayList<>(cursor.getCount());
			while(cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndexOrThrow(_ID));
				String issuer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ISSUER));
				String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_USERNAME));
				CodeGenerator.Type type = CodeGenerator.Type.get(
						cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_TYPE))
				);
				String extra = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXTRA));

				try {
					accounts.add(constructAccount(id, issuer, username, type, extra));
				} catch (CodeGeneratorConstructionException e) {
					// todo: log this, alert the user, or something
					continue;
				}
			}
			cursor.close();

			// debugging
			try {
				accounts.add(constructAccount(1, "Acme, Inc", "SuperCoolUsername", CodeGenerator.Type.TOTP, "JBSWY3DPEHPK3PXP,1,6,30"));
				accounts.add(constructAccount(2, "An example Company with a very long name", "myname@gmail.com", CodeGenerator.Type.TOTP, "JBSWY3DTEHPK3PXP,1,6,30"));
			} catch (CodeGeneratorConstructionException e) {}

			return accounts;
		}

		/**
		 * Load a particular account
		 *
		 * @param id The account ID
		 * @return The account
		 */
		public Account getAccount (int id) {
			// Construct the query
			String[] cols = {_ID, COLUMN_NAME_ISSUER, COLUMN_NAME_USERNAME, COLUMN_NAME_TYPE, COLUMN_NAME_EXTRA};
			Cursor cursor = this.dbHelper.getReadableDatabase().query(
					TABLE_NAME,
					cols,
					_ID + "=?",
					new String[]{Integer.toString(id)},
					null,
					null,
					_ID + " DESC"
			);
			// Run it and process the results
			while(cursor.moveToNext()) {
				String issuer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ISSUER));
				String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_USERNAME));
				CodeGenerator.Type type = CodeGenerator.Type.get(
						cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_TYPE))
				);
				String extra = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXTRA));

				try {
					Account account = constructAccount(id, issuer, username, type, extra);
					cursor.close();
					return account;
				} catch (CodeGeneratorConstructionException e) {
					// todo: log this, alert the user, or something
					continue;
				}
			}
			cursor.close();

			throw new NoResultsException();
		}

		/**
		 * TableHelper methods
		 */

		@Override
		protected void onCreate (SQLiteDatabase db) {
			db.execSQL(SQL_CREATE_TABLE);
		}

	}

	/**
	 * On database created
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		for (TableHelper table : tables) {
			table.onCreate(db);
		}
	}

	/**
	 * On database upgraded
	 */
	@Override
	public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
		for (TableHelper table : tables) {
			table.onUpgrade(db, oldVersion, newVersion);
		}
	}

	/**
	 * On database downgrade
	 */
	@Override
	public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
		for (TableHelper table : tables) {
			table.onDowngrade(db, oldVersion, newVersion);
		}
	}
}
