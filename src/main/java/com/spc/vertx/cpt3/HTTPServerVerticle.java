package com.spc.vertx.cpt3;

import com.spc.vertx.cpt2.TCPServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;
import java.util.Map;

public class HTTPServerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger( HTTPServerVerticle.class );
    private Buffer body = Buffer.buffer();


    @Override
    public void start() {

        //버텍스 인스턴스통해 httpServer 생성
        HttpServer httpServer = vertx.createHttpServer();

        //httpServer 통해 요청 발생 시 이벤트 처리 (request는 Handler<HttpServerRequest> handler 와 동일)
        //lambda 처리
        httpServer.requestHandler(request -> {
            String method = request.method().name();
            String uri = request.uri();
            String path = request.path();
            String query = request.query();
            logger.info("received http requests: {method=" + method + "uri="+uri+", path="+path+", query="+query + "}");

            //메소드 entrySet은 Map의 데이터를 담고 있는 Set을 반환하는 인터페이스
            //아래는 튜플리스트를 의미 (키중복 가능)
            List<Map.Entry<String, String>> params = request.params().entries();
            for(Map.Entry<String, String> param : params){
                logger.info("param["+param.getKey()+"] = "+param.getValue());
            }

            List<Map.Entry<String, String>> headers = request.headers().entries();
            for(Map.Entry<String, String> header : headers) {
                logger.info("header["+header.getKey()+"] = "+header.getValue());
            }

            //요청 인입 시 body 데이터 관련 이벤트 처리
            //buffer 는 Handler<Buffer> bodyHandler 와 동일
            request.bodyHandler(buffer -> {
                logger.info("received data: " + buffer.toString());
            });

            //요청 인입 시 buffer 기반 데이터 이벤트 처리
            request.handler(buffer -> {
               logger.info("received data: " + buffer.toString());
               //입력값 저장
               body.appendBuffer(buffer);
            });

            request.endHandler(voidHandle -> {
                //입력값 출력
                logger.info("total received data: " + body.toString());
            });

            //응답값 처리
            request.response().setStatusCode(200).end("OK");

        });

        //httpServer 통해 요청 발생 시 이벤트 처리 (request는 (Handler<ServerWebSocket> handler 와 동일)
        //WebSocket는 모든 웹브라우저에서 직접 사용할 수 있는게 아니므로 SockJS를 사용하는 것을 권고
        httpServer.websocketHandler(serverWebSocket -> {
           logger.info("A WebSocket has connected!");
           serverWebSocket.handler(buffer -> {
               logger.info("received data: " + buffer.toString());
           });
        });

        //httpServer 연결 시 이벤트 처리
        //asyncResult는 Handler<AsyncResult<HttpServer>> listenHandler 와 동일
        httpServer.listen(8080, asyncResult -> {
            logger.info("bind result: " + asyncResult.succeeded());
        });


    }

}
