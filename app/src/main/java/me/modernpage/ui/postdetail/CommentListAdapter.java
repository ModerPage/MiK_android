package me.modernpage.ui.postdetail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingComponent;
import androidx.databinding.DataBindingUtil;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.LayoutCommentViewItemBinding;
import me.modernpage.data.local.entity.relation.CommentRelation;
import me.modernpage.ui.common.DataBoundListAdapter;
import me.modernpage.util.Objects;

public class CommentListAdapter extends DataBoundListAdapter<CommentRelation, LayoutCommentViewItemBinding> {

    private final DataBindingComponent dataBindingComponent;
    private final CommentClickCallback mCallback;
    private final long uid;

    public CommentListAdapter(DataBindingComponent dataBindingComponent, CommentClickCallback callback, long uid) {
        this.dataBindingComponent = dataBindingComponent;
        mCallback = callback;
        this.uid = uid;
    }


    @Override
    protected LayoutCommentViewItemBinding createBinding(ViewGroup parent, int viewType) {
        LayoutCommentViewItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.layout_comment_view_item, parent, false, dataBindingComponent);
        binding.commentViewDelete.setOnClickListener(view -> {
            CommentRelation commentRelation = binding.getComment();
            if (commentRelation != null && mCallback != null)
                mCallback.onCommentDeleteClick(commentRelation);
        });
        return binding;
    }

    @Override
    public void onClickView(LayoutCommentViewItemBinding binding, int position) {

    }

    @Override
    protected int getItemViewType(CommentRelation item) {
        return 0;
    }

    @Override
    protected void bind(LayoutCommentViewItemBinding binding, CommentRelation item, int position) {
        binding.setComment(item);
        binding.setUid(uid);
    }

    @Override
    protected boolean areItemsTheSame(CommentRelation oldItem, int oldItemPosition, CommentRelation newItem, int newItemPosition) {
        return Objects.equals(oldItem.getComment().getId(), newItem.getComment().getId());
    }

    @Override
    protected boolean areContentsTheSame(CommentRelation oldItem, int oldItemPosition, CommentRelation newItem, int newItemPosition) {
        return Objects.equals(oldItem.getComment().getCreated(), newItem.getComment().getCreated()) &&
                Objects.equals(oldItem.getComment().getText(), newItem.getComment().getText());
    }

    public interface CommentClickCallback {
        void onCommentDeleteClick(CommentRelation comment);
    }
}
