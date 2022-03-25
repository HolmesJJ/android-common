package com.example.common.adapter.game;

import android.content.Context;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.common.R;
import com.example.common.adapter.BaseAdapter;
import com.example.common.adapter.ViewHolder;
import com.example.common.model.game.Member;

import java.util.List;

public class LeaderboardAdapter extends BaseAdapter<Member> {

    public LeaderboardAdapter(Context context, List<Member> members) {
        super(context, members);
    }

    @Override
    public void onBindContentViews(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Member member = getData().get(position);
        viewHolder.setText(R.id.tv_rank, String.valueOf(member.getRank()));
        viewHolder.setText(R.id.tv_name, member.getName());
        viewHolder.setText(R.id.tv_score, String.valueOf(member.getScore()));
    }

    @Override
    public RecyclerView.ViewHolder initContentViews(ViewGroup parent, int viewType) {
        return ViewHolder.createViewHolder(getContext(), parent, R.layout.layout_item_member);
    }
}
