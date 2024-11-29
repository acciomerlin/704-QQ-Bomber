package com.ui;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.geometry.Insets;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.scene.SubScene;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;

public class Player1win extends SubScene {
    private MediaPlayer buttonHoverSound1;
    private MediaPlayer buttonHoverSound2;
    Media mediabutton = new Media(getClass().getResource("/assets/music/click.wav").toExternalForm());
    MediaPlayer mediaButton = new MediaPlayer(mediabutton);
    Media media1 = new Media(getClass().getResource("/assets/music/winwin.mp3").toExternalForm());
    MediaPlayer mediaPlayer1 = new MediaPlayer(media1);
    private void playBackgroundMusic() {
        String musicPath = "/assets/music/game-on.mp3"; // 替换为你的音乐文件路径
        Media media1 = new Media(getClass().getResource(musicPath).toExternalForm());
        mediaPlayer1.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer1.play();
    }

    private void playMusicp1() {
        buttonHoverSound1 = new MediaPlayer(new Media(getClass().getResource("/assets/music/winner.mp3").toExternalForm()));
        buttonHoverSound2 = new MediaPlayer(new Media(getClass().getResource("/assets/music/loser.mp3").toExternalForm()));

    }
    public Player1win() {
        MediaPlayer media=MyMainMenu.getBgMusic();
        if(media!=null){
            media.stop();;
        }
        playBackgroundMusic();
        playMusicp1();
        // 创建按钮和贴图
        ImageView restartImageNormal = new ImageView(new Image("/assets/ui/restartimg2.png"));
        ImageView restartImageHover = new ImageView(new Image("/assets/ui/restartimg1.png"));
        Button restartButton = new Button("", restartImageNormal);
        restartButton.setLayoutX(60);
        restartButton.setLayoutY(300);
        restartImageNormal.setFitWidth(400);
        restartImageNormal.setFitHeight(100);
        restartImageHover.setFitWidth(400);
        restartImageHover.setFitHeight(100);
        restartButton.setStyle("-fx-background-color: transparent;");

        restartButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            restartButton.setGraphic(restartImageHover);
        });
        restartButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            restartButton.setGraphic(restartImageNormal);
        });

        restartButton.setOnAction(event -> {
            FXGL.getSceneService().popSubScene();
            FXGL.getGameController().gotoMainMenu();
            mediaPlayer1.stop();
            MyMainMenu.getBgMusic();
            media.play();
            mediaButton.play();
        });


        ImageView exitImageNormal = new ImageView(new Image("/assets/ui/exitimg2.png"));
        ImageView exitImageHover = new ImageView(new Image("/assets/ui/exitimg1.png"));
        Button exitButton = new Button("", exitImageNormal);
        exitButton.setLayoutX(515);
        exitButton.setLayoutY(300);
        exitImageNormal.setFitWidth(200);
        exitImageNormal.setFitHeight(100);
        exitImageHover.setFitWidth(200);
        exitImageHover.setFitHeight(100);
        exitButton.setStyle("-fx-background-color: transparent;");

        exitButton.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            exitButton.setGraphic(exitImageHover);
        });
        exitButton.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            exitButton.setGraphic(exitImageNormal);
        });
        exitButton.setOnAction(event -> {
            FXGL.getGameController().exit();
            mediaButton.play();
        });

        ImageView button2ImageNormal2 = null;
        if(MyMainMenu.p2_choice==1){
            button2ImageNormal2 = new ImageView(new Image("/assets/ui/p1blank.PNG"));
        }else if(MyMainMenu.p2_choice==2){
            button2ImageNormal2 = new ImageView(new Image("/assets/ui/p2blank.PNG"));
        }else if(MyMainMenu.p2_choice==3){
            button2ImageNormal2 = new ImageView(new Image("/assets/ui/p3blank.PNG"));
        }else if(MyMainMenu.p2_choice==4){
            button2ImageNormal2= new ImageView(new Image("/assets/ui/p4blank.PNG"));
        }
        ColorAdjust blackAndWhiteFilter = new ColorAdjust();
        blackAndWhiteFilter.setSaturation(-1.0);

        button2ImageNormal2.setEffect(blackAndWhiteFilter);
        Button button2 = new Button("", button2ImageNormal2);
        button2ImageNormal2.setFitWidth(120);
        button2ImageNormal2.setFitHeight(180);
        button2.setStyle("-fx-background-color: transparent;");

        button2.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            buttonHoverSound2.seek(buttonHoverSound2.getStartTime());
            buttonHoverSound2.play();
            button2.setScaleX(1.2);
            button2.setScaleY(1.2);
        });
        button2.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            buttonHoverSound2.stop();
            button2.setScaleX(1.0);
            button2.setScaleY(1.0);
        });

        button2.setOnAction(event -> {

        });

        ImageView button1ImageNormal = null;
        if(MyMainMenu.p1_choice==1){
            button1ImageNormal = new ImageView(new Image("/assets/ui/p1blankwin.PNG"));
        }else if(MyMainMenu.p1_choice==2){
            button1ImageNormal = new ImageView(new Image("/assets/ui/p2blankwin.PNG"));
        }else if(MyMainMenu.p1_choice==3){
            button1ImageNormal = new ImageView(new Image("/assets/ui/p3blankwin.PNG"));
        }else if(MyMainMenu.p1_choice==4){
            button1ImageNormal = new ImageView(new Image("/assets/ui/p4blankwin.PNG"));
        }

        Button button1 = new Button("", button1ImageNormal);
        button1ImageNormal.setFitWidth(120);
        button1ImageNormal.setFitHeight(180);
        button1.setStyle("-fx-background-color: transparent;");

        button1.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            buttonHoverSound1.seek(buttonHoverSound1.getStartTime());
            buttonHoverSound1.play();
            button1.setScaleX(1.2);
            button1.setScaleY(1.2);
        });

        button1.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            buttonHoverSound1.stop();
            button1.setScaleX(1.0);
            button1.setScaleY(1.0);
        });

        button1.setOnAction(event -> {

        });

        HBox buttonBox = new HBox(button1, button2);
        buttonBox.setAlignment(Pos.BOTTOM_LEFT);
        buttonBox.setSpacing(10);
        BorderPane centerPane = new BorderPane();
        centerPane.setBottom(buttonBox);
        centerPane.setMargin(buttonBox, new Insets(0, 0, 10, 10));
        centerPane.setMaxSize(800, 600);

        centerPane.setBottom(buttonBox);
        BorderPane.setAlignment(buttonBox, Pos.BOTTOM_CENTER);

        Image backgroundImage = new Image("/assets/ui/p1win.png");
        BackgroundImage backgroundImg = new BackgroundImage(backgroundImage, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        centerPane.setBackground(new Background(backgroundImg));

        StackPane pane = new StackPane(centerPane);
        pane.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        pane.setStyle("-fx-background-color: #0007;-fx-background-color: skyblue;");
        getContentRoot().getChildren().add(pane);
        BorderPane.setAlignment(restartButton, Pos.BOTTOM_RIGHT);
        BorderPane.setAlignment(exitButton, Pos.BOTTOM_RIGHT);




        getContentRoot().getChildren().add(restartButton);
        getContentRoot().getChildren().add(exitButton);
    }
}
