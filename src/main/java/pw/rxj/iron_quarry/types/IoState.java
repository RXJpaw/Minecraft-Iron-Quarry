package pw.rxj.iron_quarry.types;

import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import pw.rxj.iron_quarry.Main;
import pw.rxj.iron_quarry.records.TexturePosition;

import java.util.List;

public enum IoState {
    BLOCKED(0, "blocked"),
    ORANGE(1, "orange"),
    BLUE(2, "blue"),
    GREEN(3, "green"),
    PURPLE(4, "purple");

    private final int id;
    private final String name;

    private static final List<IoState> ALL = List.of(BLOCKED, ORANGE, BLUE, GREEN, PURPLE);
    public static Identifier getTextureId(){
        return new Identifier(Main.MOD_ID, "textures/gui/options_io.png");
    }
    public static SpriteIdentifier getSpriteId(){
        return new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.of(Main.MOD_ID, "gui/options_io"));
    }

    private IoState(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
    public int getId(){
        return this.id;
    }

    public IoState next() {
        int nextId = id + 1 == ALL.size() ? 0 : id + 1;
        return from(nextId);
    }
    public IoState previous() {
        int prevId = id == 0 ? ALL.size() - 1 : id - 1;
        return from(prevId);
    }

    public static IoState from(String name){
        for (IoState ioState : ALL) {
            if(ioState.getName().equalsIgnoreCase(name)){
                return ioState;
            }
        }

        return null;
    }
    public static IoState from(int id){
        return ALL.get(id);
    }

    public static TexturePosition getTexturePosition(IoState ioState){
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
