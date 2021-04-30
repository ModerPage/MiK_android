package me.modernpage.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import me.modernpage.databinding.CustomDataBindingComponent;

public abstract class BaseFragment<VM extends ViewModel, DB extends ViewDataBinding> extends Fragment {
    protected DB dataBinding;
    protected VM viewModel;

    @Inject
    protected CustomDataBindingComponent dataBindingComponent;

    @LayoutRes
    public abstract int getLayoutRes();

    public abstract Class<VM> getViewModel();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(getViewModel());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dataBinding = DataBindingUtil.inflate(inflater, getLayoutRes(), container, false, dataBindingComponent);
        dataBinding.setLifecycleOwner(this);
        return dataBinding.getRoot();
    }


}
