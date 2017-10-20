package com.zhihu.matisse;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class SelectedItem implements Parcelable {

    private final Uri rawUri;
    private final String rawAbsolutePath;
    private String newAbsolutePath;

    public SelectedItem(Uri rawUri, String rawAbsolutePath) {
        this.rawUri = rawUri;
        this.rawAbsolutePath = rawAbsolutePath;
        this.newAbsolutePath = "";
    }

    public static ArrayList<SelectedItem> createList(List<Uri> uris, List<String> paths) {
        ArrayList<SelectedItem> itemList = new ArrayList<>();
        if (uris == null || paths == null || uris.size() != paths.size()) {
            return itemList;
        }
        for (int i = 0, length = uris.size(); i < length; i++) {
            itemList.add(new SelectedItem(uris.get(i), paths.get(i)));
        }
        return itemList;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SelectedItem)) return false;

        SelectedItem that = (SelectedItem) obj;
        return rawUri.equals(that.rawUri) && rawAbsolutePath.equals(that.rawAbsolutePath);
    }

    @Override
    public int hashCode() {
        int result = rawUri.hashCode();
        result = 31 * result + rawAbsolutePath.hashCode();
        return result;
    }

    protected SelectedItem(Parcel in) {
        rawUri = in.readParcelable(Uri.class.getClassLoader());
        rawAbsolutePath = in.readString();
        newAbsolutePath = in.readString();
    }

    public static final Creator<SelectedItem> CREATOR = new Creator<SelectedItem>() {
        @Override
        public SelectedItem createFromParcel(Parcel in) {
            return new SelectedItem(in);
        }

        @Override
        public SelectedItem[] newArray(int size) {
            return new SelectedItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(rawUri, flags);
        dest.writeString(rawAbsolutePath);
        dest.writeString(newAbsolutePath);
    }

    public Uri getRawUri() {
        return rawUri;
    }

    public String getRawAbsolutePath() {
        return rawAbsolutePath;
    }

    public String getNewAbsolutePath() {
        return newAbsolutePath;
    }

    public void setNewAbsolutePath(String newAbsolutePath) {
        this.newAbsolutePath = newAbsolutePath;
    }

    public String getSimpleNewFileName() {
        String rawName = rawAbsolutePath.substring(rawAbsolutePath.lastIndexOf('/') + 1);
        return Integer.toHexString(rawUri.hashCode()) + "_" + rawName;
    }

    public static List<String> getRawPathList(List<SelectedItem> selectedItems) {
        List<String> rawPathList = new ArrayList<>();
        if (selectedItems != null && selectedItems.size() > 0) {
            for (SelectedItem item : selectedItems) {
                rawPathList.add(item.getRawAbsolutePath());
            }
        }
        return rawPathList;
    }

    public static List<String> getNewPathList(List<SelectedItem> selectedItems) {
        List<String> newPathList = new ArrayList<>();
        if (selectedItems != null && selectedItems.size() > 0) {
            for (SelectedItem item : selectedItems) {
                newPathList.add(item.getNewAbsolutePath());
            }
        }
        return newPathList;
    }

    public static List<Uri> getRawUriList(List<SelectedItem> selectedItems) {
        List<Uri> rawUriList = new ArrayList<>();
        if (selectedItems != null && selectedItems.size() > 0) {
            for (SelectedItem item : selectedItems) {
                rawUriList.add(item.getRawUri());
            }
        }
        return rawUriList;
    }
}
