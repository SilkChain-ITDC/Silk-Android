package com.osell.activity.silk;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.transition.TransitionManager;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.osell.R;
import com.osell.activity.baseactivity.OsellBaseSwipeActivity;
import com.osell.activity.silk.entity.SilkProduceRecord;
import com.osell.activity.silk.entity.SilkUser;
import com.osell.activity.silk.mission.SilkDetailActivity;
import com.osell.activity.silk.mission.SilkMissionActivity;
import com.osell.activity.silk.mission.SilkPowerMissionActivity;
import com.osell.activity.web.ShowByGetUrlAndNameActivity;
import com.osell.entity.home.ResponseData;
import com.osell.global.OSellCommon;
import com.osell.net.OSellInfo;
import com.osell.net.RestAPI;
import com.osell.util.Spans;
import com.osell.view.SilkProduceView;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author LCJIAN
 */
public class SilkActivity extends OsellBaseSwipeActivity implements View.OnClickListener {

    private TextSwitcher ts_notice;
    private LinearLayout ll_total_silk_info;
    private TickerView tv_total_silk_amount;
    private TextView tv_total_silk_power;
    private SilkProduceView spv_silk;
    private LinearLayout ll_latest_record;
    private TextView tv_more_records;
    private TextView tv_go_to_increase_power;
    private TextView tv_go_to_redeem;
    private TextView tv_in_production;

    private SoundPool mSoundPool;
    private int mSoundID;
    private double mTotalSilkAmount;

    private DecimalFormat mDecimalFormat = new DecimalFormat("0.000");

    @Override
    protected int mainLayout() {
        return R.layout.activity_silk;
    }

    @Override
    protected void initView() {
        ts_notice = findViewById(R.id.ts_notice);
        ll_total_silk_info = findViewById(R.id.ll_total_silk_info);
        tv_total_silk_amount = findViewById(R.id.tv_total_silk_amount);
        tv_total_silk_power = findViewById(R.id.tv_total_silk_power);
        spv_silk = findViewById(R.id.spv_silk);
        ll_latest_record = findViewById(R.id.ll_latest_record);
        tv_more_records = findViewById(R.id.tv_more_records);
        tv_go_to_increase_power = findViewById(R.id.tv_go_to_increase_power);
        tv_go_to_redeem = findViewById(R.id.tv_go_to_redeem);
        tv_in_production = findViewById(R.id.tv_in_production);
    }

    @Override
    protected void initVoid() {
        mSoundPool = new SoundPool(1,// 同时播放的音效
                AudioManager.STREAM_MUSIC, 0);
        mSoundID = mSoundPool.load(this, R.raw.silk_recieve_coin, 1);

        setNavigationTitle(R.string.silk);
        setNavRightBtnVisibility(View.VISIBLE);
        setNavRightBtnImageRes(R.drawable.new_help);
        tv_total_silk_amount.setCharacterLists(TickerUtils.provideNumberList());
        tv_total_silk_amount.setAnimationDuration(1500);
        tv_total_silk_amount.setOnClickListener(this);
        tv_total_silk_power.setOnClickListener(this);
        tv_more_records.setOnClickListener(this);
        tv_go_to_increase_power.setOnClickListener(this);
        tv_go_to_redeem.setOnClickListener(this);

        getSilkUserInfo();
        setupContentReceived();
        setupContentUnReceived();

        animateTheInProduction();
        animateTheNotice();
    }

    private void animateTheNotice() {
        compositeSubscription.add(Observable.interval(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        String[] ar = new String[]{getString(R.string.silk_notice_msg_1),
                                getString(R.string.silk_notice_msg_3),
                                getString(R.string.silk_notice_msg_3)};
                        ts_notice.setText(ar[aLong.intValue() % 3]);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                }));
    }

    private void animateTheInProduction() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(tv_in_production, View.TRANSLATION_Y, 0, -40);
        animator.setDuration(3000).setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setStartDelay(1000);
        animator.start();
        tv_in_production.setTag(animator);
    }

    private void animateTheSilk() {
        spv_silk.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < spv_silk.getChildCount(); i++) {
                    View view = spv_silk.getChildAt(i);
                    if (((SilkProduceView.LayoutParams) view.getLayoutParams()).layoutMode == SilkProduceView.LayoutParams.RANDOM) {
                        ((AnimatorSet) view.getTag(R.id.spv_silk)).start();
                    }
                }
            }
        }, 1000);
    }

    private void getSilkUserInfo() {
        compositeSubscription.add(RestAPI.getInstance().oSellService()
                .GetSilkUserInfo(OSellCommon.getUserId(this))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseData<SilkUser>>() {
                    @Override
                    public void call(ResponseData<SilkUser> silkUserResponseData) {
                        mTotalSilkAmount = silkUserResponseData.data.totalSilk;
                        tv_total_silk_amount.setText(getString(R.string.silk_amount, mDecimalFormat.format(mTotalSilkAmount)));
                        tv_total_silk_power.setText(getString(R.string.present_power_value, String.valueOf(silkUserResponseData.data.totalPower)));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                }));
    }

    private void setupContentReceived() {
        compositeSubscription.add(RestAPI.getInstance().oSellService()
                .getSilkProduceRecords(OSellCommon.getUserId(this), 5, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseData<List<SilkProduceRecord>>>() {
                    @Override
                    public void call(ResponseData<List<SilkProduceRecord>> listResponseData) {
                        if (listResponseData.state.code == 0) {
                            if (listResponseData.data != null && !listResponseData.data.isEmpty()) {
                                TransitionManager.beginDelayedTransition(ll_latest_record);

                                ll_latest_record.removeAllViews();
                                LayoutInflater layoutInflater = LayoutInflater.from(ll_latest_record.getContext());
                                for (SilkProduceRecord item : listResponseData.data) {
                                    View view = layoutInflater.inflate(R.layout.silk_produce_record_item, ll_latest_record, false);
                                    TextView tv_amount = view.findViewById(R.id.tv_amount);
                                    TextView tv_time = view.findViewById(R.id.tv_time);
                                    tv_amount.setText(new Spans()
                                            .append(mDecimalFormat.format(item.amount), new ForegroundColorSpan(0xfffe6400))
                                            .append(getString(R.string.silk)));
                                    tv_time.setText(item.showReceiveTime);
                                    ll_latest_record.addView(view);
                                }
                            }
                        } else {
                            showToast(listResponseData.state.message);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                }));
    }

    private void setupContentUnReceived() {
        showProgressDialog("");
        compositeSubscription.add(RestAPI.getInstance().oSellService()
                .getSilkProduceRecords(OSellCommon.getUserId(this), 5, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseData<List<SilkProduceRecord>>>() {
                    @Override
                    public void call(ResponseData<List<SilkProduceRecord>> listResponseData) {
                        hideProgressDialog();
                        if (listResponseData.state.code == 0) {
                            if (listResponseData.data != null && !listResponseData.data.isEmpty()) {
                                int i = 0;
                                for (SilkProduceRecord item : listResponseData.data) {
                                    if (i >= 5) {
                                        break;
                                    }
                                    spv_silk.addView(buildSilk(item),
                                            new SilkProduceView.LayoutParams(
                                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                                    Gravity.START | Gravity.TOP,
                                                    SilkProduceView.LayoutParams.RANDOM));
                                    i++;
                                }
                                animateTheSilk();
                            }
                        } else {
                            showToast(listResponseData.state.message);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgressDialog();
                    }
                }));
    }

    private void receiveSilkProduceRecord(SilkProduceRecord record) {
        compositeSubscription.add(RestAPI.getInstance().oSellService()
                .receiveSilkProduceRecord(record.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseData<Object>>() {
                    @Override
                    public void call(ResponseData<Object> objectResponseData) {
                        setupContentReceived();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                }));
    }

    private TextView buildSilk(SilkProduceRecord data) {
        TextView textView = new TextView(this);
        textView.setGravity(Gravity.CENTER);
        textView.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_silk_produced, 0, 0);
        textView.setTextColor(0xffffffff);
        textView.setTextSize(10);
        textView.setText(mDecimalFormat.format(data.amount));

        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(textView, View.ALPHA, 1f, 0.2f);
        animatorAlpha.setRepeatMode(ValueAnimator.REVERSE);
        animatorAlpha.setRepeatCount(ValueAnimator.INFINITE);
        ObjectAnimator animatorScaleX = ObjectAnimator.ofFloat(textView, View.SCALE_X, 1f, 0.9f);
        animatorScaleX.setRepeatMode(ValueAnimator.REVERSE);
        animatorScaleX.setRepeatCount(ValueAnimator.INFINITE);
        ObjectAnimator animatorScaleY = ObjectAnimator.ofFloat(textView, View.SCALE_Y, 1f, 0.9f);
        animatorScaleY.setRepeatMode(ValueAnimator.REVERSE);
        animatorScaleY.setRepeatCount(ValueAnimator.INFINITE);
        AnimatorSet animatorSet = new AnimatorSet().setDuration(1000);
        animatorSet.setStartDelay(new Random().nextInt(5000));
        animatorSet.playTogether(animatorAlpha, animatorScaleX, animatorScaleY);

        textView.setTag(data);
        textView.setTag(R.id.spv_silk, animatorSet);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ((AnimatorSet) v.getTag(R.id.spv_silk)).cancel();
                v.animate()
                        .x(ll_total_silk_info.getX())
                        .y(ll_total_silk_info.getY())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                v.setVisibility(View.GONE);
                                spv_silk.removeView(v);

                                mTotalSilkAmount += ((SilkProduceRecord) v.getTag()).amount;
                                tv_total_silk_amount.setText(getString(R.string.silk_amount, mDecimalFormat.format(mTotalSilkAmount)));
                            }
                        })
                        .start();
                mSoundPool.play(mSoundID, 0.7f, 0.7f, 0, 0, 1);

                receiveSilkProduceRecord((SilkProduceRecord) v.getTag());
            }
        });
        return textView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_more_records:
                SilkDetailActivity.jump(this, 1);
                break;
            case R.id.tv_total_silk_amount:
                startActivity(new Intent(this, SilkMissionActivity.class));
                break;
            case R.id.tv_total_silk_power:
            case R.id.tv_go_to_increase_power:
                startActivity(new Intent(this, SilkPowerMissionActivity.class));
                break;
            case R.id.tv_go_to_redeem:
//                startActivity(new Intent(this, CoinProductlActivity.class));
                showToast(R.string.come_soon);
                break;
            default:
                break;
        }
    }

     protected void onNavRightBtnClick() {
        startActivity(new Intent(this, ShowByGetUrlAndNameActivity.class).putExtra("url", OSellInfo.SERVER_PREFIX + "About/SilkIntro"));
    }

    @Override
    protected void onDestroy() {
        if (tv_in_production.getTag() != null) {
            ((ObjectAnimator) tv_in_production.getTag()).cancel();
        }
        for (int i = 0; i < spv_silk.getChildCount(); i++) {
            View view = spv_silk.getChildAt(i);
            if (((SilkProduceView.LayoutParams) view.getLayoutParams()).layoutMode == SilkProduceView.LayoutParams.RANDOM) {
                if (view.getTag(R.id.spv_silk) != null) {
                    ((AnimatorSet) view.getTag(R.id.spv_silk)).cancel();
                }
            }
        }
        mSoundPool.release();
        super.onDestroy();
    }
}
