package at.yawk.tilecounter;

import at.yawk.columbus.*;
import at.yawk.columbus.nbt.TagCompound;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yawkat
 */
public class IdRemove {
    public static void main(String[] args) throws IOException {
        short toRemove = Short.parseShort(args[1]);

        Path region = Paths.get(args[0], "region");
        Lighter lighter = new Lighter();
        lighter.putDefaultBlockLightData();

        AtomicInteger read = new AtomicInteger();
        Files.list(region).forEach(regionFile -> {
            String name = regionFile.getFileName().toString();
            int endx = name.indexOf('.', 2);
            int rx = Integer.parseInt(name.substring(2, endx));
            int ry = Integer.parseInt(name.substring(endx + 1, name.indexOf('.', endx + 1)));
            World world = new World(new WorldProperties(256));
            try (DataInputStream input = new DataInputStream(Files.newInputStream(regionFile))) {
                world.readRegionFile(rx, ry, input);
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean modifiedRegion = false;
            for (Chunk chunk : world.getAllChunks()) {
                int removed = 0;
                for (int regionI = 0; regionI < 256 / 16; regionI++) {
                    ChunkSection section = chunk.getChunkSection(regionI);
                    for (Iterator<TagCompound> iterator = chunk.getTileEntities().iterator(); iterator.hasNext(); ) {
                        TagCompound tile = iterator.next();
                        int y = tile.getInt("y");
                        if (y >= regionI * 16 && y < (regionI + 1) * 16) {
                            int x = tile.getInt("x");
                            int z = tile.getInt("z");
                            if (section.getBlockId(x, y, z) == toRemove) {
                                section.setBlock(x, y, z, (short) 0, (byte) 0);
                                iterator.remove();
                                removed++;
                            }
                        }
                    }
                }
                if (removed != 0) {
                    modifiedRegion = true;
                    System.out.println(
                            "Removed " + removed + " tiles and blocks from chunk " + chunk.getChunkX() + " " +
                            chunk.getChunkZ());
                    chunk.calculateHeightMap(lighter);
                }
            }
            if (modifiedRegion) {
                try (DataOutputStream input = new DataOutputStream(Files.newOutputStream(regionFile))) {
                    world.writeRegionFile(rx, ry, input);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Saved region file #" + read.incrementAndGet());
            }
        });
    }
}
