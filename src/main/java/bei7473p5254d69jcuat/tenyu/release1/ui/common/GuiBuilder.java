package bei7473p5254d69jcuat.tenyu.release1.ui.common;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;

/**
 * GUI部品を構築する。
 * 基本的にステートレスであるべき。
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class GuiBuilder {
	/**
	 * GUI部品を構築する
	 * @return	構築されたGUI部品
	 */
	public abstract Node build();

	/**
	 * @return	構築するGUI部品の名前。人向け
	 */
	public abstract String name();

	/**
	 * @return	Javaの変数名と同じ命名規則を持つ名前。CSS対応のため。プログラム向け
	 */
	public abstract String id();

	@Override
	public String toString() {
		return name();
	}

	/**
	 * フォーム的なものはグリッドレイアウトが妥当そうで、
	 * 本アプリのGUIはおおむねフォームである。
	 * ラベルを１つ置くので挿入位置が+1される。
	 * @return
	 */
	public GridPane grid() {
		return grid(id());
	}

	public static GridPane grid(String idPrefix) {
		GridPane grid = new GridPane();
		grid.setId(idPrefix + "Grid");
		grid.setFocusTraversable(true);
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		return grid;
	}

	public static Border getBorder() {
		return new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
				CornerRadii.EMPTY, BorderWidths.DEFAULT));
	}

	/**
	 * フォーム名を表示する
	 * @return
	 */
	public GridPane grid2() {
		GridPane grid = grid();
		grid.setBorder(getBorder());

		Node nameGui = nameGui();
		grid.add(nameGui, 0, 0, 2, 1);
		GridPane.setHalignment(nameGui, HPos.CENTER);

		/*
		Grid g = new Grid();
		g.setGrid(grid);
		g.addElapsed(1);
		*/

		return grid;
	}

	/**
	 * フォームの名前をGUI部品として表示する
	 * @return	名前を表示するGUI部品
	 */
	public Text nameGui() {
		Text nameGui = new Text(name());
		nameGui.setId(id() + "Name");
		nameGui.setFocusTraversable(true);
		nameGui.setFont(Font.font(20));
		return nameGui;
	}

}
