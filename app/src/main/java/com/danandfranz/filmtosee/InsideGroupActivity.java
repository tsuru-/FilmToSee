package com.danandfranz.filmtosee;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Field;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;


public class InsideGroupActivity  extends AppCompatActivity {

    private static final String TAG = "InsideGroupActivity" ;

    private FeatureCoverFlow coverFlow;
    private CoverFlowAdapter coverFlowAdapter;
    private ArrayList<Film> films;
    public JSONObject groupData;
    private String ridFilm;
    private String ridUser;

    private RelativeLayout content_group_layout;

    OkHttpClient client;
    ProgressDialog progressDialog;
    private ViewPagerAdapter fragmentAdapter;
    SessionManager session;



    //SWIPE
    private Toolbar toolbarFragment;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    //END SWIPE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inside_group);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        progressDialog = new ProgressDialog(InsideGroupActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");

        session = new SessionManager(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();
        ridUser = user.get(SessionManager.KEY_RID);
        content_group_layout=(RelativeLayout) findViewById(R.id.content_group_layout);


        client = new OkHttpClient();

        try {
            groupData = new JSONObject(getIntent().getStringExtra("json"));
                  //setSubtitle(groupData.getJSONArray("members").length() + " members.");

        } catch (Exception e) {
            e.printStackTrace();
        }
        //TOOLBAR

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarGroup);
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        try {
            ab.setTitle(groupData.getString("name"));
            ab.setSubtitle(groupData.getJSONArray("members").length()+" members");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ///TOOLBAR PER MEMBERLIST
            View.OnClickListener members =new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        memberList();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            };
            Field subTitle = Toolbar.class.getDeclaredField("mSubtitleTextView");
            subTitle.setAccessible(true);
            final TextView barTitleView = (TextView) subTitle.get(toolbar);
            barTitleView.setOnClickListener(members);

            Field title = Toolbar.class.getDeclaredField("mTitleTextView");
            title.setAccessible(true);
            final TextView titleView = (TextView) title.get(toolbar);
            titleView.setOnClickListener(members);
            ///FINE TOOLBAR PER MEMBERLIST
        } catch (Exception e) {
            e.printStackTrace();
        }



        //END OF TOOLBAR SETTINGS
        //Scarico i dati json dei film e li aggiungo alla coverflow

        //swipe

        toolbarFragment = (Toolbar) findViewById(R.id.toolbarFragment);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        //end swipe

        try {
            ridFilm = groupData.getString("rid");
            progressDialog.show();
            getFilmJsonByGroup(ridFilm,ridUser);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public String getGroupRid(){
        try {
            return groupData.getString("rid");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public String getUserRid(){
        return ridUser;

    }



    private FeatureCoverFlow.OnScrollPositionListener onScrollListener() {
        return new FeatureCoverFlow.OnScrollPositionListener() {
            @Override
            public void onScrolledToPosition(int position)
            {
                Log.v(TAG, "position: " + position);
                Film film = coverFlowAdapter.getItem(position);
                if(film.isAdd()){
                    tabLayout.setVisibility(View.INVISIBLE);
                }else{
                    tabLayout.setVisibility(View.VISIBLE);

                }
                OneFragment one = (OneFragment) fragmentAdapter.getItem(0);
                one.setMovieDetails(film);
            }
            @Override
            public void onScrolling() {
                Log.i(TAG, "scrolling");
            }
        };
    }

    private void settingFilmData(JSONArray jsonObj) throws JSONException {
        films = new ArrayList<>();
        films.add(new Film()); //Aggiungo Film Vuoto
        for(int i = 0;i<jsonObj.length();i++){
            films.add(new Film(jsonObj.getJSONObject(i)));
        }

        //Faccio partire la coverflow

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //Creo la coverflow
                RelativeLayout parent = (RelativeLayout) findViewById(R.id.relativeLayout);


                LayoutInflater inflater = (LayoutInflater)   InsideGroupActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.coverflow, parent);
                coverFlow = (FeatureCoverFlow) v.findViewById(R.id.coverflow);
                //COVER FLOW
                //coverFlow = (FeatureCoverFlow) findViewById(R.id.coverflow);
                Log.d(TAG, "CIAO" + films.size());

                coverFlowAdapter = new CoverFlowAdapter(InsideGroupActivity.this, films);
                coverFlow.setAdapter(coverFlowAdapter);
                coverFlow.setOnScrollPositionListener(onScrollListener());
                //END OF COVER FLOW
                progressDialog.hide();

            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {



            case R.id.addUserGroup:
                addMember();
                return true;

            case R.id.aggiungi_membro:
                addMember();
                return true;

            case R.id.membri_gruppo:
                try {
                    memberList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return true;
            case R.id.lascia_gruppo:
                LeaveGroup();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.inside_group, menu);
        return true;
    }

    public void memberList() throws JSONException {
        Intent intent = new Intent(this, MembersGroup.class);
        intent.putExtra("json", groupData.toString());
        startActivity(intent);

    }
    //SWIPE
    private void setupViewPager(ViewPager viewPager) {
        fragmentAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        fragmentAdapter.addFragment(new OneFragment(), "MOVIE DETAILS");
        fragmentAdapter.addFragment(new TwoFragment(), "COMMENTS");
        viewPager.setAdapter(fragmentAdapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
    //END SWIPE

    private void addMember() {

        new MaterialDialog.Builder(this)
                .title("Add Member")
                .content("Please insert the username of the user you want to add")
                .theme(Theme.LIGHT)
                .positiveText("Add Member")
                .input("Username", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                        if (input.toString().length() > 3) {


                        } else {



                        }
                    }
                }).show();

    }

    private void LeaveGroup() {

        new MaterialDialog.Builder(this)
                .title("Confirm?")
                .content("Are you sure you want to leave and delete this Group?")
                .theme(Theme.LIGHT)
                .positiveText("Yes")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        leaveGroup(getGroupRid(),getUserRid());
                    }
                })
                .negativeText("Cancel")
                .icon(this.getResources().getDrawable(R.drawable.ic_delete_24dp))
                .show();

    }


    private void leaveGroup(String groupRid, String userRid){


        RequestBody body;
        body = new FormBody.Builder()
                .add("get","leaveGroup")
                .add("groupRid",groupRid)
                .add("userRid", userRid)
                .build();
        try {
            Util.post(body,client, new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override public void onResponse(Call call , Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    //System.out.println(response.body().toString());
                    try {
                        String json = response.body().string();
                        Log.d(TAG,json);
                        JSONObject jsonObj = new JSONObject(json);
                        String result = jsonObj.getString("result");
                        Log.d(TAG, result);
                        if (result.equalsIgnoreCase("success")) {
                          /* .runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //Handle UI here
                                    //findViewById(R.id.loading).setVisibility(View.GONE);

                                    Snackbar.make(content_group_layout, "Operation completed succesfully", Snackbar.LENGTH_LONG)
                                            .setAction("Close", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    // Perform anything for the action selected
                                                }
                                            }).setDuration(Snackbar.LENGTH_LONG).show();

                                }
                            });*/
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    private void getFilmJsonByGroup(String rid, String ridUser) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("get", "filmOfGroup")
                .add("groupRid", rid)
                .add("userrid", ridUser)
                .build();

        Util.post(body,client, new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call , Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                //System.out.println(response.body().toString());
                try {
                    String json = response.body().string();
                    Log.d(TAG,json);
                    JSONArray jsonObj = new JSONArray(json);

                    if (jsonObj.length()>= 0) {
                        settingFilmData(jsonObj);

                    } else {
                        //progressDialog.hide();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }
}
