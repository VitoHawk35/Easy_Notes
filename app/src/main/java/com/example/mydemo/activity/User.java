package com.example.mydemo.activity;

/**
 * create by WUzejian on 2025/11/20
 */
public class User implements android.os.Parcelable {
        private String name;
        private int age;




    public User() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.age);
    }

    protected User(android.os.Parcel in) {
        this.name = in.readString();
        this.age = in.readInt();
    }

    public static final android.os.Parcelable.Creator<User> CREATOR = new android.os.Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(android.os.Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
