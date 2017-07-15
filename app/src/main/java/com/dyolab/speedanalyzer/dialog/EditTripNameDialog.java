package com.dyolab.speedanalyzer.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;

import com.dyolab.speedanalyzer.R;

public class EditTripNameDialog extends DialogFragment {

    private EditText mEditText;

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface EditTripNameDialogListener {
        void onFinishEditDialog(String inputText);
    }

    // Use this instance of the interface to deliver action events
    EditTripNameDialogListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.fragment_edit_trip_name_dialog, null))
                .setTitle(R.string.edit_trip_dialog_title)
                // Add action buttons
                .setPositiveButton(R.string.edit_trip_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mEditText = (EditText) ((AlertDialog) dialog).findViewById(R.id.edit_trip_name);
                        String inputText = mEditText.getText().toString();

                        mListener.onFinishEditDialog(inputText);
                    }
                })
                .setNegativeButton(R.string.edit_trip_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditTripNameDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (EditTripNameDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
