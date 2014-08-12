package at.yawk.tilecounter;

import at.yawk.columbus.Chunk;
import at.yawk.columbus.LevelFolder;
import at.yawk.columbus.World;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.Value;

/**
 * @author yawkat
 */
public class TileCounter {
    public static void main(String[] args) throws IOException {
        int limit = 10;

        List<Entry> top = new ArrayList<>();

        LevelFolder folder = new LevelFolder();
        folder.read(Paths.get(args[0]));
        World world = folder.getWorldIfExists(LevelFolder.WORLD_REGULAR);

        for (Chunk chunk : world.getAllChunks()) {
            int count = chunk.getTileEntities().size();
            Entry entry = new Entry(chunk.getChunkX(), chunk.getChunkZ(), count);
            for (int i = 0; i < top.size(); i++) {
                if (top.get(i).getCount() < count) {
                    top.add(i, entry);
                    while (top.size() > limit) {
                        top.remove(top.size() - 1);
                    }
                    break;
                }
            }
            if (top.size() < limit) {
                top.add(entry);
            }
        }

        String format = "%7s%7s%7s%7s%7s\n";
        System.out.printf(format, "Count", "ChunkX", "ChunkZ", "X", "Z");
        for (Entry entry : top) {
            System.out.printf(format,
                              entry.getCount(),
                              entry.getX(),
                              entry.getZ(),
                              entry.getX() * 16,
                              entry.getZ() * 16);
        }
    }

    @Value
    private static class Entry {
        private final int x;
        private final int z;
        private final int count;
    }
}
