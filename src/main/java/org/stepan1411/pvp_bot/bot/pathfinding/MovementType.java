package org.stepan1411.pvp_bot.bot.pathfinding;

/**
 * Types of movements, inspired by Baritone's movement system
 */
public enum MovementType {
    TRAVERSE,      // Walk forward on same level
    ASCEND,        // Jump up one block while moving forward
    DESCEND,       // Walk down one block
    DIAGONAL,      // Diagonal movement
    FALL,          // Fall down multiple blocks
    PILLAR,        // Jump straight up (pillar)
    PARKOUR,       // Jump across gaps
    CLIMB          // Climb ladders/vines
}
