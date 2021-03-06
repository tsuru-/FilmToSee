package com.danandfranz.filmtosee;

/**
 * Created by Agilulfo on 19/02/2016.
 */


        import android.content.Intent;
        import android.os.Bundle;
        import android.support.design.widget.Snackbar;
        import android.support.v4.app.Fragment;
        import android.support.v7.widget.LinearLayoutManager;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.RelativeLayout;
        import android.widget.ScrollView;
        import android.widget.TextView;

        import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
        import com.nostra13.universalimageloader.core.DisplayImageOptions;
        import com.nostra13.universalimageloader.core.ImageLoader;
        import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.IOException;
        import java.util.ArrayList;

        import it.gmariotti.cardslib.library.internal.Card;
        import it.gmariotti.cardslib.library.recyclerview.internal.CardArrayRecyclerViewAdapter;
        import it.gmariotti.cardslib.library.recyclerview.view.CardRecyclerView;
        import okhttp3.Call;
        import okhttp3.Callback;
        import okhttp3.FormBody;
        import okhttp3.OkHttpClient;
        import okhttp3.RequestBody;
        import okhttp3.Response;


public class OneFragment extends Fragment{
    OkHttpClient client;

    private View InputFragmentView;
    private RelativeLayout relativeDetails;
    private RelativeLayout relativeFilmAdd;
    private Button btnLike;
    private Button btnUnlike;
    private Button btnAddFilm;

    boolean bool;

    //Attributi Film Details
    private TextView textViewLike;
    private TextView textViewUnlike;
    private TextView title;
    private TextView director;
    private TextView writer;
    private TextView year;
    private TextView genre;
    private TextView runtime;
    private TextView actors;
    private TextView plot;
    private TextView imdbVote;
    private String TAG = "OneFragment";
    private LinearLayout addMoveDetails;
    private ImageLoader imageLoader;
    private TextView addTitle;
    private TextView addYear;
    private DisplayImageOptions options;
    private ImageView poster;
    private Button btnSelectFilm;


    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        client = new OkHttpClient();
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this.getContext())
                // You can pass your own memory cache implementation
                //.diskCacheExtraOptions(1024, 1024, null)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();
        if (!imageLoader.isInited()) {
            imageLoader.init(config);
        }
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_launcher)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        InputFragmentView = inflater.inflate(
                R.layout.fragment_one, container, false);
        btnLike = (Button) InputFragmentView.findViewById(R.id.buttonLike);
        btnUnlike = (Button) InputFragmentView.findViewById(R.id.buttonUnlike);
        relativeDetails = (RelativeLayout) InputFragmentView.findViewById(R.id.RelativeFilmDetails);
        relativeFilmAdd = (RelativeLayout) InputFragmentView.findViewById(R.id.RelativeAddFilm);

        addMoveDetails = (LinearLayout) InputFragmentView.findViewById(R.id.add_movieDetails);
        textViewLike = (TextView) InputFragmentView.findViewById(R.id.textLikes);
        textViewUnlike = (TextView) InputFragmentView.findViewById(R.id.textUnlikes);
        title = (TextView) InputFragmentView.findViewById(R.id.title);
        director = (TextView) InputFragmentView.findViewById(R.id.director);
        writer = (TextView) InputFragmentView.findViewById(R.id.writer);
        year = (TextView) InputFragmentView.findViewById(R.id.year);
        genre = (TextView) InputFragmentView.findViewById(R.id.genre);
        runtime = (TextView) InputFragmentView.findViewById(R.id.runtime);
        actors = (TextView) InputFragmentView.findViewById(R.id.actors);
        plot = (TextView) InputFragmentView.findViewById(R.id.plot);
        imdbVote = (TextView) InputFragmentView.findViewById(R.id.imdbVote);

        btnAddFilm = (Button) InputFragmentView.findViewById(R.id.button_addMovie);
        btnSelectFilm = (Button) InputFragmentView.findViewById(R.id.button_selectMovie);


        addTitle = (TextView) InputFragmentView.findViewById(R.id.add_title);
        addYear = (TextView) InputFragmentView.findViewById(R.id.add_year);
        poster = (ImageView) InputFragmentView.findViewById(R.id.poster);
        return InputFragmentView;
    }

    public void setMovieDetails(final Film film) {

        if (!film.isAdd()) {
            relativeFilmAdd.setVisibility(View.GONE);

            relativeDetails.setVisibility(View.VISIBLE);
            title.setText(film.getTitle());
            director.setText(film.getDirector());
            writer.setText(film.getWriter());
            year.setText(film.getYear());
            genre.setText(film.getGenre());
            runtime.setText(film.getRuntime());
            actors.setText(film.getActors());
            plot.setText(film.getPlot());
            imdbVote.setText(film.getImdbScore());
            textViewLike = (TextView) InputFragmentView.findViewById(R.id.textLikes);
            textViewUnlike = (TextView) InputFragmentView.findViewById(R.id.textUnlikes);


            String groupId = ((InsideGroupActivity) getActivity()).getGroupRid();
            String userId = ((InsideGroupActivity) getActivity()).getUserRid();
            try {
                getLikes(groupId,film.getImdbID(),userId);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {

            relativeDetails.setVisibility(View.GONE);
            relativeFilmAdd.setVisibility(View.VISIBLE);

            btnSelectFilm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((InsideGroupActivity) getActivity()).addFilm();
                }
            });

            //Sostituisci con qualcosa!
        }
    }

    public  void getLikes(final String grouprid, final String idfilm, final String userrid) throws IOException {
        btnUnlike.setBackgroundResource(R.drawable.thumbs_down_unselected);
        btnLike.setBackgroundResource(R.drawable.thumbs_up_unselected);

        RequestBody body = new FormBody.Builder()
                .add("get", "likesOfFilm")
                .add("userrid", userrid)
                .add("groupRid", grouprid)
                .add("id_film", idfilm)
                .build();

        Util.post(body, client, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                String json = response.body().string();
                Log.d(TAG,json);

                try {
                    JSONObject likesObj = new JSONObject(json);
                    final int likeFilm = Integer.parseInt(likesObj.getString("likes"));
                    final int dislikeFilm = Integer.parseInt(likesObj.getString("unlikes"));
                    final boolean liked = likesObj.getString("liked").equalsIgnoreCase("true");
                    final boolean myLike = likesObj.getString("myLike").equalsIgnoreCase("true");



                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Scrivo i like e coloro gli sfondi
                            textViewLike.setText("" + likeFilm);
                            textViewUnlike.setText("" + dislikeFilm);
                            if(liked){ //Ho "votato"
                                if(!myLike){
                                    btnUnlike.setBackgroundResource(R.drawable.thumbs_down_selected);
                                    btnLike.setEnabled(false);
                                    btnUnlike.setEnabled(true);
                                    //Metto handler su Unlike per levare il mio unlike
                                    btnUnlike.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            btnLike.setEnabled(false);
                                            btnUnlike.setEnabled(false);

                                           try {
                                               btnUnlike.setBackgroundResource(R.drawable.thumbs_down_unselected);

                                               textViewUnlike.setText("" + (dislikeFilm-1));

                                               tolgoLike(userrid,grouprid,idfilm);

                                                //Aggiorno dentro

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                }else{
                                    btnLike.setBackgroundResource(R.drawable.thumbs_up_selected);
                                    btnLike.setEnabled(true);
                                    btnUnlike.setEnabled(false);
                                    //Metto handler su Like
                                    btnLike.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            btnLike.setEnabled(false);
                                            btnUnlike.setEnabled(false);
                                            try {
                                                btnLike.setBackgroundResource(R.drawable.thumbs_up_unselected);
                                                textViewLike.setText("" + (likeFilm - 1));
                                                tolgoLike(userrid,grouprid,idfilm);
                                                //Aggiorno dentro

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }else {
                                //Attivo entrambi i pulsanti
                                    btnLike.setEnabled(true);
                                    btnUnlike.setEnabled(true);
                                //Metto gli handler su entrambi
                                btnLike.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            btnLike.setEnabled(false);
                                            btnUnlike.setEnabled(false);
                                            textViewLike.setText("" + (likeFilm + 1));
                                            setLike(userrid, grouprid, idfilm, true);
                                            //Aggiorno dentro

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                btnUnlike.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            btnLike.setEnabled(false);
                                            btnUnlike.setEnabled(false);
                                            textViewUnlike.setText("" + (dislikeFilm+1));
                                            setLike(userrid, grouprid, idfilm, false);
                                            //Aggiorno dentro

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                            }
                        }
                    });

                } catch (Exception e){
                    e.printStackTrace();
                }

            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //Fix Crash
        relativeDetails = (RelativeLayout) InputFragmentView.findViewById(R.id.RelativeFilmDetails);
        relativeFilmAdd = (RelativeLayout) InputFragmentView.findViewById(R.id.RelativeAddFilm);

        textViewLike = (TextView) InputFragmentView.findViewById(R.id.textLikes);
        textViewUnlike = (TextView) InputFragmentView.findViewById(R.id.textUnlikes);
        title = (TextView) InputFragmentView.findViewById(R.id.title);
        director = (TextView) InputFragmentView.findViewById(R.id.director);
        writer = (TextView) InputFragmentView.findViewById(R.id.writer);
        year = (TextView) InputFragmentView.findViewById(R.id.year);
        genre = (TextView) InputFragmentView.findViewById(R.id.genre);
        runtime = (TextView) InputFragmentView.findViewById(R.id.runtime);
        actors = (TextView) InputFragmentView.findViewById(R.id.actors);
        plot = (TextView) InputFragmentView.findViewById(R.id.plot);
        imdbVote = (TextView) InputFragmentView.findViewById(R.id.imdbVote);
    }





    public void setLike(final String userRid, final String groupRid, final String id_film,boolean thumbs)throws IOException {
        Log.d("user",userRid);
        Log.d("groupRid",groupRid);
        Log.d("id_film",id_film);
        Log.d("bool_like", String.valueOf(thumbs));

        RequestBody body = new FormBody.Builder()
                .add("get", "likes")
                .add("userRid", userRid)
                .add("groupRid", groupRid)
                .add("id_film", id_film)
                .add("bool_like", String.valueOf(thumbs))
                .build();

        Util.post(body, client, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                //System.out.println(response.body().toString());
                String json = response.body().string();
                try {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getLikes(groupRid, id_film, userRid);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });
    }

    public void tolgoLike(final String userRid, final String groupRid, final String id_film)throws IOException {
        Log.d("user",userRid);
        Log.d("groupRid",groupRid);
        Log.d("id_film",id_film);

        RequestBody body = new FormBody.Builder()
                .add("get", "tolgoLikes")
                .add("userRid", userRid)
                .add("groupRid", groupRid)
                .add("id_film", id_film)

                .build();

        Util.post(body, client, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                //System.out.println(response.body().toString());
                String json = response.body().string();
                Log.d(TAG, "unlike" + json);
                try {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getLikes(groupRid, id_film, userRid);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });

    }



    public void setAddMovieDetails(final Film film) {
        ArrayList<Film> films = ((InsideGroupActivity) getActivity()).getFilms();
        if (!films.contains(film)) {

            addMoveDetails.setVisibility(View.VISIBLE);
            addTitle.setText(film.getTitle());
            addYear.setText(film.getYear());
            imageLoader.displayImage(film.getImageLink(), poster);
            Log.d(TAG, film.getImdbID());
            btnSelectFilm.setVisibility(View.GONE);
            btnAddFilm.setVisibility(View.VISIBLE);

            btnAddFilm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RequestBody body = new FormBody.Builder()
                            .add("get", "addFilm")
                            .add("id_film", film.getImdbID())
                            .add("groupRid", ((InsideGroupActivity) getActivity()).getGroupRid())
                            .build();

                    try {
                        Util.post(body, client, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!response.isSuccessful())
                                    throw new IOException("Unexpected code " + response);
                                String json = response.body().string();
                                //RIAVVIO l'activity
                                ((InsideGroupActivity) getActivity()).restartActivity();

                            }

                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }


            });
        }else{

            Snackbar.make(relativeFilmAdd, "Film already in group!", Snackbar.LENGTH_LONG)
                    .setAction("Close", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Perform anything for the action selected
                        }
                    }).setDuration(Snackbar.LENGTH_LONG).show();
            btnSelectFilm.setVisibility(View.VISIBLE);
            btnAddFilm.setVisibility(View.GONE);
            addMoveDetails.setVisibility(View.GONE);

        }
    }

}