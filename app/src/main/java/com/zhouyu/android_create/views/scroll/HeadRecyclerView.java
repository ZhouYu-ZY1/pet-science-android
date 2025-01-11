package com.zhouyu.android_create.views.scroll;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by caizhiming on 2015/12/29.
 */
public class HeadRecyclerView extends RecyclerView {

    private final ArrayList<View> mHeaderViews = new ArrayList<>();
    private final ArrayList<View> mFooterViews = new ArrayList<>();
    private Adapter mAdapter;
    private Adapter mWrapAdapter;
    private static final int TYPE_HEADER = -101;
    private static final int TYPE_FOOTER  = -102;
    private static final int TYPE_LIST_ITEM = - 103;
    public HeadRecyclerView(Context context) {
        this(context, null);
    }
    public HeadRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public HeadRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    private void init(Context context){

    }

    @Override
    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
        mWrapAdapter = new WrapAdapter(mHeaderViews, mFooterViews, adapter);
        super.setAdapter(mWrapAdapter);
        mAdapter.registerAdapterDataObserver(mDataObserver);
    }
    public void addHeaderView(View view){
        mHeaderViews.clear();
        mHeaderViews.add(view);
    }
    public void addFooterView(View view){
        mFooterViews.clear();
        mFooterViews.add(view);
    }
    public int getHeaderViewsCount(){
        return mHeaderViews.size();
    }
    public int getFooterViewsCount(){
        return mFooterViews.size();
    }
    private final AdapterDataObserver mDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            mWrapAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mWrapAdapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }
    };
    private class WrapAdapter extends Adapter<ViewHolder>{

        private final Adapter mAdapter;
        private final List<View> mHeaderViews;
        private final List<View> mFooterViews;
        public WrapAdapter(List<View> headerViews,List<View> footerViews,Adapter adapter){
            this.mAdapter = adapter;
            this.mHeaderViews = headerViews;
            this.mFooterViews = footerViews;
        }

        public int getHeaderCount(){
            return this.mHeaderViews.size();
        }
        public int getFooterCount(){
            return this.mFooterViews.size();
        }
        public boolean isHeader(int position){
            return position >= 0 && position < this.mHeaderViews.size();
        }
        public boolean isFooter(int position){
            return position < getItemCount() && position >= getItemCount() - this.mFooterViews.size();
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == TYPE_HEADER){
                return new CustomViewHolder(this.mHeaderViews.get(0));
            }else if(viewType == TYPE_FOOTER){
                return new CustomViewHolder(this.mFooterViews.get(0));
            }else{
                return this.mAdapter.onCreateViewHolder(parent,viewType);
            }
        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
            if(isHeader(position)) return;
            if(isFooter(position)) return;
            int rePosition = position - getHeaderCount();
            int itemCount = this.mAdapter.getItemCount();
            if(this.mAdapter != null){
                if(rePosition < itemCount){
                    this.mAdapter.onBindViewHolder(holder,rePosition);
                    return;
                }
            }
        }
        @Override
        public long getItemId(int position) {
            if (this.mAdapter != null && position >= getHeaderCount()) {
                int rePosition = position - getHeaderCount();
                int itemCount = this.mAdapter.getItemCount();
                if (rePosition < itemCount) {
                    return this.mAdapter.getItemId(rePosition);
                }
            }
            return -1;
        }
        @Override
        public int getItemViewType(int position) {
            if(isHeader(position)){
                return TYPE_HEADER;
            }
            if(isFooter(position)){
                return TYPE_FOOTER;
            }
            int rePosition = position - getHeaderCount();
            int itemCount = this.mAdapter.getItemCount();
            if(rePosition < itemCount){
                return this.mAdapter.getItemViewType(position-getHeaderCount());
            }
            return TYPE_LIST_ITEM;
        }
        @Override
        public int getItemCount() {
            if(this.mAdapter != null){
                return getHeaderCount() + getFooterCount() + this.mAdapter.getItemCount();
            }else{
                return getHeaderCount() + getFooterCount();
            }
        }

        @Override
        public void registerAdapterDataObserver(@NotNull AdapterDataObserver observer) {
            if(this.mAdapter != null){
                this.mAdapter.registerAdapterDataObserver(observer);
            }
        }

        @Override
        public void unregisterAdapterDataObserver(@NotNull AdapterDataObserver observer) {
            if(this.mAdapter != null){
                this.mAdapter.unregisterAdapterDataObserver(observer);
            }
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            if(holder.getItemViewType() == TYPE_HEADER){
                super.onViewDetachedFromWindow(holder);
            }else if(holder.getItemViewType() == TYPE_FOOTER){
                super.onViewDetachedFromWindow(holder);
            }else{
                this.mAdapter.onViewDetachedFromWindow(holder);
            }
        }
        private class CustomViewHolder extends ViewHolder{

            public CustomViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
