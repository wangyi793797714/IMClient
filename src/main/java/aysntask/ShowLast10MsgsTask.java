package aysntask;

import java.util.Collections;
import java.util.List;

import net.tsz.afinal.FinalDb;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import util.FileOperator;
import util.MsgComparator;
import util.Util;
import vo.Content;
import adapter.ChatAdapter;
import android.app.Activity;
import android.content.Context;
/**
 * 
 *  @desc:显示上10条信息
 *  @author WY 
 *  创建时间 2014年4月9日 下午5:49:57
 */
public class ShowLast10MsgsTask extends BaseTask<String, Void, List<Content>> {

    private PullToRefreshListView chatList;
    
    private ChatAdapter adapter;
    
    private Activity act;
    
    public ShowLast10MsgsTask(Context activity,PullToRefreshListView chatList,ChatAdapter adapter) {
        super(activity);
        this.chatList=chatList;
        this.adapter=adapter;
        this.act=(Activity) activity;
    }

    @Override
    public List<Content> doExecute(String param) throws Exception {
       FinalDb db = FinalDb.create(act, FileOperator.getDbPath(act), true);
       List<Content> datas=db.findAllByWhere(Content.class, param, "date DESC LIMIT 10 offset "+adapter.getCount());
       return datas;
    }

    @Override
    public void doResult(List<Content> result) throws Exception {
        if(!Util.isEmpty(result)){
            Collections.sort(result, new MsgComparator());
            adapter.addItems(result, 0);
        }
        chatList.smoothScrollTo(0);
        chatList.onRefreshComplete();  
    }
}
