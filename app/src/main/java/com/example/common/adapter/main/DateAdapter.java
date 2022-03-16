package com.example.common.adapter.main;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.common.R;
import com.example.common.adapter.BaseAdapter;
import com.example.common.adapter.ViewHolder;
import com.example.common.model.main.DateOfMonth;

import java.util.List;

public class DateAdapter extends BaseAdapter<DateOfMonth> {

    private final OnItemListener onItemListener;

    public DateAdapter(Context context, List<DateOfMonth> dateOfMonths, OnItemListener onItemListener) {
        super(context, dateOfMonths);
        this.onItemListener = onItemListener;
    }

    @Override
    public void onBindContentViews(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        DateOfMonth dateOfMonth = getData().get(position);
        viewHolder.setText(R.id.tv_date, dateOfMonth.getDate());
        viewHolder.setText(R.id.tv_day, dateOfMonth.getWeekDate());
        viewHolder.setBackgroundRes(R.id.vSelected, dateOfMonth.getType() == 1 ? R.drawable.bg_select_dot : R.drawable.bg_dot);

        viewHolder.setOnClickListener(R.id.ll_date_container, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemListener != null) {
                    onItemListener.onItemListener(position);
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder initContentViews(ViewGroup parent, int viewType) {
        return ViewHolder.createViewHolder(getContext(), parent, R.layout.item_date);
    }

    public interface OnItemListener {
        void onItemListener(int position);
    }
}
