package com.spc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.Console;

//AbstractVerticle은 이미 Verticle 을 implement 하는 추상클래스로 좀더 쉽게 Verticle을 사용할 수 있도록 이미 정의되어있다.
public class TCPChatClientWorkerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger( TCPChatClientWorkerVerticle.class );
    private EventBus eb;
    private boolean readline;


    @Override
    public void start(Future<Void> startFuture) throws Exception {

        eb = vertx.eventBus();
        readline = true;

        Console console = System.console();

        if ( console != null ) {
            while ( readline ) {
                String line = console.readLine();
                if( line.equals("exit") ){
                    readline = false;
                }else{
                    logger.info("TCPChatClientWorkerVerticle event bus is sending data : " + line);
                    eb.send("com.devop.vertx.chat", Buffer.buffer().appendString(line));
                }
            }
        }

    }

    @Override
    public void stop() {
        readline = false;
    }


}
