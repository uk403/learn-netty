package com.ukyu.netty_base;

import com.ukyu.netty_base.handler.NettyWebsocketMsgHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.bootstrap.ServerBootstrapConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author ukyu
 * @desc ---
 * @dateTime 2024/12/4
 */
public class StartNetty {

    public static void main(String[] args) throws InterruptedException {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup =  new NioEventLoopGroup(4);
        ServerBootstrap bs = new ServerBootstrap();
        bs.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel sCh) throws Exception {
                        sCh.pipeline().addLast("logic", new NettyWebsocketMsgHandler());
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
