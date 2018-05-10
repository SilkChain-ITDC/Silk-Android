package com.osell.activity.silk.mission;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.osell.R;
import com.osell.activity.O2OProfileActivity;
import com.osell.activity.baseactivity.OsellBaseSwipeActivity;
import com.osell.activity.mine.wallet.RechargeActivity;
import com.osell.activity.mine.wallet.WalletOpenProtocolActivity;
import com.osell.activity.mine.wallet.WalletRepetPassActivity;
import com.osell.activity.silk.InviteSilkActivity;
import com.osell.activity.silk.PowerRankingActivity;
import com.osell.activity.silk.SilkDialog;
import com.osell.activity.silk.entity.MissionSilk;
import com.osell.activity.silk.entity.SilkUser;
import com.osell.activity.web.ShowByGetUrlAndNameActivity;
import com.osell.adapter.baserecycler.BaseQuickAdapter;
import com.osell.adapter.baserecycler.BaseViewHolder;
import com.osell.entity.home.ResponseData;
import com.osell.entity.wallet.OMoneyClientInfo;
import com.osell.entity.wallet.OMoneyTokenInfo;
import com.osell.global.OSellCommon;
import com.osell.hall.SamplesCenterActivity;
import com.osell.net.RestAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SilkPowerMissionActivity extends OsellBaseSwipeActivity {
    private String mPowerFormat;
    private String mRankingFormat;
    private TextView mValueView, mRankingView;
    private List<MissionSilk> silkPowers;
    private int mPowerValue;

    @Override
    protected void initData() {
        super.initData();
        mPowerFormat = getString(R.string.silk_power) + ": <font color= '#FFFFFF'>%d</font>";
        mRankingFormat = getString(R.string.ranking_hint);

        silkPowers = new ArrayList<>();
        compositeSubscription = new CompositeSubscription();
        refreshData();

    }

    private void refreshData() {
        compositeSubscription.add(RestAPI.getInstance().oSellService().getSilkActivitys(getLoginInfo().userID, 2, 1, 10)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseData<List<MissionSilk>>>() {
                    @Override
                    public void call(ResponseData<List<MissionSilk>> data) {
                        silkPowers = data.data;
                        adapter.setNewData(silkPowers);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("error", Log.getStackTraceString(throwable));
                    }
                }));
        compositeSubscription.add(RestAPI.getInstance().oSellService().GetSilkUserInfo(getLoginInfo().userID)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseData<SilkUser>>() {
                    @Override
                    public void call(ResponseData<SilkUser> data) {
                        mPowerValue = data.data.totalPower;
                        if (mValueView != null) {
                            mValueView.setText(Html.fromHtml(String.format(mPowerFormat, mPowerValue)));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("error", Log.getStackTraceString(throwable));
                    }
                }));
    }

    @Override
    protected void initView() {
        setNavigationTitle(getString(R.string.silk_power));
        setNavRightBtnVisibility(View.VISIBLE);
        Drawable wrap = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_help));
        DrawableCompat.setTint(wrap, getResources().getColor(R.color.white));
        setNavRightBtnImage(wrap);

        mValueView = getView(R.id.act_mission_silk_power_value);
        mValueView.setText(Html.fromHtml(String.format(mPowerFormat, mPowerValue)));

        mRankingView = getView(R.id.act_mission_silk_power_ranking);
        mRankingView.setText(String.format(mRankingFormat, 156));

        MissionFragment fragment = MissionFragment.newInstance(getString(R.string.how_to_get_silk), true);
        fragment.setAdapter(adapter);
        fragment.setListener(new MissionFragment.OnMissionClickListener() {
            @Override
            public void OnMissionClick(int position) {
                onEvent(position);
            }
        });
        getSupportFragmentManager().beginTransaction()
                .add(R.id.act_mission_silk_power_container, fragment)
                .commit();

    }

    @Override
    protected void onNavRightBtnClick() {
        super.onNavRightBtnClick();
        Intent intent = new Intent(SilkPowerMissionActivity.this, ShowByGetUrlAndNameActivity.class);
        intent.putExtra("url", "http://172.16.110.89:8089/About/SilkIntro");
        startActivity(intent);
    }

    private void onEvent(int position) {
        //任务已完成
        final MissionSilk missionSilk = silkPowers.get(position);
        if (missionSilk.IsComplete == 1 && missionSilk.SilkActivityType != 9) {
            return;
        }
        switch (missionSilk.SilkActivityType) {
            case 1://用户注册 纯展示
                break;
            case 2://钱包付款
                SilkDialog.show(this, getString(R.string.silk_activity_completed_hint));
                break;
            case 4://开箱子
                compositeSubscription.add(
                        RestAPI.getInstance().oSellService().openBox(getLoginInfo().userID)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<ResponseData<Integer>>() {
                                    @Override
                                    public void call(ResponseData<Integer> data) {
                                        if (data.data != 0) {
                                            SilkDialog.showChangedSilkDialog(SilkPowerMissionActivity.this,
                                                    getString(R.string.silk_power) +
                                                            String.format(
                                                                    getString(R.string.word_color),
                                                                    "&#160" + " +" + missionSilk.Value
                                                            )
                                            );
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        Log.e("error", Log.getStackTraceString(throwable));
                                    }
                                })
                );
                break;
            case 5://完善个人资料
                toActivity(O2OProfileActivity.class);
                break;
            case 6://购物
                toActivity(SamplesCenterActivity.class);
                break;
            case 3:
            case 7://钱包充值
                showProgressDialog("");
                center.helper.getOMoneyClientInfoOb(getLoginInfo().userID)
                        .subscribe(new Action1<ResponseData<OMoneyClientInfo>>() {
                            @Override
                            public void call(ResponseData<OMoneyClientInfo> oMoneyClientInfoResponseData) {
                                hideProgressDialog();
                                if (oMoneyClientInfoResponseData.state.code == 0) {
                                    if (oMoneyClientInfoResponseData.data.oMoneyId == null) {
                                        startActivity(new Intent(SilkPowerMissionActivity.this, WalletOpenProtocolActivity.class));
                                    } else {
                                        showProgressDialog("");
                                        center.helper.saveOMoneyClientInfo(oMoneyClientInfoResponseData.data, OSellCommon.getUserId(SilkPowerMissionActivity.this));
                                        center.helper.getOMoneyTokenOb(OSellCommon.getUserId(SilkPowerMissionActivity.this))
                                                .subscribe(new Action1<OMoneyTokenInfo>() {
                                                    @Override
                                                    public void call(OMoneyTokenInfo tokenInfo) {
                                                        hideProgressDialog();
                                                        center.helper.saveOMoneyToken(tokenInfo, OSellCommon.getUserId(SilkPowerMissionActivity.this));
                                                        if (missionSilk.SilkActivityType == 7) {
                                                            toActivity(RechargeActivity.class);
                                                        } else {
                                                            Intent intent = new Intent(SilkPowerMissionActivity.this, WalletRepetPassActivity.class);
                                                            intent.putExtra("type", 1);
                                                            startActivity(intent);
                                                        }
                                                    }
                                                }, new Action1<Throwable>() {
                                                    @Override
                                                    public void call(Throwable throwable) {
                                                        hideProgressDialog();
                                                    }
                                                });
                                    }
                                } else {
                                    showToast(oMoneyClientInfoResponseData.state.message);
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                hideProgressDialog();
                            }
                        });
                break;
            case 8://邀请注册
                toActivity(InviteSilkActivity.class);
                break;
            case 9://分享
                if (havePackage("telegram")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("tg://resolve?domain=silkchain"));
                    startActivity(intent);
                    compositeSubscription.add(
                            RestAPI.getInstance().oSellService().addSilkPowerByActivity(
                                    getLoginInfo().userID,
                                    2, missionSilk.SilkActivityType
                            )
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<ResponseData<Integer>>() {
                                        @Override
                                        public void call(ResponseData<Integer> data) {
                                            if (data.data != 0) {
                                                showToast(data.state.message);
                                            }
                                        }
                                    })
                    );
                } else {
                    showToast(getString(R.string.hint_telegram_not_exist));
                }
                break;
        }
    }

    private boolean havePackage(String packageName) {
        for (PackageInfo packageInfo : getPackageManager().getInstalledPackages(0)) {
            if (packageInfo.packageName.contains(packageName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void initVoid() {
        super.initVoid();
        //region 跳转到详情
        mValueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SilkDetailActivity.jump(SilkPowerMissionActivity.this, 2);
            }
        });
        //endregion
        //region 跳转到排行
        mRankingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SilkPowerMissionActivity.this, PowerRankingActivity.class));
            }
        });
        //endregion
    }

    @Override
    protected int mainLayout() {
        return R.layout.activity_mission_silk_power;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        refreshData();
    }

    private BaseQuickAdapter<MissionSilk> adapter = new BaseQuickAdapter<MissionSilk>(
            R.layout.item_mission_silk_power,
            Collections.<MissionSilk>emptyList()) {

        @Override
        protected void convert(BaseViewHolder helper, final MissionSilk item) {
            TextView hint = helper.getView(R.id.item_mission_silk_power_hint);
            TextView status = helper.getView(R.id.item_mission_silk_power_status);

            Drawable drawable;
            //region 判断Item类型  0 未完成 1：已完成
            switch (helper.getItemViewType()) {
                case 0://未完成
                    hint.setVisibility(View.GONE);
                    drawable = DrawableCompat.wrap(
                            mContext.getResources().getDrawable(R.drawable.round_rect_orange));
                    DrawableCompat.setTint(drawable, Color.parseColor("#F59600"));
                    status.setBackground(drawable);
                    status.setText(String.format("+%d Power", item.Value));
                    status.setTextColor(mContext.getResources().getColor(R.color.white));
                    break;
                case 1://已完成
                    hint.setText(String.format("+%d Power", item.Value));
                    status.setText(getString(R.string.completed));
                    drawable = mContext.getResources()
                            .getDrawable(R.mipmap.icon_completed);
                    drawable.setBounds(0, 0,
                            drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    status.setCompoundDrawables(drawable, null, null, null);
                    break;
            }
            //endregion

            final TextView message = helper.getView(R.id.item_mission_silk_power_message);

            message.setText(item.ActivityName);
            if (!TextUtils.isEmpty(item.ActivityImg)) {
                ImageLoader.getInstance()
                        .displayImage(
                                item.ActivityImg,
                                ((ImageView) helper.getView(R.id.item_mission_silk_power_image))
                        );
            } else {
                helper.setImageResource(R.id.item_mission_silk_power_image, R.mipmap.icon_complete_personal);
            }
        }

        @Override
        protected int getDefItemViewType(int position) {
            MissionSilk missionSilk = mData.get(position);
            if (missionSilk.ActivityName.equals(getString(R.string.register))) {
                return 1;
            }
            if (missionSilk.IsComplete == 0) {
                return 0;
            }
            return 1;
        }
    };
}
