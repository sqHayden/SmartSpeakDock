package com.lljjcoder.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 区-县
 */
public class DistrictBean implements Parcelable {

    private String id; /*110101*/
    
    private String name; /*东城区*/

    private String zipcode;
    
    public String getId() {
        return id == null ? "" : id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name == null ? "" : name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return  name ;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.zipcode);
    }

    public DistrictBean() {
    }

    protected DistrictBean(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.zipcode = in.readString();
    }

    public static final Parcelable.Creator<DistrictBean> CREATOR = new Parcelable.Creator<DistrictBean>() {
        @Override
        public DistrictBean createFromParcel(Parcel source) {
            return new DistrictBean(source);
        }

        @Override
        public DistrictBean[] newArray(int size) {
            return new DistrictBean[size];
        }
    };
}
