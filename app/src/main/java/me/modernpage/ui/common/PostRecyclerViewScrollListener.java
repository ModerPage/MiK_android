package me.modernpage.ui.common;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class PostRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    private int firstVisibleItem = 0;
    private int visibleItemCount = 0;

    private volatile boolean mEnabled = true;
    private int mPreLoadCount = 0;


    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (mEnabled) {
            LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
            visibleItemCount = manager.getChildCount();
            firstVisibleItem = manager.findFirstCompletelyVisibleItemPosition();
            onItemIsFirstVisibleItem(firstVisibleItem);
            int lastPosition = manager.findLastVisibleItemPosition();
            onItemIsLastPosition(lastPosition);

//            if(!recyclerView.canScrollVertically(1)) {
//                endOfList();
//            } else {
//                int startPosition = manager.findFirstVisibleItemPosition();
//                int endPosition = manager.findLastVisibleItemPosition();
//                visibleItemPositions(startPosition, endPosition);
//            }
        }
    }


    public abstract void onItemIsFirstVisibleItem(int index);

    public abstract void onItemIsLastPosition(int index);

    public void disableScrollListener() {
        mEnabled = false;
    }

    public void enableScrollListener() {
        mEnabled = true;
    }

    public void setPreLoadCount(int preLoadCount) {
        mPreLoadCount = preLoadCount;
    }
}
