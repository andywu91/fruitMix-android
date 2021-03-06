package com.winsun.fruitmix.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/16.
 */
public class MediaShare implements Parcelable {

    private int id;
    private String uuid;
    private String creatorUUID;
    private String time;
    private String title;
    private String desc;
    private List<String> imageDigests;
    private List<String> viewer;
    private List<String> maintainer;
    private boolean isAlbum;
    private boolean isArchived;
    private String date;
    private String coverImageDigest;
    private boolean isLocked;

    public MediaShare() {
        imageDigests = new ArrayList<>();
        viewer = new ArrayList<>();
        maintainer = new ArrayList<>();

    }

    protected MediaShare(Parcel in) {
        id = in.readInt();
        uuid = in.readString();
        creatorUUID = in.readString();
        time = in.readString();
        title = in.readString();
        desc = in.readString();
        imageDigests = in.createStringArrayList();
        viewer = in.createStringArrayList();
        maintainer = in.createStringArrayList();
        isAlbum = in.readByte() != 0;
        isArchived = in.readByte() != 0;
        date = in.readString();
        coverImageDigest = in.readString();
        isLocked = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(uuid);
        dest.writeString(creatorUUID);
        dest.writeString(time);
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeStringList(imageDigests);
        dest.writeStringList(viewer);
        dest.writeStringList(maintainer);
        dest.writeByte((byte) (isAlbum ? 1 : 0));
        dest.writeByte((byte) (isArchived ? 1 : 0));
        dest.writeString(date);
        dest.writeString(coverImageDigest);
        dest.writeByte((byte) (isLocked ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaShare> CREATOR = new Creator<MediaShare>() {
        @Override
        public MediaShare createFromParcel(Parcel in) {
            return new MediaShare(in);
        }

        @Override
        public MediaShare[] newArray(int size) {
            return new MediaShare[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCreatorUUID() {
        return creatorUUID;
    }

    public void setCreatorUUID(String creatorUUID) {
        this.creatorUUID = creatorUUID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getViewer() {
        return viewer;
    }

    public void setViewer(List<String> viewer) {
        this.viewer = viewer;
    }

    public List<String> getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(List<String> maintainer) {
        this.maintainer = maintainer;
    }

    public List<String> getImageDigests() {
        return imageDigests;
    }

    public void setImageDigests(List<String> imageDigests) {
        this.imageDigests = imageDigests;
    }

    public String getCoverImageDigest() {
        return coverImageDigest;
    }

    public void setCoverImageDigest(String coverImageDigest) {
        this.coverImageDigest = coverImageDigest;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isAlbum() {
        return isAlbum;
    }

    public void setAlbum(boolean album) {
        isAlbum = album;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public MediaShare cloneMyself() {
        MediaShare cloneMediaShare = new MediaShare();
        cloneMediaShare.setUuid(getUuid());
        cloneMediaShare.setCreatorUUID(getCreatorUUID());
        cloneMediaShare.setTime(getTime());
        cloneMediaShare.setTitle(getTitle());
        cloneMediaShare.setDesc(getDesc());
        cloneMediaShare.setImageDigests(getImageDigests());
        cloneMediaShare.setViewer(getViewer());
        cloneMediaShare.setMaintainer(getMaintainer());
        cloneMediaShare.setAlbum(isAlbum());
        cloneMediaShare.setArchived(isArchived());
        cloneMediaShare.setDate(getDate());
        cloneMediaShare.setCoverImageDigest(getCoverImageDigest());
        cloneMediaShare.setLocked(isLocked());

        return cloneMediaShare;
    }

}
