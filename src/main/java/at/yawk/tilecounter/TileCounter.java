package at.yawk.tilecounter;

import at.yawk.columbus.Chunk;
import at.yawk.columbus.World;
import at.yawk.columbus.WorldProperties;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Value;

/**
 * @author yawkat
 */
public class TileCounter {
    public static void main(String[] args) throws IOException {
        int limit = 10;

        List<Entry> top = new ArrayList<>();

        Path region = Paths.get(args[0], "region");

        AtomicInteger read = new AtomicInteger();
        Files.list(region).forEach(regionFile -> {
            String name = regionFile.getFileName().toString();
            int endx = name.indexOf('.', 2);
            int x = Integer.parseInt(name.substring(2, endx));
            int z = Integer.parseInt(name.substring(endx + 1, name.indexOf('.', endx + 1)));
            World world = new World(new WorldProperties(256));
            try (DataInputStream input = new DataInputStream(Files.newInputStream(regionFile))) {
                world.readRegionFile(x, z, input);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            System.out.println("Read region file #" + read.incrementAndGet());
        });

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
