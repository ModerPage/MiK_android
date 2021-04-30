package me.modernpage.ui.fragment.group;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingComponent;
import androidx.databinding.DataBindingUtil;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.LayoutGroupViewItemBinding;
import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.PublicGroup;
import me.modernpage.ui.common.DataBoundListAdapter;
import me.modernpage.util.Objects;

/**
 * A RecyclerView adapter for {@Link Group} class.
 */

public class PublicGroupListAdapter extends DataBoundListAdapter<PublicGroup, LayoutGroupViewItemBinding> {
    private static final String TAG = "PublicGroupListAdapter";
    private final PublicGroupClickCallback mCallback;
    private final DataBindingComponent dataBindingComponent;


    public PublicGroupListAdapter(PublicGroupClickCallback callback, DataBindingComponent dataBindingComponent) {
        mCallback = callback;
        this.dataBindingComponent = dataBindingComponent;
    }

    @Override
    protected LayoutGroupViewItemBinding createBinding(ViewGroup parent, int viewType) {
        LayoutGroupViewItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.layout_group_view_item, parent, false, dataBindingComponent);

        return binding;
    }

    @Override
    public void onClickView(LayoutGroupViewItemBinding binding, int position) {
        PublicGroup group = (PublicGroup) binding.getGroup();
        if (group != null && mCallback != null)
            mCallback.onClick(group);
    }

    @Override
    protected int getItemViewType(PublicGroup item) {
        return 0;
    }

    @Override
    protected void bind(LayoutGroupViewItemBinding binding, PublicGroup item, int position) {
        binding.setGroup(item);
    }

    @Override
    protected boolean areItemsTheSame(PublicGroup oldItem, int oldItemPosition, PublicGroup newItem, int newItemPosition) {
        return Objects.equals(oldItem.getId(), newItem.getId());
    }

    @Override
    protected boolean areContentsTheSame(PublicGroup oldItem, int oldItemPosition, PublicGroup newItem, int newItemPosition) {
        return Objects.equals(oldItem.getName(), newItem.getName());
    }

    public interface PublicGroupClickCallback {
        void onClick(Group group);
    }


}
