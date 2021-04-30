package me.modernpage.ui.groupdetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.model.Members;
import me.modernpage.data.repository.GroupRepository;
import me.modernpage.data.repository.UserRepository;
import me.modernpage.ui.common.ProcessHandler;
import me.modernpage.util.LoadState;
import me.modernpage.util.Resource;

@HiltViewModel
public class GroupDetailViewModel extends ViewModel {
    private static final String TAG = "PublicGroupDetailViewMo";
    private final UserRepository mUserRepository;
    private final SavedStateHandle mSavedStateHandle;
    private final LeaveAndDeleteHandler mLeaveAndDeleteHandler;
    private final JoinGroupHandler mJoinGroupHandler;
    private final GroupRepository mGroupRepository;

    @Inject
    public GroupDetailViewModel(GroupRepository groupRepository, UserRepository userRepository, SavedStateHandle savedStateHandle) {
        mUserRepository = userRepository;
        mSavedStateHandle = savedStateHandle;
        mGroupRepository = groupRepository;
        mLeaveAndDeleteHandler = new LeaveAndDeleteHandler(groupRepository);
        mJoinGroupHandler = new JoinGroupHandler(groupRepository);
    }

    public LiveData<Resource<Members>> getMembers(String url, long uid) {
        return mGroupRepository.getGroupMembers(url, uid);
    }

    public void deleteGroup(PrivateGroup group) {
        mLeaveAndDeleteHandler.deleteGroup(group);
    }

    public LiveData<LoadState<Boolean>> deleteLeaveState() {
        return mLeaveAndDeleteHandler.getProcessState();
    }

    public void relogin() {
        mUserRepository.relogin();
    }

    public void addMember(String url, long groupId, long uid) {
        mJoinGroupHandler.addMember(url, groupId, uid);
    }

    public LiveData<LoadState<Boolean>> joinState() {
        return mJoinGroupHandler.getProcessState();
    }

    public void leaveGroup(String url, long groupId, long uid) {
        mLeaveAndDeleteHandler.leaveGroup(url, groupId, uid);
    }

    static class LeaveAndDeleteHandler extends ProcessHandler<Boolean> {
        private final GroupRepository repository;

        public LeaveAndDeleteHandler(GroupRepository repository) {
            super();
            this.repository = repository;
        }

        void deleteGroup(PrivateGroup group) {
            unregister();
            data = repository.deleteGroup(group);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }

        void leaveGroup(String url, long groupId, long uid) {
            unregister();
            data = repository.leaveGroup(url, groupId, uid);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }

    static class JoinGroupHandler extends ProcessHandler<Boolean> {
        private final GroupRepository repository;

        public JoinGroupHandler(GroupRepository repository) {
            super();
            this.repository = repository;
        }

        void addMember(String url, long groupId, long uid) {
            unregister();
            data = repository.addMember(url, groupId, uid);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }
}
