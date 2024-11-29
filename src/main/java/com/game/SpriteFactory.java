package com.game;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.dsl.components.KeepOnScreenComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.texture.Texture;
import javafx.animation.*;
import javafx.util.Duration;

public class SpriteFactory implements EntityFactory {
    private int choice1, choice2;
    private int map_choice;
    public static final int GRID_SIZE = 32;

    public SpriteFactory(int choice1, int choice2, int map_choice) {
        this.choice1 = choice1;
        this.choice2 = choice2;
        this.map_choice = map_choice;
    }

    @Spawns("player1")
    public Entity newPlayer1(SpawnData data) {
      //  System.out.println("创建角色");
        return FXGL.entityBuilder(data)
                .type(GameElementType.PLAYER1)
                .with(new KeepOnScreenComponent())
                .with(new playerComponent(choice1))
                .at(GRID_SIZE*8 + 2, GRID_SIZE*2 + 2)
                .bbox(BoundingShape.box(25, 25))
                .collidable()
                .build();
    }
    @Spawns("player2")
    public Entity newPlayer2(SpawnData data) {
        //  System.out.println("创建角色");
        return FXGL.entityBuilder(data)
                .type(GameElementType.PLAYER2)
                .with(new KeepOnScreenComponent())
                .with(new playerComponent2(choice2))
                .at(GRID_SIZE*(8+14) + 2, GRID_SIZE*2 + 2)
                .bbox(BoundingShape.box(25, 25))
                .collidable()
                .build();
    }
    @Spawns("block")
    public Entity newBlock(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameElementType.BLOCK)
                .with(new KeepOnScreenComponent())
                .neverUpdated()
                .bbox(BoundingShape.box(27, 27))
                .collidable()
                .build();
    }
    @Spawns("breakableBlock")
    public Entity newBreakableBlock(SpawnData data){
        Texture block1 = null;
        Texture block2 = null;
        breakableComponent breakableC = new breakableComponent();
        if (map_choice == 1) {
            block1 = FXGL.texture("Skeleton.png", 32, 32);
            block2 = FXGL.texture("Skeleton1.png", 32, 32);
        }if (map_choice == 2){
            block1 = FXGL.texture("scarecow.png", 32, 32);
            block2 = FXGL.texture("grave.png", 32, 32);
        } if (map_choice == 3 || map_choice == 0) {
            block1 = FXGL.texture("brownBear.png", 32, 32);
            block2 = FXGL.texture("blueBear.png", 32, 32);
        }

        Texture breakableTexture = block1;
        breakableC.bearType = 0;
        if(FXGLMath.randomBoolean()){
            breakableTexture = block2;
            breakableC.bearType = 1;
        }
        return FXGL.entityBuilder(data)
                .type(GameElementType.BREAKABLEBLOCK)
                .with(new KeepOnScreenComponent())
                .with(breakableC)
                .neverUpdated()
                .view(breakableTexture)
                .bbox(BoundingShape.box(27, 27))
                .collidable()
                .build();
    }
    @Spawns("breakableBlock_client")
    public Entity newBreakableBlock_client(SpawnData data){
        Texture brownBear = null;
        Texture blueBear = null;
        breakableComponent breakableC = new breakableComponent();
        if (map_choice == 1) {
            brownBear = FXGL.texture("Skeleton.png", 32, 32);
            blueBear = FXGL.texture("Skeleton1.png", 32, 32);
        }if (map_choice == 2) {
            brownBear = FXGL.texture("scarecow.png", 32, 32);
            blueBear = FXGL.texture("grave.png", 32, 32);
        }if (map_choice == 3 || map_choice == 0) {
            brownBear = FXGL.texture("brownBear.png", 32, 32);
            blueBear = FXGL.texture("blueBear.png", 32, 32);
        }
        Texture breakableTexture = brownBear;
        int bearColor = data.get("bearColor");
        if(bearColor == 1){
            breakableTexture = blueBear;
        }
        return FXGL.entityBuilder(data)
                .type(GameElementType.BREAKABLEBLOCK)
                .with(new KeepOnScreenComponent())
                .with(breakableC)
                .neverUpdated()
                .view(breakableTexture)
                .bbox(BoundingShape.box(27, 27))
                .collidable()
                .build();
    }
    @Spawns("bomb")
        public Entity newBomb(SpawnData data){
        Texture bombpng = FXGL.texture("bomb.png",20,20);
        ScaleTransition st = new ScaleTransition(Duration.seconds(1.2),bombpng);
        st.setToX(1.5);st.setToY(1.5);
        st.setAutoReverse(true);st.setCycleCount(Animation.INDEFINITE);
        st.play();
        Entity bomb = FXGL.entityBuilder(data)
                .type(GameElementType.BOMB)
                .with(new KeepOnScreenComponent())
                .with(new ExpireCleanComponent(Duration.seconds(1))) //多久后移除炸弹
                .view(bombpng)
                .bbox(BoundingShape.box(18, 18))
                .collidable()
                .build();
        return bomb;
    }

    @Spawns("fakeBomb")
    public Entity newFakeBomb(SpawnData data){
        Texture bomb = FXGL.texture("bomb.png",20,20);
        ScaleTransition st = new ScaleTransition(Duration.seconds(1.2),bomb);
        st.setToX(1.3);st.setToY(1.3);
        st.setAutoReverse(true);st.setCycleCount(Animation.INDEFINITE);
        st.play();
        Entity fakeBomb = FXGL.entityBuilder(data)
                .type(GameElementType.FAKEBOMB)
                .with(new KeepOnScreenComponent())
                .with(new ExpireCleanComponent(Duration.seconds(10))) //10s后清除实体
                .view(bomb)
                .bbox(BoundingShape.box(20, 20))
                //这里的bbox只是为了能监听到碰撞结束，并不代表不能通过，因为block实体组里没有设置fakeBomb枚举类
                .collidable()
                .build();
        return fakeBomb;
    }

    @Spawns("singleBoom")
    public Entity newSingleBoom(SpawnData data){
        Texture single = FXGL.texture("singleboom.png",32,32);
        Duration duration = Duration.seconds(0.9);
        Entity boom = FXGL.entityBuilder(data)
                .type(GameElementType.BOOM)
                .view(single)
                .bbox(BoundingShape.box(18, 20))
                .collidable()
                .build();
        ScaleTransition st = new ScaleTransition(duration,single);
        st.setToX(0.9);
        st.setToY(0.9);
        //淡出动画
        FadeTransition ft = new FadeTransition(duration,single);
        ft.setToValue(0.5);
        //同时执行动画
        ParallelTransition pt = new ParallelTransition(st,ft);
        pt.setOnFinished(event->boom.removeFromWorld()); //另一种移除爆炸实体方式，boom是存一下FXGL.entitybuilder
        pt.play();
        return boom;
    }
    @Spawns("toyBomb")
    public Entity newToyBomb(SpawnData data){
        Texture toyBomb = FXGL.texture("toyBomb.png",30,30);
        toyBomb.brighter();
        Duration duration = Duration.seconds(1.5);
        Entity toybomb = FXGL.entityBuilder(data)
                .type(GameElementType.TOYBOMB)
                .with(new toyComponent())
                .view(toyBomb)
                .bbox(BoundingShape.box(18, 20))
                .collidable()
                .build();
        ScaleTransition st = new ScaleTransition(Duration.seconds(1.2),toyBomb);
        st.setToX(1.2);st.setToY(1.2);
        st.setAutoReverse(true);st.setCycleCount(Animation.INDEFINITE);
        TranslateTransition tt = new TranslateTransition(Duration.seconds(1.2),toyBomb);
        tt.setByY(-2);
        tt.setAutoReverse(true);tt.setCycleCount(Animation.INDEFINITE);
        ParallelTransition pt = new ParallelTransition(st,tt);
        pt.play();
        return toybomb;
    }
    @Spawns("toyBottle")
    public Entity newToyBottle(SpawnData data){
        Texture toyBottle = FXGL.texture("toyBottle.png",25,26);
        toyBottle.brighter();
        Duration duration = Duration.seconds(1.5);
        Entity toybottle = FXGL.entityBuilder(data)
                .type(GameElementType.TOYBOTTLE)
                .with(new toyComponent())
                .view(toyBottle)
                .bbox(BoundingShape.box(18, 20))
                .collidable()
                .build();
        ScaleTransition st = new ScaleTransition(Duration.seconds(1.2),toyBottle);
        st.setToX(1.2);st.setToY(1.2);
        st.setAutoReverse(true);st.setCycleCount(Animation.INDEFINITE);
        TranslateTransition tt = new TranslateTransition(Duration.seconds(1.2),toyBottle);
        tt.setByY(-2);
        tt.setAutoReverse(true);tt.setCycleCount(Animation.INDEFINITE);
        ParallelTransition pt = new ParallelTransition(st,tt);
        pt.play();
        return toybottle;
    }
    @Spawns("toyShoe")
    public Entity newToyShoe(SpawnData data){
        Texture toyShoe = FXGL.texture("toyShoe.png",25,26);
        toyShoe.brighter();
        Duration duration = Duration.seconds(1.5);
        Entity toyshoe = FXGL.entityBuilder(data)
                .type(GameElementType.TOYSHOE)
                .with(new toyComponent())
                .view(toyShoe)
                .bbox(BoundingShape.box(18, 20))
                .collidable()
                .build();
        ScaleTransition st = new ScaleTransition(Duration.seconds(1.2),toyShoe);
        st.setToX(1.2);st.setToY(1.2);
        st.setAutoReverse(true);st.setCycleCount(Animation.INDEFINITE);
        TranslateTransition tt = new TranslateTransition(Duration.seconds(1.2),toyShoe);
        tt.setByY(-2);
        tt.setAutoReverse(true);tt.setCycleCount(Animation.INDEFINITE);
        ParallelTransition pt = new ParallelTransition(st,tt);
        pt.play();
        return toyshoe;
    }
    @Spawns("toyPoison")
    public Entity newToyPoison(SpawnData data){
        Texture toyPoison = FXGL.texture("toyPoison.png",25,26);
        toyPoison.brighter();
        Duration duration = Duration.seconds(1.5);
        Entity toypoison = FXGL.entityBuilder(data)
                .type(GameElementType.TOYPOISON)
                .with(new toyComponent())
                .view(toyPoison)
                .bbox(BoundingShape.box(18, 20))
                .collidable()
                .build();
        ScaleTransition st = new ScaleTransition(Duration.seconds(1.2),toyPoison);
        st.setToX(1.2);st.setToY(1.2);
        st.setAutoReverse(true);st.setCycleCount(Animation.INDEFINITE);
        TranslateTransition tt = new TranslateTransition(Duration.seconds(1.2),toyPoison);
        tt.setByY(-2);
        tt.setAutoReverse(true);tt.setCycleCount(Animation.INDEFINITE);
        ParallelTransition pt = new ParallelTransition(st,tt);
        pt.play();
        return toypoison;
    }

    @Spawns("Map")
    public Entity newSnowMap(SpawnData data){
        Texture Map = null;
        System.out.println("新建地图文件,选择的地图为" + map_choice);
        if (map_choice == 1){
            Map = FXGL.texture("castleLevel.png",544,544);
        }
        if (map_choice == 3 || map_choice == 0){  //因联机不可选择地图
            Map = FXGL.texture("SnowLevel.png",544,544);
        }
        if (map_choice == 2){
            Map = FXGL.texture("GrassLevel.png",544,544);
        }
        Entity map = FXGL.entityBuilder(data)
                .with(new KeepOnScreenComponent())
                .view(Map)
                .build();
        return map;
    }

}
