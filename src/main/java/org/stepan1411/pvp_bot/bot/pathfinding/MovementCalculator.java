package org.stepan1411.pvp_bot.bot.pathfinding;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates possible movements from a position
 * Inspired by Baritone's Moves system
 */
public class MovementCalculator {
    
    // Movement costs (inspired by Baritone's ActionCosts)
    public static final double WALK_ONE_BLOCK_COST = 20.0 / 4.317; // ticks / blocks per second
    public static final double WALK_ONE_IN_WATER_COST = 20.0 / 2.2;
    public static final double JUMP_ONE_BLOCK_COST = 20.0 / 4.0;
    public static final double FALL_N_BLOCKS_COST = 20.0 / 10.0; // falling is fast
    public static final double DIAGONAL_COST = WALK_ONE_BLOCK_COST * 1.414;
    public static final double CLIMB_COST = 20.0 / 2.35;
    
    private final World world;
    
    public MovementCalculator(World world) {
        this.world = world;
    }
    
    /**
     * Get all possible movements from a position
     * Inspired by Baritone's movement calculation
     */
    public List<PossibleMovement> getMovements(BlockPos from) {
        List<PossibleMovement> movements = new ArrayList<>();
        
        // Cardinal directions
        addTraverseMovement(movements, from, 1, 0);   // East
        addTraverseMovement(movements, from, -1, 0);  // West
        addTraverseMovement(movements, from, 0, 1);   // South
        addTraverseMovement(movements, from, 0, -1);  // North
        
        // Diagonal directions
        addDiagonalMovement(movements, from, 1, 1);   // SE
        addDiagonalMovement(movements, from, 1, -1);  // NE
        addDiagonalMovement(movements, from, -1, 1);  // SW
        addDiagonalMovement(movements, from, -1, -1); // NW
        
        // Vertical movements
        addPillarMovement(movements, from);
        addFallMovement(movements, from);
        
        // Climbing
        if (isClimbable(from) || isClimbable(from.up())) {
            addClimbMovement(movements, from);
        }
        
        return movements;
    }
    
    /**
     * Traverse - walk forward on same level or descend one block
     */
    private void addTraverseMovement(List<PossibleMovement> movements, BlockPos from, int dx, int dz) {
        BlockPos to = from.add(dx, 0, dz);
        
        // Check if destination is a stair block (special handling)
        if (isStairs(to)) {
            // Stairs are walkable, treat as ascend
            if (canPassThrough(to.up()) && canPassThrough(to.up(2))) {
                movements.add(new PossibleMovement(to, WALK_ONE_BLOCK_COST * 1.2, MovementType.ASCEND));
            }
            return;
        }
        
        // Check if can walk on same level
        if (canWalkOn(to)) {
            movements.add(new PossibleMovement(to, WALK_ONE_BLOCK_COST, MovementType.TRAVERSE));
            return;
        }
        
        // Check if can ascend (jump up one block)
        BlockPos ascend = to.up();
        if (canWalkOn(ascend) && canPassThrough(from.up()) && canPassThrough(from.up(2))) {
            movements.add(new PossibleMovement(ascend, JUMP_ONE_BLOCK_COST, MovementType.ASCEND));
            return;
        }
        
        // Check if can descend (walk down one block)
        BlockPos descend = to.down();
        if (canWalkOn(descend)) {
            movements.add(new PossibleMovement(descend, WALK_ONE_BLOCK_COST * 1.2, MovementType.DESCEND));
        }
    }
    
    /**
     * Diagonal movement
     */
    private void addDiagonalMovement(List<PossibleMovement> movements, BlockPos from, int dx, int dz) {
        BlockPos to = from.add(dx, 0, dz);
        
        // Check if both adjacent blocks are passable (can't cut corners)
        BlockPos side1 = from.add(dx, 0, 0);
        BlockPos side2 = from.add(0, 0, dz);
        
        if (!canPassThrough(side1) && !canPassThrough(side2)) {
            return; // Both sides blocked, can't move diagonally
        }
        
        if (canWalkOn(to)) {
            movements.add(new PossibleMovement(to, DIAGONAL_COST, MovementType.DIAGONAL));
        }
    }
    
    /**
     * Pillar - jump straight up
     */
    private void addPillarMovement(List<PossibleMovement> movements, BlockPos from) {
        BlockPos up = from.up();
        if (canWalkOn(up) && canPassThrough(from.up()) && canPassThrough(from.up(2))) {
            movements.add(new PossibleMovement(up, JUMP_ONE_BLOCK_COST * 1.5, MovementType.PILLAR));
        }
    }
    
    /**
     * Fall - fall down multiple blocks
     */
    private void addFallMovement(List<PossibleMovement> movements, BlockPos from) {
        // Check falling straight down
        for (int fallDist = 1; fallDist <= 4; fallDist++) {
            BlockPos down = from.down(fallDist);
            
            // Check if path is clear
            boolean pathClear = true;
            for (int i = 1; i < fallDist; i++) {
                if (!canPassThrough(from.down(i))) {
                    pathClear = false;
                    break;
                }
            }
            
            if (pathClear && canWalkOn(down)) {
                double cost = FALL_N_BLOCKS_COST * fallDist;
                movements.add(new PossibleMovement(down, cost, MovementType.FALL));
                break; // Only add first valid fall
            }
        }
    }
    
    /**
     * Climb - climb up ladders/vines
     */
    private void addClimbMovement(List<PossibleMovement> movements, BlockPos from) {
        BlockPos up = from.up();
        if (isClimbable(up) && canPassThrough(up.up())) {
            movements.add(new PossibleMovement(up, CLIMB_COST, MovementType.CLIMB));
        }
    }
    
    /**
     * Check if can walk on this position (has solid ground, clear space above)
     */
    private boolean canWalkOn(BlockPos pos) {
        BlockState belowState = world.getBlockState(pos.down());
        
        // Stairs are special - can walk on them
        if (isStairs(pos.down())) {
            return canPassThrough(pos) && canPassThrough(pos.up());
        }
        
        // Must have solid ground below
        if (!isSolid(pos.down())) {
            return false;
        }
        
        // Must have clear space for feet and head
        if (!canPassThrough(pos) || !canPassThrough(pos.up())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if can pass through this position (air, liquid, or non-solid)
     */
    private boolean canPassThrough(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isAir() || state.isLiquid() || !state.isSolidBlock(world, pos);
    }
    
    /**
     * Check if block is solid
     */
    private boolean isSolid(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return !state.isAir() && state.isSolidBlock(world, pos);
    }
    
    /**
     * Check if position is climbable
     */
    private boolean isClimbable(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof net.minecraft.block.LadderBlock ||
               state.getBlock() instanceof net.minecraft.block.VineBlock ||
               state.isOf(net.minecraft.block.Blocks.SCAFFOLDING) ||
               state.isOf(net.minecraft.block.Blocks.TWISTING_VINES) ||
               state.isOf(net.minecraft.block.Blocks.WEEPING_VINES);
    }
    
    /**
     * Check if position is a stair block
     */
    private boolean isStairs(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof net.minecraft.block.StairsBlock;
    }
    
    /**
     * Represents a possible movement
     */
    public static class PossibleMovement {
        public final BlockPos destination;
        public final double cost;
        public final MovementType type;
        
        public PossibleMovement(BlockPos destination, double cost, MovementType type) {
            this.destination = destination;
            this.cost = cost;
            this.type = type;
        }
    }
}
