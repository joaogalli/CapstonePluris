package com.github.joaogalli.capstonepluris.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joaog on 14/01/2017.
 */
@IgnoreExtraProperties
public class Subreddit implements Parcelable {

    private String key;

    private String displayName;

    private String headerImg;

    private String title;

    public Subreddit() {
    }

    public Subreddit(String displayName, String title) {
        this.displayName = displayName;
        this.title = title;
    }

    public static final Parcelable.Creator<Subreddit> CREATOR = new Parcelable.Creator<Subreddit>() {

        @Override
        public Subreddit createFromParcel(Parcel parcel) {
            Subreddit subreddit = new Subreddit();
            subreddit.setKey(parcel.readString());
            subreddit.setDisplayName(parcel.readString());
            subreddit.setHeaderImg(parcel.readString());
            subreddit.setTitle(parcel.readString());
            return subreddit;
        }

        @Override
        public Subreddit[] newArray(int i) {
            return new Subreddit[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(key);
        parcel.writeString(displayName);
        parcel.writeString(headerImg);
        parcel.writeString(title);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("displayName", displayName);
        result.put("headerImg", headerImg);
        result.put("title", title);

        return result;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getHeaderImg() {
        return headerImg;
    }

    public void setHeaderImg(String headerImg) {
        this.headerImg = headerImg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
