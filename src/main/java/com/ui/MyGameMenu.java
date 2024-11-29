package com.ui;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.net.Client;
import com.almasb.fxgl.net.Server;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyGameMenu extends FXGLMenu {
    private MyMainMenu myMainMenu;
    public void setMyMainMenu(MyMainMenu myMainMenu) {
        this.myMainMenu = myMainMenu;
    }
    private Image menuImage;
    private Server<Bundle> server;
    private Client<Bundle> client;
    private boolean isMainMenu;
    Media btnMusic = new Media(getClass().getResource("/assets/music/click.wav").toExternalForm());
    MediaPlayer btnMusicPlayer = new MediaPlayer(btnMusic);
    private MediaPlayer bgMusic;
    public void setBgMusic(MediaPlayer bgMusic) {
        this.bgMusic = bgMusic;
    }
    private boolean isResume;
    private static MediaPlayer mainMusic;
    public static void setMainMusic(MediaPlayer mainMusic) {
        MyGameMenu.mainMusic = mainMusic;
    }

    public void setServer(Server<Bundle> server) {
        this.server = server;
    }
    public void setClient(Client<Bundle> client) {
        this.client = client;
    }
    private void stopAndPlayMediaButton() {
        if (btnMusicPlayer != null && btnMusicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            btnMusicPlayer.stop();
        }
        btnMusicPlayer.seek(btnMusicPlayer.getStartTime());
        btnMusicPlayer.play();
    }
    @Override
    public void onCreate() {
        isResume = true;
        System.out.println("create");
        if(bgMusic != null){
            bgMusic.pause();
        }

        if(server!=null){
            var bundle = new Bundle("pause");
            bundle.put("pause",1);
            server.broadcast(bundle);
            //还未考虑一方断开连接的情况
            if(!server.getConnections().isEmpty()){
                server.getConnections().get(0).addMessageHandlerFX((conn,message)->{
                });
            }
        }else if(client!=null){
        }
    }

    @Override
    public void onDestroy() {
        if(bgMusic != null){
            if(isResume){
                bgMusic.play();
            }else{
                bgMusic.stop();
            }
        }
        if(server!=null && !isMainMenu){
            var bundle = new Bundle("resume");
            bundle.put("resume",1);
            server.broadcast(bundle);
        }
        isMainMenu = false;
    }
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public MyGameMenu() throws FileNotFoundException {

        super(MenuType.GAME_MENU);
        Pane pane = new Pane();
        menuImage = new Image(new FileInputStream("src/main/resources/assets/ui/MyGameMenu.png"));
        ImageView imageView = new ImageView(menuImage);

        imageView.setFitWidth(getAppWidth());
        imageView.setFitHeight(getAppHeight());
        imageView.setOpacity(0.87);

        pane.getChildren().add(imageView);
        pane.setPrefSize(getAppWidth(), getAppHeight());
        getContentRoot().getChildren().setAll(pane);

        Button btn_mainmenu = new Button(" ");
        btn_mainmenu.setOpacity(0.3);
        btn_mainmenu.setPrefSize(271, 74);
        btn_mainmenu.setTranslateX(493);
        btn_mainmenu.setTranslateY(481);
        if(client!=null){
            System.out.println("ooo");
            btn_mainmenu.setDisable(true);
        }

        btn_mainmenu.setOnAction(e->{
            isResume = false;
            stopAndPlayMediaButton();
            if(server!=null){
                isMainMenu = true;
                var bundle = new Bundle("mainmenu");
                bundle.put("mainmenu",1);
                server.broadcast(bundle);
                server.setOnDisconnected(bundleConnection -> {
                    System.out.println("disconnected!");
                });
                server.stop();
                getController().gotoMainMenu();
            }if(client!=null){
                Text tip1 = new Text("ONLY HOST CAN DO THIS");
                tip1.setLayoutX(400);
                tip1.setLayoutY(500);
                tip1.setFill(Color.DARKRED);
                tip1.setFont(Font.font("null", FontWeight.BOLD, 14));
                getContentRoot().getChildren().add(tip1);
            }else {
                getController().gotoMainMenu();
                if(!myMainMenu.getM_flag().get())
                    myMainMenu.getBgMusic().play();
            }
        });
        Button btn_resume = new Button(" ");
        btn_resume.setOpacity(0.3);
        btn_resume.setPrefSize(140, 74);

        btn_resume.setTranslateX(489);
        btn_resume.setTranslateY(380);
        btn_resume.setOnAction(e->{
            stopAndPlayMediaButton();
            if(client == null){
                getController().gotoPlay();
            }else{
                Text tip = new Text("ONLY HOST CAN DO THIS");
                tip.setLayoutX(400);
                tip.setLayoutY(400);
                tip.setFill(Color.DARKRED);
                tip.setFont(Font.font("null", FontWeight.BOLD, 14));
                getContentRoot().getChildren().add(tip);
            }
        });

        getContentRoot().getChildren().add(btn_mainmenu);
        getContentRoot().getChildren().add(btn_resume);
    }
}
