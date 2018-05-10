package com.osell.activity.silk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;

import com.osell.R;
import com.osell.activity.baseactivity.OsellBaseSwipeActivity;
import com.osell.activity.silk.entity.SilkUser;
import com.osell.adapter.baserecycler.BaseQuickAdapter;
import com.osell.adapter.baserecycler.BaseViewHolder;
import com.osell.entity.home.ResponseData;
import com.osell.net.RestAPI;
import com.osell.util.ScreenUtils;
import com.osell.util.Spans;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author LCJIAN
 */
public class PowerRankingActivity extends OsellBaseSwipeActivity {

    private RecyclerView rv_power_ranking;

    private List<SilkUser> mData;
    private BaseQuickAdapter<SilkUser> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNavigationTitle(R.string.power_ranking);
    }

    @Override
    protected int mainLayout() {
        return R.layout.activity_power_ranking;
    }

    @Override
    protected void initView() {
        rv_power_ranking = findViewById(R.id.rv_power_ranking);
    }

    @Override
    protected void initVoid() {
        rv_power_ranking.setHasFixedSize(true);
        rv_power_ranking.setLayoutManager(new LinearLayoutManager(this));
        rv_power_ranking.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 1);
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                int recyclerViewTop = parent.getPaddingTop();
                int recyclerViewBottom = parent.getHeight() - parent.getPaddingBottom();
                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = parent.getChildAt(i);
                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                    int left = child.getLeft() - params.leftMargin;
                    int right = child.getRight() + params.rightMargin;
                    int top = Math.max(recyclerViewTop, child.getBottom() + params.bottomMargin);
                    int bottom = Math.min(recyclerViewBottom, top + 1);
                    c.save();
                    c.clipRect(left, top, right, bottom);
                    c.drawColor(ContextCompat.getColor(parent.getContext(), R.color.list_divider_color_gray));
                    c.restore();
                }
            }
        });

        mData = new ArrayList<>();
        mAdapter = new BaseQuickAdapter<SilkUser>(R.layout.power_ranking_item, mData) {

            private DecimalFormat mDecimalFormat = new DecimalFormat("0.000");

            @Override
            protected void convert(BaseViewHolder helper, SilkUser item) {
                Context context = helper.itemView.getContext();
                Spans spans = new Spans();
                if (item.powerRankNum == 1) {
                    spans.append("*", new ImageSpan(context, R.mipmap.ic_silk_ranking_1));
                } else if (item.powerRankNum == 2) {
                    spans.append("*", new ImageSpan(context, R.mipmap.ic_silk_ranking_2));
                } else if (item.powerRankNum == 3) {
                    spans.append("*", new ImageSpan(context, R.mipmap.ic_silk_ranking_3));
                } else {
                    spans.append(" ").append(String.valueOf(item.powerRankNum), new AbsoluteSizeSpan(ScreenUtils.dp2px(12)), new ForegroundColorSpan(0xff999999)).append("  ");
                }
                if (!TextUtils.isEmpty(item.userName)) {
                    spans.append("  ").append(item.userName);
                }
                helper.setText(R.id.tv_silk_user_name, spans)
                        .setText(R.id.tv_silk_value, mDecimalFormat.format(item.totalSilk))
                        .setText(R.id.tv_silk_power, String.valueOf(item.totalPower));
            }
        };
        rv_power_ranking.setAdapter(mAdapter);

        showProgressDialog("");
        compositeSubscription.add(RestAPI.getInstance().oSellService()
                .getSilkPowerRanking(10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseData<List<SilkUser>>>() {
                    @Override
                    public void call(ResponseData<List<SilkUser>> listResponseData) {
                        hideProgressDialog();
                        if (listResponseData.state.code == 0) {
                            mData.clear();
                            mData.addAll(listResponseData.data);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            showToast(listResponseData.state.message);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        hideProgressDialog();
                        showToast(throwable.getMessage());
                    }
                }));
    }
}
