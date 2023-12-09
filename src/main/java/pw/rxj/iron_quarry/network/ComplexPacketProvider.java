package pw.rxj.iron_quarry.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface ComplexPacketProvider<T> {
    Identifier getChannelId();

    @Nullable T read(PacketByteBuf buf);
}
