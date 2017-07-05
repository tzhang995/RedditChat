package tzcorp.snoochat.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import tzcorp.snoochat.R;

/**
 * Created by tony on 15/06/17.
 */

public class BasicDialogFragment extends DialogFragment {
    public static BasicDialogFragment newInstance(int title) {
        BasicDialogFragment frag = new BasicDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_info_outline_black_24dp)
                .setTitle(title)
                .setPositiveButton(R.string.dialog_ok_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                )
                .create();

    }
}
