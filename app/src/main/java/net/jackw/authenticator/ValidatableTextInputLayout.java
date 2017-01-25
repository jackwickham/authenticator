package net.jackw.authenticator;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.design.widget.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;

import java.util.LinkedList;


public class ValidatableTextInputLayout extends TextInputLayout {
	private boolean required;
	private int minLength;
	private String requiredText;

	private InputState state = InputState.UNINITIALISED;
	private LinkedList<InputValidator> validators = new LinkedList<>();
	private EditText editText;

	public ValidatableTextInputLayout(Context context) {
		this(context, null);
	}
	public ValidatableTextInputLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public ValidatableTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.ValidatableTextInputLayout,
				0, 0
		);

		try {
			required = a.getBoolean(R.styleable.ValidatableTextInputLayout_required, false);
			minLength = a.getInt(R.styleable.ValidatableTextInputLayout_minLength, 0);
			requiredText = a.getString(R.styleable.ValidatableTextInputLayout_requiredText);
			if (requiredText == null) {
				requiredText = getResources().getString(R.string.validate_required);
			}
		} finally {
			a.recycle();
		}
	}

	@Override
	public void addView (View child, int index, ViewGroup.LayoutParams params) {
		super.addView(child, index, params);

		if (child instanceof EditText) {
			this.editText = (EditText) child;
			addEditEventListeners(editText);
		}
	}

	/**
	 * Get the error state for this input
	 */
	public InputState getState () {
		return state;
	}

	/**
	 * Check if the value is valid
	 * @param forceRecheck Should error messages be displayed for required fields that are uninitialised
	 * @return Whether the input is valid
	 */
	public boolean isValid (boolean forceRecheck) {
		if (forceRecheck) {
			process();
		}
		return state == InputState.VALID || ((state == InputState.EMPTY || state == InputState.UNINITIALISED) && !required);
	}
	public boolean isValid () {
		return isValid(true);
	}

	/**
	 * Add a validator to check the input
	 *
	 * @param validator The validator to run on the input
	 */
	public void addValidator (InputValidator validator) {
		validators.add(validator);
	}

	/**
	 * Remove an input validator
	 *
	 * @param validator The validator to remove
	 */
	public void removeValidator (InputValidator validator) {
		validators.remove(validator);
	}

	/**
	 * Check whether the field is valid
	 *
	 * @return Error message, or null if no error
	 */
	@Nullable
	private String validate (String value) {
		if (value.length() < minLength) {
			return String.format(getResources().getString(R.string.validate_too_short), minLength);
		}
		for (InputValidator validator : validators) {
			String error = validator.validate(value);
			if (error != null) {
				return error;
			}
		}
		return null;
	}

	/**
	 * Process the field and add relevant error messages
	 */
	private void process () {
		String value = editText.getText().toString().trim();
		if (value.length() == 0) {
			state = InputState.EMPTY;
			if (required) {
				setError(requiredText);
			} else {
				setErrorEnabled(false);
			}
		} else {
			String error = validate(value);

			if (error == null) {
				// Valid
				state = InputState.VALID;
				setErrorEnabled(false);
			} else {
				// Invalid - error message is contained in error
				state = InputState.INVALID;
				setError(error);
			}
		}
	}

	private void addEditEventListeners (final EditText editText) {
		// When it loses focus, validate it
		editText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					process();
				}
			}
		});
		// If there is already an error, clear it once the value becomes valid
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }

			@Override
			public void afterTextChanged(Editable s) {
				// Only do something if an error is being displayed
				if (getError() != null) {
					switch (state) {
						case EMPTY:
							// Hide the error message - they've input something
							setErrorEnabled(false);
							break;
						case INVALID:
							// Check if it's valid now
							String error = validate(s.toString());
							if (error == null) {
								setErrorEnabled(false);
								state = InputState.VALID;
							} else {
								setError(error);
							}
							break;
					}
				}
			}
		});
	}


	/**
	 * Input state enum
	 */
	public enum InputState {
		UNINITIALISED,
		EMPTY,
		INVALID,
		VALID,
		DISABLED
	}
}
