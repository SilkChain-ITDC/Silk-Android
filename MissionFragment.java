package com.osell.activity.silk.mission;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.osell.R;
import com.osell.adapter.baserecycler.BaseQuickAdapter;
import com.osell.fragment.basefragment.OsellBaseFragment;


public class MissionFragment extends OsellBaseFragment {
    private String mTitle;
    private BaseQuickAdapter<?> mAdapter;

    private RecyclerView mListView;
    private OnMissionClickListener mListener;
    private boolean isShowDivider;

    public static MissionFragment newInstance(String title, boolean isShowDivider) {

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putBoolean("divider", isShowDivider);
        MissionFragment fragment = new MissionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initData() {
        super.initData();
        mTitle = getArguments().getString("title");
        isShowDivider = getArguments().getBoolean("divider");
    }

    @Override
    protected void initView(View layoutView) {
        ((TextView) getView(R.id.frag_mission_title)).setText(mTitle);
        mListView = getView(R.id.frag_mission_list);

        if (isShowDivider) {
            mListView.addItemDecoration(new CustomDivider(this.getContext()));
        }
        if (mAdapter != null) {
            mListView.setAdapter(mAdapter);
            mAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (mListener != null) {
                        mListener.OnMissionClick(position);
                    }
                }
            });
        }
    }

    @Override
    protected int mainLayout() {
        return R.layout.fragment_mission;
    }

    public void setAdapter(BaseQuickAdapter<?> adapter) {
        mAdapter = adapter;
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
            mAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (mListener != null) {
                        mListener.OnMissionClick(position);
                    }
                }
            });
        }
    }

    public void setListener(OnMissionClickListener mListener) {
        this.mListener = mListener;
    }

    public interface OnMissionClickListener {
        void OnMissionClick(int position);
    }
}
