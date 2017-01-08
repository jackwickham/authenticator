package net.jackw.authenticator;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountAddListener} interface
 * to handle interaction events.
 * Use the {@link AccountAddManual#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountAddManual extends Fragment {
	private final String LOG_TAG = "AccountAddManualFragment";

	private AccountAddListener mListener;

	private CodeGenerator.Type selectedType = CodeGenerator.Type.TOTP;

	// Store the form fields
	private TextInputEditText secretInput;
	private TextInputEditText issuerInput;
	private TextInputEditText usernameInput;
	private TextInputLayout secretContainer;
	private TextInputLayout issuerContainer;
	private TextInputLayout usernameContainer;
	// And their validity
	private InputState secretState = InputState.UNINITIALISED;
	private InputState issuerState = InputState.UNINITIALISED;
	private InputState usernameState = InputState.VALID; // optional so valid by default

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
		secretContainer = (TextInputLayout) view.findViewById(R.id.input_secret_container);
		issuerContainer = (TextInputLayout) view.findViewById(R.id.input_issuer_container);
		usernameContainer = (TextInputLayout) view.findViewById(R.id.input_user_container);

		setUpInputListeners();

		// Set up add button
		Button submitButton = (Button) view.findViewById(R.id.submit);
		submitButton.setOnClickListener(new SubmitButtonHandler());

		return view;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
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
		boolean valid = true;
		if (secretState != InputState.VALID) {
			valid = false;
			if (secretState == InputState.UNINITIALISED) {
				secretContainer.setError(getResources().getString(R.string.account_manual_secret_required));
			}
		}
		if (issuerState != InputState.VALID) {
			valid = false;
			if (issuerState == InputState.UNINITIALISED) {
				issuerContainer.setError(getResources().getString(R.string.account_manual_issuer_required));
			}
		}
		if (usernameState != InputState.VALID) {
			valid = false;
		}
		return valid;
	}

	private void setUpInputListeners () {
		// Secret is required and must be valid
		secretInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					if (secretInput.getText().toString().trim().length() == 0) {
						secretState = InputState.EMPTY;
						secretContainer.setError(getResources().getString(R.string.account_manual_secret_required));
					} else if (getSecret(true) == null) {
						secretState = InputState.INVALID;
						secretContainer.setError(getResources().getString(R.string.account_manual_secret_invalid));
					} else {
						secretState = InputState.VALID;
						secretContainer.setErrorEnabled(false);
					}
				}
			}
		});
		secretInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }

			@Override
			public void afterTextChanged(Editable s) {
				// Hide the error message
				secretContainer.setErrorEnabled(false);
			}
		});

		// issuer is required
		issuerInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					if (issuerInput.getText().toString().trim().length() == 0) {
						issuerState = InputState.EMPTY;
						issuerContainer.setError(getResources().getString(R.string.account_manual_issuer_required));
					} else {
						issuerState = InputState.VALID;
						issuerContainer.setErrorEnabled(false);
					}
				}
			}
		});
		issuerInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }

			@Override
			public void afterTextChanged(Editable s) {
				// Hide the error message
				issuerContainer.setErrorEnabled(false);
			}
		});
	}

	@Nullable
	private byte[] getSecret () {
		return getSecret(false);
	}
	@Nullable
	private byte[] getSecret (boolean forceUpdate) {
		if (forceUpdate) {
			try {
				secret = Utils.base32Decode(secretInput.getText().toString().trim());
			} catch (Base32ParseException e) {
				secret = null;
			}
		}
		return secret;
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
	 * Input state enum
	 */
	private enum InputState {
		UNINITIALISED,
		EMPTY,
		INVALID,
		VALID,
		DISABLED
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
			byte[] secret = getSecret();
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
}
