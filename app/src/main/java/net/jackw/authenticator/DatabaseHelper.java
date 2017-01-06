package net.jackw.authenticator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.*;

public class DatabaseHelper extends SQLiteOpenHelper  {
	private static final String LOG_TAG = "Database";

	private Context context;
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
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		tables = new TableHelper[1];
		tables[0] = accountsDb = new AccountsDb(this);

		this.context = context;
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
		} else {
			instance.context = context;
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
		public static final String COLUMN_NAME_IMAGE = "image";
		public static final String COLUMN_NAME_POS = "position";

		private static final String SQL_CREATE_TABLE = String.format(
				"CREATE TABLE IF NOT EXISTS %s" +
				"(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s INTEGER, %s TEXT, %s INTEGER DEFAULT 0, %s INTEGER)",
			TABLE_NAME, _ID, COLUMN_NAME_ISSUER, COLUMN_NAME_USERNAME, COLUMN_NAME_TYPE,
			COLUMN_NAME_EXTRA, COLUMN_NAME_IMAGE, COLUMN_NAME_POS
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
		private Account constructAccount (int id, String issuer, String username, CodeGenerator.Type type, String extra, Integer pos) throws CodeGeneratorConstructionException {
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
			Account account = new Account(gen, username, issuer, id, pos);

			return account;
		}
		/**
		 * Load all accounts from the DB
		 *
		 * @return List of accounts from the DB
		 */
		public List<Account> getAllAccounts () {
			// Construct the query
			String[] cols = {_ID, COLUMN_NAME_ISSUER, COLUMN_NAME_USERNAME, COLUMN_NAME_TYPE, COLUMN_NAME_EXTRA, COLUMN_NAME_IMAGE, COLUMN_NAME_POS};
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
				boolean image = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_IMAGE)) == 1;
				int pos = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_POS));

				try {
					Account account = constructAccount(id, issuer, username, type, extra, pos);
					account.hasImage(image, dbHelper.context);

					accounts.add(account);
				} catch (CodeGeneratorConstructionException e) {
					// todo: log this, alert the user, or something
					Log.w(DatabaseHelper.LOG_TAG, e);

					continue;
				}
			}
			cursor.close();

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
			String[] cols = {_ID, COLUMN_NAME_ISSUER, COLUMN_NAME_USERNAME, COLUMN_NAME_TYPE, COLUMN_NAME_EXTRA, COLUMN_NAME_IMAGE, COLUMN_NAME_POS};
			Cursor cursor = this.dbHelper.getReadableDatabase().query(
					TABLE_NAME,
					cols,
					_ID + "=?",
					new String[]{Integer.toString(id)},
					null,
					null,
					COLUMN_NAME_POS + " DESC"
			);
			// Run it and process the results
			while(cursor.moveToNext()) {
				String issuer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_ISSUER));
				String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_USERNAME));
				CodeGenerator.Type type = CodeGenerator.Type.get(
						cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_TYPE))
				);
				String extra = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_EXTRA));
				boolean image = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_IMAGE)) == 1;
				int pos = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NAME_POS));

				try {
					Account account = constructAccount(id, issuer, username, type, extra, pos);
					account.hasImage(image, dbHelper.context);

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
		 * Save an account to the DB
		 */
		public void saveAccount (Account account) {
			Integer pos = account.getPos();
			if (pos == null) {
				Cursor cursor = this.dbHelper.getReadableDatabase().query(
						TABLE_NAME,
						new String[]{String.format("MAX(%s) AS maxPos", COLUMN_NAME_POS)},
						null,
						null,
						null,
						null,
						null
				);
				try {
					cursor.moveToFirst();
					pos = cursor.getInt(cursor.getColumnIndexOrThrow("maxPos")) + 1;
					account.setPos(pos);
				} finally {
					cursor.close();
				}
			}

			if (account.getId() == 0) {
				// Doesn't currently exist in the DB
				// Create the values to save
				ContentValues values = new ContentValues(5);
				values.put(COLUMN_NAME_ISSUER, account.getIssuer());
				values.put(COLUMN_NAME_USERNAME, account.getUsername());
				values.put(COLUMN_NAME_TYPE, account.getCodeGenerator().getType().value);
				values.put(COLUMN_NAME_EXTRA, account.getCodeGenerator().getExtra());
				values.put(COLUMN_NAME_IMAGE, account.hasImage());
				values.put(COLUMN_NAME_POS, pos);

				// Run the query
				int id = (int) dbHelper.getWritableDatabase().insertOrThrow(TABLE_NAME, null, values);

				account.setId(id);
			} else {
				// Create the values to save
				ContentValues values = new ContentValues(5);
				values.put(COLUMN_NAME_ISSUER, account.getIssuer());
				values.put(COLUMN_NAME_USERNAME, account.getUsername());
				values.put(COLUMN_NAME_TYPE, account.getCodeGenerator().getType().value);
				values.put(COLUMN_NAME_EXTRA, account.getCodeGenerator().getExtra());
				values.put(COLUMN_NAME_IMAGE, account.hasImage());
				values.put(COLUMN_NAME_POS, pos);

				// Run the query
				int changed = dbHelper.getWritableDatabase().update(
						TABLE_NAME, values, _ID + "=?",
						new String[]{ Integer.toString(account.getId()) }
				);

				if (BuildConfig.DEBUG && changed != 1) {
					throw new AssertionError("Incorrect number of rows changed: " + Integer.toString(changed));
				}
			}
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
