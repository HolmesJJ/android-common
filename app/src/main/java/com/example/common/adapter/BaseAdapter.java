package com.example.common.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected static final String TAG = BaseAdapter.class.getSimpleName();

    public abstract void onBindContentViews(RecyclerView.ViewHolder holder, int position);

    public abstract RecyclerView.ViewHolder initContentViews(ViewGroup parent, int viewType);

    // 正常视图
    protected static final int TYPE_NORMAL = 10000;
    // 底部视图
    protected static final int TYPE_FOOTER = 20000;
    // 顶部视图
    protected static final int TYPE_HEADER = 30000;

    private final SparseArrayCompat<View> headerViews = new SparseArrayCompat<>();
    private final SparseArrayCompat<View> footerViews = new SparseArrayCompat<>();

    private final Context context;
    // 数据源
    private List<T> data = new ArrayList<>();

    public BaseAdapter(Context context) {
        this.context = context;
    }

    public BaseAdapter(Context context, List<T> data) {
        this.context = context;
        this.data = data;
    }

    public Context getContext() {
        return this.context;
    }

    public List<T> getData() {
        return this.data;
    }

    public void addData(List<T> data) {
        this.data.addAll(data);
    }

    public void addItem(T data) {
        this.data.add(data);
    }

    public void setData(List<T> data) {
        int size = data.size();
        this.data.clear();
        notifyItemRangeRemoved(0, size);
        this.data.addAll(data);
    }

    public void setData(List<T> list, int position) {
        this.data = list;
        notifyItemChanged(position);
    }

    public void setItemData(List<T> list) {
        int pos = data.size();
        this.data = list;
        notifyItemRangeChanged(pos, list.size() - pos);
    }

    public void addHeaderView(View view) {
        headerViews.put(getHeadersCount() + TYPE_HEADER, view);
    }

    public void addFooterView(View view) {
        footerViews.put(getFootersCount() + TYPE_FOOTER, view);
    }

    public void removeHeaderView(int position) {
        headerViews.remove(position + TYPE_HEADER);
    }

    public void removeAllHeaderView() {
        headerViews.clear();
    }

    public void removeFooterView(int position) {
        footerViews.remove(getItemCount() - getFootersCount() + position + TYPE_FOOTER);
    }

    public void removeAllFooterView() {
        footerViews.clear();
    }

    private boolean isHeaderView(int position) {
        return position < getHeadersCount();
    }

    private boolean isFooterView(int position) {
        return position >= getHeadersCount() + getContentsCount();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int viewType = getItemViewType(position);
                    if (headerViews.get(viewType) != null || footerViews.get(viewType) != null) {
                        return ((GridLayoutManager) layoutManager).getSpanCount();
                    }
                    return 1;
                }
            });
            gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        if (isHeaderView(position) || isFooterView(position)) {
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            if (params instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams sp =
                        (StaggeredGridLayoutManager.LayoutParams) params;
                sp.setFullSpan(true);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderView(position)) {
            return headerViews.keyAt(position);
        } else if (isFooterView(position)) {
            return footerViews.keyAt(position - getHeadersCount() - getContentsCount());
        } else {
            return position - getHeadersCount();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (headerViews.get(viewType) != null) { // header布局的viewHolder
            return  ViewHolder.createViewHolder(parent.getContext(), headerViews.get(viewType));
        } else if (footerViews.get(viewType) != null) { // footer布局的viewHolder
            return  ViewHolder.createViewHolder(parent.getContext(), footerViews.get(viewType));
        } else {
            // 初始化内容
            return initContentViews(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (isHeaderView(position)) {
            return;
        } else if (isFooterView(position)) {
            return;
        } else {
            // content的列表实现类
            onBindContentViews(holder, position - getHeadersCount());
        }
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + getContentsCount() + getFootersCount();
    }

    public int getHeadersCount() {
        return headerViews.size();
    }

    public int getFootersCount() {
        return footerViews.size();
    }

    public int getContentsCount() {
        return data.size();
    }
}
