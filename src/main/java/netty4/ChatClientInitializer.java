package netty4;

import android.app.Activity;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class ChatClientInitializer extends ChannelInitializer<SocketChannel> {

    private Activity act;

    public ChatClientInitializer(Activity act) {
        this.act = act;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("decoder", new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
        pipeline.addLast("encoder", new ObjectEncoder());
        pipeline.addLast("handler", new ChatClientHandler(act));
    }
}
