package com.spc.vertx.cpt2;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

//AbstractVerticle은 이미 Verticle 을 implement 하는 추상클래스로 좀더 쉽게 Verticle을 사용할 수 있도록 이미 정의되어있다.
public class TCPClientVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger( TCPClientVerticle.class );
    private EventBus eb;
    private NetClient client;
    private String writeHandlerID;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        //이벤트버스 생성
        eb = vertx.eventBus();
        //클라이언트 생성
        client = vertx.createNetClient();

        //클라이언트 연결 시 이벤트 처리
        client.connect(8090, "localhost", new Handler<AsyncResult<NetSocket>>() {
            @Override
            public void handle(AsyncResult<NetSocket> asyncResult) {
                logger.info("TCPClientVerticle connect result: " + asyncResult.succeeded());
                if (asyncResult.succeeded()) {
                    //netSocket을 반환받아 writeHandlerID 추출
                    final NetSocket socket = asyncResult.result();
                    writeHandlerID = socket.writeHandlerID();

                    //eb.send(writeHandlerID, Buffer.buffer().appendString("hello world"));

                    //netSocket 데이터 수신 시 이벤트 처리
                    socket.handler(new Handler<Buffer>() {
                        @Override
                        public void handle(Buffer buffer) {
                            logger.info("TCPClientVerticle received data: " + buffer.toString());
                        }
                    });

                    //netSocket 연결 종료 시 이벤트 처리
                    socket.closeHandler(new Handler<Void>() {
                        @Override
                        public void handle(Void event) {
                            logger.info("TCPClientVerticle connection closed:" + socket.remoteAddress());
                        }
                    });

                    //netSocket 에러 발생 시 이벤트 처리
                    socket.exceptionHandler(new Handler<Throwable>() {
                        @Override
                        public void handle(Throwable throwable) {
                            logger.error("TCPClientVerticle unexpected exception: ", throwable);
                        }
                    });
                }
            }

        });

        //이벤트 버스 통해 입력값 수신 후 재발송
        eb.consumer("com.devop.vertx.chat", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {

                logger.info("TCPClientVerticle eventbus received message");

                //netClient의 연결이 연결된 경우에만 발송
                if(writeHandlerID != null){
                    logger.info("TCPClientVerticle eventbus send message");
                    eb.send(writeHandlerID, message.body() );
                }
            }
        });



    }

    @Override
    public void stop() {
        if (client != null)
            client.close();
    }

}
