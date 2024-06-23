// Copyright (c) 2023-2024 gstaaij
// This code is licensed under LGPL-3.0-only

package gstaaij.packetdiscard.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.config.DynamicRegistriesS2CPacket;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import net.minecraft.network.packet.s2c.config.SelectKnownPacksS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkSentS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.StartChunkSendS2CPacket;

import java.util.Random;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gstaaij.packetdiscard.PacketDiscard;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    // Get a private field and a method needed for some stuff
    @Shadow @Final private NetworkSide side;

    // A random number generator
    @Unique
    private Random random = new Random();

    /**
     * Discard half of the unessential packets.
     * @param packet The packet given to the send method.
     * @param callbacks The callbacks given to the send method.
     * @param flush The flush argument given to the send method.
     * @param info The callback info from the Mixin
     */
    // Stop ClientConnection.send 50% of the time if the packets aren't on the "whitelist"
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V",
            at = @At("HEAD"),
            cancellable = true)
    private void packetdiscard_send(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo info) {
        // Don't discard important packets
        if (packet instanceof LoginCompressionS2CPacket ||  // All the login packets aren't discarded,
            packet instanceof LoginDisconnectS2CPacket ||   // because people should be able to log in correctly
            packet instanceof LoginHelloS2CPacket ||
            packet instanceof LoginQueryRequestS2CPacket ||
            packet instanceof LoginSuccessS2CPacket ||
            packet instanceof GameJoinS2CPacket ||          // Needed to actually finish joining the game
            packet instanceof ChunkDataS2CPacket ||         // Needed to see the chunks
            packet instanceof ChunkSentS2CPacket ||         // Also needed to see the chunks
            packet instanceof PlayerRespawnS2CPacket ||     // Needed to be able to respawn consistently
            packet instanceof DisconnectS2CPacket ||        // Needed to be able to disconnect cleanly
            packet instanceof KeepAliveS2CPacket ||         // Needed to prevent everyone getting Timed Out every so often
            // 1.20.5 packets:
            packet instanceof StartChunkSendS2CPacket ||    // Needed for faster "Loading terrain..."
            packet instanceof ReadyS2CPacket ||             // If this is discarded, you'll be stuck on "Joining world..."
            packet instanceof SelectKnownPacksS2CPacket ||  // Same as ReadyS2CPacket
            packet instanceof DynamicRegistriesS2CPacket || // Same as ReadyS2CPacket
            packet instanceof GameStateChangeS2CPacket ||   // Required for getting into the world more quickly
            packet instanceof ChunkRenderDistanceCenterS2CPacket || // Required for loading chunks properly
            packet instanceof CustomPayloadS2CPacket        // Same as ReadyS2CPacket
        ) {
            PacketDiscard.LOGGER.debug("Essential packet " + packet.getClass().getName() + " to be sent, not discarding.");
            if (packet instanceof CustomPayloadS2CPacket)
                PacketDiscard.LOGGER.debug("CustomPayloadS2CPacket with payload: " + ((CustomPayloadS2CPacket) packet).payload().getClass().getName());
            return;
        }
        // Don't discard sent package when you're the client
        if (side == NetworkSide.CLIENTBOUND)
            return;
        // Randomly choose not to send (to discard) a packet
        if (random.nextBoolean()) {
            PacketDiscard.LOGGER.debug("Packet " + packet.getClass().getName() + " discarded.");
            info.cancel();
        }
    }
}