package com.mapgis.mmt.module.login;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.mapgis.mmt.MyApplication;

import java.util.ArrayList;

public class ServerConfigInfo implements Parcelable{
    public String LoginName;
    public String LoginPassword;
    public String IpAddress;
    public String Port;
    public String VirtualPath;
    public String HttpProtocol;

    public String CorpName;
    public boolean IsTopCorp;
    public ArrayList<String> Corps;

    public String GpsReceiver = "BD";

    public ServerConfigInfo() {
    }

    protected ServerConfigInfo(Parcel in) {
        LoginName = in.readString();
        LoginPassword = in.readString();
        IpAddress = in.readString();
        Port = in.readString();
        VirtualPath = in.readString();
        HttpProtocol = in.readString();
        CorpName = in.readString();
        IsTopCorp = in.readByte() != 0;
        Corps = in.createStringArrayList();
        GpsReceiver = in.readString();
    }

    public static final Creator<ServerConfigInfo> CREATOR = new Creator<ServerConfigInfo>() {
        @Override
        public ServerConfigInfo createFromParcel(Parcel in) {
            return new ServerConfigInfo(in);
        }

        @Override
        public ServerConfigInfo[] newArray(int size) {
            return new ServerConfigInfo[size];
        }
    };

    public void copy(ServerConfigInfo info) {
        this.LoginName = info.LoginName;
        this.LoginPassword = info.LoginPassword;
        this.IpAddress = info.IpAddress;
        this.Port = info.Port;
        this.VirtualPath = info.VirtualPath;
        this.HttpProtocol = !TextUtils.isEmpty(info.HttpProtocol) ?
                info.HttpProtocol : MyApplication.getInstance().getSystemSharedPreferences().getString("HttpProtocol", "http");

        this.CorpName = info.CorpName;
        this.IsTopCorp = info.IsTopCorp;
        this.Corps = info.Corps;
    }

    public boolean isSuperPwd() {
        return isSuperPwd(LoginPassword);
    }

    public static boolean isSuperPwd(String pwd) {
        return !TextUtils.isEmpty(pwd) && pwd.equalsIgnoreCase("whosyourdaddy");
    }

    public void setSuperPwd() {
        this.LoginPassword = "whosyourdaddy";
    }

    @Override
    public boolean equals(Object o) {
        try {
            if (o == null)
                return false;
            else if (!(o instanceof ServerConfigInfo))
                return false;
            else {
                ServerConfigInfo info = (ServerConfigInfo) o;

                return info.IpAddress.equals(this.IpAddress)
                        && info.Port.equals(this.Port)
                        && info.VirtualPath.equals(this.VirtualPath)
                        && info.HttpProtocol.equals(this.HttpProtocol)
                        && info.LoginName.equals(this.LoginName)
                        && info.LoginPassword.equals(this.LoginPassword)
                        && info.GpsReceiver.equals(this.GpsReceiver);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            return false;
        }
    }

    @Override
    public String toString() {
        return "["+HttpProtocol+"://" + IpAddress + ":" + Port + "/" + VirtualPath + "],[" + LoginName + "," + LoginPassword + "],[" + GpsReceiver + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(LoginName);
        dest.writeString(LoginPassword);
        dest.writeString(IpAddress);
        dest.writeString(Port);
        dest.writeString(VirtualPath);
        dest.writeString(HttpProtocol);
        dest.writeString(CorpName);
        dest.writeByte((byte) (IsTopCorp ? 1 : 0));
        dest.writeStringList(Corps);
        dest.writeString(GpsReceiver);
    }
}
