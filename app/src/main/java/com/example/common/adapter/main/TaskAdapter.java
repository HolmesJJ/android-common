package com.example.common.adapter.main;

import android.content.Context;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.common.R;
import com.example.common.adapter.BaseAdapter;
import com.example.common.adapter.ViewHolder;
import com.example.common.model.main.Task;

import java.util.List;

public class TaskAdapter extends BaseAdapter<Task> {

    public TaskAdapter(Context context, List<Task> tasks) {
        super(context, tasks);
    }

    @Override
    public void onBindContentViews(RecyclerView.ViewHolder holder, int position) {
        Task task = getData().get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.setText(R.id.tv_content, task.getContent());
        if(task.isFinish()) {
            viewHolder.getView(R.id.riv_check).setBackgroundResource(R.color.FFB5C4B1);
        }
        else {
            viewHolder.getView(R.id.riv_check).setBackgroundResource(R.color.FFDADAD8);
        }
    }

    @Override
    public RecyclerView.ViewHolder initContentViews(ViewGroup parent, int viewType) {
        return ViewHolder.createViewHolder(getContext(), parent, R.layout.item_task);
    }
}
