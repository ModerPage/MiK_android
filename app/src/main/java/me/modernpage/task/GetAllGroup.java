package me.modernpage.task;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.modernpage.activity.R;
import me.modernpage.entity.Group;
import me.modernpage.entity.GroupType;

import static me.modernpage.activity.BaseActivity.GETALLGROUP_URL;

public class GetAllGroup extends AsyncTask<Void,Void, List<Group>> implements LoadJSON.OnLoadJSON {
    private static final String TAG = "GetAllGroup";
    private WeakReference<Context> mContext;
    private WeakReference<ProgressBar> mProgressBar;
    private List<Group> mGroups;
    private OnGetAllGroup mCallback;

    public interface OnGetAllGroup{
        void onGetAllGroupComplete(List<Group> groups);
    }

    public GetAllGroup(Context context, OnGetAllGroup callback) {
        mContext = new WeakReference<>(context);
        mCallback = callback;
    }

    @Override
    protected List<Group> doInBackground(Void... voids) {

        LoadJSON loadJSON = new LoadJSON(this);
        loadJSON.runInSameThread(GETALLGROUP_URL);

        return mGroups;
    }

    @Override
    protected void onPreExecute() {
        ConstraintLayout layout = ((Activity)mContext.get()).findViewById(R.id.group_layout);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.bottomToBottom = ConstraintSet.PARENT_ID;
        layoutParams.endToEnd = ConstraintSet.PARENT_ID;
        layoutParams.startToStart = ConstraintSet.PARENT_ID;
        layoutParams.topToTop = ConstraintSet.PARENT_ID;

        ProgressBar progressBar = new ProgressBar(mContext.get(),null,android.R.attr.progressBarStyle);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(View.INVISIBLE);
        layout.addView(progressBar);
        mProgressBar = new WeakReference<>(progressBar);
        ((Activity)mContext.get()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        mProgressBar.get().setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(List<Group> groups) {
        mProgressBar.get().setVisibility(View.INVISIBLE);
        ((Activity)mContext.get()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if(mCallback != null)
            mCallback.onGetAllGroupComplete(groups);
    }

    @Override
    public void onLoadJSONComplete(String data, DownloadStatus status) {
        if(status == DownloadStatus.OK) {
            mGroups = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONArray jsonArray = jsonObject.getJSONArray("groups");
                for(int i=0,length=jsonArray.length(); i<length; i++) {
                    JSONObject jsonGroup = jsonArray.getJSONObject(i);
                    int groupId = jsonGroup.getInt("groupId");
                    String groupName = jsonGroup.getString("groupName");
                    String groupImageURI = jsonGroup.getString("groupImageURI");
                    JSONObject groupTypeJSON = (JSONObject) jsonGroup.get("groupType");
                    GroupType groupType = new GroupType();
                    groupType.setGroupTypeId(groupTypeJSON.getInt("groupTypeId"));
                    groupType.setGroupTypeName(groupTypeJSON.getString("groupTypeName"));
                    mGroups.add(new Group(groupId,groupName,groupType,groupImageURI));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "onDownloadComplete: Error processing Json data " + e.getMessage());
                status = DownloadStatus.FAILED_OR_EMPTY;
            }
        }
    }
}
