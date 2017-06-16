package net.jackw.authenticator.ui;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import net.jackw.authenticator.Base32ParseException;
import net.jackw.authenticator.CodeGenerator;
import net.jackw.authenticator.R;
import net.jackw.authenticator.Utils;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountAddListener} interface
 * to handle interaction events.
 * Use the {@link AccountAddManual#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountAddManual extends AccountAddFragment {
	private final String LOG_TAG = "AccountAddManualFragment";

	private AccountAddListener mListener;

	private CodeGenerator.Type selectedType = CodeGenerator.Type.TOTP;

	// Store the form fields
	private TextInputEditText secretInput;
	private TextInputEditText issuerInput;
	private TextInputEditText usernameInput;
	private ValidatableTextInputLayout secretContainer;
	private ValidatableTextInputLayout issuerContainer;
	private ValidatableTextInputLayout usernameContainer;

	private byte[] secret;

	public AccountAddManual() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment Account_add_manual.
	 */
	public static AccountAddManual newInstance() {
		AccountAddManual fragment = new AccountAddManual();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_account_add_manual, container, false);

		// Allow the container to take up as much or as little space as necessary
		container.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		// Set up the type spinner
		Spinner typeSpinner = (Spinner) view.findViewById(R.id.input_type);
		GeneratorType[] generatorTypes = {
				new GeneratorType(CodeGenerator.Type.TOTP, R.string.account_type_totp),
				new GeneratorType(CodeGenerator.Type.HOTP, R.string.account_type_hotp)
		};
		ArrayAdapter<GeneratorType> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, generatorTypes);
		typeSpinner.setAdapter(spinnerAdapter);
		typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selectedType = ((GeneratorType) parent.getItemAtPosition(position)).getType();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				selectedType = null;
			}
		});

		// Init inputs
		secretInput = (TextInputEditText) view.findViewById(R.id.input_secret);
		issuerInput = (TextInputEditText) view.findViewById(R.id.input_issuer);
		usernameInput = (TextInputEditText) view.findViewById(R.id.input_user);
		secretContainer = (ValidatableTextInputLayout) view.findViewById(R.id.input_secret_container);
		issuerContainer = (ValidatableTextInputLayout) view.findViewById(R.id.input_issuer_container);
		usernameContainer = (ValidatableTextInputLayout) view.findViewById(R.id.input_user_container);

		setUpInputListeners();

		// Set up add button
		Button submitButton = (Button) view.findViewById(R.id.submit);
		submitButton.setOnClickListener(new SubmitButtonHandler());

		return view;
	}

	@Override
	protected void attachContext(Context context) {
		if (context instanceof AccountAddListener) {
			mListener = (AccountAddListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public boolean validateForm () {
		// Note: bitwise ampersand to prevent shortcircuiting - we want to run all validations
		return secretContainer.isValid() & issuerContainer.isValid() & usernameContainer.isValid();
	}

	private void setUpInputListeners () {
		// Secret
		secretContainer.addValidator(new InputValidator() {
			@Nullable
			@Override
			public String validate(String value) {
				try {
					secret = Utils.base32Decode(value);
				} catch (Base32ParseException e) {
					return getResources().getString(R.string.account_manual_secret_invalid);
				}
				// Valid
				return null;
			}
		});
	}


	/**
	 * Generator type class, for use in the spinner
	 */
	private class GeneratorType {
		private CodeGenerator.Type type;
		private int resourceId;

		public GeneratorType (CodeGenerator.Type type, int resourceId) {
			this.type = type;
			this.resourceId = resourceId;
		}

		@Override
		public String toString () {
			return getResources().getString(resourceId);
		}

		public CodeGenerator.Type getType () {
			return type;
		}
	}

	/**
	 * Submit button handler
	 */
	private class SubmitButtonHandler implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (!validateForm()) {
				return;
			}
			byte[] secret = Utils.base32Decode(secretInput.getText().toString().trim());
			String issuer = issuerInput.getText().toString();
			String username = usernameInput.getText().toString();
			if (username.equals("")) {
				username = null;
			}

			try {
				switch (selectedType) {
					case TOTP:
						mListener.addTotpAccount(secret, issuer, username, null, null, null);
						break;
					case HOTP:
						mListener.addHotpAccount(secret, issuer, username, null, null, null);
				}
			} catch (IllegalArgumentException e) {
				Log.wtf(LOG_TAG, e);
			}
		}
	}


	@Override
	public AddActivity.AddMethod getType () {
		return AddActivity.AddMethod.Manual;
	}
}
