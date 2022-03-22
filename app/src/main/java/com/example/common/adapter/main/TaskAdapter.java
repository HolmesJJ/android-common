package com.example.common.adapter.main;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.common.R;
import com.example.common.adapter.BaseAdapter;
import com.example.common.adapter.ViewHolder;
import com.example.common.model.main.Task;

import java.util.List;

public class TaskAdapter extends BaseAdapter<Task> {

    private final OnItemListener onItemListener;

    public TaskAdapter(Context context, List<Task> tasks, OnItemListener onItemListener) {
        super(context, tasks);
        this.onItemListener = onItemListener;
    }

    @Override
    public void onBindContentViews(RecyclerView.ViewHolder holder, int position) {
        Task task = getData().get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.setText(R.id.tv_content, task.getContent());
        if (task.isFinish()) {
            viewHolder.getView(R.id.riv_check).setBackgroundResource(R.drawable.ic_checked);
        } else {
            viewHolder.getView(R.id.riv_check).setBackgroundResource(R.drawable.ic_unchecked);
        }
        viewHolder.setOnClickListener(R.id.cv_container, new View.OnClickListener() {
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
        return ViewHolder.createViewHolder(getContext(), parent, R.layout.layout_item_task);
    }

    public interface OnItemListener {
        void onItemListener(int position);
    }
}
