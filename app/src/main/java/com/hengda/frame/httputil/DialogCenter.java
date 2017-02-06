package com.hengda.frame.httputil;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.hengda.zwf.hddialog.DialogClickListener;
import com.hengda.zwf.hddialog.HDialogBuilder;

/**
 * 作者：Tailyou （祝文飞）
 * 时间：2016/5/26 19:03
 * 邮箱：tailyou@163.com
 * 描述：Dialog中心
 */
public class DialogCenter {

    private static HDialogBuilder hDialogBuilder;

    /**
     * 显示Dialog-CustomView
     *
     * @param context
     * @param view
     * @param dialogClickListener
     * @param txt
     */
    public static void showDialog(Context context,
                                  View view,
                                  DialogClickListener dialogClickListener,
                                  String... txt) {
        hideDialog();
        hDialogBuilder = new HDialogBuilder(context);
        hDialogBuilder
                .withIcon(R.mipmap.ic_launcher)
                .withTitle(txt[0])
                .dlgColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setCustomView(view)
                .pBtnText(txt[1])
                .pBtnClickListener(v -> dialogClickListener.p())
                .cancelable(false);
        if (txt.length == 3) {
            hDialogBuilder
                    .nBtnText(txt[2])
                    .nBtnClickListener(v -> dialogClickListener.n());
        }
        hDialogBuilder.show();
    }

    /**
     * 显示Dialog-Message
     *
     * @param context
     * @param dialogClickListener
     * @param txt
     */
    public static void showDialog(Context context,
                                  DialogClickListener dialogClickListener,
                                  String... txt) {
        hideDialog();
        hDialogBuilder = new HDialogBuilder(context);
        hDialogBuilder
                .withIcon(R.mipmap.ic_launcher)
                .dlgColor(ContextCompat.getColor(context, R.color.colorAccent))
                .withTitle(txt[0])
                .withMsg(txt[1])
                .pBtnText(txt[2])
                .pBtnClickListener(v -> dialogClickListener.p())
                .cancelable(false);
        if (txt.length == 4) {
            hDialogBuilder
                    .nBtnText(txt[3])
                    .nBtnClickListener(v -> dialogClickListener.n());
        }
        hDialogBuilder.show();
    }

    /**
     * 隐藏Dialog
     */
    public static void hideDialog() {
        if (hDialogBuilder != null && hDialogBuilder.isShowing()) {
            hDialogBuilder.dismiss();
            hDialogBuilder = null;
        }
    }

}
