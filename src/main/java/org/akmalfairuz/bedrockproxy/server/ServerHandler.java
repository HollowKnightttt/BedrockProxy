package org.akmalfairuz.bedrockproxy.server;

import org.cloudburstmc.protocol.bedrock.BedrockClient;
import org.cloudburstmc.protocol.bedrock.BedrockPong;
import org.cloudburstmc.protocol.bedrock.BedrockServerEventHandler;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket;
import org.cloudburstmc.protocol.bedrock.codec.v2193.Bedrock_v2193;
import org.akmalfairuz.bedrockproxy.Player;
import org.akmalfairuz.bedrockproxy.ProxyServer;
import org.akmalfairuz.bedrockproxy.utils.Log;

import java.net.InetSocketAddress;

public class ServerHandler implements BedrockServerEventHandler {

    private ProxyServer proxy;

    public ServerHandler(ProxyServer proxyServer) {
        proxy = proxyServer;
    }

    @Override
    public boolean onConnectionRequest(InetSocketAddress address) {
        return true;
    }

    @Override
    public BedrockPong onQuery(InetSocketAddress address) {
        BedrockPong pong = new BedrockPong();
        pong.setEdition("MCPE");
        pong.setMotd("BedrockProxy HK Edition");
        pong.setPlayerCount(proxy.players.size());
        pong.setMaximumPlayerCount(10);
        pong.setGameType("Survival");
        pong.setVersion("1.26.50");
        pong.setProtocolVersion(Bedrock_v2193.V2193_CODEC.getProtocolVersion());
        pong.setIpv4Port(19132);
        pong.setIpv6Port(19132);
        pong.setNintendoLimited(false);
        pong.setSubMotd("BedrockProxy HK Edition For 1.26.50");
        return pong;
    }

    public DisconnectPacket createDisconnect(String message){
        DisconnectPacket dc = new DisconnectPacket();
        dc.setKickMessage(message);
        dc.setMessageSkipped(false);
        return dc;
    }

    @Override
    public void onSessionCreation(BedrockServerSession serverSession) {
        if(proxy.players.size() > 10) {
            serverSession.sendPacketImmediately(createDisconnect("Proxy full!"));
            serverSession.disconnect();
            return;
        }
        Player player = new Player(serverSession, this, proxy);
        serverSession.addDisconnectHandler((reason) -> {
            if(player.getServerSession() != null) {
                player.getServerSession().disconnect();
                player.setConnectedToServer(false);
            }
            player.close();
        });
        serverSession.setBatchHandler(new ClientBatchHandler(serverSession, player));
        Log.info(serverSession.getAddress() + " joined");
    }
}
