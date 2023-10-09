package pw.rxj.iron_quarry.interfaces;

import net.minecraft.util.Identifier;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.records.TexturePosition;
import pw.rxj.iron_quarry.types.Face;
import pw.rxj.iron_quarry.types.IoState;
import pw.rxj.iron_quarry.util.MachineConfiguration;

public interface BasicMachineLogic {
    MachineConfiguration getMachineConfiguration();

    default Identifier getIoTextureId(){
        return new Identifier(Main.MOD_ID, "textures/gui/options_io.png");
    }

    default TexturePosition getIoTexturePosition(Face face){
        MachineConfiguration config = getMachineConfiguration();
        IoState ioState = config.getIoState(face);

        final int u;
        final int v;

        switch (ioState) {
            case ORANGE -> {
                u = 8;
                v = 0;
            }
            case BLUE -> {
                u = 16;
                v = 0;
            }
            case GREEN -> {
                u = 24;
                v = 0;
            }
            case PURPLE -> {
                u = 32;
                v = 0;
            }
            default -> {
                u = 0;
                v = 0;
            }
        }

        return new TexturePosition(u, v, 8, 8);
    }
}
