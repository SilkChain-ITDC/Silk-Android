package com.osell.activity.silk.mission;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshRecycleView;
import com.osell.R;
import com.osell.activity.baseactivity.RxBasePullRefreshActivity;
import com.osell.activity.silk.entity.SilkDetailEntity;
import com.osell.adapter.baserecycler.BaseQuickAdapter;
import com.osell.adapter.baserecycler.BaseViewHolder;
import com.osell.entity.home.ResponseData;
import com.osell.net.RestAPI;
import com.osell.util.ScreenUtils;
import com.osell.view.ScrollerNumberPicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SilkDetailActivity extends RxBasePullRefreshActivity<SilkDetailEntity, RecyclerView, PullToRefreshRecycleView> implements View.OnClickListener {

    private RecyclerView rv_score_histories;

    private TextView tv_start_time;
    private TextView tv_end_time;
    private TextView tv_type;
    private TextView tv_sort_type;
    private RelativeLayout rl_start_time;
    private RelativeLayout rl_end_time;
    private RelativeLayout rl_type;
    private RelativeLayout rl_sort_type;
    private Button btn_query;

    private SilkHistoryAdapter mAdapter;

    @Override
    protected int mainLayout() {
        return R.layout.activity_recycler_view;
    }

    @Override
    protected void initView() {
        super.initView();
        rv_score_histories = pullToRefreshView.getRefreshableView();
    }

    @Override
    protected boolean getIsLogin() {
        return super.getIsLogin();
    }

    @Override
    protected void initVoid() {
        setNavigationTitle("Details");
        rv_score_histories.setId(R.id.recyclerView);
        rv_score_histories.setHasFixedSize(true);
        rv_score_histories.setLayoutManager(new LinearLayoutManager(this));
        rv_score_histories.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 1);
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                int recyclerViewTop = parent.getPaddingTop();
                int recyclerViewBottom = parent.getHeight() - parent.getPaddingBottom();
                int childCount = parent.getChildCount();
                for (int i = 1; i < childCount; i++) {
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
        mAdapter = new SilkHistoryAdapter(dataList);
        View headerView = LayoutInflater.from(this).inflate(R.layout.my_score_detail_header, rv_score_histories, false);
        tv_start_time = (TextView) headerView.findViewById(R.id.tv_start_time);
        tv_end_time = (TextView) headerView.findViewById(R.id.tv_end_time);
        tv_type = (TextView) headerView.findViewById(R.id.tv_type);
        tv_sort_type = (TextView) headerView.findViewById(R.id.tv_sort_type);
        rl_start_time = (RelativeLayout) headerView.findViewById(R.id.rl_start_time);
        rl_end_time = (RelativeLayout) headerView.findViewById(R.id.rl_end_time);
        rl_type = (RelativeLayout) headerView.findViewById(R.id.rl_type);
        rl_sort_type = (RelativeLayout) headerView.findViewById(R.id.rl_sort_type);
        btn_query = (Button) headerView.findViewById(R.id.btn_query);

        tv_type.setText(R.string.score_type_all);
        tv_sort_type.setText(R.string.sort_type_asc);
        rl_start_time.setOnClickListener(this);
        rl_end_time.setOnClickListener(this);
        rl_type.setOnClickListener(this);
        rl_sort_type.setOnClickListener(this);
        btn_query.setOnClickListener(this);
        mAdapter.addHeaderView(headerView);
        rv_score_histories.setAdapter(mAdapter);

        pullToRefreshView.onRefreshComplete();
        super.initVoid();
    }

    @Override
    protected Subscription getDataHttp(boolean isClean) {
        Observable observable;
        if (getIntent().getIntExtra("data", -1) == 1) {
            observable = RestAPI.getInstance().oSellService().getSilkDetails(
                    getLoginInfo().userID,
                    tv_type.getText().toString().equals(getString(R.string.score_type_all)) ? 0
                            : (tv_type.getText().toString().equals(getString(R.string.score_type_reward)) ? 1 : 2),
                    tv_sort_type.getText().toString().equals(getString(R.string.sort_type_asc)) ? "asc" : "desc",
                    tv_start_time.getText().toString(),
                    tv_end_time.getText().toString(),
                    page,
                    20
            );
        } else {
            observable = RestAPI.getInstance().oSellService().getSilkPowerDetails(
                    getLoginInfo().userID,
                    tv_type.getText().toString().equals(getString(R.string.score_type_all)) ? 0
                            : (tv_type.getText().toString().equals(getString(R.string.score_type_reward)) ? 1 : 2),
                    tv_sort_type.getText().toString().equals(getString(R.string.sort_type_asc)) ? "asc" : "desc",
                    tv_start_time.getText().toString(),
                    tv_end_time.getText().toString(),
                    page,
                    20
            );
        }
        return observable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(setAction(isClean), setThrowable());
    }

    @Override
    protected void setData(boolean isClean) {
        mAdapter.removeAllFooterView();
        mAdapter.setNewData(dataList);
        rv_score_histories.scrollBy(0, 20);
    }

    @Override
    protected Action1<ResponseData<List<SilkDetailEntity>>> setAction(final boolean isClean) {
        return new Action1<ResponseData<List<SilkDetailEntity>>>() {

            @Override
            public void call(ResponseData<List<SilkDetailEntity>> entityList) {
                pullToRefreshView.onRefreshComplete();
                hideProgressDialog();
                try {
                    if (entityList.data != null && entityList.state.code == 0) {
                        if (isClean) {
                            dataList.clear();
                        }
                        dataList.addAll(entityList.data);
                        setData(isClean);
                        page++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (pullToRefreshView.getMode() == PullToRefreshBase.Mode.DISABLED)
                    return;

                if (!isClean && !(entityList.data != null && entityList.data.size() > 0)) {
                    showNotMore();
                    pullToRefreshView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
                } else {
                    pullToRefreshView.setMode(PullToRefreshBase.Mode.BOTH);
                }
            }
        };
    }

    @Override
    public void showNotMore() {
        mAdapter.removeAllFooterView();
        View view = View.inflate(this, R.layout.view_load_all_bottom, null);
        mAdapter.addFooterView(view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_start_time: {
                showDatePicker(tv_start_time, convertStrToDate(tv_start_time.getText().toString(), "yyyy-MM-dd"));
            }
            break;
            case R.id.rl_end_time: {
                showDatePicker(tv_end_time, convertStrToDate(tv_end_time.getText().toString(), "yyyy-MM-dd"));
            }
            break;
            case R.id.rl_type: {
                showPicker(tv_type, new ArrayList<>(Arrays.asList(getString(R.string.score_type_all), getString(R.string.score_type_reward), getString(R.string.score_type_redeem))));
            }
            break;
            case R.id.rl_sort_type: {
                showPicker(tv_sort_type, new ArrayList<>(Arrays.asList(getString(R.string.sort_type_asc), getString(R.string.sort_type_desc))));
            }
            break;
            case R.id.btn_query: {
                pullToRefreshView.setRefreshing();
            }
            break;
            default:
                break;
        }
    }

    private void showPicker(final TextView view, ArrayList<String> data) {
        final Dialog dlg = new Dialog(this, R.style.MMThem_DataSheet);
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.simple_picker, (ViewGroup) getWindow().getDecorView(), false);

        final ScrollerNumberPicker picker = (ScrollerNumberPicker) layout.findViewById(R.id.snp_picker);

        picker.setData(data);
        picker.setDefault(0);

        layout.findViewById(R.id.btn_picker_left)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                    }
                });
        layout.findViewById(R.id.btn_picker_right)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        view.setText(picker.getSelectedText());
                        dlg.dismiss();
                    }
                });

        // set a large value put it in bottom
        Window w = dlg.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        lp.y = -1000;
        lp.gravity = Gravity.BOTTOM;
        dlg.onWindowAttributesChanged(lp);
        dlg.setCanceledOnTouchOutside(true);
        dlg.setCancelable(true);
        dlg.setContentView(layout);
        dlg.show();
    }

    private void showDatePicker(final TextView view, Date date) { // 0 start ,1 end
        final Dialog dlg = new Dialog(this, R.style.MMThem_DataSheet);
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.date_picker, (ViewGroup) getWindow().getDecorView(), false);

        final ScrollerNumberPicker snp_picker_year = (ScrollerNumberPicker) layout.findViewById(R.id.snp_picker_year);
        final ScrollerNumberPicker snp_picker_month = (ScrollerNumberPicker) layout.findViewById(R.id.snp_picker_month);
        final ScrollerNumberPicker snp_picker_day = (ScrollerNumberPicker) layout.findViewById(R.id.snp_picker_day);

        ArrayList<String> years = new ArrayList<>(Arrays.asList("2016", "2017", "2018"));
        ArrayList<String> months = new ArrayList<>(Arrays.asList("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"));
        ArrayList<String> days = new ArrayList<>();
        for (int i = 1; i < 32; i++) {
            days.add(String.format(Locale.getDefault(), "%02d", i));
        }
        snp_picker_year.setData(years);
        snp_picker_month.setData(months);
        snp_picker_day.setData(days);

        snp_picker_year.setDefault(date == null ? 0 : Integer.parseInt(convertDateToStr(date, "yyyy")) - 2016);
        snp_picker_month.setDefault(date == null ? 0 : Integer.parseInt(convertDateToStr(date, "MM")) - 1);
        snp_picker_day.setDefault(date == null ? 0 : Integer.parseInt(convertDateToStr(date, "dd")) - 1);

        layout.findViewById(R.id.btn_picker_left)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                    }
                });
        layout.findViewById(R.id.btn_picker_right)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String dateStr = snp_picker_year.getSelectedText() + "-" + snp_picker_month.getSelectedText() + "-" + snp_picker_day.getSelectedText();
                        Date date = convertStrToDate(dateStr, "yyyy-MM-dd");
                        if (date != null) {
                            view.setText(snp_picker_year.getSelectedText() + "-" + snp_picker_month.getSelectedText() + "-" + snp_picker_day.getSelectedText());
                            dlg.dismiss();
                        }
                    }
                });

        // set a large value put it in bottom
        Window w = dlg.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        lp.y = -1000;
        lp.gravity = Gravity.BOTTOM;
        dlg.onWindowAttributesChanged(lp);
        dlg.setCanceledOnTouchOutside(true);
        dlg.setCancelable(true);
        dlg.setContentView(layout);
        dlg.show();
    }

    private static String convertDateToStr(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        dateFormat.setLenient(false);
        return dateFormat.format(date);
    }

    private static Date convertStrToDate(String str, String pattern) {
        if (str == null || str.equals("")) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        dateFormat.setLenient(false);
        try {
            return dateFormat.parse(str);
        } catch (ParseException ex) {
            return null;
        }
    }

    private static class SilkHistoryAdapter extends BaseQuickAdapter<SilkDetailEntity> {


        private SilkHistoryAdapter(List<SilkDetailEntity> data) {
            super(R.layout.item_silk_detail, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, final SilkDetailEntity item) {
            helper.itemView.setBackgroundColor(helper.getLayoutPosition() % 2 != 0 ?
                    Color.parseColor("#F2F2F2") : Color.WHITE
            );

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(item.Name + "\n" + item.CreateTime)
                    .setSpan(new AbsoluteSizeSpan(ScreenUtils.dip2px(12)),
                            item.Name.length(), builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#999999")),
                    item.Name.length(), builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            helper.setText(R.id.item_silk_detail_name_and_time, builder);
            TextView view = helper.getView(R.id.item_silk_detail_value);
            view.setTextColor(
                    mContext.getResources().getColor(item.Type.equals("+") ?
                            R.color.black : R.color.head_bg_color
                    )
            );
            view.setText(String.format(item.Type + "%.3f", item.value < 0.001 ? 0.001 : item.value));
        }
    }

    /**
     * @param type 1 silk， 2 silkpower
     */
    public static void jump(Context context, int type) {
        Intent intent = new Intent(context, SilkDetailActivity.class);
        intent.putExtra("data", type);
        context.startActivity(intent);
    }

}
