package me.modernpage.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityMainBinding;
import me.modernpage.activity.databinding.LayoutNavHeaderBinding;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.ui.BaseActivity;
import me.modernpage.ui.BaseDialog;
import me.modernpage.ui.dialog.HelpAndFeedbackDialog;
import me.modernpage.ui.userload.UserLoadActivity;
import me.modernpage.ui.fragment.MapFragment;
import me.modernpage.ui.fragment.group.GroupFragment;
import me.modernpage.ui.fragment.post.PostFragment;
import me.modernpage.ui.fragment.postlist.PostListFragment;
import me.modernpage.ui.login.LoginActivity;
import me.modernpage.ui.notification.NotificationActivity;
import me.modernpage.ui.post.PostActivity;
import me.modernpage.ui.settings.SettingsActivity;
import me.modernpage.util.Constants;
import me.modernpage.util.ImagePopUpHelper;
import me.modernpage.util.Status;

@AndroidEntryPoint
public class MainActivity extends BaseActivity<ActivityMainBinding> implements
        BottomNavigationView.OnNavigationItemSelectedListener,
        PostFragment.OnSaveClicked,
        BaseDialog.DialogEvents {

    @Inject
    ImagePopUpHelper mImagePopUpHelper;

    private static final String TAG = "MainActivity";

    @Override
    public void onSaveClicked() {
        mFragmentManager.beginTransaction().hide(mActiveFragment).show(mHomeFragment).commit();
        dataBinding.mainBottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
        mActiveFragment = mHomeFragment;
        mFragmentStatus = FragmentStatus.HOME_FRAGMENT;
    }

    @Override
    public void onPositiveDialogResult(int dialogId) {

    }

    @Override
    public void onNegativeDialogResult(int dialogId) {

    }

    private enum FragmentStatus {HOME_FRAGMENT, POST_FRAGMENT, MAP_FRAGMENT, GROUP_FRAGMENT}

    private DrawerLayout mDrawerLayout;
    private long mUID;
    private FragmentStatus mFragmentStatus;

    private FragmentManager mFragmentManager;
    private Fragment mHomeFragment;
    private Fragment mPostFragment;
    private Fragment mMapFragment;
    private Fragment mGroupFragment;
    private Fragment mActiveFragment;

    MainViewModel mMainViewModel;
    LayoutNavHeaderBinding navHeaderBinding;


    @Override
    public int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        Bundle args = getIntent().getExtras();

        if (args != null) {
            mUID = args.getLong(Profile.class.getSimpleName());
            if (mUID == 0)
                throw new IllegalArgumentException("UID not present in bundle.");
        } else {
            throw new IllegalArgumentException("UID must be passed in bundle.");
        }

        mMainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mMainViewModel.setUid(mUID);
        dataBinding.setLifecycleOwner(this);
        dataBinding.setViewModel(mMainViewModel);

        // set navigation view's header layout
        navHeaderBinding = DataBindingUtil.inflate(getLayoutInflater(),
                R.layout.layout_nav_header, dataBinding.navView, false, dataBindingComponent);
        dataBinding.navView.addHeaderView(navHeaderBinding.getRoot());

        mImagePopUpHelper.enablePopUpOnClick(this, navHeaderBinding.navHeaderProfileImage);

        mMainViewModel.getUserData().observe(this, userResource -> {
            if (userResource != null) {
                if (userResource.status == Status.SUCCESS) {
                    navHeaderBinding.setUser(userResource.data);
                } else if (userResource.status == Status.LOGOUT) {
                    Snackbar.make(dataBinding.getRoot(), userResource.message, Snackbar.LENGTH_INDEFINITE)
                            .setAction("LOGOUT", view -> {
                                mMainViewModel.relogin();
                                logout();
                            }).show();
                } else if (userResource.status == Status.ERROR) {
                    Snackbar.make(dataBinding.getRoot(), userResource.message, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            mHomeFragment = PostListFragment.newInstance(mUID, Constants.Network.ENDPOINT_POSTS);
            mFragmentManager.beginTransaction().add(R.id.main_frag_container, mHomeFragment, "1").show(mHomeFragment).commit();
            mActiveFragment = mHomeFragment;
        } else {
            mHomeFragment = mFragmentManager.findFragmentByTag("1");
            mMapFragment = mFragmentManager.findFragmentByTag("2");
            mPostFragment = mFragmentManager.findFragmentByTag("3");
            mGroupFragment = mFragmentManager.findFragmentByTag("4");
            mFragmentStatus = (FragmentStatus) savedInstanceState.
                    getSerializable(FragmentStatus.class.getSimpleName());

            if (mFragmentStatus != null) {
                switch (mFragmentStatus) {
                    case MAP_FRAGMENT:
                        mActiveFragment = mMapFragment;
                        break;
                    case POST_FRAGMENT:
                        mActiveFragment = mPostFragment;
                        break;
                    case GROUP_FRAGMENT:
                        mActiveFragment = mGroupFragment;
                        break;
                    default:
                        mActiveFragment = mHomeFragment;
                        break;
                }
                mFragmentManager.beginTransaction().show(mActiveFragment).commit();
            }
        }

        BottomNavigationView bottomNavigationView = dataBinding.mainBottomNav;
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        Toolbar toolbar = (Toolbar) dataBinding.toolbar;
        setSupportActionBar(toolbar);

        mDrawerLayout = dataBinding.mainDrawerLayout;
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_nav_drawer_toggle);
        }

        NavigationView navigationView = dataBinding.navView;
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                Profile user = navHeaderBinding.getUser();
                if (user == null)
                    return false;

                switch (item.getItemId()) {
                    case R.id.nav_profile_settings:
                        intent = new Intent(MainActivity.this, SettingsActivity.class);
                        intent.putExtra(Profile.class.getSimpleName(), mUID);
                        startActivity(intent);
                        break;
                    case R.id.nav_profile_helpfeed:
                        HelpAndFeedbackDialog helpAndFeedbackDialog = new HelpAndFeedbackDialog();
                        helpAndFeedbackDialog.show(getSupportFragmentManager(), null);
                        break;
                    case R.id.nav_profile_logout:
                        mMainViewModel.logout();
                        break;
                    case R.id.nav_profile_posts:
                        intent = new Intent(MainActivity.this, PostActivity.class);
                        intent.putExtra(Profile.class.getSimpleName(), mUID);
                        intent.putExtra(LoadResult.class.getSimpleName(), user._posts());
                        startActivity(intent);
                        break;
                    case R.id.nav_profile_following:
                        intent = new Intent(MainActivity.this, UserLoadActivity.class);
                        intent.putExtra(Profile.class.getSimpleName(), mUID);
                        intent.putExtra(LoadResult.class.getSimpleName(), user._following());
                        intent.putExtra(UserLoadActivity.EXTRA_TITLE, "Following");
                        startActivity(intent);
                        break;
                    case R.id.nav_profile_followers:
                        intent = new Intent(MainActivity.this, UserLoadActivity.class);
                        intent.putExtra(Profile.class.getSimpleName(), mUID);
                        intent.putExtra(LoadResult.class.getSimpleName(), user._followers());
                        intent.putExtra(UserLoadActivity.EXTRA_TITLE, "Followers");
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });
        TextView posts_count = (TextView) navigationView.getMenu().findItem(R.id.nav_profile_posts).getActionView();
        posts_count.setGravity(Gravity.CENTER_VERTICAL);
        posts_count.setTypeface(null, Typeface.BOLD);
        mMainViewModel.getPosts().observe(this, posts -> {
            if (posts != null) {
                if (posts.status == Status.SUCCESS) {
                    posts_count.setText(posts.data == null ? "0" : String.valueOf(posts.data.getTotal()));
                } else if (posts.status == Status.LOGOUT) {
                    Snackbar.make(dataBinding.getRoot(), posts.message, Snackbar.LENGTH_INDEFINITE)
                            .setAction("LOGOUT", view -> {
                                mMainViewModel.relogin();
                                logout();
                            }).show();
                } else if (posts.status == Status.ERROR) {
                    Snackbar.make(dataBinding.getRoot(), posts.message, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        TextView followers_count = (TextView) navigationView.getMenu().findItem(R.id.nav_profile_followers).getActionView();
        followers_count.setGravity(Gravity.CENTER_VERTICAL);
        followers_count.setTypeface(null, Typeface.BOLD);
        mMainViewModel.getFollowers().observe(this, followers -> {
            if (followers != null) {
                if (followers.status == Status.SUCCESS) {
                    followers_count.setText(followers.data == null ? "0" : String.valueOf(followers.data.getTotal()));
                } else if (followers.status == Status.LOGOUT) {
                    Snackbar.make(dataBinding.getRoot(), followers.message, Snackbar.LENGTH_INDEFINITE)
                            .setAction("LOGOUT", view -> {
                                mMainViewModel.relogin();
                                logout();
                            }).show();
                } else if (followers.status == Status.ERROR) {
                    Snackbar.make(dataBinding.getRoot(), followers.message, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        TextView following_count = (TextView) navigationView.getMenu().findItem(R.id.nav_profile_following).getActionView();
        following_count.setGravity(Gravity.CENTER_VERTICAL);
        following_count.setTypeface(null, Typeface.BOLD);
        mMainViewModel.getFollowing().observe(this, following -> {
            if (following != null) {
                if (following.status == Status.SUCCESS) {
                    following_count.setText(following.data == null ? "0" : String.valueOf(following.data.getTotal()));
                } else if (following.status == Status.LOGOUT) {
                    Snackbar.make(dataBinding.getRoot(), following.message, Snackbar.LENGTH_INDEFINITE)
                            .setAction("LOGOUT", view -> {
                                mMainViewModel.relogin();
                                logout();
                            }).show();
                } else if (following.status == Status.ERROR) {
                    Snackbar.make(dataBinding.getRoot(), following.message, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        mMainViewModel.logoutState().observe(this, state -> {
            if (state == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified()) {
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", view -> {
                                    mMainViewModel.relogin();
                                    logout();
                                }).show();
                    }
                }
                Boolean data = state.getData();
                if (data != null && data) {
                    logout();
                }
            }
            dataBinding.executePendingBindings();
        });
        Log.d(TAG, "onCreate: ends");

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                mFragmentManager.beginTransaction().hide(mActiveFragment).show(mHomeFragment).commit();
                mActiveFragment = mHomeFragment;
                mFragmentStatus = FragmentStatus.HOME_FRAGMENT;
                return true;
            case R.id.nav_map:
                if (mMapFragment == null) {
                    mMapFragment = new MapFragment();
                    mFragmentManager.beginTransaction().add(R.id.main_frag_container, mMapFragment, "2").commit();
                }
                mFragmentManager.beginTransaction().hide(mActiveFragment).show(mMapFragment).commit();
                mActiveFragment = mMapFragment;
                mFragmentStatus = FragmentStatus.MAP_FRAGMENT;
                return true;
            case R.id.nav_post:
                if (mPostFragment == null) {
                    mPostFragment = PostFragment.newInstance(mUID, Constants.Network.ENDPOINT_POSTS);
                    mFragmentManager.beginTransaction().add(R.id.main_frag_container, mPostFragment, "3").commit();
                }
                mFragmentManager.beginTransaction().hide(mActiveFragment).show(mPostFragment).commit();
                mActiveFragment = mPostFragment;
                mFragmentStatus = FragmentStatus.POST_FRAGMENT;
                return true;
            case R.id.nav_group:
                if (mGroupFragment == null) {
                    mGroupFragment = GroupFragment.newInstance(mUID);
                    mFragmentManager.beginTransaction().add(R.id.main_frag_container, mGroupFragment, "4").commit();
                }
                mFragmentManager.beginTransaction().hide(mActiveFragment).show(mGroupFragment).commit();
                mActiveFragment = mGroupFragment;
                mFragmentStatus = FragmentStatus.GROUP_FRAGMENT;
                return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notification_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_notify) {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(FragmentStatus.class.getSimpleName(), mFragmentStatus);
        super.onSaveInstanceState(outState);
    }

    public void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
