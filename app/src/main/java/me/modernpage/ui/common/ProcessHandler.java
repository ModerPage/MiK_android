package me.modernpage.ui.common;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import me.modernpage.util.LoadState;
import me.modernpage.util.Resource;

/**
 * @param <T> data type
 */
public class ProcessHandler<T> implements Observer<Resource<T>> {
    @Nullable
    protected LiveData<Resource<T>> data;
    protected final MutableLiveData<LoadState<T>> processState = new MutableLiveData<>();

    public ProcessHandler() {
        reset();
    }

    @Override
    public void onChanged(Resource<T> result) {
        if (result == null) {
            reset();
        } else {
            switch (result.status) {
                case SUCCESS:
                    unregister();
                    processState.setValue(new LoadState<T>(false, true, null, result.data));
                    break;
                case ERROR:
                    unregister();
                    processState.setValue(new LoadState<T>(false, true, result.message, null));
                    break;
                case LOGOUT:
                    unregister();
                    processState.setValue(new LoadState<>(false, false, result.message, null));
            }
        }


    }

    protected void reset() {
        unregister();
        processState.setValue(null);
    }

    protected void unregister() {
        if (data != null) {
            data.removeObserver(this);
            data = null;
        }
    }

    public MutableLiveData<LoadState<T>> getProcessState() {
        return processState;
    }
}
