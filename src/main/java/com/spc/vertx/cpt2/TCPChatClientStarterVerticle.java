package com.spc.vertx.cpt2;

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

        //vertx 인스턴스 상해서 해당 경로의 버티클을 배포한다.(커맨드 실행과 동일)
        //메인클래스 io.vertx.core.Launcher 통해 실행할 경우, class 경로를 입력하여야 한다.
        vertx.deployVerticle("TCPClientVerticle.java");

        //worker 옵션 추가
        DeploymentOptions options = new DeploymentOptions().setWorker(true);
        vertx.deployVerticle("TCPChatClientWorkerVerticle.java", options);

    }

}
