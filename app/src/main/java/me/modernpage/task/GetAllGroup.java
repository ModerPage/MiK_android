package me.modernpage.task;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.util.Constants;
import me.modernpage.entity.Group;
import me.modernpage.entity.GroupType;


public class GetAllGroup extends AsyncTask<Void,Void, List<Group>> implements LoadJSON.OnLoadJSON {
    private static final String TAG = "GetAllGroup";
    private List<Group> mGroups;
    private OnGetAllGroup mCallback;

    public interface OnGetAllGroup{
        void onGetAllGroupComplete(List<Group> groups);
    }

    public GetAllGroup(OnGetAllGroup callback) {
        mCallback = callback;
    }

    @Override
    protected List<Group> doInBackground(Void... voids) {

        LoadJSON loadJSON = new LoadJSON(this);
        loadJSON.runInSameThread(Constants.Network.GETALLGROUP_URL);

        return mGroups;
    }

    @Override
    protected void onPostExecute(List<Group> groups) {
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
                    long groupId = jsonGroup.getLong("groupId");
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
                Log.e(TAG, "onDownloadComplete: Error processing Json data " + e.getMessage(), e);
                status = DownloadStatus.FAILED_OR_EMPTY;
            }
        }
    }
}
