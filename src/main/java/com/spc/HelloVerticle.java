package com.spc;

import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

//AbstractVerticle은 이미 Verticle 을 implement 하는 추상클래스로 좀더 쉽게 Verticle을 사용할 수 있도록 이미 정의되어있다.
public class HelloVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger( HelloVerticle.class );

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        logger.info("hello, world");
    }
}
