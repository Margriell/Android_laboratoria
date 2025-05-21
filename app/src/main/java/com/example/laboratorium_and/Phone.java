package com.example.laboratorium_and;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "phone_table")
public class Phone {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "phone_name")
    private String name;

    @ColumnInfo(name = "phone_brand")
    private String brand;

    @ColumnInfo(name = "android_version")
    private String androidVersion;

    @ColumnInfo(name = "website")
    private String website;

    public Phone(String name, String brand, String androidVersion, String website) {
        this.name = name;
        this.brand = brand;
        this.androidVersion = androidVersion;
        this.website = website;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
