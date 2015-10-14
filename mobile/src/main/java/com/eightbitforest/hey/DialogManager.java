package com.eightbitforest.hey;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;

public class DialogManager {
    private static DialogManager i = new DialogManager();
    private ProgressDialog indeterminateProgressDialog;
    private ProgressDialog determinateProgressDialog;

    public static DialogManager getInstance() {
        return i;
    }

    public boolean isProgressOpen() {
        return (indeterminateProgressDialog != null);
    }

    public void showProgress(Context context, String title, String message) {
        indeterminateProgressDialog = ProgressDialog.show(context, title, message);
    }

    public void closeProgress() {
        if (indeterminateProgressDialog != null)
            indeterminateProgressDialog.dismiss();
        if (determinateProgressDialog != null)
            determinateProgressDialog.dismiss();

        indeterminateProgressDialog = null;
        determinateProgressDialog = null;
    }

    public void showProgress(Context context, String title, String message, int max) {
        determinateProgressDialog = new ProgressDialog(new ContextThemeWrapper(context, R.style.AppTheme));
        determinateProgressDialog.setMax(max);
        determinateProgressDialog.setTitle(title);
        determinateProgressDialog.setMessage(message);
        determinateProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        determinateProgressDialog.setProgressPercentFormat(null);
        determinateProgressDialog.setProgressNumberFormat(null);
        determinateProgressDialog.setCancelable(false);
        determinateProgressDialog.show();
    }

    public void updateProgress(int amount) {
        determinateProgressDialog.incrementProgressBy(amount);
    }

    public void showError(Context context, String message) {
        showAlert(context, context.getString(R.string.error_title), message);
    }

    public void showError(Context context, String message, DialogInterface.OnClickListener clickListener) {
        showAlert(context, context.getString(R.string.error_title), message, clickListener);
    }

    public void showAlert(Context context, String title, String message) {
        showAlert(context, title, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

    }

    public void showAlert(Context context, String title, String message, DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(context.getText(R.string.button_positive), clickListener);
        builder.setCancelable(false);
        builder.create().show();
    }
}
