/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.networking.test.play;

import net.fabricmc.loader.api.ModContainer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public final class NetworkingPlayPacketClientTest implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		//ClientPlayNetworking.registerGlobalReceiver(NetworkingPlayPacketTest.TEST_CHANNEL, this::receive);

		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			ClientPlayNetworking.registerReceiver(NetworkingPlayPacketTest.TEST_CHANNEL, (client1, handler1, buf, sender1) -> receive(handler1, sender1, client1, buf));
		});
	}

	private void receive(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client, PacketByteBuf buf) {
		Text text = buf.readText();
		client.execute(() -> client.inGameHud.setOverlayMessage(text, true));
	}
}
