package me.modernpage.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import de.hdodenhof.circleimageview.CircleImageView;
import me.modernpage.util.Constants;
import me.modernpage.entity.Group;
import me.modernpage.entity.UserEntity;
import me.modernpage.fragment.home.HomeFragment;
import me.modernpage.fragment.MapFragment;
import me.modernpage.fragment.group.GroupFragment;
import me.modernpage.fragment.post.PostFragment;
import me.modernpage.task.GetAllGroup;
import me.modernpage.task.ProcessUser;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ProcessUser.OnProcessUser, GetAllGroup.OnGetAllGroup {
    private static final String TAG = "MainActivity";
    private static final String ACTIVE_FRAGMENT_EXTRA = "active_fragment";


    private enum FragmentStatus {HOME_FRAGMENT, POST_FRAGMENT, MAP_FRAGMENT, GROUP_FRAGMENT}

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private UserEntity mCurrentUser;
    private String mCurrentUsername;
    private FragmentStatus mFragmentStatus;
    private CountDownLatch mCountDownLatch;
    private List<Group> mCurrentGroups;
    private ProgressBar mProgressBar;
    private BottomNavigationView mBottomNavigationView;

    private FragmentManager mFragmentManager;
    private Fragment mHomeFragment;
    private Fragment mPostFragment;
    private Fragment mMapFragment;
    private Fragment mGroupFragment;
    private Fragment mActiveFragment;

    private NavigationView mNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = findViewById(R.id.main_progress_bar);
        mCurrentUsername = getIntent().getStringExtra(Constants.User.USERNAME_EXTRA);
        mFragmentManager = getSupportFragmentManager();

        if (mFragmentManager.getFragments().isEmpty()) {

            mCountDownLatch = new CountDownLatch(2);

            GetAllGroup getAllGroup = new GetAllGroup(this);
            getAllGroup.execute();

            ProcessUser processUser = new ProcessUser(this);
            processUser.execute(mCurrentUsername);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "run: before await");
                        mCountDownLatch.await();
                        Log.d(TAG, "run: after await");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "runOnUiThread: called");
                                MainActivity.this.stopProgressBar();
                                mHomeFragment = HomeFragment.newInstance(mCurrentUser);
                                mMapFragment = new MapFragment();
                                mPostFragment = PostFragment.newInstance(mCurrentUser, mCurrentGroups);
                                mGroupFragment = GroupFragment.newInstance(mCurrentGroups);
                                mFragmentManager.beginTransaction().add(R.id.main_frag_container, mGroupFragment, "4").hide(mGroupFragment).commit();
                                mFragmentManager.beginTransaction().add(R.id.main_frag_container, mPostFragment, "3").hide(mPostFragment).commit();
                                mFragmentManager.beginTransaction().add(R.id.main_frag_container, mMapFragment, "2").hide(mMapFragment).commit();
                                mFragmentManager.beginTransaction().add(R.id.main_frag_container, mHomeFragment, "1").hide(mHomeFragment).commit();
                                mActiveFragment = mHomeFragment;
                                mFragmentStatus = FragmentStatus.HOME_FRAGMENT;
                                mFragmentManager.beginTransaction().show(mActiveFragment).commit();
                            }
                        });

                    } catch (InterruptedException e) {
                        Log.e(TAG, "run: InterruptedException " + e.getMessage(), e);
                    }
                }
            });

            startProgressBar();
            thread.start();
        } else {
            mHomeFragment = mFragmentManager.findFragmentByTag("1");
            mMapFragment = mFragmentManager.findFragmentByTag("2");
            mPostFragment = mFragmentManager.findFragmentByTag("3");
            mGroupFragment = mFragmentManager.findFragmentByTag("4");
        }


        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.main_bottom_nav);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.nav_open_drawer,
                R.string.nav_close_drawer);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_nav_drawer_toggle);
        }

        mNavigationView = findViewById(R.id.nav_view);
        View header = mNavigationView.getHeaderView(0);
        header.findViewById(R.id.nav_header_profile_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(MainActivity.this, SettingsActivity.class);
                intent.putExtra(Constants.User.CURRENT_USER_EXTRA, mCurrentUser);
                startActivity(intent);
            }
        });

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.nav_profile_settings:
                        intent = new Intent(MainActivity.this, SettingsActivity.class);
                        intent.putExtra(Constants.User.CURRENT_USER_EXTRA, mCurrentUser);
                        startActivity(intent);
                        break;

                    case R.id.nav_profile_helpfeed:
                        intent = new Intent(MainActivity.this, HelpFeedbackActivity.class);
                        intent.putExtra(Constants.User.CURRENT_USER_EXTRA, mCurrentUser);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });


        Log.d(TAG, "onCreate: ends");
    }

    public void moveFragment(Fragment source, Fragment dest, int menu) {
        mFragmentManager.beginTransaction().hide(source).show(dest).commit();
        mBottomNavigationView.getMenu().findItem(menu).setChecked(true);
        mActiveFragment = dest;

        if (mHomeFragment == dest)
            mFragmentStatus = FragmentStatus.HOME_FRAGMENT;
        else if (mMapFragment == dest)
            mFragmentStatus = FragmentStatus.MAP_FRAGMENT;
        else if (mPostFragment == dest)
            mFragmentStatus = FragmentStatus.POST_FRAGMENT;
        else if (mGroupFragment == dest)
            mFragmentStatus = FragmentStatus.GROUP_FRAGMENT;

    }

    public void startProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);

        // make the main window untouchable while showing progress bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void stopProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        // make the main window touchable after progress bar is gone
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: called");
        mFragmentStatus = (FragmentStatus) savedInstanceState.getSerializable(ACTIVE_FRAGMENT_EXTRA);
        if (mFragmentStatus != null) {
            if (mFragmentStatus == FragmentStatus.HOME_FRAGMENT)
                mActiveFragment = mFragmentManager.findFragmentByTag("1");
            else if (mFragmentStatus == FragmentStatus.GROUP_FRAGMENT)
                mActiveFragment = mFragmentManager.findFragmentByTag("4");
            else if (mFragmentStatus == FragmentStatus.POST_FRAGMENT)
                mActiveFragment = mFragmentManager.findFragmentByTag("3");
            else if (mFragmentStatus == FragmentStatus.MAP_FRAGMENT)
                mActiveFragment = mFragmentManager.findFragmentByTag("2");
        }

        mCurrentGroups = (ArrayList<Group>) savedInstanceState.getSerializable(Constants.User.GROUP_EXTRA);
        mCurrentUser = (UserEntity) savedInstanceState.getSerializable(Constants.User.CURRENT_USER_EXTRA);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: called");

        Log.d(TAG, "onResume: mFragmentManager size:" + mFragmentManager.getFragments().size());
        if (mFragmentManager.getFragments().size() == 0) {


        }


        TextView following_count =(TextView) mNavigationView.getMenu().findItem(R.id.nav_profile_following).getActionView();
        following_count.setGravity(Gravity.CENTER_VERTICAL);
        following_count.setTypeface(null, Typeface.BOLD);
        following_count.setText("0");

        TextView followers_count = (TextView) mNavigationView.getMenu().findItem(R.id.nav_profile_followers).getActionView();
        followers_count.setGravity(Gravity.CENTER_VERTICAL);
        followers_count.setTypeface(null, Typeface.BOLD);
        followers_count.setText("0");

        TextView posts_count = (TextView) mNavigationView.getMenu().findItem(R.id.nav_profile_posts).getActionView();
        posts_count.setGravity(Gravity.CENTER_VERTICAL);
        posts_count.setTypeface(null, Typeface.BOLD);
        posts_count.setText("0");
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: called");

        outState.putSerializable(ACTIVE_FRAGMENT_EXTRA, mFragmentStatus);
        outState.putSerializable(Constants.User.GROUP_EXTRA, (ArrayList<Group>) mCurrentGroups);
        outState.putSerializable(Constants.User.CURRENT_USER_EXTRA, mCurrentUser);

        for (Fragment f : mFragmentManager.getFragments())
            mFragmentManager.beginTransaction().hide(f).commit();

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                mFragmentManager.beginTransaction().hide(mActiveFragment).show(mHomeFragment).commit();
                mActiveFragment = mHomeFragment;
                mFragmentStatus = FragmentStatus.HOME_FRAGMENT;
                return true;
            case R.id.nav_map:
                if (mFragmentStatus == FragmentStatus.HOME_FRAGMENT)
                    ((HomeFragment) mActiveFragment).stopPlay();
                mFragmentManager.beginTransaction().hide(mActiveFragment).show(mMapFragment).commit();
                mActiveFragment = mMapFragment;
                mFragmentStatus = FragmentStatus.MAP_FRAGMENT;
                return true;
            case R.id.nav_post:
                if (mFragmentStatus == FragmentStatus.HOME_FRAGMENT)
                    ((HomeFragment) mActiveFragment).stopPlay();
                mFragmentManager.beginTransaction().hide(mActiveFragment).show(mPostFragment).commit();
                mActiveFragment = mPostFragment;
                mFragmentStatus = FragmentStatus.POST_FRAGMENT;
                return true;
            case R.id.nav_group:
                if (mFragmentStatus == FragmentStatus.HOME_FRAGMENT)
                    ((HomeFragment) mActiveFragment).stopPlay();
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
        inflater.inflate(R.menu.notification_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menu_notify) {

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public void onProcessUserFinished(UserEntity user) {
        Log.d(TAG, "onProcessUserFinished: called");
        mCurrentUser = user;
        mCountDownLatch.countDown();
        View headerView = mNavigationView.getHeaderView(0);
        CircleImageView imageView = headerView.findViewById(R.id.nav_header_profile_image);
        if (user.getImageUri() != null) {
            Picasso.get().load(Constants.Network.BASE_URL + user.getImageUri())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(imageView);
        }

        TextView fullname = headerView.findViewById(R.id.nav_header_fullname);
        fullname.setText(user.getFullname());


        TextView username = headerView.findViewById(R.id.nav_header_username);
        username.setText(user.getUsername());

        TextView email = headerView.findViewById(R.id.nav_header_email);
        email.setText(user.getEmail());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: called");
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode);
        mActiveFragment.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void clickLogout(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onGetAllGroupComplete(List<Group> groups) {
        Log.d(TAG, "onGetAllGroupComplete: called");
        mCurrentGroups = groups;
        mCountDownLatch.countDown();
    }
}
