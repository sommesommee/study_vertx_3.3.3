package com.spc.vertx.cpt2;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.shareddata.LocalMap;

//AbstractVerticle은 이미 Verticle 을 implement 하는 추상클래스로 좀더 쉽게 Verticle을 사용할 수 있도록 이미 정의되어있다.
public class TCPServerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger( TCPServerVerticle.class );
    //private List<NetSocket> sockets;
    private LocalMap<String, String> sockets;
    private NetServer server;
    EventBus eb;
    
    //verticle 시작 시 이벤트 처리
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        
        //서버 생성
        server = vertx.createNetServer();
        //이벤트버스 생성
        eb = vertx.eventBus();
        
        //sharedData 생성, NetSocket List를 저장
        sockets = vertx.sharedData().getLocalMap("sockets");

        //서버 연결 시 이벤트 처리
        server.connectHandler(new Handler<NetSocket>(){
            @Override
            public void handle(NetSocket socket) {
                
                //List에 NetSocket 추가
                logger.info("socket connected handlerID: " + socket.writeHandlerID());
                sockets.put(socket.writeHandlerID(), socket.writeHandlerID());
                
                //NetSocket 데이터 수신 시 이벤트 처리
                socket.handler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer){
                        //NetSocket 응답값 출력
                        logger.info("Thread ["+Thread.currentThread ().getId ()+"] received data: "+buffer.toString()+" handlerID: "+socket.writeHandlerID());
                        //NetSocket List에 이미 포함된 경우, 응답값을 이벤트 버스로 발송
                        if(sockets.get(socket.writeHandlerID()) != null){
                            logger.info("Thread ["+Thread.currentThread ().getId()+"] send data: "+buffer.toString()+" handlerID: "+socket.writeHandlerID());
                            eb.send(socket.writeHandlerID(), buffer);
                        }
                    }
                });
                
                //NetSocket 연결 종료 시 이벤트 처리
                socket.closeHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                        logger.info("connection closed: " + socket.remoteAddress()+" handlerID: "+socket.writeHandlerID());
                        //List에 NetSocket 제거
                        sockets.remove(socket.writeHandlerID());

                    }
                });
                
                //NetSocket 예외 처리
                socket.exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable event) {
                        logger.error("unexpected exception: ", event);
                    }
                });



            }
        });
        
        //서버 포트 설정 밑 기동 시 이벤트 처리
        server.listen(8090, "localhost", new Handler<AsyncResult<NetServer>>(){
            @Override
            public void handle(AsyncResult<NetServer> event) {
                logger.info("bind result:" + event.succeeded());
            }
        });


    }
    
    //verticle 종료 시 이벤트 처리
    @Override
    public void stop() throws Exception {

        if(server != null){
            server.close();
        }

    };

}
