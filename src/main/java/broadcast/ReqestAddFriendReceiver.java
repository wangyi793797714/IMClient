package broadcast;

import net.tsz.afinal.FinalDb;
import util.FileOperator;
import vo.AddFriendRequest;
import vo.Myself;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import aysntask.AddFriendResponseTask;
import config.Const;

public class ReqestAddFriendReceiver extends BroadcastReceiver {

    private Activity act;
    
    public ReqestAddFriendReceiver(Activity act) {
        this.act = act;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Const.ACTION_ADDFRIEND_REQUEST.equals(intent.getAction())) {
            final FinalDb db=FinalDb.create(act,FileOperator.getDbPath(act),true);
            final AddFriendRequest afr = (AddFriendRequest) intent.getSerializableExtra("req");
            new AlertDialog.Builder(act)   
            .setTitle("收到"+afr.getMyselfName()+"加我为好友的申请")  
            .setPositiveButton("同意", new Dialog.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //我同意对方加好友的请求，把自己的信息反馈给申请者
                    new AddFriendResponseTask(act,afr.getMyselfNum()).execute(db.findAll(Myself.class).get(0));
                }
            })  
            .setNegativeButton("拒绝", new Dialog.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //我拒绝对方加好友的请求，把自己的信息反馈给申请者
                    Myself me =db.findAll(Myself.class).get(0);
                    me.setChannelId(-1);
                    new AddFriendResponseTask(act,afr.getMyselfNum()).execute(me);
                }
            })  
            .show(); 
            
        }
    }
}
