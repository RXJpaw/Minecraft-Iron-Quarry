package pw.rxj.iron_quarry.util;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class BlockEntryBuilder {
    private Block block;
    private BlockItem blockItem;
    private Identifier identifier;

    public BlockEntryBuilder setBlock(Block block) {
        this.block = block;
        return this;
    }
    public BlockEntryBuilder setItemSettings(Item.Settings settings) {
        if(this.block == null) throw new IllegalStateException();

        this.blockItem = new BlockItem(this.block, settings);
        return this;
    }

    public BlockEntryBuilder setIdentifier(String namespace, String path) {
        this.identifier = new Identifier(namespace, path);
        return this;
    }

    public Block getBlock() {
        return block;
    }
    public Identifier getId() {
        return identifier;
    }
    public BlockItem getBlockItem(){
        return blockItem;
    }
}
