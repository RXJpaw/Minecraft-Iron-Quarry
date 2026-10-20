package pw.rxj.iron_quarry.resource;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import pw.rxj.iron_quarry.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ResourceReloadListener implements ResourceReloader {
    public static final Identifier ID = Identifier.of(Main.MOD_ID, "resource_reloader");
    private static final List<Identifier> resourceIdList = new ArrayList<>();

    @Override
    public CompletableFuture<Void> reload(Store store, Executor prepareExecutor, Synchronizer synchronizer, Executor applyExecutor) {
        return reloadResources(synchronizer, store.getResourceManager(), prepareExecutor, applyExecutor);
    }

    public static CompletableFuture<Void> reloadResources(Synchronizer synchronizer, ResourceManager resourceManager, Executor prepareExecutor, Executor applyExecutor) {
        TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();

        return CompletableFuture
                .supplyAsync(() -> {
                    List<Identifier> loadableResourceIds = new ArrayList<>();

                    resourceIdList.forEach(resourceId -> {
                        Optional<Resource> resource = resourceManager.getResource(resourceId);

                        if (resource.isEmpty()) {
                            textureManager.registerTexture(resourceId, new NativeImageBackedTexture(resourceId::toString, MissingSprite.createImage()));
                        }

                        loadableResourceIds.add(resourceId);
                    });

                    return loadableResourceIds;
                }, prepareExecutor)
                .thenCompose(synchronizer::whenPrepared)
                .thenAcceptAsync(loadableResourceIds -> {
                    loadableResourceIds.forEach(textureManager::getTexture);
                }, applyExecutor);
    }

    public static void include(String path){
        include(Identifier.of(Main.MOD_ID, path));
    }
    public static void include(Identifier resourceId){
        resourceIdList.add(resourceId);
    }
    public static void register() {
        ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(ID, new ResourceReloadListener());
    }
}
