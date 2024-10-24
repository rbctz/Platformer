package entities;

import main.Game;

import java.awt.geom.Rectangle2D;

import static utilz.Constants.Directions.LEFT;
import static utilz.Constants.Directions.RIGHT;
import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.*;

public abstract class Enemy extends Entity{

    protected int animationIndex, enemyState, enemyType;
    protected int animationSpeed = 25, animationTick;
    protected boolean firstUpdate = true;
    protected boolean inAir = false;
    protected float fallSpeed;
    protected float gravity = 0.04f * Game.SCALE;
    protected float xSpeed;
    protected float walkSpeed = 0.4f * Game.SCALE;
    protected int walkDir;
    protected int tileY;
    protected float attackRange = 18 * Game.SCALE;
    protected int maxHP = 100;
    protected int currHP = maxHP;
    protected boolean active = true;
    protected boolean attackChecked;


    public Enemy(float x, float y, int width, int height, int enemyType) {
        super(x, y, width, height);
        this.enemyType = enemyType;
        initHitbox(x, y, width, height);
        maxHP = GetMaxHP(enemyType);
        currHP = maxHP;
    }

    protected void updateAnimationTick() {
        animationTick++;
        if (animationTick >= animationSpeed) {
            animationTick = 0;
            animationIndex++;
            if (animationIndex >= GetSpriteAmount(PIG, enemyState)) {
                animationIndex = 0;
                switch (enemyState) {
                    case ATTACK, HIT -> enemyState = IDLE;
                    case DEAD -> active = false;
                }
            }
        }
    }

    protected void firstUpdateCheck(int[][] levelData) {
        if (firstUpdate) {
            firstUpdate = false;
            if (IsEntityInAir(hitbox, levelData)) {
                inAir = true;
            }
        }
    }

    protected void updateInAir(int[][] levelData) {
        if (CanMoveHere(hitbox.x, hitbox.y + fallSpeed, hitbox.width, hitbox.height, levelData)) {
            hitbox.y += fallSpeed;
            fallSpeed += gravity;
        } else {
            inAir = false;
            hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, fallSpeed);
            tileY = (int) (hitbox.y / Game.TILE_SIZE);
        }
    }

    protected void move(int[][] levelData) {
        xSpeed = 0;
        if (walkDir == LEFT)
            xSpeed -= walkSpeed;
        else
            xSpeed += walkSpeed;

        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, levelData)) {
            if (IsFloor(hitbox, xSpeed, levelData)) {
                hitbox.x += xSpeed;
                return;
            }
        }
        changeWalkDir();
    }

    protected void turnTowardsPlayer(Player player) {
        if (player.hitbox.x > hitbox.x)
            walkDir = RIGHT;
        else
            walkDir = LEFT;
    }

    protected boolean canSeePlayer(int[][] levelData, Player player) {
        int playerTileY = (int) (player.hitbox.y / Game.TILE_SIZE);
        if (playerTileY == tileY) {
            if (isPlayerInRange(player)) {
                if(IsSightClear(hitbox, player.hitbox, tileY, levelData))
                    return true;
            }
        }
        return false;
    }

    protected boolean isPlayerInRange(Player player) {
        int distance = (int) Math.abs(player.hitbox.x - hitbox.x);

        return distance <= attackRange * 5;
    }

    protected boolean isPlayerInAttackRange(Player player) {
        int distance = (int) Math.abs(player.hitbox.x - hitbox.x);
        if (xSpeed < 0)
            return distance <= attackRange + 7 * Game.SCALE;
        else
            return distance <= attackRange;

    }

    protected void newState(int enemyState) {
        this.enemyState = enemyState;
        resetAnimation();
    }

    protected void changeWalkDir() {
        if (walkDir == LEFT)
            walkDir = RIGHT;
        else
            walkDir = LEFT;
    }

    protected void resetAnimation() {
        animationIndex = 0;
        animationTick = 0;
    }

    protected void hurt(int amount) {
        currHP -= amount;
        if (currHP > 0)
            newState(HIT);
        else
            newState(DEAD);
    }

    protected void checkEnemyHit(Rectangle2D.Float attackBox, Player player) {
        if (attackBox.intersects(player.hitbox))
            player.changeHP(GetEnemyDamage(enemyType) * (-1));
        attackChecked = true;
    }

    protected void resetEnemy() {
        hitbox.x = x;
        hitbox.y = y;
        firstUpdate = true;
        currHP = maxHP;
        newState(IDLE);
        active = true;
        fallSpeed = 0;
    }
}
