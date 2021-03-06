/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redkale.mq;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.redkale.net.http.HttpSimpleRequest;

/**
 * HttpSimpleRequest的MessageCoder实现
 *
 * <p>
 * 详情见: https://redkale.org
 *
 * @author zhangjx
 *
 * @since 2.1.0
 */
public class HttpSimpleRequestCoder implements MessageCoder<HttpSimpleRequest> {

    private static final HttpSimpleRequestCoder instance = new HttpSimpleRequestCoder();

    public static HttpSimpleRequestCoder getInstance() {
        return instance;
    }

    @Override
    public byte[] encode(HttpSimpleRequest data) {
        byte[] requestURI = MessageCoder.getBytes(data.getRequestURI()); //long-string
        byte[] path = MessageCoder.getBytes(data.getRequestURI()); //short-string
        byte[] remoteAddr = MessageCoder.getBytes(data.getRemoteAddr());//short-string
        byte[] sessionid = MessageCoder.getBytes(data.getSessionid());//short-string
        byte[] contentType = MessageCoder.getBytes(data.getContentType());//short-string
        byte[] headers = MessageCoder.getBytes(data.getHeaders());
        byte[] params = MessageCoder.getBytes(data.getParams());
        byte[] body = MessageCoder.getBytes(data.getBody());
        int count = 1 + 4 + requestURI.length + 2 + path.length + 2 + remoteAddr.length + 2 + sessionid.length
            + 2 + contentType.length + 4 + headers.length + params.length + 4 + body.length;
        byte[] bs = new byte[count];
        ByteBuffer buffer = ByteBuffer.wrap(bs);
        buffer.put((byte) (data.isRpc() ? 'T' : 'F'));
        buffer.putInt(requestURI.length);
        if (requestURI.length > 0) buffer.put(requestURI);
        buffer.putChar((char) path.length);
        if (path.length > 0) buffer.put(path);
        buffer.putChar((char) remoteAddr.length);
        if (remoteAddr.length > 0) buffer.put(remoteAddr);
        buffer.putChar((char) sessionid.length);
        if (sessionid.length > 0) buffer.put(sessionid);
        buffer.putChar((char) contentType.length);
        if (contentType.length > 0) buffer.put(contentType);
        buffer.putInt(data.getCurrentUserid());
        buffer.put(headers);
        buffer.put(params);
        buffer.putInt(body.length);
        if (body.length > 0) buffer.put(body);
        return bs;
    }

    @Override
    public HttpSimpleRequest decode(byte[] data) {
        if (data == null) return null;
        ByteBuffer buffer = ByteBuffer.wrap(data);
        HttpSimpleRequest req = new HttpSimpleRequest();
        req.setRpc(buffer.get() == 'T');
        req.setRequestURI(MessageCoder.getLongString(buffer));
        req.setPath(MessageCoder.getShortString(buffer));
        req.setRemoteAddr(MessageCoder.getShortString(buffer));
        req.setSessionid(MessageCoder.getShortString(buffer));
        req.setContentType(MessageCoder.getShortString(buffer));
        req.setCurrentUserid(buffer.getInt());
        req.setHeaders(MessageCoder.getMap(buffer));
        req.setParams(MessageCoder.getMap(buffer));
        int len = buffer.getInt();
        if (len > 0) {
            byte[] bs = new byte[len];
            buffer.get(bs);
            req.setBody(bs);
        }
        return req;
    }

    protected static String getString(ByteBuffer buffer) {
        int len = buffer.getInt();
        if (len == 0) return null;
        byte[] bs = new byte[len];
        buffer.get(bs);
        return new String(bs, StandardCharsets.UTF_8);
    }
}
