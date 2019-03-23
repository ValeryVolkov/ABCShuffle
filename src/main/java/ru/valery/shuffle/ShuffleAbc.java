package ru.valery.shuffle;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.Shadow;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.EventQueue;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ShuffleAbc extends Application {
	private static final int NUM_OF_PAIRS = 12;
	private static final int NUM_PER_ROWS = 4;
	private static final int SQUARE_SIZE = 64;
	private static final Image[] IMAGES = new Image[20];

	static {
		initImages();
	}

	private void playMedia(String path) {
		try {
			URI uri = ShuffleAbc.class.getClassLoader().getResource(path).toURI();
			Media hit = new Media(uri.toString());
			MediaPlayer mediaPlayer = new MediaPlayer(hit);
			mediaPlayer.setStopTime(Duration.seconds(0));
			mediaPlayer.setStopTime(Duration.seconds(1.5));
			mediaPlayer.play();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}


	private Tile selected = null;
	private int clickCount = 2;

	public static void main(final String[] args) {
		launch(args);
	}

	private static void initImages() {

		try {
			final URL url = ShuffleAbc.class.getClassLoader().getResource("ru/valery/shuffle/L.jpeg");
			final Image image = new Image(url.toString());
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 5; j++) {
					IMAGES[i + j * 4] = new WritableImage(image.getPixelReader(), i * 75, j * 60, 75, 60);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(final Stage primaryStage) throws UnsupportedEncodingException{
		primaryStage.setScene(new Scene(createContent()));
		primaryStage.setTitle("Найди пару");
		primaryStage.show();

	}

	/**
	 * Создаем интерфейс
	 *
	 * @return Панель
	 */
	private Pane createContent() throws UnsupportedEncodingException{
		final Pane root = new Pane();
		Locale locale = Locale.ENGLISH;
		root.setPrefSize(NUM_PER_ROWS * SQUARE_SIZE, NUM_OF_PAIRS / NUM_PER_ROWS * 2 * SQUARE_SIZE);
		ResourceBundle bundle = ResourceBundle.getBundle("fruits", locale);
		ResourceBundle bundleRu = ResourceBundle.getBundle("fruits");
		List<Tile> tiles = new ArrayList<>();
		for (int i = 0; i < NUM_OF_PAIRS; i++) {
			String fruitName = new String(bundle.getString("fruit." + (i + 1)));
			String fruitNameRu = new String(bundleRu.getString("fruit." + (i + 1)));
			tiles.add(new Tile(String.valueOf(i), fruitName, IMAGES[i]));
			tiles.add(new Tile(String.valueOf(i), fruitNameRu, IMAGES[i]));
		}
		Collections.shuffle(tiles);

		for (int i = 0; i < tiles.size(); i++) {
			Tile tile = tiles.get(i);
			tile.setTranslateX(SQUARE_SIZE * (i % NUM_PER_ROWS));
			tile.setTranslateY(SQUARE_SIZE * (i / NUM_PER_ROWS));
			root.getChildren().add(tile);
		}

		return root;
	}

	/**
	 * Описание тайла и его поведение
	 */
	private class Tile extends StackPane {
		private final Rectangle border = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
		private final Text text = new Text();
		private final ImagePattern image;
		private final FadeTransition ftOpen;
		private final FadeTransition ftClose;
		private final String key;

		Tile(final String key, final String value, final Image image) {
			this.key = key;
			this.image = new ImagePattern(image);
			border.setFill(null);
			border.setStroke(Color.BLACK);

			text.setText(String.format("%9.9s",value));
			text.setFont(Font.font(10));
			text.setEffect(new Shadow(1.5, Color.BLACK));
			setAlignment(Pos.BOTTOM_RIGHT);
			getChildren().addAll(border, text);
			setOnMouseClicked(this::handleMouseClick);

			ftOpen = new FadeTransition(Duration.seconds(0.5), text);
			ftOpen.setToValue(1);

			ftClose = new FadeTransition(Duration.seconds(0.5), text);
			ftClose.setToValue(0);

			close();

		}

		/**
		 * Описание поведения при нажатии указателем мыши на тайл
		 *
		 * @param mouseEvent (В данной реализации не используем)
		 */
		private void handleMouseClick(final MouseEvent mouseEvent) {

			if (isOpen() || clickCount == 0) {
				return;
			}
			clickCount--;
			border.setFill(image);
			if (selected == null) {
				selected = this;
				open(() -> {
				});
			} else {
				open(() -> {
					if (!hasSaveValue(selected)) {
						selected.border.setFill(null);
						border.setFill(null);
						selected.close();
						this.close();
					} else {
						EventQueue.invokeLater(()-> {
							playMedia("ru/valery/shuffle/S.mp3");
						});
					}
					selected = null;
					clickCount = 2;
				});
			}
		}

		private boolean isOpen() {
			return text.getOpacity() == 1;
		}

		private boolean hasSaveValue(final Tile other) {
			return key.equals(other.getKey());
		}

		private void open(final Runnable action) {
			ftOpen.setOnFinished(e -> action.run());
			ftOpen.play();
		}

		private void close() {
			ftClose.play();
		}
		public String getKey(){
			return key;
		}
	}

}
