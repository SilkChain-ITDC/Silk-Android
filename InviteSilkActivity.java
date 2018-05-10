package com.osell.activity.silk;

import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.osell.R;
import com.osell.activity.baseactivity.OsellBaseActivity;
import com.osell.activity.silk.entity.SilkUser;
import com.osell.adapter.baserecycler.BaseQuickAdapter;
import com.osell.adapter.baserecycler.BaseViewHolder;
import com.osell.entity.home.ResponseData;
import com.osell.net.RestAPI;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wyj on 2018/4/8.
 */

public class InviteSilkActivity extends OsellBaseActivity {

    SilkUser silkUser;


    /**
     * 二、初始化主layout
     */
    @Override
    protected int mainLayout() {
        return R.layout.invite_silk;
    }

    /**
     * 三、初始化视图view
     */
    @Override
    protected void initView() {
         setNavigationTitle(getString(R.string.invite_to_register));
    }

    @Override
    protected void initVoid() {

        super.initVoid();
        getData();
    }


    private void getData() {
        RestAPI.getInstance().oSellService().GetSilkUserInfo(getLoginInfo().userID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseData<SilkUser>>() {
                               @Override
                               public void call(ResponseData<SilkUser> silkUserResponseData) {
                                   silkUser = silkUserResponseData.data;
                                   setViewData();
                               }
                           }

                        , new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {

                            }
                        })
        ;
    }


    public void setViewData() {
        TextView tv = getView(R.id.invit_code);
        tv.setText(silkUser.inviteCode);
        int lastCount = silkUser.inviteNum - silkUser.areadyInviteNum;


        TextView tvN = getView(R.id.invit_note);
        tvN.setText(String.format(getString(R.string.you_ve_invited), silkUser.areadyInviteNum, lastCount));

        TextView tvN2 = getView(R.id.invit_note_2);
        tvN2.setText(Html.fromHtml( String.format(getString(R.string.total_x_friends), silkUser.areadyInviteNum, silkUser.totalPower)));

        getView(R.id.invit_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(silkUser.inviteCode);
                showToast(R.string.copy_success);
            }
        });


        RecyclerView rec = getView(R.id.invit_rec);
        StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        rec.setLayoutManager(mLayoutManager);

        InviteAdapter adapter = new InviteAdapter(silkUser.friends, mActivity);
        rec.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        int lines = Math.min(silkUser.friends.size(), 4);
        int height = dipToPx(mActivity, 46 * lines);
        ViewGroup.LayoutParams params = rec.getLayoutParams();
        params.height = height;
        rec.setLayoutParams(params);

    }

    public class InviteAdapter extends BaseQuickAdapter<SilkUser.Friends> {


        private Context context;


        public InviteAdapter(List<SilkUser.Friends> data, Context context) {
            super(R.layout.invited_item, data);
            this.context = context;
        }

        @Override
        protected void convert(final BaseViewHolder helper, final SilkUser.Friends data) {
            final int pos = helper.getLayoutPosition() - getHeaderViewsCount();
            helper.setText(R.id.invite_item_t_1, data.nickName)
                    .setText(R.id.invite_item_t_2, data.inviteTime)
                    .setText(R.id.invite_item_t_3, "+" + data.powerNum);

        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}