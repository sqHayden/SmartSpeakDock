package com.idx.smartspeakdock.shopping.shoproom.entity;

/**
 * Created by ryan on 18-1-12.
 * Email: Ryan_chan01212@yeah.net
 */
/*@Entity(tableName = "shopping")
public class Shopping {
    @PrimaryKey(autoGenerate = true)
    public Integer id;
    @ColumnInfo(name = "web_name")
    public String webName;
    @ColumnInfo(name = "web_url")
    public String webUrl;
}*/

public class Shopping {
    public String webName;
    public String webUrl;

    public String getWebName() {
        return webName;
    }

    public void setWebName(String webName) {
        this.webName = webName;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
}