package aysntask;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalDb;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.activity.HomeActivity;
import com.activity.LoginAct;

import util.FileOperator;
import vo.Myself;
import config.Const;

public class LoginTask extends BaseTask<Myself, Void, Myself> {

    private LoginAct act;

    public static String SEND_NAME;

    public static String currentName = "";

    public LoginTask(LoginAct context) {
        super(context);
        act = context;
    }

    @Override
    public Myself doExecute(Myself info) throws Exception {
        final String url = Const.BASE_URL + "login?n=" + info.getChannelId() + "&" + "p="
                + info.getPass();
        HttpHeaders reqtHeaders = new HttpHeaders();
        List<MediaType> acceptMediaTypes = new ArrayList<MediaType>();
        acceptMediaTypes.add(MediaType.APPLICATION_JSON);
        reqtHeaders.setAccept(acceptMediaTypes);

        HttpEntity<?> requestEntity = new HttpEntity<Object>(reqtHeaders);

        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        ResponseEntity<Myself> resp = rest.exchange(url, HttpMethod.GET, requestEntity,
                Myself.class);
        if (resp.getStatusCode() == HttpStatus.OK) {
            return resp.getBody();
        }
        return null;
    }

    @Override
    public void doResult(Myself result) throws Exception {
        if (result != null) {
            currentName = result.getName();
            FinalDb db = FinalDb.create(act, FileOperator.getDbPath(act), true);
            db.save(result);
            act.skip(HomeActivity.class, result);
            act.finish();
        }else{
            toast(act,"登陆失败,请检查输入信息");
        }
    }
}
