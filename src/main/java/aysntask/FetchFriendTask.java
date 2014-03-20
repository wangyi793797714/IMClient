package aysntask;

import java.util.ArrayList;
import java.util.Arrays;
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
import adapter.FriendAdapter;
import android.app.Activity;
import config.Const;

public class FetchFriendTask extends BaseTask<Integer, Void, List<Myself>> {

    private FriendAdapter adapter;

    public FetchFriendTask(Activity context, FriendAdapter adapter) {
        super(context);
        this.adapter = adapter;
    }

    @Override
    public List<Myself> doExecute(Integer num) throws Exception {
        final String url = Const.BASE_URL + "fetchFriend?n=" + num;
        HttpHeaders reqtHeaders = new HttpHeaders();
        List<MediaType> acceptMediaTypes = new ArrayList<MediaType>();
        acceptMediaTypes.add(MediaType.APPLICATION_JSON);
        reqtHeaders.setAccept(acceptMediaTypes);

        HttpEntity<?> requestEntity = new HttpEntity<Object>(reqtHeaders);

        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        ResponseEntity<Myself[]> resp = rest.exchange(url, HttpMethod.GET, requestEntity,
                Myself[].class);

        if (resp.getStatusCode() == HttpStatus.OK) {
            return Arrays.asList(resp.getBody());
        }
        return null;
    }

    @Override
    public void doResult(List<Myself> result) throws Exception {
        if (result != null) {
            adapter.addItems(result);
        }
    }
}
