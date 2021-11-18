package ru.gb.cloud;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final static String serverDir = "server-cloud-storage/Server-DATA";
    private static Path serverPath = Paths.get(serverDir);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        ctx.writeAndFlush(new ListResponse(serverPath));
        ctx.writeAndFlush(new PathResponse(serverDir));
        log.debug("Client connected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("Message: {}", msg);
        if (msg instanceof FileRequest) {
            FileRequest fileRequest = (FileRequest) msg;
            FileMessage fileMessage = new FileMessage(serverPath.resolve(fileRequest.getFilename()));
            ctx.writeAndFlush(fileMessage);
        }
        if (msg instanceof FileMessage) {
            FileMessage message = (FileMessage) msg;
            Files.write(serverPath.resolve(message.getFilename()), message.getBuffer());
            ctx.writeAndFlush(new ListResponse(serverPath));
        }
        if (msg instanceof FileRequest) {
            ctx.writeAndFlush(new ListResponse(serverPath));
        }
        if (msg instanceof PathRequestUp) {
            if (serverPath.getParent() != null) {
                serverPath = serverPath.getParent();
                ctx.writeAndFlush(new PathResponse(serverPath.toString()));
                ctx.writeAndFlush(new ListResponse(serverPath));
            }
        }
        if (msg instanceof PathRequestIn) {
            PathRequestIn pathRequestIn = (PathRequestIn) msg;
            Path newPath = serverPath.resolve(pathRequestIn.getDir());
            if (Files.isDirectory(newPath)) {
                serverPath = newPath;
                ctx.writeAndFlush(new PathResponse(serverPath.toString()));
                ctx.writeAndFlush(new ListResponse(serverPath));
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception ServerHandler: ", cause);
        ctx.close();
    }
}
