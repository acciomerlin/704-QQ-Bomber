package com.ui;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.net.Client;
import com.almasb.fxgl.net.Server;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static com.almasb.fxgl.dsl.FXGL.*;
import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Pos.CENTER_LEFT;

public class MyMainMenu extends FXGLMenu {
    private GameMode gameMode;
    public static int flag=0;//控制音乐播放
    private AtomicBoolean m_flag;
    private Vector<Double> p1BOMB = new Vector<>();
    private int bld1 = 3;
    public int getBld1() {
        return bld1;
    }
    public void setBld1(int bld1) {
        this.bld1 = bld1;
    }
    private int bld2 = 3;
    public int getBld2() {
        return bld2;
    }
    public void setBld2(int bld2) {
        this.bld2 = bld2;
    }
    private int speed1 = 0;
    public int getSpeed1() {
        return speed1;
    }
    private int speed2 = 0;
    public int getSpeed2() {
        return speed2;
    }
    public Vector<Double> getP1BOMB() {
        return p1BOMB;
    }
    public void setP1BOMB(Vector<Double> p1BOMB) {
        this.p1BOMB = p1BOMB;
    }
    private Vector<Double> p2BOMB = new Vector<>();
    public Vector<Double> getP2BOMB() {
        return p2BOMB;
    }
    public void setP2BOMB(Vector<Double> p2BOMB) {
        this.p2BOMB = p2BOMB;
    }
    public AtomicBoolean getM_flag() {
        return m_flag;
    }
    public GameMode getGameMode() {
        return gameMode;
    }
    private Server<Bundle> server;
    public Server<Bundle> getServer() {
        return server;
    }
    private Client<Bundle> client;
    public Client<Bundle> getClient() {
        return client;
    }
    private InetAddress serverIP;
    private String serverIP_string;
    private ListView<String> chatListView;
    private TextField messageField;
    private String p1name;
    private String p2name;
    boolean isServer;
    private boolean isConnected = false;
    Media btn_moveon = new Media(getClass().getResource("/assets/music/MouseMoveOn.wav").toExternalForm());
    MediaPlayer btn_MoveOn = new MediaPlayer(btn_moveon);
    Media btn_click = new Media(getClass().getResource("/assets/music/click.wav").toExternalForm());
    MediaPlayer btn_Click = new MediaPlayer(btn_click);

    //广播可破坏障碍物熊颜色
    private Vector<Integer> bearColors = new Vector<>();
    public Vector<Integer> getBearColors() {
        return bearColors;
    }
    public void setBearColors(Vector<Integer> bearColors) {
        this.bearColors = bearColors;
    }
    //收到的随机生成道具
    private Vector<Pair<Integer, Pair<Integer,Integer>>> ranToys = new Vector<>();
    public Vector<Pair<Integer, Pair<Integer, Integer>>> getRanToys() {
        return ranToys;
    }
    //收到的p1/p2行动
    private Vector<Pair<Integer, Pair<Double,Double>>> p1moves = new Vector<>();
    public Vector<Pair<Integer, Pair<Double, Double>>> getP1moves() {
        return p1moves;
    }
    private Vector<Pair<Integer, Pair<Double,Double>>> p2moves = new Vector<>();
    public Vector<Pair<Integer, Pair<Double, Double>>> getP2moves() {
        return p2moves;
    }
    //收到的p1/p2放炸弹
    private int p1putbomb = 0;
    public int getP1putbomb() {
        return p1putbomb;
    }
    public void setP1putbomb(int p1putbomb) {
        this.p1putbomb = p1putbomb;
    }
    private int p2putbomb = 0;
    public int getP2putbomb() {
        return p2putbomb;
    }
    public void setP2putbomb(int p2putbomb) {
        this.p2putbomb = p2putbomb;
    }
    //广播爆出来的道具
    private Vector<Pair<Integer, Pair<Integer,Integer>>> brkToys = new Vector<>();
    public Vector<Pair<Integer, Pair<Integer, Integer>>> getBrkToys() {
        return brkToys;
    }
    private static MediaPlayer bgMusic;
    public static MediaPlayer getBgMusic() {
        return bgMusic;
    }
    public  void playBackgroundMusic() { //主菜单音乐
        String musicPath = "/assets/music/background1.wav";
        Media media = new Media(getClass().getResource(musicPath).toExternalForm());
        bgMusic = new MediaPlayer(media);
        bgMusic.setCycleCount(MediaPlayer.INDEFINITE);
       bgMusic.play();
    }
    private void stopAndPlaybtn_moveon() {
        //如果mediaButton不为null并且正在播放，则停止
        if (btn_MoveOn != null && btn_MoveOn.getStatus() == MediaPlayer.Status.PLAYING) {
            btn_MoveOn.stop();
        }
        //重新设置播放位置到起始位置
        btn_MoveOn.seek(btn_MoveOn.getStartTime());
        //播放按钮音效
        btn_MoveOn.play();
    }
    private void stopAndPlaybtn_click() {
        if (btn_Click != null && btn_Click.getStatus() == MediaPlayer.Status.PLAYING) {
            btn_Click.stop();
        }
        btn_Click.seek(btn_Click.getStartTime());
        btn_Click.play();
    }
    private void stopBackgroundmusic(){
        bgMusic.stop();
    }
    private double originVolume = 0;
    private void silenceBackgroundmusic(){
        originVolume = bgMusic.getVolume();
        bgMusic.setVolume(0);
    }
    private void phonateBgMusic() {
        bgMusic.setVolume(originVolume);
    }
    private ImageView read_image;
    private ImageView role1_image;
    private ImageView role2_image;
    private ImageView role3_image;
    private ImageView role4_image;
    private ImageView btnSELECT_image;
    private ImageView btnSTART_image;
    private ImageView castle_image;
    private ImageView grass_image;
    private ImageView snow_image;
    private ImageView btnSELECT_image2;
    boolean player1Flag = false;
    boolean player2Flag = false;
    boolean player3Flag = false;
    boolean player4Flag = false;
    boolean ThisFlagIsMeanToConfuseYou_HaHaHa_OvO = false;
    boolean ThisFlagIsMeanToConfuseMe_HaHaHa_OnO = false;
    boolean selectBT = false;
    Label lb_choosinginfo;
    public int getP1_choice() {
        return p1_choice;
    }
    public int getP2_choice() {
        return p2_choice;
    }
    public static int p1_choice = 0;
    public static int p2_choice = 0;
    private int isChoosing = 0;// from 1 to 4 representing character 1 to 4
    public static int MyMap;
    public static int isChoosingMap = 0;
    private int mapChoice = 0;
    public int getMapChoice() {return mapChoice;}
    int sum = 0;
    int sum2 = 0;
    private Button createTransparentButton(Pane pane, double x, double y, String buttonName) {
        Button button = new Button();
        button.setPrefSize(130, 217);
        button.setStyle("-fx-background-color: transparent;");
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setId(buttonName);
        pane.getChildren().add(button);
        return button;
    }
    private void handleButtonClick1(Button btn_role1, Button btnSELECT) {
        if (!btn_role1.isDisabled()) {
            isChoosing = 1;
            selectBT = true;

            ColorAdjust colorful = new ColorAdjust();
            colorful.setSaturation(0);
            role1_image.setEffect(colorful);  //角色一被选中，设成彩色

            ColorAdjust grey = new ColorAdjust();
            grey.setSaturation(-1);  //其他角色设成灰色
            role2_image.setEffect(grey);
            role3_image.setEffect(grey);
            role4_image.setEffect(grey);
            if (isChoosing != 0) {
                btnSELECT_image.setEffect(colorful);
                btnSELECT.setDisable(false);
            }
            if (player2Flag) {
                role2_image.setEffect(colorful);
            }
            if (player3Flag) {
                role3_image.setEffect(colorful);
            }
            if (player4Flag) {
                role4_image.setEffect(colorful);
            }
            System.out.println("角色一已被选中");
        }
    }
    private void handleButtonClick2(Button btn_role2, Button btnSELECT) {
        if (!btn_role2.isDisabled()) {
            isChoosing = 2;
            selectBT = true;

            ColorAdjust colorful = new ColorAdjust();
            colorful.setSaturation(0);
            role2_image.setEffect(colorful);
            ColorAdjust grey = new ColorAdjust();
            grey.setSaturation(-1);
            role1_image.setEffect(grey);
            role3_image.setEffect(grey);
            role4_image.setEffect(grey);
            if (isChoosing != 0) {
                btnSELECT_image.setEffect(colorful);
                btnSELECT.setDisable(false);
            }
            if (player1Flag)
                role1_image.setEffect(colorful);
            if (player3Flag)
                role3_image.setEffect(colorful);
            if (player4Flag)
                role4_image.setEffect(colorful);
            System.out.println("角色二已被选中");
        }
    }
    private void handleButtonClick3(Button btn_role3, Button btnSELECT) {
        if (!btn_role3.isDisabled()) {
            isChoosing = 3;
            selectBT = true;
            ColorAdjust colorful = new ColorAdjust();
            colorful.setSaturation(0);
            role3_image.setEffect(colorful);
            if (isChoosing != 0) {
                btnSELECT_image.setEffect(colorful);
            }
            ColorAdjust grey = new ColorAdjust();
            grey.setSaturation(-1);
            role1_image.setEffect(grey);
            role2_image.setEffect(grey);
            role4_image.setEffect(grey);
            if (isChoosing != 0) {
                btnSELECT_image.setEffect(colorful);
                btnSELECT.setDisable(false);
            }
            if (player2Flag)
                role2_image.setEffect(colorful);
            if (player1Flag)
                role1_image.setEffect(colorful);
            if (player4Flag)
                role4_image.setEffect(colorful);
            System.out.println("角色三已被选中");
        }
    }
    private void handleButtonClick4(Button btn_role4, Button btnSELECT) {
        if (!btn_role4.isDisabled()) {
            isChoosing = 4;
            selectBT = true;
            ColorAdjust colorAdjustt = new ColorAdjust();
            colorAdjustt.setSaturation(0);
            role4_image.setEffect(colorAdjustt);
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setSaturation(-1);
            role1_image.setEffect(colorAdjust);
            role2_image.setEffect(colorAdjust);
            role3_image.setEffect(colorAdjust);
            if (isChoosing != 0) {
                btnSELECT_image.setEffect(colorAdjustt);
                btnSELECT.setDisable(false);
            }
            if (player2Flag)
                role2_image.setEffect(colorAdjustt);
            if (player3Flag)
                role3_image.setEffect(colorAdjustt);
            if (player1Flag)
                role1_image.setEffect(colorAdjustt);
            System.out.println("角色四已被选中");
        }
    }
    private void handleButtonClick5(Button map_bt1, Button btnSELECT2) {
        if (!map_bt1.isDisabled()) {
            isChoosingMap = 1;
            ColorAdjust colorAdjustt = new ColorAdjust();
            colorAdjustt.setSaturation(0);
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setSaturation(-1);
            snow_image.setEffect(colorAdjust);
            grass_image.setEffect(colorAdjust);
            castle_image.setEffect(colorAdjustt);
            if (isChoosingMap != 0) {
                btnSELECT_image2.setEffect(colorAdjustt);
                btnSELECT2.setDisable(false);
            }
            System.out.println("地图一已被选中"+"地图的选择数是"+isChoosingMap);
            MyMap=1;
        }
    }
    private void handleButtonClick6(Button map_bt2, Button btnSELECT2) {
        if (!map_bt2.isDisabled()) {
            isChoosingMap = 2;
            ColorAdjust colorAdjustt = new ColorAdjust();
            colorAdjustt.setSaturation(0);
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setSaturation(-1);
            snow_image.setEffect(colorAdjust);
            castle_image.setEffect(colorAdjust);
            grass_image.setEffect(colorAdjustt);
            if (isChoosingMap != 0) {
                btnSELECT_image2.setEffect(colorAdjustt);
                btnSELECT2.setDisable(false);
            }
            System.out.println("地图二已被选中"+"地图的选择数是"+isChoosingMap);
            MyMap=2;
        }
    }
    private void handleButtonClick7(Button map_bt3, Button btnSELECT2) {
        if (!map_bt3.isDisabled()) {
            isChoosingMap = 3;
            ColorAdjust colorAdjustt = new ColorAdjust();
            colorAdjustt.setSaturation(0);
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setSaturation(-1);
            grass_image.setEffect(colorAdjust);
            castle_image.setEffect(colorAdjust);
            snow_image.setEffect(colorAdjustt);

            if (isChoosingMap != 0) {
                btnSELECT_image2.setEffect(colorAdjustt);
                btnSELECT2.setDisable(false);
            }
            System.out.println("地图三已被选中"+"地图的选择数是"+isChoosingMap);
            MyMap=3;
        }

    }
    private void handleBtn_select(Button btn_role1, Button btn_role2, Button btn_role3, Button btn_role4, Button btnSELECT, Button btnSTART){
        sum++;
        sum2++;
        System.out.println("选择次数" + sum + "次");
        if (sum2 == 1){
            p1_choice = isChoosing;
            lb_choosinginfo.setText("P2 Choose Character"); // 修改标签的文本内容
        } else if (sum2 == 2){
            lb_choosinginfo.setText("Characters selected!");
            p2_choice = isChoosing;
            System.out.println("p1 = "+ p1_choice +"  p2 = "+ p2_choice);
            btn_role1.setDisable(true);
            btn_role2.setDisable(true);
            btn_role3.setDisable(true);
            btn_role4.setDisable(true);
            btnSELECT.setDisable(true);
            if (p1_choice != 0 && p2_choice != 0 && mapChoice != 0 && sum == 3){
                System.out.println("*******1    "+p1_choice+" "+p2_choice+" "+mapChoice+" "+sum2);
                btnSTART.setDisable(false);

                ColorAdjust colorful = new ColorAdjust();
                colorful.setSaturation(0);
                btnSTART_image.setEffect(colorful);
            }

        }
        isChoosing = 0;
        if (p1_choice == 1){
            player1Flag = true;
            btn_role1.setDisable(true);
        }
        if (p1_choice == 2){
            player2Flag = true;
            btn_role2.setDisable(true);
        }
        if (p1_choice == 3){
            player3Flag = true;
            btn_role3.setDisable(true);
        }
        if (p1_choice == 4){
            player4Flag = true;
            btn_role4.setDisable(true);
        }
        btnSELECT.setDisable(true);
    }
    private void handleBtn_select2(Button map_bt1, Button map_bt2, Button map_bt3, Button btnSELECT2, Button btnSTART){
            mapChoice = isChoosingMap;
            sum++;
            System.out.println("p1 = "+ p1_choice +"  p2 = "+ p2_choice);
            map_bt1.setDisable(true);
            map_bt2.setDisable(true);
            map_bt3.setDisable(true);
            btnSELECT2.setDisable(true);
            if (p1_choice != 0 && p2_choice != 0 && mapChoice != 0 && sum == 3){
                System.out.println("*******2    "+p1_choice+" "+p2_choice+" "+mapChoice+" "+sum);
                btnSTART.setDisable(false);
                ColorAdjust colorful = new ColorAdjust();
                colorful.setSaturation(0);
                btnSTART_image.setEffect(colorful);
            }

        btnSELECT2.setDisable(true);
    }
    private void handleBtn_start() throws FileNotFoundException {
        getContentRoot().getChildren().clear();
        originMenu();
        getController().startNewGame();
    }
    private void handleBtn_back() throws FileNotFoundException {
        getContentRoot().getChildren().clear();
        if(!m_flag.get())
            bgMusic.play();
        originMenu();
    }
    private void role_choose_localMode(Pane pane){
        try {
            lb_choosinginfo = new Label("P1 Choose Character");
            lb_choosinginfo.setLayoutX(430);
            lb_choosinginfo.setLayoutY(108);
            lb_choosinginfo.setFont(Font.font("Arial", FontWeight.BOLD, 16)); // 设置字体样式
            lb_choosinginfo.setTextAlignment(TextAlignment.CENTER); // 设置文本居中对齐
            pane.getChildren().add(lb_choosinginfo);


            Image player1 = new Image(new FileInputStream("src/main/resources/assets/ui/player1.png"));
            role1_image = new ImageView(player1);
            role1_image.setFitWidth(123);
            role1_image.setFitHeight(166);
            role1_image.setLayoutX(260);
            role1_image.setLayoutY(155);

            Image player2 = new Image(new FileInputStream("src/main/resources/assets/ui/player2.png"));
            role2_image = new ImageView(player2);
            role2_image.setFitWidth(123);
            role2_image.setFitHeight(166);
            role2_image.setLayoutX(396);
            role2_image.setLayoutY(155);

            Image player3 = new Image(new FileInputStream("src/main/resources/assets/ui/player3.png"));
            role3_image = new ImageView(player3);
            role3_image.setFitWidth(123);
            role3_image.setFitHeight(170);
            role3_image.setLayoutX(532);
            role3_image.setLayoutY(155);

            Image player4 = new Image(new FileInputStream("src/main/resources/assets/ui/player4.png"));
            role4_image = new ImageView(player4);
            role4_image.setFitWidth(123);
            role4_image.setFitHeight(170);
            role4_image.setLayoutX(669);
            role4_image.setLayoutY(155);

            Image select = new Image(new FileInputStream("src/main/resources/assets/ui/selectBT.png"));
            btnSELECT_image = new ImageView(select);
            btnSELECT_image.setFitWidth(110);
            btnSELECT_image.setFitHeight(45);
            btnSELECT_image.setLayoutX(468);
            btnSELECT_image.setLayoutY(371);

            Image start = new Image(new FileInputStream("src/main/resources/assets/ui/startBT.png"));
            btnSTART_image = new ImageView(start);
            btnSTART_image.setFitWidth(163);
            btnSTART_image.setFitHeight(81);
            btnSTART_image.setLayoutX(595);
            btnSTART_image.setLayoutY(488);

            Image level1 = new Image(new FileInputStream("src/main/resources/assets/ui/castleselect.png"));
            castle_image = new ImageView(level1);
            castle_image.setFitWidth(99);
            castle_image.setFitHeight(79);
            castle_image.setLayoutX(20);
            castle_image.setLayoutY(455);

            Image level2 = new Image(new FileInputStream("src/main/resources/assets/ui/treeselect.png"));
            grass_image = new ImageView(level2);
            grass_image.setFitWidth(99);
            grass_image.setFitHeight(79);
            grass_image.setLayoutX(133);
            grass_image.setLayoutY(455);

            Image level3 = new Image(new FileInputStream("src/main/resources/assets/ui/snowselect.png"));
            snow_image = new ImageView(level3);
            snow_image.setFitWidth(99);
            snow_image.setFitHeight(79);
            snow_image.setLayoutX(247);
            snow_image.setLayoutY(455);

            Image sss = new Image(new FileInputStream("src/main/resources/assets/ui/selectBT2.png"));
            btnSELECT_image2 = new ImageView(sss);
            btnSELECT_image2.setFitWidth(83);
            btnSELECT_image2.setFitHeight(30);
            btnSELECT_image2.setLayoutX(141);
            btnSELECT_image2.setLayoutY(545);

            // 设置图片滤镜为黑白滤镜
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setSaturation(-1); // 设置为-1表示转换为黑白色
            role1_image.setEffect(colorAdjust);
            role2_image.setEffect(colorAdjust);
            role3_image.setEffect(colorAdjust);
            role4_image.setEffect(colorAdjust);
            castle_image.setEffect(colorAdjust);
            grass_image.setEffect(colorAdjust);
            snow_image.setEffect(colorAdjust);
            btnSELECT_image.setEffect(colorAdjust);
            btnSELECT_image2.setEffect(colorAdjust);
            btnSTART_image.setEffect(colorAdjust);
            pane.getChildren().add(role1_image);
            pane.getChildren().add(role2_image);
            pane.getChildren().add(role3_image);
            pane.getChildren().add(role4_image);
            pane.getChildren().add(btnSELECT_image);
            pane.getChildren().add(btnSTART_image);
            pane.getChildren().add(castle_image);
            pane.getChildren().add(grass_image);
            pane.getChildren().add(snow_image);
            pane.getChildren().add(btnSELECT_image2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // 处理文件未找到异常
        }
        System.out.println("大厅已显示");


        Button btn_role1 = createTransparentButton(pane, 260, 137, "btn_role1");
        Button btn_role2 = createTransparentButton(pane, 396, 137, "btn_role2");
        Button btn_role3 = createTransparentButton(pane, 532, 137, "btn_role3");
        Button btn_role4 = createTransparentButton(pane, 669, 137, "btn_role4");
        Button map_bt1 = createTransparentButton(pane, 20, 455, "map_bt1");
        Button map_bt2 = createTransparentButton(pane, 133, 455, "map_bt2");
        Button map_bt3 = createTransparentButton(pane, 247, 455, "map_bt3");
        Button btnSELECT = new Button();//select
        btnSELECT.setPrefSize(110, 45);
        btnSELECT.setStyle("-fx-background-color: transparent;");
        btnSELECT.setLayoutX(468);
        btnSELECT.setLayoutY(371);
        btnSELECT.setId("btnSELECT");
        btnSELECT.setDisable(true);
        pane.getChildren().add(btnSELECT);

        Button btnSELECT2 = new Button();//select
        btnSELECT2.setPrefSize(83, 30);
        btnSELECT2.setStyle("-fx-background-color: transparent;");
        btnSELECT2.setLayoutX(141);
        btnSELECT2.setLayoutY(545);
        btnSELECT2.setId("btnSELECT2");
        btnSELECT.setDisable(true);
        pane.getChildren().add(btnSELECT2);

        Button btnSTART = new Button();
        btnSTART.setPrefSize(163, 81);
        btnSTART.setStyle("-fx-background-color: transparent;");
        btnSTART.setLayoutX(595);
        btnSTART.setLayoutY(488);
        btnSTART.setId("btnSELECT");
        btnSTART.setDisable(true);
        pane.getChildren().add(btnSTART);

        Button btnBACK = new Button();
        btnBACK.setPrefSize(55, 64);
        btnBACK.setStyle("-fx-background-color: transparent;");
        btnBACK.setLayoutX(742);
        btnBACK.setLayoutY(10);
        btnBACK.setId("btnBACK");
        pane.getChildren().add(btnBACK);

        btn_role1.setOnAction(e -> {
            stopAndPlaybtn_moveon();;handleButtonClick1(btn_role1,btnSELECT);});
        btn_role2.setOnAction(e -> {
            stopAndPlaybtn_moveon();;handleButtonClick2(btn_role2,btnSELECT);});
        btn_role3.setOnAction(e -> {
            stopAndPlaybtn_moveon();handleButtonClick3(btn_role3,btnSELECT);});
        btn_role4.setOnAction(e -> {
            stopAndPlaybtn_moveon();handleButtonClick4(btn_role4, btnSELECT);});
        map_bt1.setOnAction(e -> {
            stopAndPlaybtn_moveon();handleButtonClick5(map_bt1, btnSELECT2);});
        map_bt2.setOnAction(e -> {
            stopAndPlaybtn_moveon();handleButtonClick6(map_bt2, btnSELECT2);});
        map_bt3.setOnAction(e -> {
            stopAndPlaybtn_moveon();handleButtonClick7(map_bt3, btnSELECT2);});
        btnSELECT.setOnAction(e -> {stopAndPlaybtn_click();handleBtn_select(btn_role1, btn_role2, btn_role3, btn_role4, btnSELECT, btnSTART);
            });
        btnSELECT2.setOnAction(e ->{ stopAndPlaybtn_click();handleBtn_select2(map_bt1, map_bt2, map_bt3,  btnSELECT2, btnSTART);
            });
        btnSTART.setOnAction(e -> {
            stopAndPlaybtn_click();
            try {
                handleBtn_start();
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
        btnBACK.setOnAction(e -> {
            stopAndPlaybtn_click();
            try {
                handleBtn_back();
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            if(isServer){
                //server模式,先让自己能看到自己发的信息
                chatListView.getItems().add(p1name+"[You]: " + message);
                String mess = p1name+"[Host]: " + message;
                messageField.clear();
                //打包这条信息
                var bundle = new Bundle("msg");
                bundle.put("p1msg",mess);
                //发给client
                server.broadcast(bundle);
            }else {
                chatListView.getItems().add(p2name+"[You]: " + message);
                String mess = p2name+"[Guest]: " + message;
                messageField.clear();
                //同理server发信息
                var bundle = new Bundle("msg");
                bundle.put("p2msg",mess);
                client.broadcast(bundle);
            }
        }
    }
    private static final Pattern PATTERN_IP = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private boolean checkIP(String text)
    {
        return PATTERN_IP.matcher(text).matches() ? true : false;
    }
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void handlebtnSELECT_host(Button btn_role1, Button btn_role2, Button btn_role3,  Button btn_role4, Button btnSELECT, Button btnSTART){
        p1_choice = isChoosing;
        var bundle = new Bundle("p1choice");
        bundle.put("p1role",p1_choice);
        server.broadcast(bundle);
        lb_choosinginfo.setText("Waiting for P2 to Choose Character..."); // 修改标签的文本内容
        btn_role1.setDisable(true);
        btn_role2.setDisable(true);
        btn_role3.setDisable(true);
        btn_role4.setDisable(true);
        ColorAdjust grey = new ColorAdjust();
        grey.setSaturation(-1);
        btnSELECT_image.setEffect(grey);
        btnSELECT.setDisable(true);
        isChoosing = 0;
        if (p1_choice == 1){
            player1Flag = true;
        }
        if (p1_choice == 2){
            player2Flag = true;
        }
        if (p1_choice == 3){
            player3Flag = true;
        }
        if (p1_choice == 4){
            player4Flag = true;
        }
    }
    private void handlebtnSELECT_guest(Button btn_role1, Button btn_role2, Button btn_role3,  Button btn_role4, Button btnSELECT, Button btnSTART){
        p2_choice = isChoosing;
        var bundle = new Bundle("p2choice");
        bundle.put("p2role",p2_choice);
        client.broadcast(bundle);
        lb_choosinginfo.setText("Finished!Waiting for host to start the game...:)"); // 修改标签的文本内容
        btn_role1.setDisable(true);
        btn_role2.setDisable(true);
        btn_role3.setDisable(true);
        btn_role4.setDisable(true);
        ColorAdjust grey = new ColorAdjust();
        grey.setSaturation(-1);
        btnSELECT_image.setEffect(grey);
        btnSELECT.setDisable(true);
        isChoosing = 0;
        if (p2_choice == 1){
            player1Flag = true;
        }
        if (p2_choice == 2){
            player2Flag = true;
        }
        if (p2_choice == 3){
            player3Flag = true;
        }
        if (p2_choice == 4){
            player4Flag = true;
        }
    }
    /**
     *界面们
     */
    //构造函数，初始主菜单
    public MyMainMenu() throws FileNotFoundException {

        super(MenuType.MAIN_MENU);

        //静音按钮
        ImageView musicON = new ImageView(FXGL.image("main_musicON.png"));
        musicON.setFitWidth(56);
        musicON.setFitHeight(50);
        ImageView musicOFF = new ImageView(FXGL.image("main_musicOFF.png"));
        musicOFF.setFitWidth(56);
        musicOFF.setFitHeight(50);
        Button btn_stopbgmusic = new Button();
        btn_stopbgmusic.setGraphic(musicON);
        btn_stopbgmusic.setPrefSize(56, 50);
        btn_stopbgmusic.setTranslateX(695);
        btn_stopbgmusic.setTranslateY(176);
        btn_stopbgmusic.setStyle("-fx-background-color: transparent;");
        m_flag = new AtomicBoolean(false);
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

        playBackgroundMusic();
        p1name = "";p2name="";
        Pane pane = new Pane();
        Image Menu_image = new Image(new FileInputStream("src/main/resources/assets/ui/Menu.png"));
        BackgroundImage Menu_bg = new BackgroundImage(
                Menu_image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
        );
        Background background = new Background(Menu_bg);
        pane.setPrefSize(getAppWidth(), getAppHeight());
        pane.setBackground(background);
        getContentRoot().getChildren().setAll(pane);

        ImageView local_mode = new ImageView(FXGL.image("local_mode.png"));
        ImageView local_mode_light = new ImageView(FXGL.image("local_mode_light.png"));

        Button btn1 = new Button();
        btn1.setGraphic(local_mode);
        btn1.setPrefSize(200, 50);
        btn1.setTranslateX(450);
        btn1.setTranslateY(230);
        btn1.setStyle("-fx-background-color: transparent;");
        btn1.setOnMouseEntered(e -> {
            stopAndPlaybtn_moveon();
            btn1.setGraphic(local_mode_light);
        });
        btn1.setOnMouseExited(e -> btn1.setGraphic(local_mode));
        btn1.setOnAction(e->{
            stopAndPlaybtn_click();
            getContentRoot().getChildren().clear();
            try {
                consoleMode(); //单机模式UI此处走
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });

        ImageView online_mode = new ImageView(FXGL.image("online_mode.png"));
        ImageView online_mode_light = new ImageView(FXGL.image("online_mode_light.png"));
        Button btn2 = new Button();
        btn2.setGraphic(online_mode);
        btn2.setPrefSize(200, 50);
        btn2.setTranslateX(430);
        btn2.setTranslateY(210);
        btn2.setStyle("-fx-background-color: transparent;");
        btn2.setOnMouseEntered(e -> {
            stopAndPlaybtn_moveon();
            btn2.setGraphic(online_mode_light);
        });
        btn2.setOnMouseExited(e -> btn2.setGraphic(online_mode));
        btn2.setOnAction(event->{
            stopAndPlaybtn_click();
            getContentRoot().getChildren().clear(); //清屏，伪界面跳转
            netMode();
        });

        ImageView help = new ImageView(FXGL.image("help.png"));
        ImageView help_light = new ImageView(FXGL.image("help_light.png"));
        Button btn3 = new Button();
        btn3.setGraphic(help);
        btn3.setPrefSize(200, 50);
        btn3.setTranslateX(582);
        btn3.setTranslateY(190);
        btn3.setStyle("-fx-background-color: transparent;");
        btn3.setOnMouseEntered(e -> {
            stopAndPlaybtn_moveon();
            btn3.setGraphic(help_light);
        });
        btn3.setOnMouseExited(e -> btn3.setGraphic(help));
        btn3.setOnAction(event->{
            bgMusic.pause();
            stopAndPlaybtn_click();
            getContentRoot().getChildren().clear(); //清屏，伪界面跳转
            try {
                read_help();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        ImageView exit = new ImageView(FXGL.image("exit.png"));
        ImageView exit_light = new ImageView(FXGL.image("exit_light.png"));
        Button btn4 = new Button();
        btn4.setGraphic(exit);
        btn4.setPrefSize(200, 50);
        btn4.setTranslateX(582);
        btn4.setTranslateY(160);
        btn4.setStyle("-fx-background-color: transparent;");
        btn4.setOnMouseEntered(e -> {
            stopAndPlaybtn_moveon();
            btn4.setGraphic(exit_light);});
        btn4.setOnMouseExited(e -> btn4.setGraphic(exit));
        btn4.setOnAction(event->{
            getController().exit();
        });

        VBox box = new VBox(btn1,btn2,btn3,btn4);

        getContentRoot().getChildren().add(box);
        getContentRoot().getChildren().add(btn_stopbgmusic);
    }
    //帮助文档查看界面
    public void read_help() throws FileNotFoundException {
        Pane pane = new Pane();
        Image Menu_image = new Image(new FileInputStream("src/main/resources/assets/textures/read_help_bg.png"));
        BackgroundImage Menu_bg = new BackgroundImage(
                Menu_image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
        );
        Background background = new Background(Menu_bg);
        pane.setPrefSize(getAppWidth(), getAppHeight());
        pane.setBackground(background);
        getContentRoot().getChildren().setAll(pane);

        ImageView menu = new ImageView(FXGL.image("menu.png"));
        ImageView menu_light = new ImageView(FXGL.image("menu_light.png"));

        Image readMeImage = new Image(new FileInputStream("src/main/resources/assets/textures/read_me.png"));
        read_image = new ImageView(readMeImage);
        read_image.setFitWidth(607);
        read_image.setFitHeight(500);
        read_image.setLayoutX(97);
        read_image.setLayoutY(50);


        Button button1 = new Button();
        button1.setGraphic(menu);
        button1.setPrefSize(200, 50);
        button1.setTranslateX(85);
        button1.setTranslateY(460);
        button1.setStyle("-fx-background-color: transparent;");


        button1.setOnMouseEntered(e -> {
            stopAndPlaybtn_moveon();
            button1.setGraphic(menu_light);
        });
        button1.setOnMouseExited(e -> button1.setGraphic(menu));
        button1.setOnAction(e->{
            stopAndPlaybtn_click();
            getContentRoot().getChildren().clear();
            try {
                originMenu();
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
        getContentRoot().getChildren().add(read_image);
        getContentRoot().getChildren().add(button1);
    }
    //单机游戏大厅界面
    public void consoleMode() throws FileNotFoundException {
        bgMusic.pause();
        this.gameMode = GameMode.LOCAL;
        Pane pane = new Pane();
        Image bg = new Image(new FileInputStream("src/main/resources/assets/ui/localHall.png"));
        BackgroundImage backgroundImage = new BackgroundImage(
                bg,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
        );
        Background background = new Background(backgroundImage);
        pane.setPrefSize(getAppWidth(), getAppHeight());
        pane.setBackground(background);

        role_choose_localMode(pane); //把角色选择逻辑封了

        getContentRoot().getChildren().setAll(pane);
    }
    //联机模式选择创建或加入房间界面
    public void netMode(){
        bgMusic.pause();
        Text text1 = new Text("联机模式");
        Button button1 = new Button("创建房间");
        button1.setOnAction(e->{
            stopAndPlaybtn_click();
            getContentRoot().getChildren().clear();
            crtRoom();
        });
        Button button2 = new Button("加入房间");
        button2.setOnAction(e->{
            stopAndPlaybtn_click();
            getContentRoot().getChildren().clear();
            joinRoom();
        });
        Button button3 = new Button("返回主界面");
        button3.setOnAction(e->{
            stopAndPlaybtn_click();
            getContentRoot().getChildren().clear();
            try {
                originMenu();
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
        button1.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white; -fx-font-size: 20px; -fx-min-width: 150px; -fx-min-height: 40px;");
        button2.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white; -fx-font-size: 20px; -fx-min-width: 150px; -fx-min-height: 40px;");
        button3.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white; -fx-font-size: 20px; -fx-min-width: 150px; -fx-min-height: 40px;");
        text1.setStyle("-fx-font: normal bold 50px 'serif' ");
        text1.setLayoutX(100);
        text1.setLayoutY(280);
        button1.setLayoutX(100);
        button1.setLayoutY(320);
        button2.setLayoutX(300);
        button2.setLayoutY(320);
        button3.setLayoutX(500);
        button3.setLayoutY(320);
        Pane pane = new Pane();
        pane.setStyle("-fx-background-color: BEIGE;");
        pane.setMinSize(getAppWidth(), getAppHeight());
        pane.getChildren().add(button1);
        pane.getChildren().add(button2);
        pane.getChildren().add(button3);
        pane.getChildren().add(text1);

        getContentRoot().getChildren().setAll(pane);
    }
    //联机模式创建房间界面
    public void crtRoom(){
        this.gameMode = GameMode.SERVER;
        isServer = true;
        Text text1 = new Text("Nickname");
        TextField textField1 = new TextField();
        textField1.setText("HOST");
        Button btn_crtRoom = new Button("创建房间");
        Button button2 = new Button("返回上级界面");
        //Creating a Grid Pane
        GridPane gridPane = new GridPane();
        //Setting size for the pane
        gridPane.setMinSize(getAppWidth(), getAppHeight());
        //Setting the padding
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        //Setting the vertical and horizontal gaps between the columns
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        //Setting the Grid alignment
        gridPane.setAlignment(CENTER);
        //Arranging all the nodes in the grid
        gridPane.add(text1, 0, 0);
        gridPane.add(textField1, 1, 0);
        gridPane.add(btn_crtRoom, 0, 2);
        gridPane.add(button2, 1, 2);
        //Styling nodes
        btn_crtRoom.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
        button2.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
        text1.setStyle("-fx-font: normal bold 20px 'serif' ");
        gridPane.setStyle("-fx-background-color: BEIGE;");
        getContentRoot().getChildren().setAll(gridPane);
        //返回上级界面和房间创建按钮事件处理
        button2.setOnAction(e->{
            stopAndPlaybtn_moveon();
            getContentRoot().getChildren().clear();
            netMode();
        });
        btn_crtRoom.setOnAction(event->{
            stopAndPlaybtn_moveon(); //按钮声音
            //避免检验时影响界面线程另开一个executor
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    //检验ip是否可用
                    serverIP = InetAddress.getLocalHost();
                    System.out.println(serverIP);
                    Platform.runLater(() -> {
                        //ip可用，准备创建房间，进行界面跳转
                        getContentRoot().getChildren().clear();
                        p1name = textField1.getText();
                        System.out.println(p1name);
                        try {
                            gameHall_net();
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });
            executor.shutdown();
        });
    }
    //联机模式加入房间界面
    public  void  joinRoom(){
        this.gameMode = GameMode.CLIENT;
        isServer = false;
        Text text1 = new Text("Nickname");
        Text text2 = new Text("IP address");
        TextField tf_clientname = new TextField();
        tf_clientname.setText("player2");
        TextField tf_serverip = new TextField();
        Button btn_joinRoom = new Button("加入房间");
        btn_joinRoom.setDisable(true);
        Button button2 = new Button("返回上级");
        Label label = new Label("Invalid ip address");
        label.setVisible(false);
        Label waitCntlabel = new Label("正在连接.......");
        waitCntlabel.setVisible(false);
        GridPane gridPane = new GridPane();
        gridPane.setMinSize(getAppWidth(), getAppHeight());
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setAlignment(CENTER);
        //Arranging all the nodes in the grid
        gridPane.add(text1, 0, 0);
        gridPane.add(tf_clientname, 1, 0);
        gridPane.add(text2, 0, 1);
        gridPane.add(tf_serverip, 1, 1);
        gridPane.add(label,2,1);
        gridPane.add(btn_joinRoom, 0, 2);
        gridPane.add(waitCntlabel, 0, 3);
        gridPane.add(button2, 1, 2);
        //Styling nodes
        btn_joinRoom.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
        button2.setStyle("-fx-background-color: darkslateblue; -fx-text-fill: white;");
        text1.setStyle("-fx-font: normal bold 20px 'serif' ");
        text2.setStyle("-fx-font: normal bold 20px 'serif' ");
        gridPane.setStyle("-fx-background-color: BEIGE;");
        getContentRoot().getChildren().setAll(gridPane);

        //ip不合法不可以连接房间
        tf_serverip.textProperty().addListener(e -> {
            serverIP_string = tf_serverip.getText().trim();
            if (!checkIP(serverIP_string) && !serverIP_string.isEmpty()){
                label.setVisible(true);
                btn_joinRoom.setDisable(true);
            }else if(serverIP_string.isEmpty()){
                label.setVisible(false);
            }else {
                label.setVisible(false);
                btn_joinRoom.setDisable(false);
            }

        });
        btn_joinRoom.setOnAction(e->{
            stopAndPlaybtn_moveon(); //按钮声音
            waitCntlabel.setVisible(true);
            int CONNECTION_TIMEOUT = 4000; // 设置连接超时时间为5秒
            AtomicBoolean validRoom = new AtomicBoolean(false);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    InetAddress serverAddress = InetAddress.getByName(serverIP_string);
                    int serverPort = 8889;
                    //用isReacherbale检验是否可达
                    serverAddress.isReachable(CONNECTION_TIMEOUT);
                    System.out.println("can be connected to the server!");
                    validRoom.set(true);
                    Platform.runLater(() -> {
                        waitCntlabel.setVisible(false);
                        if(validRoom.get()){
                            try {
                                serverIP = serverAddress;
                                p2name = tf_clientname.getText();
                                gameHall_net();
                            } catch (FileNotFoundException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                } catch (IOException ex) {
                    //不可达，报错显示专门的报错提示
                    Platform.runLater(() -> showErrorAlert("Connection Error", "Failed to connect to the server: " + ex.getMessage()));
                    ex.printStackTrace();
                    waitCntlabel.setVisible(false);
                }
            });
            executor.shutdown();
        });
        button2.setOnAction(e->{
            stopAndPlaybtn_moveon();
            if(client != null){
                client.disconnect();
            }
            getContentRoot().getChildren().clear();
            netMode();
        });
    }
    //联机模式游戏大厅
    public void gameHall_net() throws FileNotFoundException {
        /*****UI****/
        mapChoice = 3;
        Image bg =  new Image(new FileInputStream("src/main/resources/assets/ui/netHall.png"));
        BackgroundImage backgroundImage = new BackgroundImage(
                bg,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
        );
        Background background = new Background(backgroundImage);
        //返回按钮
        Button bckButton = new Button();
        bckButton.setLayoutX(746);
        bckButton.setLayoutY(13);
        bckButton.setPrefSize(53,51);
        bckButton.setStyle("-fx-background-color: transparent");
        bckButton.setOnMouseClicked(e->{
            stopAndPlaybtn_click();
            if(server != null){
                server.stop();
            }
            getContentRoot().getChildren().clear();
            if(isServer){
                crtRoom();
            }else{
                joinRoom();
            }
        });
        //p1name显示
        TextField tf_p1 = new TextField();
        tf_p1.setLayoutX(63);
        tf_p1.setLayoutY(183);
        tf_p1.setPrefWidth(90);
        tf_p1.setAlignment(CENTER_LEFT);
        tf_p1.setEditable(false);
        tf_p1.setFont(Font.font("Arial", 12));
        tf_p1.setStyle("-fx-background-color: transparent; -fx-text-fill: #8B0000;");
        tf_p1.setText(p1name);
        //p2name显示
        TextField tf_p2 = new TextField();
        tf_p2.setLayoutX(63);
        tf_p2.setLayoutY(221);
        tf_p2.setPrefWidth(90);
        tf_p2.setAlignment(CENTER);
        tf_p2.setEditable(false);
        tf_p2.setFont(Font.font("Arial", 12));
        tf_p2.setStyle("-fx-background-color: transparent; -fx-text-fill: #3A3A3A;");
        tf_p2.setText(p2name);
        //聊天框实现
        BorderPane chatPane = new BorderPane();
        chatPane.setPadding(new Insets(5));
        chatPane.setLayoutX(4);
        chatPane.setLayoutY(463);
        chatListView = new ListView<>();
        chatListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: black");
                } else {
                    setText(item);
                    setStyle("-fx-font-size: 9pt;");
                }
            }
        });
        chatListView.setPrefHeight(100);
        chatPane.setCenter(chatListView);
        HBox inputBox = new HBox();
        inputBox.setSpacing(10);
        inputBox.setPadding(new Insets(3));
        messageField = new TextField();
        messageField.setAlignment(CENTER_LEFT); // 设置文本居中对齐
        messageField.setPrefWidth(340);
        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());
        inputBox.getChildren().addAll(messageField, sendButton);
        chatPane.setBottom(inputBox);
        Pane pane = new Pane();
        pane.setPrefSize(getAppWidth(),getAppHeight());
        pane.setBackground(background);
        pane.getChildren().add(bckButton);
        pane.getChildren().add(tf_p1);
        pane.getChildren().add(tf_p2);
        pane.getChildren().add(chatPane);
        try {
            lb_choosinginfo = new Label(" ");
            lb_choosinginfo.setLayoutX(400);
            lb_choosinginfo.setLayoutY(108);
            lb_choosinginfo.setFont(Font.font("Arial", FontWeight.BOLD, 16)); // 设置字体样式
            lb_choosinginfo.setTextAlignment(TextAlignment.LEFT); // 设置文本居中对齐
            pane.getChildren().add(lb_choosinginfo);
            Image player1 = new Image(new FileInputStream("src/main/resources/assets/ui/player1.png"));
            role1_image = new ImageView(player1);
            role1_image.setFitWidth(123);
            role1_image.setFitHeight(166);
            role1_image.setLayoutX(260);
            role1_image.setLayoutY(155);
            Image player2 = new Image(new FileInputStream("src/main/resources/assets/ui/player2.png"));
            role2_image = new ImageView(player2);
            role2_image.setFitWidth(123);
            role2_image.setFitHeight(166);
            role2_image.setLayoutX(396);
            role2_image.setLayoutY(155);
            Image player3 = new Image(new FileInputStream("src/main/resources/assets/ui/player3.png"));
            role3_image = new ImageView(player3);
            role3_image.setFitWidth(123);
            role3_image.setFitHeight(170);
            role3_image.setLayoutX(532);
            role3_image.setLayoutY(155);
            Image player4 = new Image(new FileInputStream("src/main/resources/assets/ui/player4.png"));
            role4_image = new ImageView(player4);
            role4_image.setFitWidth(123);
            role4_image.setFitHeight(170);
            role4_image.setLayoutX(669);
            role4_image.setLayoutY(155);
            Image select = new Image(new FileInputStream("src/main/resources/assets/ui/selectBT.png"));
            btnSELECT_image = new ImageView(select);
            btnSELECT_image.setFitWidth(110);
            btnSELECT_image.setFitHeight(45);
            btnSELECT_image.setLayoutX(468);
            btnSELECT_image.setLayoutY(371);
            Image start = new Image(new FileInputStream("src/main/resources/assets/ui/startBT.png"));
            btnSTART_image = new ImageView(start);
            btnSTART_image.setFitWidth(163);
            btnSTART_image.setFitHeight(81);
            btnSTART_image.setLayoutX(595);
            btnSTART_image.setLayoutY(488);
            // 设置图片滤镜为黑白滤镜
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setSaturation(-1); // 设置为-1表示转换为黑白色
            role1_image.setEffect(colorAdjust);
            role2_image.setEffect(colorAdjust);
            role3_image.setEffect(colorAdjust);
            role4_image.setEffect(colorAdjust);
            btnSELECT_image.setEffect(colorAdjust);
            btnSTART_image.setEffect(colorAdjust);
            pane.getChildren().add(role1_image);
            pane.getChildren().add(role2_image);
            pane.getChildren().add(role3_image);
            pane.getChildren().add(role4_image);
            pane.getChildren().add(btnSELECT_image);
            pane.getChildren().add(btnSTART_image);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("HOST大厅已显示");
        Button btn_role1 = createTransparentButton(pane, 260, 137, "btn_role1");
        Button btn_role2 = createTransparentButton(pane, 396, 137, "btn_role2");
        Button btn_role3 = createTransparentButton(pane, 532, 137, "btn_role3");
        Button btn_role4 = createTransparentButton(pane, 669, 137, "btn_role4");
        Button btnSELECT = new Button();//select
        btnSELECT.setPrefSize(110, 45);
        btnSELECT.setStyle("-fx-background-color: transparent;");
        btnSELECT.setLayoutX(468);
        btnSELECT.setLayoutY(371);
        btnSELECT.setId("btnSELECT");
        btnSELECT.setDisable(true);
        pane.getChildren().add(btnSELECT);
        Button btnSTART = new Button();
        btnSTART.setPrefSize(163, 81);
        btnSTART.setStyle("-fx-background-color: transparent;");
        btnSTART.setLayoutX(595);
        btnSTART.setLayoutY(488);
        btnSTART.setId("btnSELECT");
        btnSTART.setDisable(true);
        pane.getChildren().add(btnSTART);
        Button btnBACK = new Button();
        btnBACK.setPrefSize(55, 64);
        btnBACK.setStyle("-fx-background-color: transparent;");
        btnBACK.setLayoutX(742);
        btnBACK.setLayoutY(10);
        btnBACK.setId("btnBACK");
        pane.getChildren().add(btnBACK);
        btnBACK.setOnAction(e -> {
            stopAndPlaybtn_click();
            if(isServer){
                showErrorAlert("WARNING:","Your Room will be closed");
                var bundle = new Bundle("leftINFO");
                bundle.put("lastmsg",1);
                server.broadcast(bundle);
                server.stop();
            }else {
                showErrorAlert("INFO:","You left the room");
                String lastMess = p2name + "[Guest]离开了房间";
                var bundle = new Bundle("leftINFO");
                bundle.put("lastmsg",lastMess);
                client.broadcast(bundle);
                client.disconnect();
            }
            try {
                handleBtn_back();
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });  //回到上级
        /******UI*****/
        /*****NET******/
        if (isServer) {
            //聊天框添加房主创建房间信息
            lb_choosinginfo.setText("Waiting for p2 to join the room...");
            String first = p1name + "[You]创建了房间(ip address: " + serverIP + ")";
            chatListView.getItems().add(first);
            //开启TCPserver可以接受连接了
            server = getNetService().newTCPServer(8889);
            server.startAsync();
            //启动监听连接，一旦连接上让角色选择等按钮变为可用
            server.setOnConnected(connection -> {
                System.out.println("a client!");
                btn_role1.setOnAction(e -> {stopAndPlaybtn_moveon();handleButtonClick1(btn_role1,btnSELECT);});
                btn_role2.setOnAction(e -> {stopAndPlaybtn_moveon();handleButtonClick2(btn_role2,btnSELECT);});
                btn_role3.setOnAction(e -> {stopAndPlaybtn_moveon();handleButtonClick3(btn_role3,btnSELECT);});
                btn_role4.setOnAction(e -> {stopAndPlaybtn_moveon();handleButtonClick4(btn_role4, btnSELECT);});
                btnSELECT.setOnAction(e -> {
                    stopAndPlaybtn_click();
                    handlebtnSELECT_host(btn_role1, btn_role2, btn_role3, btn_role4, btnSELECT, btnSTART);
                });
                btnSTART.setOnAction(e -> {
                    stopAndPlaybtn_click();
                    var bundle = new Bundle("start");
                    bundle.put("start",true);
                    server.broadcast(bundle);
                    try {
                        handleBtn_start();
                    } catch (FileNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                }); //开始游戏
                //监听来自client的信息
                connection.addMessageHandlerFX((conn, message) -> {
                    System.out.println("receive form client");
                    lb_choosinginfo.setText("P2 has joined! Please select your character:");
                    btn_role1.setDisable(false);
                    btn_role2.setDisable(false);
                    btn_role3.setDisable(false);
                    btn_role4.setDisable(false);
                    if(message.get("connecting")!=null){
                        isConnected = message.get("connecting");
                        if(isConnected){
                            p2name = message.get("p2name");  //p2昵称
                            tf_p2.setText(p2name);
                            String firstGuestMsg = p2name + "[Guest]进入了房间(ip address: " + serverIP + ")";
                            chatListView.getItems().add(firstGuestMsg);
                            String firstMess = p1name + "[Host]创建了房间(ip address: " + serverIP + ")";
                            var bundle = new Bundle("roomINFO");
                            bundle.put("p1name",p1name);
                            bundle.put("firstmsg",firstMess);
                            server.broadcast(bundle);
                        }
                    }
                    if(message.get("p2msg")!=null){
                        String p2msg = message.get("p2msg");
                        chatListView.getItems().add(p2msg);
                    }
                    if(message.get("p2role")!=null){
                        lb_choosinginfo.setText("P2 Has Finished! Press the btn to start the game! :)");
                        p2_choice = message.get("p2role");
                        btn_role1.setDisable(true);
                        btn_role2.setDisable(true);
                        btn_role3.setDisable(true);
                        btn_role4.setDisable(true);
                        btnSELECT.setDisable(true);
                        ColorAdjust colorful = new ColorAdjust();
                        colorful.setSaturation(0);
                        btnSTART_image.setEffect(colorful);
                        btnSTART.setDisable(false);
                        if (p2_choice == 1){
                            player1Flag = true;
                            role1_image.setEffect(colorful);
                        }
                        if (p2_choice == 2){
                            player2Flag = true;
                            role2_image.setEffect(colorful);
                        }
                        if (p2_choice == 3){
                            player3Flag = true;
                            role3_image.setEffect(colorful);
                        }
                        if (p2_choice == 4){
                            player4Flag = true;
                            role4_image.setEffect(colorful);
                        }
                    }
                    if(message.get("p2move")!=null){
                        System.out.println("p2move!");
                        int dir = message.get("p2move");
                        double x = message.get("p2move_x");
                        double y = message.get("p2move_y");
                        Pair<Double,Double> xy = new Pair<>(x,y);
                        Pair<Integer,Pair<Double,Double>> p2move = new Pair<>(dir,xy);
                        p2moves.add(p2move);
                    }
                    if(message.get("p2ptbomb")!=null){
                        p2putbomb = 1;
                        p2BOMB = message.get("p2ptbomb");
                    }
                    if(message.get("brkToy")!=null){
                        System.out.println("brkToy!");
                        int type = message.get("brkToy");
                        int i = message.get("brkToy_i");
                        int j = message.get("brkToy_j");
                        System.out.println(type);
                        System.out.println(i+" "+j);
                        Pair<Integer,Integer> ij = new Pair<>(i,j);
                        Pair<Integer,Pair<Integer,Integer>> brkToy = new Pair<>(type,ij);
                        if(brkToys.size()>0){
                            System.out.println(brkToys.firstElement());
                            brkToys.remove(0);
                        }
                        brkToys.add(brkToy);
                    }
                    if(message.get("lastmsg")!=null){
                        String p2msg = message.get("lastmsg");
                        chatListView.getItems().add(p2msg);
                        tf_p2.setText("");
                        lb_choosinginfo.setText("Waiting for p2 to join the room...");
                        //设置图片滤镜为黑白滤镜
                        ColorAdjust colorAdjust = new ColorAdjust();
                        colorAdjust.setSaturation(-1); // 设置为-1表示转换为黑白色
                        role1_image.setEffect(colorAdjust);
                        role2_image.setEffect(colorAdjust);
                        role3_image.setEffect(colorAdjust);
                        role4_image.setEffect(colorAdjust);
                        btnSELECT_image.setEffect(colorAdjust);
                        btnSTART_image.setEffect(colorAdjust);
                        btn_role1.setDisable(true);
                        btn_role2.setDisable(true);
                        btn_role3.setDisable(true);
                        btn_role4.setDisable(true);
                        btnSELECT.setDisable(true);
                        btnSTART.setDisable(true);
                    }
                });
            });
        } else {
            //开启TCPClient
            client = getNetService().newTCPClient(serverIP_string, 8889);
            client.connectAsync();
            client.setOnConnected(connection -> {
                btn_role1.setDisable(true);
                btn_role2.setDisable(true);
                btn_role3.setDisable(true);
                btn_role4.setDisable(true);
                btn_role1.setOnAction(e -> handleButtonClick1(btn_role1,btnSELECT));
                btn_role2.setOnAction(e -> handleButtonClick2(btn_role2,btnSELECT));
                btn_role3.setOnAction(e -> handleButtonClick3(btn_role3,btnSELECT));
                btn_role4.setOnAction(e -> handleButtonClick4(btn_role4, btnSELECT));
                btnSELECT.setOnAction(e -> {handlebtnSELECT_guest(btn_role1, btn_role2, btn_role3, btn_role4, btnSELECT, btnSTART); btn_Click.play();});
                boolean isConnected = true; //一旦连接成功，就给sever发包：p2的nickname,连接成功的信息
                var bundle = new Bundle("client");
                bundle.put("connecting",isConnected);
                bundle.put("p2name",p2name);
                client.broadcast(bundle);
                //监听来自server的信息
                connection.addMessageHandlerFX((conn, message) -> {
                    lb_choosinginfo.setText("Hi P2! Waiting for p1 to choose the character...");
                    if(message.get("p1name")!=null){
                        p1name = message.get("p1name");
                        tf_p1.setText(p1name);
                    }
                    if(message.get("firstmsg")!=null){
                        String p1first = message.get("firstmsg");
                        chatListView.getItems().add(p1first);
                        String firstMsg = p2name + "[You]进入了房间(ip address: " + serverIP + ")";
                        chatListView.getItems().add(firstMsg);
                    }
                    if(message.get("p1msg")!=null){
                        String p1msg = message.get("p1msg");
                        chatListView.getItems().add(p1msg);
                    }
                    if(message.get("p1role")!=null){
                        lb_choosinginfo.setText("P1 Has Finished! Please select your character:");
                        p1_choice = message.get("p1role");
                        ColorAdjust colorful = new ColorAdjust();
                        colorful.setSaturation(0);
                        if (p1_choice == 1){
                            player1Flag = true;
                            role1_image.setEffect(colorful);
                            btn_role1.setDisable(true);
                            btn_role2.setDisable(false);
                            btn_role3.setDisable(false);
                            btn_role4.setDisable(false);
                        }
                        if (p1_choice == 2){
                            player2Flag = true;
                            role2_image.setEffect(colorful);
                            btn_role2.setDisable(true);
                            btn_role1.setDisable(false);
                            btn_role3.setDisable(false);
                            btn_role4.setDisable(false);
                        }
                        if (p1_choice == 3){
                            player3Flag = true;
                            role3_image.setEffect(colorful);
                            btn_role3.setDisable(true);
                            btn_role2.setDisable(false);
                            btn_role1.setDisable(false);
                            btn_role4.setDisable(false);
                        }
                        if (p1_choice == 4){
                            player4Flag = true;
                            role4_image.setEffect(colorful);
                            btn_role4.setDisable(true);
                            btn_role2.setDisable(false);
                            btn_role3.setDisable(false);
                            btn_role1.setDisable(false);
                        }

                    }
                    if(message.get("start")!=null){
                        System.out.println("START!");
                        try {
                            handleBtn_start();
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(message.get("bear")!=null){
                        int bearColor = message.get("bear");
                        bearColors.add(bearColor);
                    }
                    if(message.get("ranToy")!=null){
                        int type = message.get("ranToy");
                        int i = message.get("ranToy_col");
                        int j = message.get("ranToy_line");
                        Pair<Integer,Integer> ij = new Pair<>(i,j);
                        Pair<Integer,Pair<Integer,Integer>> ranToy = new Pair<>(type,ij);
                        ranToys.add(ranToy);
                    }
                    if(message.get("p1move")!=null){
                        int dir = message.get("p1move");
                        double x = message.get("p1move_x");
                        double y = message.get("p1move_y");
                        Pair<Double,Double> xy = new Pair<>(x,y);
                        Pair<Integer,Pair<Double,Double>> p1move = new Pair<>(dir,xy);
                        p1moves.add(p1move);
                    }
                    if(message.get("p1ptbomb")!=null){
                        p1putbomb = 1;
                        p1BOMB = message.get("p1ptbomb");
                    }
                    if(message.get("brkToy")!=null){
                        int type = message.get("brkToy");
                        int i = message.get("brkToy_i");
                        int j = message.get("brkToy_j");
                        Pair<Integer,Integer> ij = new Pair<>(i,j);
                        Pair<Integer,Pair<Integer,Integer>> brkToy = new Pair<>(type,ij);
                        brkToys.add(brkToy);
                    }
                    if(message.get("bld1")!=null){
                        bld1 = message.get("bld1");
                    }
                    if(message.get("bld2")!=null){
                        bld2 = message.get("bld2");
                    }
                    if(message.get("speed1")!=null){
                        speed1 = message.get("speed1");
                    }
                    if(message.get("speed2")!=null){
                        speed2 = message.get("speed2");
                    }
                    if(message.get("pause")!=null){
                        System.out.println("pause from p1");
                        getController().gotoGameMenu();
                    }
                    if(message.get("resume")!=null){
                        System.out.println("resume from p1");
                        getController().gotoPlay();
                    }
                    if(message.get("mainmenu")!=null){
                        System.out.println("mainmenu from p1");
                        client.disconnect();
                        getController().gotoMainMenu();
                    }
                    if(message.get("lastmsg")!=null){
                        System.out.println("p1 left");
                        showErrorAlert("INFO:","房主离开了房间，房间关闭，即将返回主菜单");
                        getContentRoot().getChildren().clear();
                        try {
                            originMenu();
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            });
        }
        /*****NET******/
        getContentRoot().getChildren().setAll(pane);
    }
    //保存初始主菜单，要回来的
    public void originMenu() throws FileNotFoundException {
        p1name = "";p2name="";
        ImageView imageView;
        ImageView imageView2;
        ImageView imageView3;
        ImageView imageView4;
        ImageView imageView5;
        ImageView imageView6;

        player1Flag = false;
        player2Flag = false;
        player3Flag = false;
        player4Flag = false;

        ThisFlagIsMeanToConfuseYou_HaHaHa_OvO = false;
        ThisFlagIsMeanToConfuseMe_HaHaHa_OnO = false;
        selectBT = false;
        Label label;

        isChoosing = 0;// from 1 to 4 representing character 1 to 4
        isChoosingMap = 0;

        sum = 0;
        sum2 = 0;

        //静音按钮
        ImageView musicON = new ImageView(FXGL.image("main_musicON.png"));
        musicON.setFitWidth(56);
        musicON.setFitHeight(50);
        ImageView musicOFF = new ImageView(FXGL.image("main_musicOFF.png"));
        musicOFF.setFitWidth(56);
        musicOFF.setFitHeight(50);
        Button btn_stopbgmusic = new Button();
        if(!m_flag.get()){
            btn_stopbgmusic.setGraphic(musicON);
        }else{
            btn_stopbgmusic.setGraphic(musicOFF);
        }
        btn_stopbgmusic.setPrefSize(56, 50);
        btn_stopbgmusic.setTranslateX(695);
        btn_stopbgmusic.setTranslateY(176);
        btn_stopbgmusic.setStyle("-fx-background-color: transparent;");
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


        Pane pane = new Pane();
        Image Menu_image = new Image(new FileInputStream("src/main/resources/assets/ui/Menu.png"));
        BackgroundImage Menu_bg = new BackgroundImage(
                Menu_image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT
        );
        Background background = new Background(Menu_bg);
        pane.setPrefSize(getAppWidth(), getAppHeight());
        pane.setBackground(background);
        getContentRoot().getChildren().setAll(pane);

        ImageView local_mode = new ImageView(FXGL.image("local_mode.png"));
        ImageView local_mode_light = new ImageView(FXGL.image("local_mode_light.png"));

        Button btn1 = new Button();
        btn1.setGraphic(local_mode);

        btn1.setPrefSize(200, 50);

        btn1.setTranslateX(450);
        btn1.setTranslateY(230);

        btn1.setStyle("-fx-background-color: transparent;");

        btn1.setOnMouseEntered(e -> {
            stopAndPlaybtn_moveon();
            btn1.setGraphic(local_mode_light);
        });
        btn1.setOnMouseExited(e -> btn1.setGraphic(local_mode));
        btn1.setOnAction(e->{
            stopAndPlaybtn_click();
            getContentRoot().getChildren().clear();
            try {
                consoleMode(); //单机模式UI此处走
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });


        ImageView online_mode = new ImageView(FXGL.image("online_mode.png"));
        ImageView online_mode_light = new ImageView(FXGL.image("online_mode_light.png"));

        Button btn2 = new Button();
        btn2.setGraphic(online_mode);

        btn2.setPrefSize(200, 50);

        btn2.setTranslateX(430);
        btn2.setTranslateY(210);

        btn2.setStyle("-fx-background-color: transparent;");

        btn2.setOnMouseEntered(e -> {
            stopAndPlaybtn_moveon();
            btn2.setGraphic(online_mode_light);
        });
        btn2.setOnMouseExited(e -> btn2.setGraphic(online_mode));
        btn2.setOnAction(event->{
            stopAndPlaybtn_click();
            getContentRoot().getChildren().clear(); //清屏，伪界面跳转
            netMode();
        });

        ImageView help = new ImageView(FXGL.image("help.png"));
        ImageView help_light = new ImageView(FXGL.image("help_light.png"));
        Button btn3 = new Button();
        btn3.setGraphic(help);
        btn3.setPrefSize(200, 50);
        btn3.setTranslateX(582);
        btn3.setTranslateY(190);
        btn3.setStyle("-fx-background-color: transparent;");
        btn3.setOnMouseEntered(e -> {
            stopAndPlaybtn_moveon();
            btn3.setGraphic(help_light);
        });
        btn3.setOnMouseExited(e -> btn3.setGraphic(help));
        btn3.setOnAction(event->{
            stopAndPlaybtn_click();
            getContentRoot().getChildren().clear(); //清屏，伪界面跳转
            try {
                read_help();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        ImageView exit = new ImageView(FXGL.image("exit.png"));
        ImageView exit_light = new ImageView(FXGL.image("exit_light.png"));
        Button btn4 = new Button();
        btn4.setGraphic(exit);
        btn4.setPrefSize(200, 50);
        btn4.setTranslateX(582);
        btn4.setTranslateY(160);
        btn4.setStyle("-fx-background-color: transparent;");
        btn4.setOnMouseEntered(e -> {
            stopAndPlaybtn_moveon();
            btn4.setGraphic(exit_light);});
        btn4.setOnMouseExited(e -> btn4.setGraphic(exit));
        btn4.setOnAction(event->{
            getController().exit();
        });

        VBox box = new VBox(btn1,btn2,btn3,btn4);


        getContentRoot().getChildren().add(box);
        getContentRoot().getChildren().add(btn_stopbgmusic);
    }


}


