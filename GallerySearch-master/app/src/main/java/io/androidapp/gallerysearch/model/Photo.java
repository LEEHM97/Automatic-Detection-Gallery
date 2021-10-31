package io.androidapp.gallerysearch.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

@Entity
public class Photo implements Parcelable {
    @PrimaryKey
    @NonNull
    private String path;
    private String uriString;
    private Long date;
    private String displayName;
    private Integer size;
    private LinkedHashSet<String> tags = new LinkedHashSet<>();

    public Photo(String path) {
        this.path = path;
    }

    protected Photo(Parcel in) {
        path = in.readString();
        uriString = in.readString();
        if (in.readByte() == 0) {
            date = null;
        } else {
            date = in.readLong();
        }
        displayName = in.readString();
        if (in.readByte() == 0) {
            size = null;
        } else {
            size = in.readInt();
        }
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LinkedHashSet<String> getTags() {
        return tags;
    }

    public void setTags(LinkedHashSet<String> tags) {
        this.tags = tags;
    }

    public void addTags(String tag) {tags.add(tag);}

    public Long getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = Long.parseLong(date);
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getUriString() {
        return uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(uriString);
        if (date == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(date);
        }
        dest.writeString(displayName);
        if (size == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(size);
        }
    }
}
