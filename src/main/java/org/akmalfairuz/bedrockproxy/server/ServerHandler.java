package org.akmalfairuz.bedrockproxy.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.akmalfairuz.bedrockproxy.Player;
import org.akmalfairuz.bedrockproxy.ProxyServer;
import org.akmalfairuz.bedrockproxy.utils.Log;
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.protocol.bedrock.BedrockPong;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.codec.v2193.Bedrock_v2193;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockServerInitializer;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler;
import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

// AI GENERATED CODE!!!!! (claude.ai) 
public class ServerHandler extends BedrockServerInitializer {

    private static final int MAX_PLAYERS = 10;

    private final ProxyServer proxy;

    public ServerHandler(ProxyServer proxyServer) {
        this.proxy = proxyServer;
    }

    
    public BedrockPong createPong() {
        return new BedrockPong()
                .edition("MCPE")
                .motd("BedrockProxy HK Edition")
                .subMotd("BedrockProxy HK Edition For 1.26.50")
                .playerCount(proxy.players.size())
                .maximumPlayerCount(MAX_PLAYERS)
                .gameType("Survival")
                .version("1.26.50")
                .protocolVersion(Bedrock_v2193.CODEC.getProtocolVersion())
                .ipv4Port(19132)
                .ipv6Port(19132)
                .nintendoLimited(false);
    }

    public void updatePong(Channel serverChannel) {
        serverChannel.config().setOption(RakChannelOption.RAK_ADVERTISEMENT, createPong().toByteBuf());
    }

    public Channel bind(InetSocketAddress address) {
        Channel channel = new ServerBootstrap()
                .channelFactory(RakChannelFactory.server(NioDatagramChannel.class))
                .option(RakChannelOption.RAK_ADVERTISEMENT, createPong().toByteBuf())
                .group(new NioEventLoopGroup())
                .childHandler(this)
                .bind(address)
                .syncUninterruptibly()
                .channel();

        channel.eventLoop().scheduleAtFixedRate(() -> updatePong(channel), 5, 5, TimeUnit.SECONDS);
        return channel;
    }

    public DisconnectPacket createDisconnect(String message) {
        DisconnectPacket dc = new DisconnectPacket();
        dc.setKickMessage(message);
        dc.setMessageSkipped(false);
        return dc;
    }

    @Override
    public void initSession(BedrockServerSession session) {
        session.setCodec(Bedrock_v2193.CODEC);

        if (proxy.players.size() >= MAX_PLAYERS) {
            session.disconnect("Proxy full!");
            return;
        }

        Player player = new Player(session, this, proxy);
        session.setPacketHandler(new ClientPacketHandler(player));
        Log.info(session.getSocketAddress() + " joined");
    }

    private static class ClientPacketHandler implements BedrockPacketHandler {

        private final Player player;

        ClientPacketHandler(Player player) {
            this.player = player;
        }

        @Override
        public PacketSignal handlePacket(BedrockPacket packet) {
            return PacketSignal.UNHANDLED;
        }

        @Override
        public void onDisconnect(CharSequence reason) {
            if (player.getServerSession() != null) {
                player.getServerSession().disconnect();
                player.setConnectedToServer(false);
            }
            player.close();
        }
    }
                                         }
