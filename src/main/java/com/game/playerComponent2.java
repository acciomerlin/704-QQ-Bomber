package com.game;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityGroup;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.time.LocalTimer;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import java.util.List;


public class playerComponent2 extends Component {
   public int INIT_SPEED = 40;
    private Point2D playerCenter;
    public int speed;
    public LocalTimer unbeatTimer;
    public Duration unbeatDealy = Duration.seconds(1.4); //受伤后的无敌时间
    public int myBomb = 1; //初始可一次性放1个炸弹
    public int addFire = 0;
    public int mylife = 3;
    private boolean isMoving = false;
    private BoundingBoxComponent bbox;
    private LazyValue<EntityGroup> blocks = new LazyValue<>(() -> entity.getWorld().getGroup(
            GameElementType.BLOCK, GameElementType.BREAKABLEBLOCK, GameElementType.BOMB));
    //block是实体组，储存了枚举类为BLOCK和BOMB的元素，下面循环里如果识别到实体是这个实体组里的就碰撞，当成障碍物
    private AnimationChannel acUp, acDown, acLeft, acRight;

    public AnimatedTexture getAt() {
        return at;
    }

    private AnimatedTexture at;
    private boolean stopFlag;
    public Direction dir;
    private Vec2 velocity = new Vec2();
    public int choice = 0;
    public playerComponent2(int choice) {//构造函数，加入动作帧图
        this.choice = choice;
        velocity.set(0, 0);
        System.out.println("正在调用 地图角色实体//////");
        dir = Direction.DOWN;
        System.out.println("玩家二选择的角色为"+choice);
        if (choice == 1) {
            acUp = new AnimationChannel(FXGL.image("player1.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 0, 3);
            acRight = new AnimationChannel(FXGL.image("player1.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 4, 7);
            acDown = new AnimationChannel(FXGL.image("player1.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 8, 11);
            acLeft = new AnimationChannel(FXGL.image("player1.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 12, 15);
        }

        if (choice == 2) {
            acUp = new AnimationChannel(FXGL.image("player2.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 0, 3);
            acRight = new AnimationChannel(FXGL.image("player2.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 4, 7);
            acDown = new AnimationChannel(FXGL.image("player2.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 8, 11);
            acLeft = new AnimationChannel(FXGL.image("player2.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 12, 15);
        }

        if (choice == 3) {
            acUp = new AnimationChannel(FXGL.image("player3.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 0, 3);
            acRight = new AnimationChannel(FXGL.image("player3.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 4, 7);
            acDown = new AnimationChannel(FXGL.image("player3.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 8, 11);
            acLeft = new AnimationChannel(FXGL.image("player3.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 12, 15);
        }

        if (choice == 4) {
            acUp = new AnimationChannel(FXGL.image("player4.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 0, 3);
            acRight = new AnimationChannel(FXGL.image("player4.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 4, 7);
            acDown = new AnimationChannel(FXGL.image("player4.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 8, 11);
            acLeft = new AnimationChannel(FXGL.image("player4.png"),
                    4, 112 / 4, 112 / 4,
                    Duration.seconds(0.6), 12, 15);
        }

            at = new AnimatedTexture(acDown);
            playerCenter = new Point2D(44, 44);

    }
    @Override
    public void onAdded(){
            entity.getViewComponent().addChild(at);
        }

    @Override
    public void onUpdate(double tpf){
        isMoving = false;
        playerCenter = entity.getCenter();
        //   System.out.println(playerCenter);
        /*if (dir == Direction.DOWN || dir == Direction.UP){
            entity.translateY((double) (speed) /60);
            entity.translateX(0);
        }else{
            entity.translateX((double) (speed) /60);
            entity.translateY(0);
        }*/

        // System.out.println("Now direction is " + dir);
        if (dir == Direction.UP){
            if (at.getAnimationChannel() != acUp || stopFlag){
                at.loopAnimationChannel(acUp);
                stopFlag = false;
            }
        }
        if (dir == Direction.DOWN){
            if (at.getAnimationChannel() != acDown || stopFlag){
                at.loopAnimationChannel(acDown);
                stopFlag = false;
            }
        }
        if (dir == Direction.LEFT){
            if (at.getAnimationChannel() != acLeft || stopFlag){
                at.loopAnimationChannel(acLeft);
                stopFlag = false;
            }
        }
        if (dir == Direction.RIGHT){
            if (at.getAnimationChannel() != acRight || stopFlag){
                at.loopAnimationChannel(acRight);
                stopFlag = false;
            }
        }
        speed = (int) (speed * 0.9);
        if (Math.abs(speed) < 1) {
            speed = 0;
            at.stop();
            stopFlag = true;
        }
    }

    public void moveUp(){
        if (isMoving)
            return;
        isMoving = true;
        dir = Direction.UP;
        speed = -INIT_SPEED;
        velocity.set((float) (0), (float) (speed));
        move();
    }
    public void moveDown(){
        if (isMoving)
            return;
        isMoving = true;
        dir = Direction.DOWN;
        speed = INIT_SPEED;
        velocity.set((float) (0), (float) (speed));
        move();
    }
    public void moveLeft(){
        if (isMoving)
            return;
        isMoving = true;
        dir = Direction.LEFT;
        speed = -INIT_SPEED;
        velocity.set((float) (speed), (float) (0));
        move();
    }
    public void moveRight(){
        if (isMoving)
            return;
        isMoving = true;
        dir = Direction.RIGHT;
        speed = INIT_SPEED;
        velocity.set((float) (speed), (float) (0));
        move();
    }

    private void move() {
        List<Entity> blockList;
        blockList = blocks.get().getEntitiesCopy();
        int length = Math.round(velocity.length());
        velocity.normalizeLocal();
        for (int i = 0; i < length; i++) {
            //System.out.println("速度为:" + length);
            entity.translate(velocity.x / 60, velocity.y / 60);
            boolean collision = false;
            for (int j = 0; j < blockList.size(); j++) {
                if (blockList.get(j).getBoundingBoxComponent().isCollidingWith(bbox)) {//碰撞判断，bbox直接能自动匹配对应实体
                    collision = true;
                    break;
                }
            }
            //运动, 遇到障碍物回退
            if (collision) {
                entity.translate(-velocity.x, -velocity.y);
                break;
            }

        }
    }


}