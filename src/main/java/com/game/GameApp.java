package com.game;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.net.Client;
import com.almasb.fxgl.net.Server;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.time.LocalTimer;
import com.ui.*;
import javafx.animation.*;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.input.UserAction;
import javafx.scene.input.KeyCode;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.almasb.fxgl.dsl.FXGL.*;
import static java.lang.Thread.sleep;

public class GameApp extends GameApplication{
    private MyMainMenu myMainMenu;
    private Server<Bundle> server;
    private Client<Bundle> client;
    playerComponent playercomponent1;
    playerComponent2 playercomponent2;
    public static final int MAP_SIZE = 17;
    breakableComponent breakableCompo;
    toyComponent toyCompo;
    int maxToys = 20;
    Entity toy;
    Vector<Entity> toys = new Vector<>();  //为解决炸出两个炸弹不能都回复可碰撞的问题
    Entity fakeBomb1;
    Entity fakeBomb2;
    Entity realBomb;
    Entity player1;
    Entity player2;
    GameMode gameMode;
    private int p1_choice;
    private int p2_choice;
    private UserAction moveup;
    private UserAction movedown;
    private UserAction moveleft;
    private UserAction moveright;
    private int map_choice;
    private int[][] MAPCOPY = new int[20][20];
    private int[][] MAP = new int[20][20];
    private MyGameMenu myGameMenu;
    private AnimatedTexture at1;
    private AnimatedTexture at2;
    private Text full1;
    private Text full2;
    private Text full3;
    private Text full4;
    private Text countdownText;
    private static MediaPlayer bgMusic;
    private double originVolume = 0;
    private boolean isFirstInitInput = true;
    private LocalTimer toyTimer;
    private int ranToySave = 0;

    public GameApp(){
        System.out.println("构造器==> "+Thread.currentThread().getName());
    }
    public  void playBackgroundMusic() {
        String musicPath=null;
        if(MyMainMenu.MyMap==1){
            musicPath = "/assets/music/deadmap.mp3";
        }else if(MyMainMenu.MyMap==2){
            musicPath = "/assets/music/map2music.mp3";
        }else if(MyMainMenu.MyMap==3){
            musicPath = "/assets/music/snowmap.mp3";
        }else{
            musicPath = "/assets/music/snowmap.mp3";
        }
        Media media = new Media(getClass().getResource(musicPath).toExternalForm());
        bgMusic = new MediaPlayer(media);
        bgMusic.setVolume(0.2);
        bgMusic.setCycleCount(MediaPlayer.INDEFINITE);
        bgMusic.play();
    }
    private void silenceBackgroundmusic(){
        originVolume = bgMusic.getVolume();
        bgMusic.setVolume(0);
    }
    private void phonateBgMusic() {
        bgMusic.setVolume(originVolume);
    }
    private void initBackGroundElement(){
        MyMainMenu.flag=0;

        countdownText = FXGL.addVarText("countdown", 35, 400);
        countdownText.setFill(Color.web("#264653"));
        countdownText.textProperty().bind(FXGL.getip("countdown").asString("离本局游戏结束还有\n\n           %d s"));
        FXGL.getGameTimer().runAtInterval(() -> {
            int countdownValue = FXGL.geti("countdown");

            if (countdownValue > 0) {
                FXGL.set("countdown", countdownValue - 1);
            } else {
                silenceBackgroundmusic();
                FXGL.getGameTimer().clear();
                FXGL.getSceneService().pushSubScene(new MySubScene());
            }
        }, Duration.seconds(1));
        //静音按钮
        ImageView musicON = new ImageView(FXGL.image("music.png"));
        musicON.setFitWidth(50);
        musicON.setFitHeight(50);
        ImageView musicOFF = new ImageView(FXGL.image("musicOFF.png"));
        musicOFF.setFitWidth(56);
        musicOFF.setFitHeight(50);
        Button btn_stopbgmusic = new Button();
        btn_stopbgmusic.setGraphic(musicON);
        btn_stopbgmusic.setTranslateX(14);
        btn_stopbgmusic.setTranslateY(520);
        btn_stopbgmusic.setStyle("-fx-background-color: transparent;");
        AtomicBoolean m_flag = new AtomicBoolean(false);
        btn_stopbgmusic.setOnMouseEntered(e -> {
            if(!m_flag.get())
                btn_stopbgmusic.setGraphic(musicOFF);
        });
        btn_stopbgmusic.setOnMouseExited(e -> {
            if(!m_flag.get())
                btn_stopbgmusic.setGraphic(musicON);
            else
                btn_stopbgmusic.setGraphic(musicOFF);
        });
        btn_stopbgmusic.setOnMouseClicked(e->{
            if(!m_flag.get()){
                silenceBackgroundmusic();
                btn_stopbgmusic.setGraphic(musicOFF);
                m_flag.set(true);
            }else{
                phonateBgMusic();
                btn_stopbgmusic.setGraphic(musicON);
                m_flag.set(false);
            }
        });
        btn_stopbgmusic.setFocusTraversable(false);  //转移焦点以防影响其他键盘输入事件
        FXGL.addUINode(btn_stopbgmusic);

        //背景图
        ImageView background = new ImageView(FXGL.image("game_background.png"));
        background.setFitWidth(800); // 设置宽度为矩形的宽度
        background.setFitHeight(600); // 设置高度为矩形的高度
        Entity Background = FXGL.entityBuilder()
                .view(background)
                .at(0, 0) // 设置位置
                .buildAndAttach();
        //人物信息框
        ImageView inside1= new ImageView(FXGL.image("f2.png"));
        inside1.setFitWidth(180); // 设置宽度为矩形的宽度
        inside1.setFitHeight(110); // 设置高度为矩形的高度
        Entity firstRect = FXGL.entityBuilder()
                .at(10, 28) // 设置位置
                .view(inside1) // 使用蓝色矩形表示
                .buildAndAttach();
        ImageView inside2= new ImageView(FXGL.image("f2.png")); // 替换为您的矩形图像
        inside2.setFitWidth(180); // 设置宽度为矩形的宽度
        inside2.setFitHeight(110); // 设置高度为矩形的高度
        Entity nextRect = FXGL.entityBuilder()
                .at(10, 28+110+5) // 设置位置
                .view(inside2) // 使用蓝色矩形表示
                .buildAndAttach();

        //红瓶子full标识
        full1 = new Text("FULL");
        full1.setLayoutX(147);
        full1.setLayoutY(100);
        full1.setFill(Color.DARKRED);
        full1.setFont(Font.font("null", FontWeight.BOLD, 10));
        full1.setVisible(false);
        full2 = new Text("FULL");
        full2.setLayoutX(147);
        full2.setLayoutY(220);
        full2.setFill(Color.DARKRED);
        full2.setFont(Font.font("null", FontWeight.BOLD, 10));
        full2.setVisible(false);
        //鞋子道具full标志
        full3 = new Text("FULL");
        full3.setLayoutX(147);
        full3.setLayoutY(125);
        full3.setFill(Color.DARKRED);
        full3.setFont(Font.font("null", FontWeight.BOLD, 10));
        full3.setVisible(false);
        full4 = new Text("FULL");
        full4.setLayoutX(147);
        full4.setLayoutY(240);
        full4.setFill(Color.DARKRED);
        full4.setFont(Font.font("null", FontWeight.BOLD, 10));
        full4.setVisible(false);
    }
    private void local_initGame(){
        p1_choice = myMainMenu.getP1_choice();
        p2_choice = myMainMenu.getP2_choice();
        map_choice = myMainMenu.getMapChoice();
        if (map_choice == 1 ){
            for (int i = 0; i < 17; i++){
                for (int j = 0; j < 17; j++){
                    MAP[i][j] = MapMessage.levelThree[i][j];
                    MAPCOPY[i][j] = MapMessage.levelThreeCopy[i][j];
                }
            }
        } else if (map_choice == 2){
            for (int i = 0; i < 17; i++){
                for (int j = 0; j < 17; j++){
                    MAP[i][j] = MapMessage.levelTwo[i][j];
                    MAPCOPY[i][j] = MapMessage.levelTwoCopy[i][j];
                }
            }
        }else if (map_choice == 3){
            for (int i = 0; i < 17; i++){
                for (int j = 0; j < 17; j++){
                    MAP[i][j] = MapMessage.levelOne[i][j];
                    MAPCOPY[i][j] = MapMessage.levelOneCopy[i][j];
                }
            }
        }
        FXGL.getGameWorld().addEntityFactory(new SpriteFactory(p1_choice, p2_choice, map_choice));
        initBackGroundElement();
        FXGL.getGameWorld().spawn("Map", new SpawnData(7*32, 32));
        for (int i = 0; i < MAP_SIZE; i++){
            for (int j = 0; j < MAP_SIZE; j++) {
                if (MAPCOPY[j][i] == 1) {
                    FXGL.getGameWorld().spawn("block", new SpawnData((i+7) * 32, (j+1) * 32));
                }
                if (MAPCOPY[j][i] == 2) {
                    Entity breakBloc = FXGL.getGameWorld().spawn("breakableBlock", new SpawnData((i+7) * 32, (j+1) * 32));
                    breakBloc.getComponent(breakableComponent.class).j = j;
                    breakBloc.getComponent(breakableComponent.class).i = i;
                }
            }
        }

        player1 = spawn("player1");
        player2 = spawn("player2");

        playercomponent1 = player1.getComponent(playerComponent.class);
        at1 = playercomponent1.getAt();
        playercomponent2 = player2.getComponent(playerComponent2.class);
        at2 = playercomponent2.getAt();
        playercomponent1.choice = p1_choice;
        playercomponent2.choice = p2_choice;
        playercomponent1.unbeatTimer = FXGL.newLocalTimer();
        playercomponent2.unbeatTimer = FXGL.newLocalTimer();
    }
    private void server_initGame(){
        p1_choice = myMainMenu.getP1_choice();
        p2_choice = myMainMenu.getP2_choice();
        map_choice = myMainMenu.getMapChoice();
        if (map_choice == 1){
            for (int i = 0; i < 17; i++){
                for (int j = 0; j < 17; j++){
                    MAP[i][j] = MapMessage.levelThree[i][j];
                    MAPCOPY[i][j] = MapMessage.levelThreeCopy[i][j];
                }
            }
        } else if (map_choice == 2){
            for (int i = 0; i < 17; i++){
                for (int j = 0; j < 17; j++){
                    MAP[i][j] = MapMessage.levelTwo[i][j];
                    MAPCOPY[i][j] = MapMessage.levelTwoCopy[i][j];
                }
            }
        }else if (map_choice == 3 || map_choice == 0){
            for (int i = 0; i < 17; i++){
                for (int j = 0; j < 17; j++){
                    MAP[i][j] = MapMessage.levelOne[i][j];
                    MAPCOPY[i][j] = MapMessage.levelOneCopy[i][j];
                }
            }
        }
        FXGL.getGameWorld().addEntityFactory(new SpriteFactory(p1_choice, p2_choice, map_choice));
        initBackGroundElement();
        FXGL.getGameWorld().spawn("Map", new SpawnData(7*32, 32));
        for (int i = 0; i < MAP_SIZE; i++){
            for (int j = 0; j < MAP_SIZE; j++) {
                if (MAPCOPY[j][i] == 1) {
                    FXGL.getGameWorld().spawn("block", new SpawnData((i+7) * 32, (j+1) * 32));
                }
                if (MAPCOPY[j][i] == 2) {
                    double x = (i+7) * 32;double y = (j+1) * 32;
                    Entity breakBloc = FXGL.getGameWorld().spawn("breakableBlock", new SpawnData(x, y));
                    int bearType = breakBloc.getComponent(breakableComponent.class).bearType;
                    var bundle = new Bundle("brek");
                    bundle.put("bear",bearType);
                    server.broadcast(bundle);
                    breakBloc.getComponent(breakableComponent.class).j = j;
                    breakBloc.getComponent(breakableComponent.class).i = i;
                }
            }
        }

        player1 = spawn("player1");
        player2 = spawn("player2");

        playercomponent1 = player1.getComponent(playerComponent.class);
        at1 = playercomponent1.getAt();
        playercomponent2 = player2.getComponent(playerComponent2.class);
        at2 = playercomponent2.getAt();
        playercomponent1.choice = p1_choice;
        playercomponent2.choice = p2_choice;
        playercomponent1.unbeatTimer = FXGL.newLocalTimer();
        playercomponent2.unbeatTimer = FXGL.newLocalTimer();
    }
    private void client_initGame(){
        Vector<Integer> bearColors;
        bearColors = myMainMenu.getBearColors();
        int k = 0;
        p1_choice = myMainMenu.getP1_choice();
        p2_choice = myMainMenu.getP2_choice();
        map_choice = myMainMenu.getMapChoice();
        if (map_choice == 1){
            for (int i = 0; i < 17; i++){
                for (int j = 0; j < 17; j++){
                    MAP[i][j] = MapMessage.levelThree[i][j];
                    MAPCOPY[i][j] = MapMessage.levelThreeCopy[i][j];
                }
            }
        } else if (map_choice == 2){
            for (int i = 0; i < 17; i++){
                for (int j = 0; j < 17; j++){
                    MAP[i][j] = MapMessage.levelTwo[i][j];
                    MAPCOPY[i][j] = MapMessage.levelTwoCopy[i][j];
                }
            }
        }else if (map_choice == 3 || map_choice == 0){
            for (int i = 0; i < 17; i++){
                for (int j = 0; j < 17; j++){
                    MAP[i][j] = MapMessage.levelOne[i][j];
                    MAPCOPY[i][j] = MapMessage.levelOneCopy[i][j];
                }
            }
        }
        FXGL.getGameWorld().addEntityFactory(new SpriteFactory(p1_choice, p2_choice, map_choice));
        initBackGroundElement();
        FXGL.getGameWorld().spawn("Map", new SpawnData(7*32, 32));
        for (int i = 0; i < MAP_SIZE; i++){
            for (int j = 0; j < MAP_SIZE; j++) {
                if (MAPCOPY[j][i] == 1) {
                    FXGL.getGameWorld().spawn("block", new SpawnData((i+7) * 32, (j+1) * 32));
                }
                if (MAPCOPY[j][i] == 2) {
                    while (bearColors.size() < k + 1) {
                        try {
                            sleep(100); //暂停100毫秒防clientInit的时候server的信息还没传到
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    Entity breakBloc = FXGL.getGameWorld().spawn("breakableBlock_client", new SpawnData((i+7) * 32, (j+1) * 32).put("bearColor", bearColors.get(k)));
                    breakBloc.getComponent(breakableComponent.class).j = j;
                    breakBloc.getComponent(breakableComponent.class).i = i;
                    k++;
                }
            }
        }
        myMainMenu.setBearColors(new Vector<>());

        player1 = spawn("player1");
        player2 = spawn("player2");

        playercomponent1 = player1.getComponent(playerComponent.class);
        at1 = playercomponent1.getAt();
        playercomponent2 = player2.getComponent(playerComponent2.class);
        at2 = playercomponent2.getAt();
        playercomponent1.choice = p1_choice;
        playercomponent2.choice = p2_choice;
        playercomponent1.unbeatTimer = FXGL.newLocalTimer();
        playercomponent2.unbeatTimer = FXGL.newLocalTimer();
    }
    private Vector<Double> p1PutBomb(double p1x, double p1y){
        Vector<Double> v = new Vector<>();
        if(playercomponent1.myBomb == 0){
            return v;
        }
        //在单独的线程中播放声音
        new Thread(() -> {
            FXGL.play("put.wav");
        }).start();
        playercomponent1.myBomb--; //可用炸弹数减少
        double x;
        double y;
        if(gameMode!=GameMode.CLIENT){
           x = player1.getCenter().getX();
           y = player1.getCenter().getY();
           v.add(x);v.add(y);
        }else{
            x = p1x;
            y = p1y;
        }
        fakeBomb1 = FXGL.getGameWorld().spawn("fakeBomb",
                new SpawnData(x-10, y-6));
        //爆炸伤害
        PauseTransition pt = new PauseTransition(Duration.seconds(1.5)); //几秒后爆炸
        pt.setOnFinished(event->{
            fakeBomb1.setVisible(false);
            //在单独的线程中播放声音
            new Thread(() -> {
                FXGL.play("Fire.wav");
            }).start();
            if(realBomb != null){
                realBomb.removeFromWorld();
            }
            int i = (int) (y/32)-1;
            int j = (int) (x/32)-7;
            boolean aboveFlag = false;boolean belowFlag = false;boolean leftFlag = false;boolean rightFlag = false; //防止穿透障碍物产生火焰
            for(int k = 0; k <= playercomponent1.addFire; k++){
                boolean aboveOut = false;boolean belowOut = false;boolean leftOut = false;boolean rightOut = false;
                if((i-1-k) < 0)
                    aboveOut = true;
                if((i+1+k) > 16)
                    belowOut =true;
                if((j-1-k) < 0)
                    leftOut =true;
                if((j+1+k) > 16)
                    rightOut =true;
                if(!aboveOut && MAP[i-1-k][j] == 1){  //above位置
                    aboveFlag = true;
                }
                if(!belowOut && MAP[i+1+k][j] == 1){  //below位置
                    belowFlag = true;
                }
                if(!leftOut  && MAP[i][j-1-k] == 1){  //left位置
                    leftFlag = true;
                }
                if(!rightOut && MAP[i][j+1+k] == 1){  //right位置
                    rightFlag = true;
                }  //防止火焰穿透flag设置完毕
                if(!aboveOut && MAP[i-1-k][j] != 1 && !aboveFlag){  //above位置
                    if(MAP[i-1-k][j] == 2){
                        spawn("singleBoom",x-16,y-16-32-32*k);
                    }else if(MAP[i-1-k][j] != 2){
                        spawn("singleBoom",x-16,y-16-32-32*k);
                    }
                }
                Entity singleB = spawn("singleBoom",x-16,y-16);  //中心位置
                if(!belowOut && MAP[i+1+k][j] != 1 && !belowFlag){  //below位置
                    if(MAP[i+1+k][j] == 2){
                        spawn("singleBoom",x-16,y-16+32+32*k);
                    }else if(MAP[i+1+k][j] != 2){
                        spawn("singleBoom",x-16,y-16+32+32*k);
                    }
                }
                if(!leftOut && MAP[i][j-1-k] != 1 && !leftFlag){  //left位置
                    if(MAP[i][j-1-k] == 2){
                        spawn("singleBoom",x-16-32-32*k,y-16);
                    }else if (MAP[i][j-1-k] != 2){
                        spawn("singleBoom",x-16-32-32*k,y-16);
                    }
                }
                if(!rightOut && MAP[i][j+1+k] != 1 && !rightFlag){  //right位置
                    if(MAP[i][j+1+k] == 2){
                        spawn("singleBoom",x-16+32+32*k,y-16);
                    }else if (MAP[i][j+1+k] != 2){
                        spawn("singleBoom",x-16+32+32*k,y-16);
                    }
                }
            }
            PauseTransition pt2 = new PauseTransition(Duration.seconds(1.83));
            pt2.setOnFinished(event2->{
                if(!toys.isEmpty()){
                    for(Entity toy : toys){
                        if(!toy.hasComponent(CollidableComponent.class)){
                            toy.addComponent(new CollidableComponent(true));
                        }
                    }
                    toys.clear();
                }
                playercomponent1.myBomb++;
            });
            pt2.play();
        });
        pt.play();

        return v;
    }
    private Vector<Double> p2PutBomb(double p2x, double p2y){
        Vector<Double> v = new Vector<>();
        if(playercomponent2.myBomb == 0){
            return v;
        }
        // 在单独的线程中播放声音
        new Thread(() -> {
            FXGL.play("put.wav");
        }).start();

        playercomponent2.myBomb--; //可用炸弹数减少
        double x;
        double y;
        if(gameMode!=GameMode.SERVER){
            x = player2.getCenter().getX();
            y = player2.getCenter().getY();
            v.add(x);v.add(y);
        }else{
            x = p2x;
            y = p2y;
        }
        fakeBomb2 = FXGL.getGameWorld().spawn("fakeBomb",
                new SpawnData(x-10, y-6));
        //爆炸伤害
        PauseTransition pt = new PauseTransition(Duration.seconds(1.5)); //几秒后爆炸
        pt.setOnFinished(event->{
            fakeBomb2.setVisible(false);
            //在单独的线程中播放声音
            new Thread(() -> {
                FXGL.play("Fire.wav");
            }).start();
            if(realBomb != null){
                realBomb.removeFromWorld();
            }
            int i = (int) (y/32)-1;
            int j = (int) (x/32)-7;
            boolean aboveFlag = false;boolean belowFlag = false;boolean leftFlag = false;boolean rightFlag = false; //防止穿透障碍物产生火焰
            for(int k = 0; k <= playercomponent2.addFire; k++){
                boolean aboveOut = false;boolean belowOut = false;boolean leftOut = false;boolean rightOut = false;
                if((i-1-k) < 0)
                    aboveOut = true;
                if((i+1+k) > 16)
                    belowOut =true;
                if((j-1-k) < 0)
                    leftOut =true;
                if((j+1+k) > 16)
                    rightOut =true;
                if(!aboveOut && MAP[i-1-k][j] == 1){  //above位置
                    aboveFlag = true;
                }
                if(!belowOut && MAP[i+1+k][j] == 1){  //below位置
                    belowFlag = true;
                }
                if(!leftOut  && MAP[i][j-1-k] == 1){  //left位置
                    leftFlag = true;
                }
                if(!rightOut && MAP[i][j+1+k] == 1){  //right位置
                    rightFlag = true;
                }  //防止火焰穿透flag设置完毕
                if(!aboveOut && MAP[i-1-k][j] != 1 && !aboveFlag){  //above位置
                    if(MAP[i-1-k][j] == 2){
                        spawn("singleBoom",x-16,y-16-32-32*k);
                    }else if(MAP[i-1-k][j] != 2){
                        spawn("singleBoom",x-16,y-16-32-32*k);
                    }
                }
                Entity singleB = spawn("singleBoom",x-16,y-16);  //中心位置
                if(!belowOut && MAP[i+1+k][j] != 1 && !belowFlag){  //below位置
                    if(MAP[i+1+k][j] == 2){
                        spawn("singleBoom",x-16,y-16+32+32*k);
                    }else if(MAP[i+1+k][j] != 2){
                        spawn("singleBoom",x-16,y-16+32+32*k);
                    }
                }
                if(!leftOut && MAP[i][j-1-k] != 1 && !leftFlag){  //left位置
                    if(MAP[i][j-1-k] == 2){
                        spawn("singleBoom",x-16-32-32*k,y-16);
                    }else if (MAP[i][j-1-k] != 2){
                        spawn("singleBoom",x-16-32-32*k,y-16);
                    }
                }
                if(!rightOut && MAP[i][j+1+k] != 1 && !rightFlag){  //right位置
                    if(MAP[i][j+1+k] == 2){
                        spawn("singleBoom",x-16+32+32*k,y-16);
                    }else if (MAP[i][j+1+k] != 2){
                        spawn("singleBoom",x-16+32+32*k,y-16);
                    }
                }
            }
            PauseTransition pt2 = new PauseTransition(Duration.seconds(1.83));
            pt2.setOnFinished(event2->{
                if(!toys.isEmpty()){
                    for(Entity toy : toys){
                        if(!toy.hasComponent(CollidableComponent.class)){
                            toy.addComponent(new CollidableComponent(true));
                        }
                    }
                    toys.clear();
                }
                playercomponent2.myBomb++;
            });
            pt2.play();
        });
        pt.play();

        return v;
    }
    private void my_initInput(){
        if (isFirstInitInput){
            //点击WASD移动，空格放炸弹
            FXGL.getInput().addAction(new UserAction("playBomb_server") {
                @Override
                protected void onActionBegin() {//如果点回车就先放一个fakeBomb，可以保证人物能走出去
                    if(gameMode == GameMode.LOCAL){
                        p1PutBomb(0,0);
                    }
                    if(gameMode == GameMode.SERVER){
                        Vector v = p1PutBomb(0,0);
                        var bundle = new Bundle("p1ptbomb");
                        bundle.put("p1ptbomb",v);
                        server.broadcast(bundle);
                    }
                    if(gameMode == GameMode.CLIENT){
                        Vector v = p2PutBomb(0,0);
                        var bundle = new Bundle("p2ptbomb");
                        bundle.put("p2ptbomb", v);
                        client.broadcast(bundle);
                    }
                }
            }, KeyCode.SPACE);

            FXGL.getInput().addAction(moveup = new UserAction("moveup") {
                @Override
                protected void onAction() {
                if(gameMode ==GameMode.LOCAL)
                {
                    playercomponent1.moveUp();
                }
                if(gameMode ==GameMode.SERVER)
                {
                    playercomponent1.moveUp();
                    double x = player1.getX();
                    double y = player1.getY();
                    //将p1向上走动作标签和对应坐标打包发送给client
                    var bundle = new Bundle("p1move");
                    bundle.put("p1move", 0);
                    bundle.put("p1move_x", x);
                    bundle.put("p1move_y", y);
                    server.broadcast(bundle);
                }
                if(gameMode ==GameMode.CLIENT)
                {
                    playercomponent2.moveUp();
                    double x = player2.getX();
                    double y = player2.getY();
                    var bundle = new Bundle("p2move");
                    bundle.put("p2move", 0);
                    bundle.put("p2move_x", x);
                    bundle.put("p2move_y", y);
                    client.broadcast(bundle);
                }
            }
            },KeyCode.W);


            FXGL.getInput().addAction(movedown = new UserAction("movedown") {
                @Override
                protected void onAction() {
                    if (gameMode == GameMode.LOCAL) {
                        playercomponent1.moveDown();
                    }
                    if (gameMode == GameMode.SERVER) {
                        playercomponent1.moveDown();
                        double x = player1.getX();
                        double y = player1.getY();
                        var bundle = new Bundle("p1move");
                        bundle.put("p1move", 1);
                        bundle.put("p1move_x", x);
                        bundle.put("p1move_y", y);
                        server.broadcast(bundle);
                    }
                    if (gameMode == GameMode.CLIENT) {
                        playercomponent2.moveDown();
                        double x = player2.getX();
                        double y = player2.getY();
                        var bundle = new Bundle("p2move");
                        bundle.put("p2move", 1);
                        bundle.put("p2move_x", x);
                        bundle.put("p2move_y", y);
                        client.broadcast(bundle);
                    }
                }
            },KeyCode.S);

            FXGL.getInput().addAction(moveleft = new UserAction("moveleft") {
                @Override
                protected void onAction() {
                    if (gameMode == GameMode.LOCAL) {
                        playercomponent1.moveLeft();
                    }
                    if (gameMode == GameMode.SERVER) {
                        playercomponent1.moveLeft();
                        double x = player1.getX();
                        double y = player1.getY();
                        var bundle = new Bundle("p1move");
                        bundle.put("p1move", 2);
                        bundle.put("p1move_x", x);
                        bundle.put("p1move_y", y);
                        server.broadcast(bundle);
                    }
                    if (gameMode == GameMode.CLIENT) {
                        playercomponent2.moveLeft();
                        double x = player2.getX();
                        double y = player2.getY();
                        var bundle = new Bundle("p2move");
                        bundle.put("p2move", 2);
                        bundle.put("p2move_x", x);
                        bundle.put("p2move_y", y);
                        client.broadcast(bundle);
                    }
                }
            },KeyCode.A);
            FXGL.getInput().addAction(moveright = new UserAction("moveright") {
                @Override
                protected void onAction() {
                    if (gameMode == GameMode.LOCAL) {
                        playercomponent1.moveRight();
                    }
                    if (gameMode == GameMode.SERVER) {
                        playercomponent1.moveRight();
                        double x = player1.getX();
                        double y = player1.getY();
                        var bundle = new Bundle("p1move");
                        bundle.put("p1move", 3);
                        bundle.put("p1move_x", x);
                        bundle.put("p1move_y", y);
                        server.broadcast(bundle);
                    }
                    if (gameMode == GameMode.CLIENT) {
                        playercomponent2.moveRight();
                        double x = player2.getX();
                        double y = player2.getY();
                        var bundle = new Bundle("p2move");
                        bundle.put("p2move", 3);
                        bundle.put("p2move_x", x);
                        bundle.put("p2move_y", y);
                        client.broadcast(bundle);
                    }
                }
            },KeyCode.D);
            FXGL.getInput().addAction(new UserAction("playBomb2") {
                @Override
                protected void onActionBegin() {
                    if(gameMode == GameMode.LOCAL){
                        p2PutBomb(0,0);
                    }
                }
            }, KeyCode.ENTER);

            onKey(KeyCode.UP, () -> {
                if(gameMode == GameMode.LOCAL){
                    playercomponent2.moveUp();
                }
            });
            onKey(KeyCode.DOWN, () -> {
                if(gameMode == GameMode.LOCAL){
                    playercomponent2.moveDown();
                }
            });
            onKey(KeyCode.LEFT, () -> {
                if(gameMode == GameMode.LOCAL){
                    playercomponent2.moveLeft();
                }
            });
            onKey(KeyCode.RIGHT, () -> {
                if(gameMode == GameMode.LOCAL){
                    playercomponent2.moveRight();
                }
            });
            FXGL.getInput().addAction(new UserAction("pause") {
                @Override
                protected void onActionBegin() {
                    if(gameMode==GameMode.CLIENT){
                        getGameController().gotoGameMenu();
                    }
                }
                @Override
                protected void onAction() {

                }
            }, KeyCode.ESCAPE);
            isFirstInitInput = false;
        }

    }
    private void changeVars(){
        if(gameMode==GameMode.CLIENT){
            playercomponent1.mylife = myMainMenu.getBld1();
            playercomponent2.mylife = myMainMenu.getBld2();
            playercomponent1.INIT_SPEED = myMainMenu.getSpeed1();
            playercomponent2.INIT_SPEED = myMainMenu.getSpeed2();
        }
        int score1111=FXGL.geti("blood1");
        FXGL.set("blood1",playercomponent1.mylife);
        int score2222=FXGL.geti("bob1");
        FXGL.set("bob1",playercomponent1.myBomb);
        int score3333=FXGL.geti("speed1");
        FXGL.set("speed1",(int) (playercomponent1.INIT_SPEED >= 70 ? 6: (playercomponent1.INIT_SPEED - 40) / 5));
        int score4444=FXGL.geti("cut1");
        FXGL.set("cut1", playercomponent1.addFire);

        int score111=FXGL.geti("blood2");
        FXGL.set("blood2",playercomponent2.mylife);
        int score222=FXGL.geti("bob2");
        FXGL.set("bob2",playercomponent2.myBomb);
        int score333=FXGL.geti("speed2");
        FXGL.set("speed2",(int)(playercomponent2.INIT_SPEED >= 70 ? 6: (playercomponent2.INIT_SPEED - 40) / 5));
        int score444=FXGL.geti("cut2");
        FXGL.set("cut2",playercomponent2.addFire);

        if(gameMode == GameMode.SERVER){
            var bundle = new Bundle("bundle");
            bundle.put("speed1",playercomponent1.INIT_SPEED);
            bundle.put("speed2",playercomponent2.INIT_SPEED);
            server.broadcast(bundle);
        }
    }
    public void local_onupdate(){
        changeVars();
        if (toyTimer.elapsed(Duration.seconds(7)) && maxToys > 0) {
            int j = FXGLMath.random(1,15); int i = FXGLMath.random(1,15);
            if(MAPCOPY[j][i] == 0){
                int k = FXGLMath.random(1,40);
                if (k <= 12){
                    spawn("toyBomb", (i+7)*32, (j+1)*32);
                }else if (k <= 24){
                    spawn("toyBottle", (i+7)*32, (j+1)*32);
                }else if (k <= 34){
                    spawn("toyShoe", (i+7)*32, (j+1)*32);
                }else{
                    spawn("toyPoison", (i+7)*32, (j+1)*32);
                }

                MAPCOPY[j][i] = 3;  //道具占格子
                maxToys--;
                System.out.println("空地隔10S爆出道具！,剩余："+maxToys);
            }
            toyTimer.capture();
        }
    }
    public void server_onupdate(){
        changeVars();
        //接受p2放炸弹消息
        if(myMainMenu.getP2putbomb()==1){
            Vector<Double> v = myMainMenu.getP2BOMB();
            if(v.size() == 2){
                p2PutBomb(v.get(0),v.get(1));
            }
            myMainMenu.setP2putbomb(0);
            myMainMenu.setP2BOMB(new Vector<>());
        }
        //接受p2移动消息
        Vector<Pair<Integer, Pair<Double,Double>>> p2moves = myMainMenu.getP2moves();
        if(p2moves.size()>0){
            int dir = p2moves.firstElement().getKey();
            double x = p2moves.firstElement().getValue().getKey();
            double y = p2moves.firstElement().getValue().getValue();
            if(dir == 0){
                playercomponent2.moveUp();
                player2.setPosition(x,y);
            }else if(dir== 1){
                playercomponent2.moveDown();
                player2.setPosition(x,y);
            }else if(dir== 2){
                playercomponent2.moveLeft();
                player2.setPosition(x,y);
            }else if(dir== 3){
                playercomponent2.moveRight();
                player2.setPosition(x,y);
            }
            p2moves.remove(0);
        }
        if (toyTimer.elapsed(Duration.seconds(7)) && maxToys > 0) {
            int j = FXGLMath.random(1,15); int i = FXGLMath.random(1,15);
            if(MAPCOPY[j][i] == 0){
                int k = FXGLMath.random(1,40);
                if (k <= 12){
                    spawn("toyShoe", (i+7)*32, (j+1)*32);
                    var bundle = new Bundle("randomToy");
                    bundle.put("ranToy",1);
                    bundle.put("ranToy_col",i);
                    bundle.put("ranToy_line",j);
                    server.broadcast(bundle);
                }else if (k <= 24){
                    spawn("toyBottle", (i+7)*32, (j+1)*32);
                    var bundle = new Bundle("randomToy");
                    bundle.put("ranToy",2);
                    bundle.put("ranToy_col",i);
                    bundle.put("ranToy_line",j);
                    server.broadcast(bundle);
                }else if (k <= 34){
                    spawn("toyBomb", (i+7)*32, (j+1)*32);
                    var bundle = new Bundle("randomToy");
                    bundle.put("ranToy",3);
                    bundle.put("ranToy_col",i);
                    bundle.put("ranToy_line",j);
                    server.broadcast(bundle);
                }else{
                    spawn("toyPoison", (i+7)*32, (j+1)*32);
                    var bundle = new Bundle("randomToy");
                    bundle.put("ranToy",4);
                    bundle.put("ranToy_col",i);
                    bundle.put("ranToy_line",j);
                    server.broadcast(bundle);
                }
                MAPCOPY[j][i] = 3;  //道具占格子
                maxToys--;
                System.out.println("空地隔10S爆出道具！,剩余："+maxToys);
            }
            toyTimer.capture();
        }
        //接收爆道具信息
        Vector<Pair<Integer, Pair<Integer, Integer>>> brkToys = myMainMenu.getBrkToys();
        if(brkToys.size()>0){
            Entity theToy = new Entity();
            int type = brkToys.firstElement().getKey();
            System.out.println("brkToy from client："+type);
            int i = brkToys.firstElement().getValue().getKey();
            int j = brkToys.firstElement().getValue().getValue();
            boolean no = false;
            if(MAPCOPY[i][j]==3){
                System.out.println("repete toy");
                no = true;
            }
            if(!no){
                if(type == 1){
                    theToy = spawn("toyBomb", (i+7)*32, (j+1)*32);
                }
                if(type == 2){
                    theToy = spawn("toyBottle", (i+7)*32, (j+1)*32);
                }
                if(type == 3){
                    theToy = spawn("toyShoe", (i+7)*32, (j+1)*32);
                }
                if(type == 4){
                    theToy = spawn("toyPoison", (i+7)*32, (j+1)*32);
                }
                theToy.removeComponent(CollidableComponent.class);
                theToy.getComponent(toyComponent.class).j = j;
                theToy.getComponent(toyComponent.class).i = i;
                toys.add(theToy);
                MAPCOPY[j][i] = 3;  //道具占格子
                maxToys--;
                System.out.println("可破坏障碍物爆出道具！,剩余："+maxToys);
                brkToys.remove(0);
            }
        }
    }
    public void client_onupdate(){
        changeVars();
        Vector<Pair<Integer, Pair<Integer, Integer>>> ranToys = myMainMenu.getRanToys();
        if(ranToys.size() >= ranToySave + 1){
            int type = ranToys.get(ranToySave).getKey();
            int i = ranToys.get(ranToySave).getValue().getKey();
            int j = ranToys.get(ranToySave).getValue().getValue();
            if(type == 1){
                spawn("toyShoe", (i+7)*32, (j+1)*32);
            }
            if(type == 2){
                spawn("toyBottle", (i+7)*32, (j+1)*32);
            }
            if(type == 3){
                spawn("toyBomb", (i+7)*32, (j+1)*32);
            }
            if(type == 4){
                spawn("toyPoison", (i+7)*32, (j+1)*32);
            }
            MAPCOPY[j][i] = 3;  //道具占格子
            maxToys--;
            System.out.println("空地隔10S爆出道具！,剩余："+maxToys);
            ranToySave++;
        }
        //接受p1移动消息
        Vector<Pair<Integer, Pair<Double,Double>>> p1moves = myMainMenu.getP1moves();
        if(p1moves.size()>0){
            int dir = p1moves.firstElement().getKey();
            double x = p1moves.firstElement().getValue().getKey();
            double y = p1moves.firstElement().getValue().getValue();
            if(dir == 0){
                playercomponent1.moveUp();
                player1.setPosition(x,y);
            }else if(dir== 1){
                playercomponent1.moveDown();
                player1.setPosition(x,y);
            }else if(dir== 2){
                playercomponent1.moveLeft();
                player1.setPosition(x,y);
            }else if(dir== 3){
                playercomponent1.moveRight();
                player1.setPosition(x,y);
            }
            p1moves.remove(0);
        }
        //接受p1放炸弹消息
        if(myMainMenu.getP1putbomb()==1){
            Vector<Double> v = myMainMenu.getP1BOMB();
            if(v.size() == 2){
                p1PutBomb(v.get(0),v.get(1));
            }
            myMainMenu.setP1putbomb(0);
            myMainMenu.setP1BOMB(new Vector<>());
        }
        //接收爆道具信息
        Vector<Pair<Integer, Pair<Integer, Integer>>> brkToys = myMainMenu.getBrkToys();
        if(brkToys.size()>0){
            Entity theToy = new Entity();
            int type = brkToys.firstElement().getKey();
            System.out.println("brkToy from server："+type);
            int i = brkToys.firstElement().getValue().getKey();
            int j = brkToys.firstElement().getValue().getValue();
            boolean no = false;
            if(MAPCOPY[i][j]==3){
                no = true;  //防止server左上角收到奇怪的道具生成
            }
            if(!no){
                if(type == 1){
                    theToy = spawn("toyShoe", (i+7)*32, (j+1)*32);
                }
                if(type == 2){
                    theToy = spawn("toyBottle", (i+7)*32, (j+1)*32);
                }
                if(type == 3){
                    theToy = spawn("toyBomb", (i+7)*32, (j+1)*32);
                }
                if(type == 4){
                    theToy = spawn("toyPoison", (i+7)*32, (j+1)*32);
                }
                theToy.removeComponent(CollidableComponent.class);
                theToy.getComponent(toyComponent.class).j = j;
                theToy.getComponent(toyComponent.class).i = i;
                toys.add(theToy);
                MAPCOPY[j][i] = 3;  //道具占格子
                maxToys--;
                System.out.println("可破坏障碍物爆出道具！,剩余："+maxToys);
                brkToys.remove(0);
            }
        }
    }
    //游戏窗口大小设置、图标设置等
    @Override
    protected void initSettings(GameSettings settings){
        settings.setTitle("QQBOMBER-704");
        settings.setVersion("0.1");
        settings.setAppIcon("icon.png");

        settings.setWidth(800);
        settings.setHeight(600);
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);

        settings.setSceneFactory(new SceneFactory(){
            @Override
            public FXGLMenu newMainMenu() {
                try {
                    myMainMenu = new MyMainMenu();

                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return myMainMenu;
            }

            @NotNull
            @Override
            public FXGLMenu newGameMenu() {
                try {
                    myGameMenu = new MyGameMenu();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                return myGameMenu;
            }
        });
        System.out.println("Test.initSettings==> "+Thread.currentThread().getName());

    }
    //游戏变量初始化
    @Override
    protected void initGameVars(Map<String, Object> vars){
        maxToys = 20; //每局结束回复总道具数
        vars.put("countdown", 180);  //添加 'countdown' 变量并初始化为 180
        vars.put("blood1",3);
        vars.put("blood2",3);

        vars.put("speed1",0);
        vars.put("speed2",0);

        vars.put("bob1",0);
        vars.put("bob2",0);

        vars.put("cut1",0);
        vars.put("cut2",0);
        System.out.println("Test.initGameVars==> "+Thread.currentThread().getName());
    }
    //游戏主体逻辑
    @Override
    protected void initGame(){
        playBackgroundMusic();
        myGameMenu.setBgMusic(bgMusic);
        gameMode = myMainMenu.getGameMode();
        if(gameMode == GameMode.LOCAL){
            System.out.println("localmode");
            local_initGame();
        }else if(gameMode == GameMode.SERVER){
            System.out.println("hostmode");
            server = myMainMenu.getServer();
            myGameMenu.setServer(server);
            server_initGame();
        }else if(gameMode == GameMode.CLIENT){
            System.out.println("guestmode");
            client = myMainMenu.getClient();
            myGameMenu.setClient(client);
            client_initGame();
        }
        System.out.println("Test.initGame==> "+Thread.currentThread().getName());
    }
    //游戏过程中处理碰撞等，由FXGL的物理引擎支持
    @Override
    protected void initPhysics() {
        //菜单音乐停了吗的传参
        myGameMenu.setMyMainMenu(myMainMenu);
        /***因为initInput的触发早于menu出现，如果在那写判定不了选择的游戏模式所以删除了initinput全部移到这里了***/
        gameMode = myMainMenu.getGameMode();
        my_initInput();
        /***因为initInput的触发早于menu出现，如果在那写判定不了选择的游戏模式所以删除了initinput全部移到这里了***/

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.PLAYER1, GameElementType.FAKEBOMB) {
            @Override//监听碰撞，如果碰撞结束，就把无bbox的fakeBomb类移除掉，原位置替换成有bbox的Bomb类
            protected void onCollisionEnd(Entity player, Entity fakeBomb) {
                System.out.println("碰撞结束");
                if(!fakeBomb.isVisible()){
                    return;
                }
                realBomb = FXGL.getGameWorld().spawn("bomb",
                        new SpawnData(fakeBomb.getX(), fakeBomb.getY()));
                fakeBomb.removeFromWorld();
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.PLAYER2, GameElementType.FAKEBOMB) {
            @Override//监听碰撞，如果碰撞结束，就把无bbox的fakeBomb类移除掉，原位置替换成有bbox的Bomb类
            protected void onCollisionEnd(Entity player, Entity fakeBomb) {
                System.out.println("碰撞结束");
                if(!fakeBomb.isVisible()){
                    return;
                }
                realBomb = FXGL.getGameWorld().spawn("bomb",
                        new SpawnData(fakeBomb.getX(), fakeBomb.getY()));
                fakeBomb.removeFromWorld();
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.PLAYER1, GameElementType.BOOM) {
            @Override
            protected void onCollisionBegin(Entity player, Entity boom) {
                if(!playercomponent1.unbeatTimer.elapsed(playercomponent1.unbeatDealy)){
                    return;
                }
                else {
                    at1.setOpacity(0.5);
                    System.out.println("受到炸弹伤害啦");
                    if(gameMode!=GameMode.CLIENT){
                        playercomponent1.mylife--;
                        if(gameMode == GameMode.SERVER){
                            var bundle = new Bundle("blood1");
                            bundle.put("bld1",playercomponent1.mylife);
                            server.broadcast(bundle);
                        }
                    }
                    //sounds
                    Media media = new Media(getClass().getResource("/assets/music/Fire.mp3").toExternalForm());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.play();

                    playercomponent1.unbeatTimer.capture();
                    Entity explode = new Entity();
                    explode.setPosition(player.getX()-15, player.getY()-15);
                    int blood = playercomponent1.mylife;
                    System.out.println("blood:"+blood);
                    if (blood > 0 && gameMode!=GameMode.CLIENT){
                        AnimatedTexture texture = FXGL.texture("e.png").toAnimatedTexture(15, Duration.seconds(1.4));
                        texture.setOnCycleFinished(() -> {
                            at1.setOpacity(1);
                            player.setVisible(true); //设置玩家为可见
                            explode.removeFromWorld(); //移除爆炸实体
                        });
                        explode.getViewComponent().addChild(texture);
                        FXGL.getGameWorld().addEntity(explode);
                        //播放动画
                        texture.play();
                    }
                    else if (blood <= 0) {
                        player.setVisible(false);
                        AnimatedTexture texture = FXGL.texture("explode.png").toAnimatedTexture(15, Duration.seconds(1.4));
                        texture.setOnCycleFinished(() -> {

                            explode.removeFromWorld(); //移除爆炸实体
                        });
                        explode.getViewComponent().addChild(texture);
                        FXGL.getGameWorld().addEntity(explode);
                        //播放动画
                        texture.play();
                        silenceBackgroundmusic();

                        //创建一个持续3秒的暂停
                        PauseTransition pause = new PauseTransition(Duration.seconds(3));
                        pause.setOnFinished(event -> {
                            FXGL.getSceneService().pushSubScene(new Player2win());
                            myMainMenu.setBld1(3);myMainMenu.setBld2(3);
                            if (server!=null){
                                if(!server.getConnections().isEmpty())
                                    server.getConnections().get(0).terminate();
                                server.stop();
                                server = null;
                            }
                            if(client!=null){
                                client.disconnect();
                                client = null;
                            }
                        });
                        pause.play();
                    }
                    else if (gameMode == GameMode.CLIENT && blood == 1){
                        player.setVisible(false);
                        AnimatedTexture texture = FXGL.texture("explode.png").toAnimatedTexture(15, Duration.seconds(1.4));
                        texture.setOnCycleFinished(() -> {

                            explode.removeFromWorld(); //移除爆炸实体
                        });
                        explode.getViewComponent().addChild(texture);
                        FXGL.getGameWorld().addEntity(explode);
                        //播放动画
                        texture.play();
                        silenceBackgroundmusic();

                        //创建一个持续3秒的暂停
                        PauseTransition pause = new PauseTransition(Duration.seconds(3));
                        pause.setOnFinished(event -> {
                            FXGL.getSceneService().pushSubScene(new Player2win());
                            myMainMenu.setBld1(3);myMainMenu.setBld2(3);
                            if (server!=null){
                                if(!server.getConnections().isEmpty())
                                    server.getConnections().get(0).terminate();
                                server.stop();
                                server = null;
                            }
                            if(client!=null){
                                client.disconnect();
                                client = null;
                            }
                        });
                        pause.play();
                    }
                }
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.PLAYER2, GameElementType.BOOM) {
            @Override
            protected void onCollisionBegin(Entity player, Entity boom) {
                if(!playercomponent2.unbeatTimer.elapsed(playercomponent1.unbeatDealy)){
                    return;
                }
                else {
                    at2.setOpacity(0.5);
                    Media media = new Media(getClass().getResource("/assets/music/Fire.mp3").toExternalForm());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.play();
                    if(gameMode!=GameMode.CLIENT){
                        playercomponent2.mylife--;
                        if(gameMode == GameMode.SERVER){
                            var bundle = new Bundle("blood2");
                            bundle.put("bld2",playercomponent2.mylife);
                            server.broadcast(bundle);
                        }
                    }
                    playercomponent2.unbeatTimer.capture();
                    Entity explode = new Entity();
                    explode.setPosition(player.getX()-15, player.getY()-15);
                    int blood = playercomponent2.mylife;
                    if (blood > 0 && gameMode != GameMode.CLIENT){
                        AnimatedTexture texture = FXGL.texture("e.png").toAnimatedTexture(15, Duration.seconds(1.4));
                        texture.setOnCycleFinished(() -> {
                            at2.setOpacity(1);
                            player.setVisible(true); //设置玩家为可见
                            explode.removeFromWorld(); //移除爆炸实体
                        });
                        explode.getViewComponent().addChild(texture);
                        FXGL.getGameWorld().addEntity(explode);
                        //播放动画
                        texture.play();
                    }
                    else if (blood <= 0) {
                        player.setVisible(false);
                        AnimatedTexture texture = FXGL.texture("explode.png").toAnimatedTexture(15, Duration.seconds(1.4));
                        texture.setOnCycleFinished(() -> {
                            at2.setOpacity(1);
                            explode.removeFromWorld(); //移除爆炸实体
                        });
                        explode.getViewComponent().addChild(texture);
                        FXGL.getGameWorld().addEntity(explode);
                        //播放动画
                        texture.play();
                        silenceBackgroundmusic();

                        //创建一个持续3秒的暂停
                        PauseTransition pause = new PauseTransition(Duration.seconds(3));

                        //在暂停结束时执行跳转到下一个场景的操作
                        pause.setOnFinished(event -> {
                            FXGL.getSceneService().pushSubScene(new Player1win());
                            if (server!=null){
                                if(!server.getConnections().isEmpty())
                                    server.getConnections().get(0).terminate();
                                server.stop();
                                server = null;
                            }
                            if(client!=null){
                                client.disconnect();
                                client = null;
                            }
                        });
                        pause.play();
                    }
                    else if (gameMode == GameMode.CLIENT && blood == 1){
                        player.setVisible(false);
                        AnimatedTexture texture = FXGL.texture("explode.png").toAnimatedTexture(15, Duration.seconds(1.4));
                        texture.setOnCycleFinished(() -> {
                            at2.setOpacity(1);
                            explode.removeFromWorld(); //移除爆炸实体
                        });
                        explode.getViewComponent().addChild(texture);
                        FXGL.getGameWorld().addEntity(explode);
                        //播放动画
                        texture.play();
                        silenceBackgroundmusic();

                        //创建一个持续3秒的暂停
                        PauseTransition pause = new PauseTransition(Duration.seconds(3));

                        //在暂停结束时执行跳转到下一个场景的操作
                        pause.setOnFinished(event -> {
                            FXGL.getSceneService().pushSubScene(new Player1win());
                            if (server!=null){
                                if(!server.getConnections().isEmpty())
                                    server.getConnections().get(0).terminate();
                                server.stop();
                                server = null;
                            }
                            if(client!=null){
                                client.disconnect();
                                client = null;
                            }
                        });
                        pause.play();
                    }


                }

            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.BREAKABLEBLOCK, GameElementType.BOOM) {
            @Override
            protected void onCollisionBegin(Entity breakable, Entity boom) {
                breakableCompo = breakable.getComponent(breakableComponent.class);
                int j = breakableCompo.j;int i = breakableCompo.i;
                System.out.println(i+" "+j);
                MAPCOPY[j][i] = 0;
                double x = breakable.getX();
                double y = breakable.getY();
                breakable.removeFromWorld();
                boolean randomFlag = false;
                int randomCreate = FXGLMath.random(1,40);
                if (randomCreate < 25 && gameMode!=GameMode.CLIENT){
                    randomFlag = true;
                }
                if(randomFlag && maxToys > 0 && MAPCOPY[j][i] != 3){
                    MAPCOPY[j][i] = 3;  //道具占格子
                    //生成随机数判断要生成哪种道具
                    int k = FXGLMath.random(1,40);
                    if (k <= 17){
                        toy = spawn("toyShoe", x, y);
                        var bundle = new Bundle("brkToy");
                        if(gameMode==GameMode.SERVER){
                            bundle.put("brkToy",1);
                            bundle.put("brkToy_i",i);
                            bundle.put("brkToy_j",j);
                            server.broadcast(bundle);
                        }
                    }else if (k <= 24){
                        toy = spawn("toyBottle",  x, y);
                        var bundle = new Bundle("brkToy");
                        if(gameMode==GameMode.SERVER){
                            bundle.put("brkToy",2);
                            bundle.put("brkToy_i",i);
                            bundle.put("brkToy_j",j);
                            server.broadcast(bundle);
                        }
                    }else if (k <= 34){
                        toy = spawn("toyBomb", x, y);
                        var bundle = new Bundle("brkToy");
                        if(gameMode==GameMode.SERVER){
                            bundle.put("brkToy",3);
                            bundle.put("brkToy_i",i);
                            bundle.put("brkToy_j",j);
                            server.broadcast(bundle);
                        }
                    }else{
                        toy = spawn("toyPoison", x, y);
                        var bundle = new Bundle("brkToy");
                        if(gameMode==GameMode.SERVER){
                            bundle.put("brkToy",4);
                            bundle.put("brkToy_i",i);
                            bundle.put("brkToy_j",j);
                            server.broadcast(bundle);
                        }
                    }
                    toy.removeComponent(CollidableComponent.class);
                    toy.getComponent(toyComponent.class).j = j;
                    toy.getComponent(toyComponent.class).i = i;
                    toys.add(toy);

                    maxToys--;
                }
            }
        });


        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.TOYBOMB, GameElementType.BOOM) {
            @Override
            protected void onCollisionBegin(Entity toyBomb, Entity boom) {
                maxToys++;
                toyCompo = toyBomb.getComponent(toyComponent.class);
                int j = toyCompo.j;int i = toyCompo.i;
                MAPCOPY[j][i] = 0;
                toyBomb.removeFromWorld();
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.TOYBOTTLE, GameElementType.BOOM) {
            @Override
            protected void onCollisionBegin(Entity toyBottle, Entity boom) {
                maxToys++;
                toyCompo = toyBottle.getComponent(toyComponent.class);
                int j = toyCompo.j;int i = toyCompo.i;
                MAPCOPY[j][i] = 0;
                toyBottle.removeFromWorld();
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.TOYSHOE, GameElementType.BOOM) {
            @Override
            protected void onCollisionBegin(Entity toyShoe, Entity boom) {
                maxToys++;
                toyCompo = toyShoe.getComponent(toyComponent.class);
                int j = toyCompo.j;int i = toyCompo.i;
                MAPCOPY[j][i] = 0;
                toyShoe.removeFromWorld();
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.TOYPOISON, GameElementType.BOOM) {
            @Override
            protected void onCollisionBegin(Entity toyPosion, Entity boom) {
                maxToys++;
                toyCompo = toyPosion.getComponent(toyComponent.class);
                int j = toyCompo.j;int i = toyCompo.i;
                MAPCOPY[j][i] = 0;
                toyPosion.removeFromWorld();
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.PLAYER1, GameElementType.TOYBOMB) {
            @Override
            protected void onCollisionBegin(Entity player, Entity toybomb) {
                new Thread(() -> {
                    FXGL.play("pick.wav");
                }).start();
                maxToys++;
                playercomponent1.myBomb++;
                toybomb.removeFromWorld();
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.PLAYER2, GameElementType.TOYBOMB) {
            @Override
            protected void onCollisionBegin(Entity player, Entity toybomb) {
                new Thread(() -> {
                    FXGL.play("pick.wav");
                }).start();
                maxToys++;
                playercomponent2.myBomb++;
                toybomb.removeFromWorld();
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.PLAYER1, GameElementType.TOYBOTTLE) {
            @Override
            protected void onCollisionBegin(Entity player, Entity toybottle) {
                new Thread(() -> {
                    FXGL.play("pick.wav");
                }).start();
                maxToys++;
                if(playercomponent1.addFire<2){
                    playercomponent1.addFire++;

                }
                if(playercomponent1.addFire==2){
                    full1.setVisible(true);
                }
                toybottle.removeFromWorld();
            }
        });
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.PLAYER2, GameElementType.TOYBOTTLE) {
            @Override
            protected void onCollisionBegin(Entity player, Entity toybottle) {
                new Thread(() -> {
                    FXGL.play("pick.wav");
                }).start();
                maxToys++;
                if(playercomponent2.addFire<2){
                    playercomponent2.addFire++;
                }
                if(playercomponent2.addFire==2){
                    full2.setVisible(true);
                }

                toybottle.removeFromWorld();
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.PLAYER1, GameElementType.TOYSHOE) {
            @Override
            protected void onCollisionBegin(Entity player, Entity toyshoe) {
                new Thread(() -> {
                    FXGL.play("pick.wav");
                }).start();
                maxToys++;
                if (playercomponent1.INIT_SPEED < 70){
                    playercomponent1.INIT_SPEED += 5;
                }if (playercomponent1.INIT_SPEED >= 70){
                    System.out.println("玩家1已达到速度上限");
                    full3.setVisible(true);
                }
                toyshoe.removeFromWorld();
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.PLAYER2, GameElementType.TOYSHOE) {
            @Override
            protected void onCollisionBegin(Entity player, Entity toyshoe) {
                new Thread(() -> {
                    FXGL.play("pick.wav");
                }).start();
                maxToys++;
                if (playercomponent2.INIT_SPEED < 70){
                    playercomponent2.INIT_SPEED += 5;
                }if (playercomponent2.INIT_SPEED >= 70){
                    System.out.println("玩家2已达到速度上限");
                    full4.setVisible(true);
                }
                toyshoe.removeFromWorld();
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.PLAYER1, GameElementType.TOYPOISON) {
            @Override
            protected void onCollisionBegin(Entity player, Entity toypoison) {
                new Thread(() -> {
                    FXGL.play("pick.wav");
                }).start();
                maxToys++;
                if (playercomponent1.addFire > 0){
                    playercomponent1.addFire--;
                    if(playercomponent1.addFire < 2 ){
                        full1.setVisible(false);
                    }
                }
                toypoison.removeFromWorld();
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameElementType.PLAYER2, GameElementType.TOYPOISON) {
            @Override
            protected void onCollisionBegin(Entity player, Entity toypoison) {
                new Thread(() -> {
                    FXGL.play("pick.wav");
                }).start();
                maxToys++;
                if (playercomponent2.addFire > 0){
                    playercomponent2.addFire--;
                    if(playercomponent2.addFire < 2){
                        full2.setVisible(false);
                    }
                }

                toypoison.removeFromWorld();
            }
        });

        System.out.println("Test.initPhysics");
        System.out.println("Test.initPhysics==> "+Thread.currentThread().getName());
    }
    //初始化游戏过程中的UI
    @Override
    protected void initUI(){
        ImageView player11 =new ImageView(FXGL.image("player1head.png")); // 替换为您的矩形图像
        player11.setFitWidth(80); // 设置宽度为矩形的宽度
        player11.setFitHeight(80); // 设置高度为矩形的高度
        ImageView player12=new ImageView(FXGL.image("player2head.png")); // 替换为您的矩形图像
        player12.setFitWidth(80); // 设置宽度为矩形的宽度
        player12.setFitHeight(80); // 设置高度为矩形的高度
        ImageView player13 =new ImageView(FXGL.image("player3head.png")); // 替换为您的矩形图像
        player13.setFitWidth(80); // 设置宽度为矩形的宽度
        player13.setFitHeight(80); // 设置高度为矩形的高度
        ImageView player14 =new ImageView(FXGL.image("player4head.png")); // 替换为您的矩形图像
        player14.setFitWidth(80); // 设置宽度为矩形的宽度
        player14.setFitHeight(80); // 设置高度为矩形的高度
        Entity mapEntity44 = null; // 声明并初始化为null

        if (MyMainMenu.p2_choice == 1) {
            mapEntity44 = FXGL.entityBuilder()
                    .at(20, 150)
                    .view(player11)
                    .buildAndAttach();
        } else if (MyMainMenu.p2_choice == 2) {
            mapEntity44 = FXGL.entityBuilder()
                    .at(20, 150)
                    .view(player12)
                    .buildAndAttach();
        } else if (MyMainMenu.p2_choice == 3) {
            mapEntity44 = FXGL.entityBuilder()
                    .at(20, 150)
                    .view(player13)
                    .buildAndAttach();
        } else if (MyMainMenu.p2_choice == 4) {
            mapEntity44 = FXGL.entityBuilder()
                    .at(20, 150)
                    .view(player14)
                    .buildAndAttach();
        }

        ImageView player1 =new ImageView(FXGL.image("player1head.png")); // 替换为您的矩形图像
        player11.setFitWidth(80); // 设置宽度为矩形的宽度
        player11.setFitHeight(80); // 设置高度为矩形的高度
        ImageView player2 =new ImageView(FXGL.image("player2head.png")); // 替换为您的矩形图像
        player11.setFitWidth(80); // 设置宽度为矩形的宽度
        player11.setFitHeight(80); // 设置高度为矩形的高度
        ImageView player3 =new ImageView(FXGL.image("player3head.png")); // 替换为您的矩形图像
        player11.setFitWidth(80); // 设置宽度为矩形的宽度
        player11.setFitHeight(80); // 设置高度为矩形的高度
        ImageView player4 =new ImageView(FXGL.image("player4head.png")); // 替换为您的矩形图像
        player11.setFitWidth(80); // 设置宽度为矩形的宽度
        player11.setFitHeight(80); // 设置高度为矩形的高度
        Entity mapEntity4 = null; // 声明并初始化为null

        if (MyMainMenu.p1_choice == 1) {
            mapEntity4 = FXGL.entityBuilder()
                    .at(20, 35)
                    .view(player11)
                    .buildAndAttach();
        } else if (MyMainMenu.p1_choice== 2) {
            mapEntity4 = FXGL.entityBuilder()
                    .at(20, 35)
                    .view(player12)
                    .buildAndAttach();
        } else if (MyMainMenu.p1_choice== 3) {
            mapEntity4 = FXGL.entityBuilder()
                    .at(20, 35)
                    .view(player13)
                    .buildAndAttach();
        } else if (MyMainMenu.p1_choice== 4) {
            mapEntity4 = FXGL.entityBuilder()
                    .at(20, 35)
                    .view(player14)
                    .buildAndAttach();
        }


        ImageView blood1= new ImageView(FXGL.image("life.png")); // 替换为您的矩形图像
        blood1.setFitWidth(20); // 设置宽度为矩形的宽度
        blood1.setFitHeight(20); // 设置高度为矩形的高度

        Entity mapEntity0 = FXGL.entityBuilder()
                .at(115, 35)
                .view(blood1) // 使用绿色矩形表示地图
                .buildAndAttach();

        ImageView boob1= new ImageView(FXGL.image("imgboob.png")); // 替换为您的矩形图像
        boob1.setFitWidth(20); // 设置宽度为矩形的宽度
        boob1.setFitHeight(20); // 设置高度为矩形的高度
        Entity mapEntity1 = FXGL.entityBuilder()
                .at(115, 60)
                .view(boob1) // 使用绿色矩形表示地图
                .buildAndAttach();

        ImageView third1= new ImageView(FXGL.image("redme.png")); // 替换为您的矩形图像
        third1.setFitWidth(20); // 设置宽度为矩形的宽度
        third1.setFitHeight(20); // 设置高度为矩形的高度
        Entity mapEntity2 = FXGL.entityBuilder()
                .at(115, 85)
                .view(third1) // 使用绿色矩形表示地图
                .buildAndAttach();

        ImageView move= new ImageView(FXGL.image("moveboot.png")); // 替换为您的矩形图像
        move.setFitWidth(20); // 设置宽度为矩形的宽度
        move.setFitHeight(20); // 设置高度为矩形的高度
        Entity mapEntity3 = FXGL.entityBuilder()
                .at(115, 110)
                .view(move) // 使用绿色矩形表示地图
                .buildAndAttach();




        Text text1=FXGL.addVarText("blood1",150,50);
        Text text2=FXGL.addVarText("bob1",150,75);
        Text redBottleNum1 = FXGL.addVarText("cut1",150,100);
        Text text4=FXGL.addVarText("speed1",150,125);

        ImageView blood2= new ImageView(FXGL.image("life.png")); // 替换为您的矩形图像
        blood2.setFitWidth(20); // 设置宽度为矩形的宽度
        blood2.setFitHeight(20); // 设置高度为矩形的高度
        Entity mapEntity00 = FXGL.entityBuilder()
                .at(115, 150)
                .view(blood2)
                .buildAndAttach();

        ImageView boob2= new ImageView(FXGL.image("imgboob.png")); // 替换为您的矩形图像
        boob2.setFitWidth(20); // 设置宽度为矩形的宽度
        boob2.setFitHeight(20); // 设置高度为矩形的高度
        Entity mapEntity11 = FXGL.entityBuilder()
                .at(115,175 )
                .view(boob2)
                .buildAndAttach();

        ImageView third2= new ImageView(FXGL.image("redme.png")); // 替换为您的矩形图像
        third2.setFitWidth(20); // 设置宽度为矩形的宽度
        third2.setFitHeight(20); // 设置高度为矩形的高度
        Entity mapEntity22 = FXGL.entityBuilder()
                .at(115, 200)
                .view(third2)
                .buildAndAttach();

        ImageView move1 =new ImageView(FXGL.image("moveboot.png")); // 替换为您的矩形图像
        move1.setFitWidth(20); // 设置宽度为矩形的宽度
        move1.setFitHeight(20); // 设置高度为矩形的高度

        Entity mapEntity33 = FXGL.entityBuilder()
                .at(115, 225)
                .view(move1)
                .buildAndAttach();


        Text text11=FXGL.addVarText("blood2",150,165);
        Text text22=FXGL.addVarText("bob2",150,190);
        Text redBottleNum2 = FXGL.addVarText("cut2",150,215);
        Text text44=FXGL.addVarText("speed2",150,240);
        FXGL.addUINode(full1);FXGL.addUINode(full2);
        FXGL.addUINode(full3);FXGL.addUINode(full4);

        toyTimer = FXGL.newLocalTimer();
    }
    //游戏刷新函数，帧率与电脑刷新率有关
    @Override
    protected void onUpdate(double tpf){
        if(gameMode == GameMode.LOCAL){
            local_onupdate();
        }else if(gameMode == GameMode.SERVER){
            server_onupdate();
        }else if(gameMode == GameMode.CLIENT){
            client_onupdate();
        }
    }



    public static void main(String[] args){
        launch(args);
    }

}
