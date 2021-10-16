package com.edutalk.app.utils;

import android.app.Dialog;

import java.util.concurrent.LinkedBlockingQueue;

public class DialogQueue {
    private static LinkedBlockingQueue<Dialog> dialogsToShow = new LinkedBlockingQueue<>();
    public static void showDialog(final Dialog dialog) {
        if(dialogsToShow.isEmpty()) {
            dialog.show();
        }
        dialogsToShow.offer(dialog);
        dialog.setOnDismissListener((d) -> {
            dialogsToShow.remove(dialog);
            if(!dialogsToShow.isEmpty()) {
                dialogsToShow.peek().show();
            }
        });
    }
}
