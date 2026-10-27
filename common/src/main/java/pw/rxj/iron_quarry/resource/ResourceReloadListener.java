package pw.rxj.iron_quarry.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.platform.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ResourceReloadListener implements PreparableReloadListener {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(Main.MOD_ID, "resource_reloader");
    private static final List<Identifier> resourceIdList = new ArrayList<>();

    @Override
    public CompletableFuture<Void> reload(SharedState store, Executor prepareExecutor, PreparationBarrier synchronizer, Executor applyExecutor) {
        return reloadResources(synchronizer, store.resourceManager(), prepareExecutor, applyExecutor);
    }

    public static CompletableFuture<Void> reloadResources(PreparationBarrier synchronizer, ResourceManager resourceManager, Executor prepareExecutor, Executor applyExecutor) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        return CompletableFuture
                .supplyAsync(() -> {
                    List<Identifier> loadableResourceIds = new ArrayList<>();

                    resourceIdList.forEach(resourceId -> {
                        Optional<Resource> resource = resourceManager.getResource(resourceId);

                        if (resource.isEmpty()) {
                            textureManager.register(resourceId, new DynamicTexture(resourceId::toString, MissingTextureAtlasSprite.generateMissingImage()));
                        }

                        loadableResourceIds.add(resourceId);
                    });

                    return loadableResourceIds;
                }, prepareExecutor)
                .thenCompose(synchronizer::wait)
                .thenAcceptAsync(loadableResourceIds -> {
                    loadableResourceIds.forEach(textureManager::getTexture);
                }, applyExecutor);
    }

    public static void include(String path){
        include(Identifier.fromNamespaceAndPath(Main.MOD_ID, path));
    }
    public static void include(Identifier resourceId){
        resourceIdList.add(resourceId);
    }
    public static void register() {
        Platform.CLIENT.registerReloadListener(ID, new ResourceReloadListener());
    }
}
