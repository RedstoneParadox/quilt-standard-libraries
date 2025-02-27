/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.resource.loader.mixin;

import java.io.IOException;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@Mixin(NamespaceResourceManager.class)
public class NamespaceResourceManagerMixin {
	/**
	 * Acts as a pseudo-local variable in {@link NamespaceResourceManager#getAllResources(Identifier)}.
	 * Not thread-safe so a ThreadLocal is required.
	 */
	@Unique
	private final ThreadLocal<List<Resource>> quilt$getAllResources$resources = new ThreadLocal<>();

	@Inject(
			method = "getAllResources",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/NamespaceResourceManager;getMetadataPath(Lnet/minecraft/util/Identifier;)Lnet/minecraft/util/Identifier;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onGetAllResources(Identifier id, CallbackInfoReturnable<List<Resource>> cir, List<Resource> resources) {
		this.quilt$getAllResources$resources.set(resources);
	}

	@Redirect(
			method = "getAllResources",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/pack/ResourcePack;contains(Lnet/minecraft/resource/ResourceType;Lnet/minecraft/util/Identifier;)Z"
			)
	)
	private boolean onResourceAdd(ResourcePack pack, ResourceType type, Identifier id) throws IOException {
		if (pack instanceof GroupResourcePack groupResourcePack) {
			ResourceLoaderImpl.appendResourcesFromGroup((NamespaceResourceManagerAccessor) this, id, groupResourcePack,
					this.quilt$getAllResources$resources.get());
			return false;
		}

		return pack.contains(type, id);
	}
}
