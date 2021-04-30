package me.modernpage.ui.postdetail;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.LayoutPostVideoDetailViewBinding;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.relation.PostRelation;
import me.modernpage.databinding.MediaBindingAdapter;
import me.modernpage.ui.BaseFragment;
import me.modernpage.ui.dialog.UserInfoDialog;

@AndroidEntryPoint
public class VideoPostDetailFragment extends BaseFragment<PostDetailViewModel, LayoutPostVideoDetailViewBinding> {

    private PostDetailHandler mHandler;

    @Override
    public int getLayoutRes() {
        return R.layout.layout_post_video_detail_view;
    }

    @Override
    public Class<PostDetailViewModel> getViewModel() {
        return PostDetailViewModel.class;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Activity activity = getActivity();
        if (!(activity instanceof PostDetailHandler))
            throw new ClassCastException(activity.getClass().getSimpleName() +
                    " must implement PostDetailHandler interface.");
        mHandler = (PostDetailHandler) activity;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mHandler = null;
        super.onDetach();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();
        final long uid = args.getLong(Profile.class.getSimpleName());
        final PostRelation post = (PostRelation) args.getSerializable(PostRelation.class.getSimpleName());
        if (uid == 0 || post == null) {
            throw new IllegalArgumentException("Uid and/or Post not present in bundle.");
        }
        dataBinding.postViewMore.setVisibility(View.INVISIBLE);
        dataBinding.setIsMuted(MediaBindingAdapter.isMuted);
        dataBinding.setPost(post);
        viewModel.setCommentQuery(post.getPost()._comments());

        viewModel.getLikes(post.getPost()._likes(), uid).observe(getViewLifecycleOwner(), likesResource -> {
            dataBinding.setLikes(likesResource == null ? null : likesResource.data);
            dataBinding.executePendingBindings();
        });

        viewModel.getComments().observe(getViewLifecycleOwner(), commentsResource -> {
            dataBinding.setComments(commentsResource == null ? null : commentsResource.data);
            dataBinding.executePendingBindings();
        });

        dataBinding.postViewLike.setOnClickListener(view1 -> {
            viewModel.likeControl(post.getPost()._likes(), post.getPost().getId(), uid);
        });

        dataBinding.postViewAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uid == post.getOwner().getId())
                    return;
                FragmentManager fragmentManager = getChildFragmentManager();
                Fragment prev = fragmentManager.findFragmentByTag("dialog");
                FragmentTransaction ft = fragmentManager.beginTransaction();
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                UserInfoDialog userInfoDialog = UserInfoDialog.newInstance(post.getOwner(), uid);
                userInfoDialog.show(ft, "dialog");
            }
        });

        dataBinding.postViewShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHandler != null)
                    mHandler.onShareClicked(post);
            }
        });
    }

    @Override
    public void onPause() {
        MediaBindingAdapter.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        MediaBindingAdapter.play();
    }

    @Override
    public void onDestroyView() {
        MediaBindingAdapter.release();
        super.onDestroyView();
    }
}
