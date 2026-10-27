package pw.rxj.iron_quarry.platform;

import java.nio.file.Path;

public interface IPlatformLoader {
    boolean isModLoaded(String modId);
    Path getConfigDir();
    boolean isDedicatedServer();
}
