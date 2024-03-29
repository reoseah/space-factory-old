package spacefactory.block.entity;

import com.google.common.collect.MapMaker;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import spacefactory.SpaceFactory;
import spacefactory.api.EU;

import java.util.*;

public class CableNetwork {
    private static final Map<World, CableNetwork> INSTANCES = new MapMaker().weakKeys().makeMap();

    public static CableNetwork of(World world) {
        if (!INSTANCES.containsKey(world)) {
            INSTANCES.put(world, new CableNetwork(world));
        }
        return INSTANCES.get(world);
    }

    protected final World world;
    /**
     * Cable paths from a cable position to all connected consumers, sorted by length.
     */
    private final Map<BlockPos, List<CablePath>> cache = new HashMap<>();

    private CableNetwork(World world) {
        this.world = world;
    }

    /**
     * Tries to send specified amount of energy through cables, returns amount that was sent.
     */
    public int send(BlockPos pos, int amount) {
        int left = amount;

        if (!this.cache.containsKey(pos)) {
            this.cache.put(pos, CablePath.buildPaths(this.world, pos, false));
        }

        if (this.cache.get(pos).size() == 0) {
            return 0;
        }

        int average = -Math.floorDiv(-amount, this.cache.get(pos).size()); // effectively "ceilDiv"
        int excess = 0;

        for (Iterator<CablePath> it = this.cache.get(pos).iterator(); it.hasNext(); ) {
            // TODO consider extracting this into CablePath#send?

            CablePath path = it.next();
            EU.Receiver endpoint = EU.findReceiver(this.world, path.end);
            if (endpoint == null || !endpoint.canReceiveEnergy(path.side)) {
                it.remove();
                if (this.cache.get(pos).isEmpty()) {
                    this.cache.remove(pos);
                }
                continue;
            } else {
                int min = Math.min(left, average + excess);
                int inserted = endpoint.receiveEnergy(min, path.side);
                left -= inserted;
                excess = min - inserted;

                for (BlockPos cablePos : path.cables) {
                    BlockEntity cablePosEntity = this.world.getBlockEntity(cablePos);
                    if (cablePosEntity instanceof CableBlockEntity cable) {
                        cable.current += inserted;
                        if (cable.current > cable.getTransferLimit()) {
                            world.removeBlock(cablePos, false);
                            for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, cablePos)) {
                                PacketByteBuf buf = PacketByteBufs.create();
                                buf.writeBlockPos(cablePos);
                                ServerPlayNetworking.send(player, SpaceFactory.id("burnt_cable"), buf);
                            }
                            break;
                        }
                    }
                }
            }
            if (left == 0) {
                break;
            }
        }

        return amount - left;
    }

    /**
     * Call this whenever a cable is added, changed or removed to correctly update the network.
     */
    public void onCableUpdate(BlockPos cable) {
        List<BlockPos> removed = new ArrayList<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(cable);
        while (!queue.isEmpty()) {
            BlockPos pos = queue.remove();
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.offset(direction);
                if (this.cache.containsKey(neighbor) || CablePath.isCable(this.world, neighbor)) {
                    this.cache.remove(neighbor);
                }
                if (CablePath.isCable(this.world, neighbor) && !queue.contains(neighbor) && !removed.contains(neighbor)) {
                    removed.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        this.cache.remove(cable);
    }
}
