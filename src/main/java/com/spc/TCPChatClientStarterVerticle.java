package com.spc;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//AbstractVerticle은 이미 Verticle 을 implement 하는 추상클래스로 좀더 쉽게 Verticle을 사용할 수 있도록 이미 정의되어있다.
public class TCPChatClientStarterVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {

        vertx.deployVerticle("TCPClientVerticle.java");

        DeploymentOptions options = new DeploymentOptions().setWorker(true);
        vertx.deployVerticle("TCPChatClientWorkerVerticle.java", options);

    }

}
