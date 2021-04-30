package me.modernpage.ui.fragment.group;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingComponent;
import androidx.databinding.DataBindingUtil;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.LayoutGroupViewItemBinding;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.ui.common.DataBoundListAdapter;
import me.modernpage.util.Objects;

public class PrivateGroupListAdapter extends DataBoundListAdapter<PrivateGroup, LayoutGroupViewItemBinding> {
    private final PrivateGroupClickCallback mCallback;
    private final DataBindingComponent dataBindingComponent;

    public PrivateGroupListAdapter(PrivateGroupClickCallback callback, DataBindingComponent dataBindingComponent) {
        mCallback = callback;
        this.dataBindingComponent = dataBindingComponent;
    }

    @Override
    protected LayoutGroupViewItemBinding createBinding(ViewGroup parent, int viewType) {
        LayoutGroupViewItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.layout_group_view_item,
                parent, false, dataBindingComponent);

        return binding;
    }

    @Override
    public void onClickView(LayoutGroupViewItemBinding binding, int position) {
        PrivateGroup group = (PrivateGroup) binding.getGroup();
        if (group != null && mCallback != null) {
            mCallback.onClick(group);
        }
    }

    @Override
    protected int getItemViewType(PrivateGroup item) {
        return 0;
    }

    @Override
    protected void bind(LayoutGroupViewItemBinding binding, PrivateGroup item, int position) {
        binding.setGroup(item);
    }

    @Override
    protected boolean areItemsTheSame(PrivateGroup oldItem, int oldItemPosition, PrivateGroup newItem, int newItemPosition) {
        return Objects.equals(oldItem.getId(), newItem.getId());
    }

    @Override
    protected boolean areContentsTheSame(PrivateGroup oldItem, int oldItemPosition, PrivateGroup newItem, int newItemPosition) {
        return Objects.equals(oldItem.getName(), newItem.getName());
    }

//    @Override
//    protected boolean getChangePayload(PrivateGroup oldItem, int oldItemPosition, PrivateGroup newItem, int newItemPosition) {
//        return false;
//    }

    public interface PrivateGroupClickCallback {
        void onClick(PrivateGroup group);
    }
}
