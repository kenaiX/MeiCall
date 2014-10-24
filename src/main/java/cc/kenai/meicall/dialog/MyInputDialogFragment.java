package cc.kenai.meicall.dialog;

import android.app.DialogFragment;
import android.os.Bundle;

public class MyInputDialogFragment extends DialogFragment {
	public static MyInputDialogFragment newInstance(int title) {

		MyInputDialogFragment frag = new MyInputDialogFragment();

		Bundle args = new Bundle();

		args.putInt("title", title);

		frag.setArguments(args);

		return frag;

	}
}
