package com.github.joaogalli.capstonepluris.model;

/**
 * Created by joao.galli on 20/01/2017.
 */

public class Post {

    private int _id;

    private String title;

    public Post() {
    }

    public Post(int _id, String title) {
        this._id = _id;
        this.title = title;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
