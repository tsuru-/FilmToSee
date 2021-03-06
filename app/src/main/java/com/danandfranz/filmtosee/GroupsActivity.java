package com.danandfranz.filmtosee;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // Session Manager Class
    SessionManager session;
    private static final String TAG = "GroupsActivity";
    LetterTileProvider tileProvider;
    OkHttpClient client;
    ProgressDialog progressDialog;

int numFilms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

       // try{ getActionBar().setTitle("Groups");}catch (Exception e){} //Cambio nome  activity "in sicurezza"

        //CONTROLLO LOGIN
        session = new SessionManager(getApplicationContext());
        //Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();
        /**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in
         * */
        session.checkLogin();
        tileProvider = new LetterTileProvider(this,R.array.letter_tile_colors);
        // get user data from session
        HashMap<String, String> user = session.getUserDetails();

        // name
        String name = user.get(SessionManager.KEY_NAME);
        // email
        String email = user.get(SessionManager.KEY_EMAIL);
        String rid = user.get(SessionManager.KEY_RID);
        if(name==null){
            session.logoutUser();
        }
        Log.d(TAG,"RID: "+rid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Log.d(TAG, "Name " + name + " Email " + email);
        try {
            setAvatar(name, email);
        }catch (Exception e){

        }
        // FINE LOGIN: Alla fine del login sostituisco avatar e stringhe
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    createNewGroup();
                }catch (Exception e){
                   // e.printStackTrace();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //CARTE
        client = new OkHttpClient();
        try {
            progressDialog = new ProgressDialog(GroupsActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            getCards(name);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void getCards(String username) {


        RequestBody body;
        body = new FormBody.Builder()
                .add("get","groupOfUserAndUsersOfGroup")
                .add("username", username )
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
                        /*
                        String result = jsonObj.getString("result");
                        String rid = jsonObj.getString("id");
                        String username = jsonObj.getString("username");
*/
                        final JSONArray groups = jsonObj.getJSONArray("groups");

                        GroupsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Handle UI here
                                //findViewById(R.id.loading).setVisibility(View.GONE);


                                try {
                                    setCards(groups);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                progressDialog.hide();


                            }
                        });

                        } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setCards(JSONArray groups) throws JSONException {

        ArrayList<Card> cards = new ArrayList<Card>();

        for(int i = 0; i<groups.length();i++){

            String name = groups.getJSONObject(i).getString("name");
            JSONArray members = groups.getJSONObject(i).getJSONArray("members");
            String ridGroup= groups.getJSONObject(i).getString("rid");

            cards.add(createCard(name,groups.getJSONObject(i)));
        }

        CardArrayRecyclerViewAdapter mCardArrayAdapter = new CardArrayRecyclerViewAdapter(this, cards);
        //Staggered grid view
        CardRecyclerView mRecyclerView = (CardRecyclerView) this.findViewById(R.id.carddemo_recyclerview);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Set the empty view
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mCardArrayAdapter);
            //mCardArrayAdapter.notifyDataSetChanged();
        }
    }
    private Card createCard(String groupName, final JSONObject group) throws JSONException {
        //Create a Card
        Card card = new Card(this);
        String memstr = "";
        JSONArray members = group.getJSONArray("members");


        for ( int z = 0;z<members.length();z++){
            memstr+=" "+members.getString(z)+" ";


            if(z>9){
                memstr+="...";
                z=members.length();
            }

        }
        for ( int y = 0;y<members.length();y++){


            switch (y) {
                case 0:
                    card.setBackgroundResourceId(R.drawable.demo_card_selector_color5);
                    break;
                case 1:
                    card.setBackgroundResourceId(R.drawable.demo_card_selector_color4);
                    break;
                case 2:
                    card.setBackgroundResourceId(R.drawable.demo_card_selector_color3);
                    break;
                case 3:
                    card.setBackgroundResourceId(R.drawable.demo_card_selector_color2);
                    break;
                case 4:
                    card.setBackgroundResourceId(R.drawable.demo_card_selector_color1);
                    break;
                default:
                    card.setBackgroundResourceId(R.drawable.demo_card_selector_color_default);
                    break;
            }
        }

        card.setTitle(
                "Members:" + memstr + " "); //Aggiorna il numero
        //Create a CardHeader
        CardHeader header = new CardHeader(this);
        header.setTitle(groupName);
        //Add Header to card
        card.addCardHeader(header);
       /*
        header.setPopupMenu(R.menu.popupmain, new CardHeader.OnClickCardHeaderPopupMenuListener() {
            @Override
            public void onMenuItemClick(BaseCard card, MenuItem item) {
                Toast.makeText(GroupsActivity.this, "Click on " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });*/


        //Add ClickListener
        card.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Intent intent = new Intent(GroupsActivity.this, InsideGroupActivity.class);
                intent.putExtra("json",group.toString());
                startActivity(intent);

            }
        });

        return card;

    }
    private void setAvatar(String name, String email) {
//        View headerView = LayoutInflater.from(this).inflate(R.layout.nav_header_groups, null);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView=navigationView.getHeaderView(0);

        ImageView avatar = (ImageView) headerView.findViewById(R.id.avatar);
        TextView username = (TextView) headerView.findViewById(R.id.user);
        TextView mail = (TextView) headerView.findViewById(R.id.email);
        username.setText(name);
        mail.setText(email);
        //controlla se c'è l'avatar sul db e semmai lo scarica
        if(!avatarPresent()){
            final Resources res = getResources();
            final int tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);
            final RoundedAvatarDrawable letterTile = tileProvider.getCircleLetterTile(name, "key", tileSize, tileSize);
            avatar.setImageDrawable(letterTile);
        }
    }

    private boolean avatarPresent() {
        //interra funziona php avatar e lo scarica semmai ?!
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onRestart(){
        super.onRestart();
        Log.d(TAG,"RESTARTED");
        HashMap<String, String> user = session.getUserDetails();
        // name
        String name = user.get(SessionManager.KEY_NAME);

        try {
            progressDialog = new ProgressDialog(GroupsActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            getCards(name);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.new_group) {
            createNewGroup();
        } /*else if (id == R.id.invite_friend) {

        } else if (id == R.id.settings) {

            Intent intent = new Intent(this, InsideGroupActivity.class);
            startActivity(intent);

        } else if (id == R.id.faq) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://normandy.dmi.unipg.it/blockchainvis/Film/faq.html"));
            startActivity(browserIntent);

        }*/ else if (id == R.id.logout) {
            session.logoutUser();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createNewGroup() {

        new MaterialDialog.Builder(this)
                .title("Create new Group")
                .content("Name of the group")
                .theme(Theme.LIGHT)
                .input("Choose a good name!", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                        if (input.toString().length() > 3) {
                            Log.d(TAG, "call createGroupPost" + input.toString());
                            createGroupPost(input.toString());
                        } else {
                            NavigationView rootlayout = (NavigationView) findViewById(R.id.nav_view);

                            Snackbar.make(rootlayout, "Group name has to be atleast 4 character long!", Snackbar.LENGTH_LONG)
                                    .setAction("Close", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // Perform anything for the action selected
                                        }
                                    }).setDuration(Snackbar.LENGTH_LONG).show();

                        }
                    }
                }).show();

    }
    private void createGroupPost(String input){
        HashMap<String, String> user = session.getUserDetails();
        final String name = user.get(SessionManager.KEY_NAME);
        RequestBody body;
        body = new FormBody.Builder()
                .add("get","createGroup")
                .add("groupName", input)
                .add("username", name)
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
                            GroupsActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //Handle UI here
                                    //findViewById(R.id.loading).setVisibility(View.GONE);
                                    getCards(name);
                                }
                            });
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


    private void getGroupFilms(String groupRid){

        RequestBody body;
        body = new FormBody.Builder()
                .add("get", "filmOfGrroup")
                .add("groupRid",groupRid)
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
                        int films= jsonObj.getInt("films");
                        Log.d(TAG, result);
                        if (result.equalsIgnoreCase("success")) {
                                numFilms=films;
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

}
