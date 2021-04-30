package me.modernpage.ui.common;

import androidx.annotation.Nullable;

import me.modernpage.util.LoadState;
import me.modernpage.util.Resource;

public class NextPageHandler extends ProcessHandler<Boolean> {
    @Nullable
    protected String url;
    boolean hasMore;

    @Override
    public void onChanged(Resource<Boolean> result) {
        if (result == null) {
            reset();
        } else {
            switch (result.status) {
                case SUCCESS:
                    hasMore = Boolean.TRUE.equals(result.data);
                    unregister();
                    processState.setValue(new LoadState<Boolean>(false, true, null, result.data));
                    break;
                case ERROR:
                    hasMore = true;
                    unregister();
                    processState.setValue(new LoadState<Boolean>(false,
                            true, result.message, result.data));
                    break;
                case LOGOUT:
                    hasMore = true;
                    unregister();
                    processState.setValue(new LoadState<>(false, false, result.message, null));
                    break;
            }
        }

    }

    @Override
    protected void unregister() {
        if (data != null) {
            data.removeObserver(this);
            data = null;
            if (hasMore) {
                url = null;
            }
        }
    }

    @Override
    protected void reset() {
        unregister();
        hasMore = true;
        processState.setValue(new LoadState<Boolean>(false, true, null, null));
    }

}
