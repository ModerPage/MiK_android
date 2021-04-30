package me.modernpage.ui.userload;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingComponent;
import androidx.databinding.DataBindingUtil;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.LayoutUserViewBinding;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.ui.common.DataBoundListAdapter;
import me.modernpage.util.Objects;

public class DataListAdapter extends DataBoundListAdapter<Profile, LayoutUserViewBinding> {
    private final DataBindingComponent dataBindingComponent;
    private final SparseBooleanArray selectedItems;
    private final List<Profile> selectedUsers;

    public DataListAdapter(DataBindingComponent dataBindingComponent) {
        this.dataBindingComponent = dataBindingComponent;
        selectedItems = new SparseBooleanArray();
        selectedUsers = new ArrayList<>();
    }

    @Override
    protected LayoutUserViewBinding createBinding(ViewGroup parent, int viewType) {
        LayoutUserViewBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.layout_user_view, parent, false, dataBindingComponent);
        return binding;
    }

    @Override
    public void onClickView(LayoutUserViewBinding binding, int position) {

    }

    @Override
    protected int getItemViewType(Profile item) {
        return 0;
    }

    @Override
    protected void bind(LayoutUserViewBinding binding, Profile item, int position) {
        binding.setUser(item);
    }

    @Override
    protected boolean areItemsTheSame(Profile oldItem, int oldItemPosition, Profile newItem, int newItemPosition) {
        return Objects.equals(oldItem.getId(), newItem.getId());
    }

    @Override
    protected boolean areContentsTheSame(Profile oldItem, int oldItemPosition, Profile newItem, int newItemPosition) {
        return Objects.equals(oldItem.getFullname(), newItem.getFullname()) &&
                Objects.equals(oldItem.getCreated(), newItem.getCreated()) &&
                Objects.equals(oldItem.getBirthdate(), newItem.getBirthdate());
    }
}
