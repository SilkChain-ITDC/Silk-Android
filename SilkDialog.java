package com.osell.activity.silk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.osell.R;


/**
 * Created by wyj on 2018/4/18.
 */

public  class SilkDialog {
    public static void show(Context context, String str) {
        Dialog dialog = new AlertDialog.Builder(context, R.style.item_dialog).create();
        dialog.setCancelable(true);
        dialog.show();
        dialog.getWindow().setContentView(R.layout.silk_dialog);
        dialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        TextView tv = dialog.getWindow().findViewById(R.id.silk_dialog_t);
        tv.setText(str);


    }

    public static void showAddSilkDialog(Context context, String str) {
        final Dialog dialog = new AlertDialog.Builder(context, R.style.item_dialog).create();
        dialog.setCancelable(true);
        dialog.show();
        dialog.getWindow().setContentView(R.layout.red_pak_dialog_main);
        dialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        TextView tv = dialog.getWindow().findViewById(R.id.reward_integral);
        tv.setText(Html.fromHtml(str));
        dialog.getWindow().findViewById(R.id.dialog_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }
    public static void showChangedSilkDialog(Context context, String str) {
        final Dialog dialog = new AlertDialog.Builder(context, R.style.item_dialog).create();
        dialog.setCancelable(true);
        dialog.show();
        dialog.getWindow().setContentView(R.layout.red_pak_chang_dialog_main);
        dialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        TextView tv = dialog.getWindow().findViewById(R.id.reward_integral);
        tv.setText(Html.fromHtml(str));



        dialog.getWindow().findViewById(R.id.dialog_layout_unopen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.getWindow().findViewById(R.id.dialog_layout_unopen).setVisibility(View.GONE);
                dialog.getWindow().findViewById(R.id.dialog_layout_unopen_text).setVisibility(View.GONE);
                dialog.getWindow().findViewById(R.id.dialog_layout).setVisibility(View.VISIBLE);
                dialog.getWindow().findViewById(R.id.reward_lay).setVisibility(View.VISIBLE);
            }
        });






        dialog.getWindow().findViewById(R.id.dialog_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }
}
