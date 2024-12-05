package com.ukyu.netty_base.simpleWebSocket;

import com.ukyu.netty_base.simpleWebSocket.handler.NettyWebsocketMsgHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @author ukyu
 * @desc ---
 * @dateTime 2024/12/4
 */
public class StartNetty {

    public static void main(String[] args) throws InterruptedException {

        WebSocketFrameProcessor webSocketFrameProcessor = new WebSocketFrameProcessor();

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup =  new NioEventLoopGroup(4);
        ServerBootstrap bs = new ServerBootstrap();
        bs.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast("http-codec", new HttpServerCodec());
                        //  负责将 Http 的一些信息例如版本 和 Http 的内容继承一个 FullHttpRequesst
                        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
                        ch.pipeline().addLast("logic", new NettyWebsocketMsgHandler(webSocketFrameProcessor));
                    }
                })
        ;
        final ChannelFuture future = bs.bind(8000).sync();
        future.addListener((f) -> {
            if (f.isSuccess()) {
                System.out.println("netty websocket server start success");
            } else {
                throw new Exception("netty websocket server start error");
            }
        });


    }

}
