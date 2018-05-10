package com.osell.activity.silk.mission;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.osell.R;
import com.osell.activity.baseactivity.OsellBaseSwipeActivity;
import com.osell.activity.silk.entity.MissionSilk;
import com.osell.activity.silk.entity.SilkUser;
import com.osell.activity.web.ShowByGetUrlActivity;
import com.osell.activity.web.ShowByGetUrlAndNameActivity;
import com.osell.adapter.baserecycler.BaseQuickAdapter;
import com.osell.adapter.baserecycler.BaseViewHolder;
import com.osell.app.OsellCenter;
import com.osell.entity.home.Advertisement2;
import com.osell.entity.home.ResponseData;
import com.osell.global.ImageLoader;
import com.osell.net.RestAPI;
import com.osell.util.ImageOptionsBuilder;
import com.osell.view.AutoViewPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SilkMissionActivity extends OsellBaseSwipeActivity {
//    private List<Pair<String, Integer>> missions = Arrays.asList(
//            new Pair<>("Click Ads", R.mipmap.icon_click_ads),
//            new Pair<>("Get Power", R.mipmap.icon_get_power),
//            new Pair<>("Lottery Draw", R.mipmap.icon_lottery_draw),
//            new Pair<>("Crowd Sale", R.mipmap.icon_crowd_sale),
//            new Pair<>("Group To Share Silk", R.mipmap.icon_group_to_share_silk)
//    );

    private List<Pair<String, Integer>> games;
    List<MissionSilk> mMissionSilks;
    private AutoViewPager mViewPager;

    @Override
    protected void initData() {
        super.initData();
        mMissionSilks = new ArrayList<>();
        compositeSubscription = new CompositeSubscription();

        games = Arrays.asList(
                new Pair<>(getString(R.string.basketball), R.mipmap.icon_basketball),
                new Pair<>(getString(R.string.blackjack_vegas), R.mipmap.icon_blackjack_vegas),
                new Pair<>(getString(R.string.sexy_slots), R.mipmap.icon_sexy_slots)
        );

        //region 获取数据
        //region 任务数据
        compositeSubscription.add(RestAPI.getInstance().oSellService().getSilkActivitys(getLoginInfo().userID, 1, 1, 10)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseData<List<MissionSilk>>>() {
                    @Override
                    public void call(ResponseData<List<MissionSilk>> listResponseData) {
                        mMissionSilks = listResponseData.data;
                        adapter.setNewData(mMissionSilks);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("error", Log.getStackTraceString(throwable));
                    }
                }));
        //endregion
        //region silk 用户数据
        compositeSubscription.add(RestAPI.getInstance().oSellService().GetSilkUserInfo(getLoginInfo().userID)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseData<SilkUser>>() {
                    @Override
                    public void call(ResponseData<SilkUser> data) {
                        ((TextView) getView(R.id.act_mission_silk_value))
                                .setText(String.format(getString(R.string.silk) + ": %.3f", data.data.totalSilk));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("error", Log.getStackTraceString(throwable));
                    }
                }));
        //endregion
        //region 广告数据
        compositeSubscription.add(RestAPI.getInstance().oSellService().getADBinnerByAreaKey(61)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseData<List<Advertisement2>>>() {
                    @Override
                    public void call(ResponseData<List<Advertisement2>> data) {
                        if (mViewPager == null) {
                            mViewPager = getView(R.id.act_mission_silk_ad);
                            mViewPager.setInterval(3000);
                        }
                        mViewPager.setAdapter(new ImageAdapter(data.data));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("error", Log.getStackTraceString(throwable));
                    }
                }));
        //endregion
        //endregion

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //region silk 用户数据
        compositeSubscription.add(RestAPI.getInstance().oSellService().GetSilkUserInfo(getLoginInfo().userID)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseData<SilkUser>>() {
                    @Override
                    public void call(ResponseData<SilkUser> data) {
                        ((TextView) getView(R.id.act_mission_silk_value))
                                .setText(String.format(getString(R.string.silk) + ": %.3f", data.data.totalSilk));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("error", Log.getStackTraceString(throwable));
                    }
                }));
        //endregion
    }

    @Override
    protected void initView() {
        setNavigationTitle(getString(R.string.silk));
        setNavRightBtnVisibility(View.VISIBLE);
        Drawable wrap = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_help));
        DrawableCompat.setTint(wrap, getResources().getColor(R.color.white));
        setNavRightBtnImage(wrap);

        MissionFragment mMisstionFragement = MissionFragment.newInstance(getString(R.string.how_to_get_silk), false);
        MissionFragment mGameFragement = MissionFragment.newInstance(getString(R.string.games), false);

        mGameFragement.setAdapter(new BaseQuickAdapter<Pair<String, Integer>>(R.layout.item_mission_silk, games) {
            @Override
            protected void convert(BaseViewHolder helper, Pair<String, Integer> item) {
                helper.setText(R.id.item_mission_silk_text, item.first)
                        .setImageBitmap(R.id.item_mission_silk_image, ImageLoader.getRoundedCornerBitmap(
                                ((BitmapDrawable) mContext.getResources().getDrawable(item.second)).getBitmap(),
                                25
                        ));
            }
        });
        mGameFragement.setListener(mGameMissionListener);

        mMisstionFragement.setAdapter(adapter);
        mMisstionFragement.setListener(mMisstionListener);


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SilkDetailActivity.jump(SilkMissionActivity.this, 1);
            }
        };

        getView(R.id.act_mission_silk_value).setOnClickListener(onClickListener);
        getView(R.id.act_mission_silk_image).setOnClickListener(onClickListener);
        //兑换
        getView(R.id.act_mission_silk_redeem_channel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(getString(R.string.comming_soon));
            }
        });
        //提现
        getView(R.id.act_mission_silk_withdraw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(getString(R.string.comming_soon));
            }
        });

        getSupportFragmentManager().beginTransaction()
                .add(R.id.act_mission_silk_container_mission, mMisstionFragement)
                .add(R.id.act_mission_silk_container_game, mGameFragement)
                .commit();

    }

    @Override
    protected void onNavRightBtnClick() {
        super.onNavRightBtnClick();
        Intent intent = new Intent(SilkMissionActivity.this, ShowByGetUrlAndNameActivity.class);
        intent.putExtra("url", "http://172.16.110.89:8089/About/SilkIntro");
        startActivity(intent);
    }

    @Override
    protected int mainLayout() {
        return R.layout.activity_mission_silk;
    }

    private MissionFragment.OnMissionClickListener mMisstionListener = new MissionFragment.OnMissionClickListener() {
        @Override
        public void OnMissionClick(int position) {
            Intent intent;
            MissionSilk missionSilk = mMissionSilks.get(position);
            switch (mMissionSilks.get(position).SilkActivityType) {
                case 1:// 用户注册 纯展示
                    break;
                case 10:// 看广告
                    showToast(getString(R.string.comming_soon));
                    break;
                case 11:// SilkPower任务界面
                    startActivity(new Intent(SilkMissionActivity.this, SilkPowerMissionActivity.class));
                    break;
                case 12:// todo 大转盘
                    showToast(getString(R.string.comming_soon));
                    break;
                case 13:// 预售
                    intent = new Intent(SilkMissionActivity.this, ShowByGetUrlAndNameActivity.class);
                    intent.putExtra("url", "https://www.silkchain.io/pc");
                    startActivity(intent);
                    break;
                case 14:// 分享
                    OsellCenter.getInstance().helper
                            .shareBySelf(missionSilk.ActivityName, missionSilk.UrlAddress, SilkMissionActivity.this);
                    break;
                default:
                    if (!TextUtils.isEmpty(missionSilk.UrlAddress)) {
                        intent = new Intent(SilkMissionActivity.this, ShowByGetUrlAndNameActivity.class);
                        intent.putExtra("url", missionSilk.UrlAddress);
                        startActivity(intent);
                    }
                    break;
            }

        }
    };
    //游戏
    private MissionFragment.OnMissionClickListener mGameMissionListener = new MissionFragment.OnMissionClickListener() {
        @Override
        public void OnMissionClick(int position) {
            Intent intent;
            switch (position) {
                case 0:
                    intent = new Intent(SilkMissionActivity.this, SilkWebActivity.class);
                    intent.putExtra("url", "http://172.16.110.89:8089/PlayGame/PlayBasketballStart?userID=140353&lan=en");
                    startActivity(intent);
                    break;
                case 1:
                    intent = new Intent(SilkMissionActivity.this, SilkWebActivity.class);
                    intent.putExtra("url", "http://172.16.110.89:8089/PlayGame/BlackjackIndex?userid="+getLoginInfo().userID);
                    intent.putExtra("isLandscape", true);
                    startActivity(intent);
                    break;
                case 2:
                    break;
            }

        }
    };
    private static BaseQuickAdapter<MissionSilk> adapter = new BaseQuickAdapter<MissionSilk>(
            R.layout.item_mission_silk, Collections.<MissionSilk>emptyList()) {
        @Override
        protected void convert(BaseViewHolder helper, MissionSilk item) {
            helper.setText(R.id.item_mission_silk_text, item.ActivityName);
            com.nostra13.universalimageloader.core.ImageLoader.getInstance()
                    .displayImage(
                            TextUtils.isEmpty(item.ActivityImg) ? "" : item.ActivityImg,
                            (ImageView) helper.getView(R.id.item_mission_silk_image)
                    );
        }
    };


    private static class ImageAdapter extends android.support.v4.view.PagerAdapter {

        private List<View> mRecycledViews = new ArrayList<>();

        private List<Advertisement2> data;

        private ImageAdapter(List<Advertisement2> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            mRecycledViews.add((View) object);
        }

        public void setData(List<Advertisement2> data) {
            this.data.clear();
            this.data.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            ImageView view;
            if (mRecycledViews.isEmpty()) {
                view = new ImageView(container.getContext());
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                view = (ImageView) mRecycledViews.get(0);
                mRecycledViews.remove(0);
            }
            com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(data.get(position).image, view, ImageOptionsBuilder.getInstance().getNomalScaleOptions());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(container.getContext(), ShowByGetUrlActivity.class);
                    intent.putExtra("url", data.get(position).url);
                    container.getContext().startActivity(intent);
                }
            });
            container.addView(view);
            return view;
        }
    }
}
