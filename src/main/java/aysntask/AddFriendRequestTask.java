package aysntask;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import vo.Myself;
import android.app.Activity;
import config.Const;

public class AddFriendRequestTask extends BaseTask<Myself, Void, String> {
    private int friendNum;
    private Activity act;

    public AddFriendRequestTask(Activity act, int friendNum) {
        super(act);
        this.friendNum = friendNum;
        this.act = act;
    }

    @Override
    public String doExecute(Myself info) throws Exception {
        final String url = Const.BASE_URL + "addFriendRequest?fn=" + friendNum + "&" + "name="
                + info.getName() + "&" + "mn=" + info.getChannelId();
        HttpHeaders reqtHeaders = new HttpHeaders();
        List<MediaType> acceptMediaTypes = new ArrayList<MediaType>();
        acceptMediaTypes.add(MediaType.APPLICATION_JSON);
        reqtHeaders.setAccept(acceptMediaTypes);

        HttpEntity<?> requestEntity = new HttpEntity<Object>(reqtHeaders);

        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.GET, requestEntity,
                String.class);
        if (resp.getStatusCode() == HttpStatus.OK) {
            return resp.getBody();
        }
        return null;
    }

    @Override
    public void doResult(String result) throws Exception {
        if (result != null) {
            toast(act, result);
        }
    }
}
