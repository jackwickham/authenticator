package net.jackw.authenticator;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountAddQr.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountAddQr#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountAddQr extends AccountAddFragment {

	private AccountAddListener mListener;

	public AccountAddQr() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment AccountAddQr.
	 */
	// TODO: Rename and change types and number of parameters
	public static AccountAddQr newInstance() {
		AccountAddQr fragment = new AccountAddQr();
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
		View view = inflater.inflate(R.layout.fragment_account_add_qr, container, false);

		return view;
	}


	@Override
	protected void attachContext (Context context) {
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

	@Override
	public AddActivity.AddMethod getType () {
		return AddActivity.AddMethod.QR;
	}


}
