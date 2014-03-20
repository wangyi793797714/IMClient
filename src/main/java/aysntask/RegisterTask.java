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
import android.widget.EditText;
import config.Const;

public class RegisterTask extends BaseTask<Myself, Void, Myself> {

    private EditText channelId;

    private Activity act;

    public RegisterTask(Activity act, EditText channelId) {
        super(act);
        this.channelId = channelId;
        this.act = act;
    }

    @Override
    public Myself doExecute(Myself info) throws Exception {
        final String url = Const.BASE_URL + "register?u=" + info.getName() + "&" + "p="
                + info.getPass();
        HttpHeaders reqtHeaders = new HttpHeaders();
        List<MediaType> acceptMediaTypes = new ArrayList<MediaType>();
        acceptMediaTypes.add(MediaType.APPLICATION_JSON);
        reqtHeaders.setAccept(acceptMediaTypes);

        HttpEntity<?> requestEntity = new HttpEntity<Object>(reqtHeaders);

        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        ResponseEntity<Myself> resp = rest.exchange(url, HttpMethod.POST, requestEntity,
                Myself.class);
        if (resp.getStatusCode() == HttpStatus.OK) {
            return resp.getBody();
        }
        return null;
    }

    @Override
    public void doResult(Myself result) throws Exception {
        if (result != null) {
            channelId.setText(result.getChannelId() + "");
        } else {
            toast(act, "注册失败");
        }
    }
}
