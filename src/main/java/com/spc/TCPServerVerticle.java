package com.spc;

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
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.LocalMap;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//AbstractVerticle은 이미 Verticle 을 implement 하는 추상클래스로 좀더 쉽게 Verticle을 사용할 수 있도록 이미 정의되어있다.
public class TCPServerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger( TCPServerVerticle.class );
    //private List<NetSocket> sockets;
    private LocalMap<String, String> sockets;
    private NetServer server;
    EventBus eb;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        server = vertx.createNetServer();
        eb = vertx.eventBus();
        //sockets = new ArrayList<NetSocket>();
        sockets = vertx.sharedData().getLocalMap("sockets");

        //1회차
        server.connectHandler(new Handler<NetSocket>(){
            @Override
            public void handle(NetSocket socket) {

                //sockets.add(socket.writeHandlerID());
                logger.info("socket connected handlerID: " + socket.writeHandlerID());
                sockets.put(socket.writeHandlerID(), socket.writeHandlerID());

                socket.handler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buffer){
                        logger.info("Thread ["+Thread.currentThread ().getId ()+"] received data: "+buffer.toString()+" handlerID: "+socket.writeHandlerID());

                        if(sockets.get(socket.writeHandlerID()) != null){
                            logger.info("Thread ["+Thread.currentThread ().getId()+"] send data: "+buffer.toString()+" handlerID: "+socket.writeHandlerID());
                            eb.send(socket.writeHandlerID(), buffer);
                        }

                        /*for (String s : sockets){
                            if(!socket.equals(s)){
                                logger.info("Thread ["+Thread.currentThread ().getId()+"] send data: "+buffer.toString());
                                eb.send(s, buffer);
                            }
                        }*/
                    }
                });

                socket.closeHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                        logger.info("connection closed: " + socket.remoteAddress()+" handlerID: "+socket.writeHandlerID());

                        sockets.remove(socket.writeHandlerID());

                        /*Iterator<NetSocket> it = sockets.iterator();
                        while(it.hasNext()){
                            if(socket.equals(it.next())){
                                it.remove();
                                break;
                            }
                        }*/

                    }
                });

                socket.exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable event) {
                        logger.error("unexpected exception: ", event);
                    }
                });



            }
        });

        server.listen(8090, "localhost", new Handler<AsyncResult<NetServer>>(){
            @Override
            public void handle(AsyncResult<NetServer> event) {
                logger.info("bind result:" + event.succeeded());
            }
        });


    }

    @Override
    public void stop() throws Exception {

        if(server != null){
            server.close();
        }

    };

}
