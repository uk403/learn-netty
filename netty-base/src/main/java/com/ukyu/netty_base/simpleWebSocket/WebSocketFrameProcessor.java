package com.ukyu.netty_base.simpleWebSocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.util.Date;

/**
 * @date 2024/12/4
 * @user ukyu
 */
public class WebSocketFrameProcessor {

    private WebSocketServerHandshaker handshaker;

    public void process(ChannelHandlerContext ctx, WebSocketFrame msg) {

        if(msg instanceof TextWebSocketFrame){
            String text = ((TextWebSocketFrame) msg).text();
            System.out.printf("websocket receive msg: %s \n", text);
            String resp = String.format("now: %s, receive msg: %s", new Date(), text);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(resp));
        }

    }

    /**
     * http -> ws
     */
    public void processHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {

        // 如果HTTP解码失败，返回http异常
        if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        // 构造握手响应返回
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
            "ws://" + req.headers().get(HttpHeaderNames.HOST) + req.uri(), null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory
                .sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
        // 如果是非Keep-Alive，关闭链接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        f.addListener(ChannelFutureListener.CLOSE);
    }
}
