package com.spc.vertx.cpt3;

import com.spc.vertx.cpt2.TCPServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

import java.util.List;
import java.util.Map;

public class SockJSServerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger( SockJSServerVerticle.class );
    private Buffer body = Buffer.buffer();


    @Override
    public void start() {

        //라우터 생성
        Router router = Router.router(vertx);
        
        //sockJSHandler 생성
        SockJSHandlerOptions sockJShandlerOptions = new SockJSHandlerOptions().setHeartbeatInterval(2000);
        BridgeOptions beOptions = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddress("test"))
                .addInboundPermitted(new PermittedOptions().setAddress("test"));
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, sockJShandlerOptions).bridge(beOptions, be -> {
            if(be.type() == BridgeEventType.REGISTER){
                logger.info("sockJS : connected");
            }
            be.complete(true);
        });

        //sockJSHandler 연결 시 이벤트 처리 -> sockJSSocket 전달
        sockJSHandler.socketHandler(sockJSSocket -> {

            logger.info("socketHandler action");


            //sockJSSocket 연결 시 이벤트 처리
            sockJSSocket.handler(buffer -> {
                logger.info("sockJSHandler received data: " + buffer.toString());
                sockJSSocket.write(buffer);
            });
            
            //sockJSSocket 예외 발생 시 이벤트 처리
            sockJSSocket.exceptionHandler(throwable -> {
                logger.error("sockJSHandler unexpected exception: ", throwable);
            });

            sockJSSocket.endHandler(voidHandle -> {
                logger.info("sockJSHandler end");
            });

        });

        //router상 sockjs 경로 생성
        router.route("/sockjs/*").handler(sockJSHandler);

        router.route("/index").handler(rtx -> {
            HttpServerResponse response = rtx.response();
            response.sendFile("index.html");
        });

        //router상 나머지 경로 생성
        /*router.route("/*").handler(rtx -> {
            
            HttpServerRequest request = rtx.request();
            String method = request.method().name();
            String uri = request.uri();
            String path = request.path();
            String query = request.query();
            logger.info("received http requests: { method= " + method + ", uri= "+uri+", path= "+path+", query= "+query + " }");

            //메소드 entrySet은 Map의 데이터를 담고 있는 Set을 반환하는 인터페이스
            //아래는 튜플리스트를 의미 (키중복 가능)
            List<Map.Entry<String, String>> params = request.params().entries();
            for(Map.Entry<String, String> param : params){
                logger.info("param["+param.getKey()+"]= "+param.getValue());
            }

            List<Map.Entry<String, String>> headers = request.headers().entries();
            for(Map.Entry<String, String> header : headers) {
                logger.info("header["+header.getKey()+"]= "+header.getValue());
            }


            //요청 인입 시 body 데이터 관련 이벤트 처리
            //buffer 는 Handler<Buffer> bodyHandler 와 동일
            request.bodyHandler(buffer -> {
                logger.info("received data: " + buffer.toString());
            });

            //응답값 처리
            request.response().setStatusCode(200).end("OK");

        });*/

        //버텍스 인스턴스통해 httpServer 생성 (router 지정)
        HttpServer httpServer = vertx.createHttpServer().requestHandler(router);

        //httpServer 연결 시 이벤트 처리
        //asyncResult는 Handler<AsyncResult<HttpServer>> listenHandler 와 동일
        httpServer.listen(8080, asyncResult -> {
            logger.info("bind result: " + asyncResult.succeeded());
        });


    }

}
