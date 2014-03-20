package vo;

import java.io.Serializable;

import net.tsz.afinal.annotation.sqlite.Table;

/**
 * 
 *  @desc:仅仅作为用户上线下线的标志
 *  @author WY 
 *  创建时间 2014年3月19日 上午11:19:26
 */
@SuppressWarnings("serial")
@Table(name = "online_friends")
public class OnlineFriends implements Serializable {

    private int id;

    /**好友名字*/
    private String name;
    
    /**好友编号*/
    private int channelId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User [name=" + name + ", channelId=" + channelId + "]";
    }
}