package com.activity;

import java.util.List;

import net.tsz.afinal.annotation.view.ViewInject;
import util.InitUtil;
import util.Util;
import vo.Myself;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import application.IMApplication;
import aysntask.LoginTask;
import aysntask.RegisterTask;
import config.Const;

public class LoginAct extends BaseActivity {

    @ViewInject(id = R.id.login_name)
    private EditText name;

    @ViewInject(id = R.id.login_pass)
    private EditText pass;

    @ViewInject(id = R.id.login_channelid)
    private EditText channelId;

    @ViewInject(id = R.id.login_login)
    private Button login;

    @ViewInject(id = R.id.login_register)
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        IMApplication.APP.addActivity(this);
        login = (Button) findViewById(R.id.login_login);
        List<Myself> list = db.findAll(Myself.class);
        if (!Util.isEmpty(list)) {
            Myself vo = list.get(0);
            if (vo != null) {
                name.setText(vo.getName());
                pass.setText(vo.getPass());
                channelId.setText(vo.getChannelId() + "");
            }
        }
        login.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Myself info = new Myself();
                info.setName(name.getText().toString());
                info.setPass(pass.getText().toString());
                info.setChannelId(Integer.parseInt(channelId.getText().toString()));
                new LoginTask(LoginAct.this).execute(info);
            }
        });
        register.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Myself info = new Myself();
                info.setName(name.getText().toString());
                info.setPass(pass.getText().toString());
                new RegisterTask(LoginAct.this, channelId).execute(info);
            }
        });
        initFile();
    }

    private void initFile() {
        InitUtil.createFolder(this);
        InitUtil.createFile(this, Const.DB__NAME);
    }
}
