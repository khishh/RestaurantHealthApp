package com.example.cmpt276project.ui.welcome;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cmpt276project.R;

import java.util.Objects;

// https://developer.android.com/guide/topics/ui/dialogs
public class UpdateDialog extends DialogFragment {

    public interface UpdateDialogListener {
        void onUpdateDialogPositiveClick();
        void onUpdateDialogNegativeClick();
    }

    UpdateDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (UpdateDialogListener) context;
        }catch(ClassCastException e){
            throw new ClassCastException("must implement UpdateDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.update_dialog_title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onUpdateDialogPositiveClick();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onUpdateDialogNegativeClick();
                    }
                });
        return builder.create();
    }
}
