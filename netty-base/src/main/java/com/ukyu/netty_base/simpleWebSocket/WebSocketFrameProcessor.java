package com.ukyu.netty_base.simpleWebSocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.Date;

/**
 * @date 2024/12/4
 * @user ukyu
 */
public class WebSocketFrameProcessor {
    public void process(ChannelHandlerContext ctx, WebSocketFrame msg) {
        System.out.printf("websocket msg: %s", msg);

        if(msg instanceof TextWebSocketFrame){
            String text = ((TextWebSocketFrame) msg).text();
            String resp = String.format("now: %s, receive msg: %s", new Date(), text);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(resp));
        }

    }
}
