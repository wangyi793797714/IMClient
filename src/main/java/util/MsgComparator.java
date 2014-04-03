package util;

import java.util.Comparator;
import java.util.Date;

import vo.Content;

/**
 * 
 *  @desc:消息按照时间降序排列：用于在聊天界面中查询最近的10条信息
 *  @author WY 
 *  创建时间 2014年4月3日 下午4:18:13
 */
public class MsgComparator implements Comparator<Content> {

    @Override
    public int compare(Content f, Content s) {
        Date date1 = f.getDate();
        Date date2 = s.getDate();
        return date1.compareTo(date2);
    }
}
