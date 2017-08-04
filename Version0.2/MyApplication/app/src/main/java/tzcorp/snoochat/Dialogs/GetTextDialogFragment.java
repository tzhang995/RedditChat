package tzcorp.snoochat.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;

import tzcorp.snoochat.Activities.ChatActivity;
import tzcorp.snoochat.R;

/**
 * Created by tony on 05/07/17.
 */

public class GetTextDialogFragment extends DialogFragment {
    public Context mContext;
    public static GetTextDialogFragment newInstance(int title, Context context) {
        GetTextDialogFragment frag = new GetTextDialogFragment();
        frag.mContext = context;
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        final EditText input = new EditText(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setPositiveButton(R.string.dialog_ok_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(mContext instanceof ChatActivity) {
                                    ((ChatActivity) mContext).addChannels(input.getText().toString());
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.dialog_cancel_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                .setView(input)
                .create();

    }
}
