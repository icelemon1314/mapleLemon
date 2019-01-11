package handling.netty;

import handling.MapleServerHandler;
import handling.mina.MaplePacketDecoder;
import handling.mina.MaplePacketEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private int world;
    private int channels;

    public ServerInitializer(int world, int channels) {
        this.world = world;
        this.channels = channels;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipe = channel.pipeline();
        pipe.addLast("decoder", new MaplePacketDecoder()); // decodes the packet
        pipe.addLast("encoder", new MaplePacketEncoder()); //encodes the packet
        pipe.addLast("handler", new MapleServerHandler(channels));
    }


}
