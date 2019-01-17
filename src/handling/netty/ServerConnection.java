package handling.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import constants.ServerConstants;

public class ServerConnection {

    private int port;
    private int world = -1;
    private int channels = -1;
    private ServerBootstrap boot;
    private EventLoopGroup bossGroup = new NioEventLoopGroup(); //The initial connection thread where all the new connections go to
    private EventLoopGroup workerGroup = new NioEventLoopGroup(); //Once the connection thread has finished it will be moved over to this group where the thread will be managed
    private Channel channel;

    public ServerConnection(int port) {
        this.port = port;
    }
        /*
        public ServerConnection(int port, int channels) {
		this.port = port;
		this.channels = channels;
	}*/
    // Multiple worlds

    public ServerConnection(int port, int world, int channels) {
        this.port = port;
        this.world = world;
        this.channels = channels;
    }

    public void run() {
        try {
            boot = new ServerBootstrap().group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, ServerConstants.MAXIMUM_CONNECTIONS)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ServerInitializer(this.world, this.channels));
                channel = boot.bind(port).sync().channel().closeFuture().channel();
            System.out.println("Listening to port: " + port);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.printf("Connection to %s failed.", channel.remoteAddress());
        }
    }

    public void close () {
        channel.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
