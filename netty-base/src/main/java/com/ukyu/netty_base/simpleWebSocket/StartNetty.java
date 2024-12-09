package com.ukyu.netty_base.simpleWebSocket;

import com.ukyu.netty_base.simpleWebSocket.handler.NettyWebsocketMsgHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.internal.PlatformDependent;

import java.util.Scanner;

/**
 * @author ukyu
 * @desc ---
 * @dateTime 2024/12/4
 */
public class StartNetty {

    private static final NettyWebsocketMsgHandler wsMsgHandler;


    public static final int _1MB = 1024 * 1024;
    public static final int _17MB = 17 * 1024 * 1024;

    static {
        WebSocketFrameProcessor webSocketFrameProcessor = new WebSocketFrameProcessor();
        wsMsgHandler = new NettyWebsocketMsgHandler(webSocketFrameProcessor);
    }

    public static void main(String[] args) throws InterruptedException {


//        System.out.println("Use direct buffer no cleaner: " + PlatformDependent.useDirectBufferNoCleaner());
//        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
//        PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
//        ByteBuf directBuffer = allocator.directBuffer(Integer.MAX_VALUE);
//        ByteBuf directBuffer1 = allocator.directBuffer(Integer.MAX_VALUE);
//        System.out.println("Direct ByteBuf allocated with capacity: " + directBuffer.capacity());

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
                        ch.pipeline().addLast("logic", wsMsgHandler);
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
        startMsgThread();
    }

    public static void startMsgThread(){

        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while(sc.hasNext()){
                send(sc.nextLine());
            }
        }).start();
    }

    public static void send(String text) {
        for (Channel channel : wsMsgHandler.getChannelGroup()) {
            channel.writeAndFlush(new TextWebSocketFrame(Unpooled.copiedBuffer(text, CharsetUtil.UTF_8)));
        }
    }

}
