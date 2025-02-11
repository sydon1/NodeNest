package be.kuleuven.gt.nodenest.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class userLogin implements Parcelable {
    private String username;
    private String password;
    private int id;
    public userLogin(String username, String password, int id) {
        this.username = username;
        this.password = password;
        this.id = id;
    }
    public userLogin(Parcel in) {
        this.username = in.readString();
        this.password = in.readString();
        this.id = in.readInt();
    }
    public static final Creator<userLogin> CREATOR = new Creator<userLogin>() {
        @Override
        public userLogin createFromParcel(Parcel in) {
            return new userLogin(in);
        }

        @Override
        public userLogin[] newArray(int size) {
            return new userLogin[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    public int getId() {
        return id;
    }
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
        dest.writeInt(id);
    }
}
