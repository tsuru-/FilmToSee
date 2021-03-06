package com.danandfranz.filmtosee;


import org.json.JSONObject;

public class Film {

    private String title;
    private int imageSource;
    private String imageLink;
    private String year;
    private String runtime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Film film = (Film) o;

        return imdbID.equals(film.imdbID);

    }

    @Override
    public int hashCode() {
        return imdbID.hashCode();
    }

    private String genre;
    private String director;
    private String writer;
    private String actors;
    private String plot;
    private String imdbScore;
    private String imdbID;
    private int like;
    private int dislike;
    private boolean myLike;
    private String liked;
    private boolean add;




    

    public Film() {
        this.title = "Add Film";
        this.imageLink = "assets://addfilmcover.png";
        this.add = true;
    }
    public Film(String title,String year) {
        this.title = title;
        this.year = year;
        this.add = false;
    }

    public Film(JSONObject filmJson,boolean autocomplete){
        if(autocomplete){
            try {
                title = filmJson.getString("Title");
                year = filmJson.getString("Year");
                imdbID=filmJson.getString("imdbID");
                imageLink = filmJson.getString("Poster");
                this.add = false;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            new Film(filmJson);
        }
    }
    public Film(JSONObject filmJson){
        try {
            title = filmJson.getString("Title");
            year = filmJson.getString("Year");
            runtime =filmJson.getString("Runtime");
            genre =filmJson.getString("Genre");
            director = filmJson.getString("Director");
            writer = filmJson.getString("Writer");
            actors =filmJson.getString("Actors");
            plot =filmJson.getString("Plot");
            imdbScore  =filmJson.getString("imdbRating");
            imdbID=filmJson.getString("imdbID");
            imageLink = filmJson.getString("Poster");

            like= filmJson.getInt("likes");
            dislike= filmJson.getInt("unlikes");
            myLike=Boolean.parseBoolean(filmJson.getString("myLike"));
            liked= filmJson.getString("liked");
           //comm

            this.add = false;
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getTitle() {
        return title;
    }

    public int getImageSource() {
        return imageSource;
    }

    public String getImageLink() {
        String link = imageLink;
        if(link.equalsIgnoreCase("N/A")){
            link = "assets://noposter.png";
        }

        return link;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getImdbScore() {
        return imdbScore;
    }

    public void setImdbScore(String imdbScore) {
        this.imdbScore = imdbScore;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getDislike() {
        return dislike;
    }

    public void setDislike(int dislike) {
        this.dislike = dislike;
    }

    public boolean isAdd() {
        return add;
    }

    public boolean isMyLike() {
        return myLike;
    }

    public void setMyLike(boolean myLike) { this.myLike=myLike;}


    public String isLiked() {
        return liked;
    }

    public void setIsLiked(String liked) {
        this.liked=liked;

    }
}