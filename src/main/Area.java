package main;

import objects.Block;
import objects.PhysicalObject;
import objects.Player;
import objects.Shell;
import processing.core.PVector;
import util.Props;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import static main.ScreenManager.instance;
import static objects.Shell.S;

public class Area {

    private static final float DISPLACEMENT = instance.getHeight() * Props.getLong("terrain displacement") / 100f;
    private static final double ROUGHNESS = Props.getDouble("terrain roughness");
    private static float[] coords;
    /**
     * With {@link LinkedHashSet}, the iterator goes through the elements in the order they were inserted.
     * Blocks are inserted top to bottom. The first to enter is the highest, the last to enter is at the bottom.
     *
     * @see Area#destroyBlocksInColumn(int, PVector, float) for use.
     */
    private static final ArrayList<LinkedHashSet<Block>> BLOCK_SETS = new ArrayList<>();
    private static final ArrayList<Block> HIGHEST = new ArrayList<>();
    private static final LinkedHashSet<Block> FALLING_BLOCKS = new LinkedHashSet<>();
    private static int MIN_BLOCK_Y, MAX_BLOCK_Y;


    public static boolean isFalling() {
        return !FALLING_BLOCKS.isEmpty();
    }

    private static int bound(int x) {
        if (x < 0) return 0;
        else if (x >= BLOCK_SETS.size()) return BLOCK_SETS.size() - 1;
        else return x;
    }

    private static int getColumnOf(float x) {
        return bound((int) x / Block.WIDTH);
    }

    private static int leftCol(PhysicalObject o) {
        return getColumnOf(o.getX());
    }

    private static int rightCol(PhysicalObject o) {
        return getColumnOf(o.getX() + o.getWidth());
    }

    private static float restrictBlockY(float y) {
        if (y < MIN_BLOCK_Y) return MIN_BLOCK_Y;
        if (y > MAX_BLOCK_Y) return MAX_BLOCK_Y;
        else return y;
    }

    public static void generateTerrain(int playerHeight) {
        MIN_BLOCK_Y = playerHeight;
        MAX_BLOCK_Y = instance.getHeight() - playerHeight * 3;
        Block.loadImage();
        int blocksInRow = instance.getWidth() / Block.WIDTH;
        coords = new float[blocksInRow];
        coords[0] = (instance.getHeight() / 2f + (instance.random(1) * DISPLACEMENT * 2) - DISPLACEMENT);
        coords[blocksInRow - 1] = (instance.getHeight() / 2f + (instance.random(1) * DISPLACEMENT * 2) - DISPLACEMENT);
        calcSegment(0, blocksInRow - 1, (int) (DISPLACEMENT * ROUGHNESS));

        // Fix height
        for (int i = 0; i < coords.length; i++) {
            coords[i] = restrictBlockY(instance.getHeight() - coords[i]);
        }

        generateBlocks();
    }

    private static void calcSegment(int left, int right, int displacement) {
        // Bottom: nothing between left and right.
        if (right - left == 1) return;

        int index = (left + right) / 2;
        float value = (coords[left] + coords[right]) / 2;
        value += instance.random(-1, 1) * displacement;
        coords[index] = value;

        // Recurse to the left half.
        calcSegment(left, index, (int) (displacement * ROUGHNESS));
        // Recurse to the right half.
        calcSegment(index, right, (int) (displacement * ROUGHNESS));
    }

    static void clearTerrain() {
        HIGHEST.clear();
        BLOCK_SETS.clear();
        FALLING_BLOCKS.clear();
    }

    public static void resetTerrain() {
        clearTerrain();
        generateTerrain(MIN_BLOCK_Y);
    }

    private static void generateBlocks() {
        for (int col = 0; col < coords.length; col++) {
            LinkedHashSet<Block> these = new LinkedHashSet<>();
            float y = coords[col];
            Block b = new Block(col * Block.WIDTH, y);
            these.add(b);
            HIGHEST.add(b);
            y += Block.height();
            while (y < instance.getHeight()) {
                b = new Block(col * Block.WIDTH, y);
                these.add(b);
                y += Block.height();
            }
            BLOCK_SETS.add(these);
        }
    }

    private static void drawBlocks() {
        BLOCK_SETS.forEach(set -> set.forEach(Block::drawSelf));
    }

    private static Block highestUnder(PhysicalObject o) {
        int left = leftCol(o);
        int right = rightCol(o);

        Block high = HIGHEST.get(left);
        for (int i = left + 1; i < right; i++) {
            Block current = HIGHEST.get(i);
            if (current.isHigher(high))
                high = current;
        }

        return high;
    }

    public static PVector getHighPointUnder(PhysicalObject o) {
        Block b = highestUnder(o);
        return new PVector(b.getX(), b.getY());
    }

    public static float getHeightUnder(PhysicalObject o) {
        return highestUnder(o).getY();
    }

    private static void pullDownPlayer(Player p) {
        Block high = highestUnder(p);
        float highY = high.getY();

        // If the player is about to fall under ground
        if (p.nextPosition().y + p.getHeight() > highY) {
            // stop the tank.
            p.setY(highY - p.getHeight());
            p.halt();

            Player o = instance.game.getOpponent(p);
            if (p.intersects(o))
                p.setY(o.getY() - p.getHeight());
        }
        // If the player is in the air
        else if (p.getY() + p.getHeight() < highY)
            p.applyGravity();
    }

    public static void detectShellCollision() {
        int left = leftCol(S), right = rightCol(S);
        // Check further to each side for better detection.
        left = bound(left - 1);
        right = bound(right + 1);

        columnIterator:
        for (int i = left; i <= right; i++) {
            LinkedHashSet<Block> col = BLOCK_SETS.get(i);
            for (Block b : col)
                if (S.intersects(b)) {
                    Shell.explode();
                    // No need to search for other collisions
                    break columnIterator;
                }
        }
    }

    private static void destroyBlocksInColumn(int i, PVector explosionOrigin, float expRad) {
        LinkedHashSet<Block> blocks = BLOCK_SETS.get(i);
        LinkedHashSet<Block> destroyed = new LinkedHashSet<>();

        // Store all destroyed blocks.
        for (Block b : blocks)
            if (b.centerDistance(explosionOrigin) <= expRad)
                destroyed.add(b);

        // Note: cannot remove the blocks in one iteration because it will cause ConcurrentModificationException.
        Block lastDestroyed = null;
        // Remove them from the original set.
        // Blocks are iterated in insertion order due to LinkedHashSet.
        // So the last destroyed block is the bottom of the destroyed.
        for (Block d : destroyed) {
            lastDestroyed = d;
            blocks.remove(d);
        }

        // Nothing else to do, if nothing was destroyed.
        if (lastDestroyed == null) return;

        // Now set new height.
        // Start at a block below any possible
        Block topStill = new Block(i * Block.WIDTH, instance.getHeight() + 1);
        for (Block b : blocks) {
            // Blocks higher than the destroyed need to be pulled down.
            if (b.isHigher(lastDestroyed)) {
                b.startFalling();
                FALLING_BLOCKS.add(b);
            }
            // If the block is not falling, it is a valid candidate for top still.
            else if (b.isHigher(topStill))
                topStill = b;
        }
        HIGHEST.set(i, topStill);
    }

    public static void destroyBlocks(PVector explosionOrigin, float expRad) {
        int left = bound((int) ((explosionOrigin.x - expRad) / Block.WIDTH));
        int right = bound((int) ((explosionOrigin.x + expRad) / Block.WIDTH));
        for (int i = left; i <= right; i++)
            destroyBlocksInColumn(i, explosionOrigin, expRad);
    }

    private static void pullDownBlocks() {
        if (FALLING_BLOCKS.isEmpty()) return;

        LinkedHashSet<Block> grounded = new LinkedHashSet<>();
        for (Block falling : FALLING_BLOCKS) {
            int col = (int) (falling.getX()) / Block.WIDTH;
            Block high = HIGHEST.get(col);
            float groundY = high.getY();
            if (falling.nextPosition().y + Block.height() > groundY) {
                falling.setY(groundY - Block.height());
                falling.halt();
                grounded.add(falling);
                HIGHEST.set(col, falling);  // new highest
            }
            else falling.applyGravity();
        }

        FALLING_BLOCKS.removeAll(grounded);
    }

    private static void moveFalling() {
        FALLING_BLOCKS.forEach(PhysicalObject::move);
    }

    public static void gravitation() {
        pullDownBlocks();
        moveFalling();
        drawBlocks();
        pullDownPlayer(instance.game.player1);
        pullDownPlayer(instance.game.player2);
    }

}
