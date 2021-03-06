package aysntask;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import vo.FriendBody;
import android.app.Activity;
import config.Const;

public class AddFriendToRoomTask extends BaseTask<FriendBody, Void, String> {

    public AddFriendToRoomTask(Activity activity) {
        super(activity);
    }

    @Override
    public String doExecute(FriendBody fb) throws Exception {
        final String url = Const.BASE_URL + "addf2room";
        HttpHeaders reqtHeaders = new HttpHeaders();
        List<MediaType> acceptMediaTypes = new ArrayList<MediaType>();
        acceptMediaTypes.add(MediaType.APPLICATION_JSON);
        reqtHeaders.setAccept(acceptMediaTypes);

        HttpEntity<FriendBody> requestEntity = new HttpEntity<FriendBody>(fb, reqtHeaders);
        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        ResponseEntity<String> resp = rest.postForEntity(url, requestEntity, String.class);

        if (resp.getStatusCode() == HttpStatus.OK) {
            return "ok";
        }
        return null;
    }

    @Override
    public void doResult(String result) throws Exception {

    }
}
