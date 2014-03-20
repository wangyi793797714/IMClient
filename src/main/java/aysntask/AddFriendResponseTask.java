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

import vo.AddFriendResponse;
import vo.Myself;
import android.app.Activity;
import config.Const;

public class AddFriendResponseTask extends BaseTask<Myself, Void, AddFriendResponse> {

    /** 申请加我为好友的发送者的编号 */
    private int friendNum;

    public AddFriendResponseTask(Activity context,int friendNum) {
        super(context);
        this.friendNum=friendNum;
    }

    @Override
    public AddFriendResponse doExecute(Myself info) throws Exception {
        final String url = Const.BASE_URL + "addFriendResponse?rn=" + info.getChannelId() + "&"
                + "name=" + info.getName() + "&" + "tn=" + friendNum;
        HttpHeaders reqtHeaders = new HttpHeaders();
        List<MediaType> acceptMediaTypes = new ArrayList<MediaType>();
        acceptMediaTypes.add(MediaType.APPLICATION_JSON);
        reqtHeaders.setAccept(acceptMediaTypes);

        HttpEntity<?> requestEntity = new HttpEntity<Object>(reqtHeaders);

        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        ResponseEntity<AddFriendResponse> resp = rest.exchange(url, HttpMethod.GET, requestEntity,
                AddFriendResponse.class);
        if (resp.getStatusCode() == HttpStatus.OK) {
            return resp.getBody();
        }
        return null;
    }

    @Override
    public void doResult(AddFriendResponse result) throws Exception {
        if(result.isAccept()){
            
        }
    }
}
