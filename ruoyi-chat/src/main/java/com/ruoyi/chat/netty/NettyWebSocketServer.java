package com.ruoyi.chat.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
public class NettyWebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(NettyWebSocketServer.class);

    @Value("${chat.netty.port:8081}")
    private int port;

    @Value("${chat.netty.boss-threads:1}")
    private int bossThreads;

    @Value("${chat.netty.worker-threads:0}")
    private int workerThreads;

    @Value("${chat.netty.websocket-path:/ws}")
    private String websocketPath;

    @Autowired
    private ChatChannelHandler chatChannelHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    @PostConstruct
    public void start() {
        new Thread(() -> {
            try {
                bossGroup = new NioEventLoopGroup(bossThreads);
                workerGroup = new NioEventLoopGroup(workerThreads);

                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                
                                // HTTP编解码器
                                pipeline.addLast(new HttpServerCodec());
                                // HTTP对象聚合器
                                pipeline.addLast(new HttpObjectAggregator(65536));
                                // 支持大文件传输
                                pipeline.addLast(new ChunkedWriteHandler());
                                // 心跳检测
                                pipeline.addLast(new IdleStateHandler(60, 30, 120, TimeUnit.SECONDS));
                                // WebSocket协议处理器
                                pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath, null, true, 65536));
                                // 自定义聊天处理器
                                pipeline.addLast(chatChannelHandler);
                            }
                        });

                ChannelFuture future = bootstrap.bind(port).sync();
                serverChannel = future.channel();
                log.info("Netty WebSocket服务器启动成功，端口：{}, WebSocket路径：{}", port, websocketPath);
                
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                log.error("Netty WebSocket服务器启动失败", e);
            } finally {
                stop();
            }
        }, "netty-websocket-server").start();
    }

    @PreDestroy
    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("Netty WebSocket服务器已关闭");
    }
}
