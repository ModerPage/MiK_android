package me.modernpage.fragment.group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.util.Constants;
import me.modernpage.activity.R;
import me.modernpage.entity.Group;

public class GroupRecViewAdapter extends RecyclerView.Adapter<GroupRecViewAdapter.GroupViewHoleder> {
    private static final String TAG = "GroupRecViewAdapter";
    private List<Group> mGroups;

    public GroupRecViewAdapter(List<Group> groups, String groupType) {
        mGroups = getRightGroup(groups, groupType);
    }

    void loadNewGroups(List<Group> groups, String groupType) {
        mGroups = getRightGroup(groups, groupType);
        notifyDataSetChanged();
    }

    private List<Group> getRightGroup(List<Group> groups, String groupType) {
        List<Group> newGroup = new ArrayList<>();
        for(Group g: groups) {
            if(groupType.equals(g.getGroupType().getGroupTypeName())) {
                newGroup.add(g);
            }
        }
        return newGroup;
    }

    @NonNull
    @Override
    public GroupViewHoleder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_view, parent, false);
        return new GroupViewHoleder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHoleder holder, int position) {
        if(mGroups == null || mGroups.size() == 0) {
            holder.mImageView.setImageResource(R.drawable.placeholder);
            holder.mTextView.setText(R.string.empty_private_group);
        } else {
            Picasso.get()
                    .load(Constants.Network.BASE_URL + mGroups.get(position).getImageURI())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.mImageView);
            holder.mTextView.setText(mGroups.get(position).getGroupName());
        }
    }

    @Override
    public int getItemCount() {
        return (mGroups != null && mGroups.size() > 0) ? mGroups.size() : 1;
    }

    static class GroupViewHoleder extends RecyclerView.ViewHolder {
        ImageView mImageView;
        TextView mTextView;
        GroupViewHoleder(@NonNull View view) {
            super(view);
            mImageView = view.findViewById(R.id.group_view_image);
            mTextView = view.findViewById(R.id.group_view_name);

        }
    }
}
