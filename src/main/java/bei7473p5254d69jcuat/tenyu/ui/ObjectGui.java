package bei7473p5254d69jcuat.tenyu.ui;

import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;

/**
 * モデルのGUIのビルダーみたいなもの
 *
 * ここにある汎用的なGUIの設計思想について。
 *
 * sbf=Submit button funcは送信ボタン押下時の動作を決定する。
 * このアイデアはページ間の連携を可能にする。
 * 即ち元ページがサブページを作成し、同時に元ページがサブページのSBFを指定し、
 * サブページで送信ボタン押下時に作成されたデータを元ページが受け取る等できる。
 *
 * GUI部品をモデルオブジェクト毎に作成し、
 * sbfを外部からGUI部品に与える事で、
 * モデル間の関係性をなぞってGUIページの関係性を作成できる。
 *
 * そのためbuild系メソッドはsbfを必ず受け取る。
 *
 * モデルは他のモデルから参照される場合があり、
 * 各モデルについてそのCRUD系GUIを作ると参照された他のモデルについても
 * 表示または選択または内容を設定する必要が生じる場合がある。
 * １画面で複数のモデルの情報を構築する事になるが、
 * どのような文脈でも構築する情報が同じで必要なGUI部品が同じである一方で、
 * 送信ボタンの動作など機能は文脈毎に異なるという事になる。
 * つまり、独立して作成される場合であれ、他のモデルのメンバー変数として作成される
 * 場合であれ、他のモデルから参照される場合であれ、
 * モデルが同じなら表示や内容設定のGUIは同じで、
 * しかし送信ボタンを押した時の動作が違う。
 *
 * ここでいう送信ボタンはbuildExternalButtonのボタンで、
 * 他にもそのクラス内に閉じたボタンもある。
 * 例えば、リストを編集する場合、順序を入れ替えたり要素を削除するボタンがある。
 * そのような内部的に使用されるボタンはそのモデルにとって一般的だが、
 * 外部GUIに対する送信ボタンは外部GUIが与えるもので、
 * 内部GUI側で動作を決定できない。
 *
 * build系メソッドの引数でsbfを受け取り、nullでなければ
 * そのオブジェクトが構築する一連のGUI部品の最後にそのsbfでボタンを表示する。
 * ObjectGui系が作るGUI部品は情報を表示、設定し、
 * それをどう使うかが外部（画面構築メソッド）から与えられるsbfによって決定する。
 *
 * 長い時間UIの設計について悩んだが、
 * sbfを受け取るという一貫したUI設計は非常に重要な発見だった。
 * 一貫した、組み立て可能なGUI部品を作っていける。
 *
 * クラスXが様々なクラスでメンバー変数として宣言された時、
 * そしてX自体も独立したモデルとしてDBに記録される時、
 * Xの内容設定のGUIは常に同じである一方で、
 * 内容を設定して送信ボタンを押した時の動作は、
 * 最終送信となり通信が発生するのか、1つ上のクラスでList<X>があり
 * そのListの要素として加わりエンドユーザーはさらに他のクラスの
 * 内容設定を続行するのか等異なるが、
 * そのような違いに完全に対応できる設計パターンになっている。
 * "モデルに沿ってGUI部品を作って、画面ごとに組み立てるだけ"。
 *
 * read,update,deleteは既存のオブジェクトがあるはずなので
 * そのオブジェクトをGUIにset()する。
 * GUI部品が一通りbuildされてからじゃないとできないから、
 * 各具象クラスのメソッドで最後に呼び出す必要がある。
 * つまり画面を構築する途中でset()を呼ぶ事はできない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class ObjectGui<V> {
	protected static final int tableWidth = 720;

	protected CRUDContext ctx;
	protected int elapsed;
	/**
	 * 最終送信ボタン
	 *
	 * 多くの場合、画面の一番下にただ1つ送信ボタンがあり、
	 * そのGUI操作をさせている文脈を完了させる動作をする。
	 * 1画面に複数の最終送信ボタンがある事を想定していない。
	 * しかし、ほとんどの場合このフィールドは使用されないので、
	 * もし複数の最終送信ボタンがある場合、buildSubmitButton()で作成する事になるが、
	 * このフィールドを要求する処理が存在しているか配慮する必要がある。
	 */
	protected SubmitButtonGui externalButton;
	protected SubmitButtonFuncs externalButtonInfo;

	/**
	 * TenyuのGUIは基本的に２列多行のGridPaneで構築される。
	 * Gui系クラスはこれを構築する事が主な処理になる。
	 */
	protected GridPane grid;

	protected String idPrefix;
	protected String name;

	/**
	 * gridをコンストラクタで受け取らない仕様は
	 * モデル毎にgridを作成し、gridを連結して最終的なgridを作る事を意図している。
	 * grid毎にborderが設定されるので、意味的な単位が視覚的に分かり易くなる。
	 *
	 * @param name	GUIの名前
	 * @param cssIdPrefix	各GUI部品のCSSのidの接頭辞
	 * @param ctx
	 */
	public ObjectGui(String name, String cssIdPrefix) {
		this.grid = GuiBuilder.grid(cssIdPrefix);
		grid.setBorder(GuiBuilder.getBorder());

		this.name = name;
		Node nameGui = nameGui();
		grid.add(nameGui, 0, 0, 2, 1);
		GridPane.setHalignment(nameGui, HPos.CENTER);
		addElapsed(1);

		this.idPrefix = cssIdPrefix;
	}

	protected String getNecessary() {
		String r = "";
		if (ctx != CRUDContext.SEARCH) {
			r = Lang.NECESSARY.toString();
		}
		return r;
	}

	/**
	 * grid連結
	 * @param guiPart	構築済み部分GUI
	 */
	public void add(ObjectGui<?> guiPart) {
		grid.add(guiPart.getGrid(), 0, elapsed, 2, guiPart.getElapsed());
		addElapsed(guiPart.getElapsed());
	}

	public void addElapsed(int add) {
		this.elapsed += add;
	}

	public GridPane buildCreate() {
		ctx = CRUDContext.CREATE;
		return grid;
	}

	public GridPane buildCreateBatch() {
		ctx = CRUDContext.CREATE_BATCH;
		return grid;
	}

	public GridPane buildDelete(V exist) {
		ctx = CRUDContext.DELETE;
		return grid;
	}

	public GridPane buildDeleteBatch() {
		ctx = CRUDContext.DELETE_BATCH;
		return grid;
	}

	public SubmitButtonGui buildExternalButton(String buttonName,
			String idPrefix, Function<SubmitButtonGui, Boolean> validateFunc,
			Function<SubmitButtonGui, Boolean> sendFunc,
			Consumer<SubmitButtonGui> successFunc,
			Consumer<SubmitButtonGui> failedFunc) {
		return buildExternalButton(new SubmitButtonFuncs(buttonName, idPrefix,
				validateFunc, sendFunc, successFunc, failedFunc));
	}

	public SubmitButtonGui buildExternalButton(
			Function<SubmitButtonGui, Boolean> validateFunc,
			Function<SubmitButtonGui, Boolean> sendFunc,
			Consumer<SubmitButtonGui> successFunc,
			Consumer<SubmitButtonGui> failedFunc) {
		return buildExternalButton(new SubmitButtonFuncs(validateFunc, sendFunc,
				successFunc, failedFunc));
	}

	/**
	 * 通常のbuild系メソッドは外部GUIと連携するためのボタンを作成しないので、
	 * 必要ならbuild後に続けてこのメソッドを呼ぶ。
	 * 一連のGUI部品の下にある想定。
	 */
	public SubmitButtonGui buildExternalButton(
			SubmitButtonFuncs externalButtonInfo) {
		this.externalButtonInfo = externalButtonInfo;
		externalButton = buildSubmitButton(externalButtonInfo);
		return externalButton;
	}

	public GridPane buildRead() {
		ctx = CRUDContext.READ;
		return grid;
	}

	public GridPane buildReadSimple() {
		ctx = CRUDContext.READ_SIMPLE;
		return grid;
	}

	public SubmitButtonGui buildSubmitButton(
			Function<SubmitButtonGui, Boolean> validateFunc,
			Function<SubmitButtonGui, Boolean> sendFunc,
			Consumer<SubmitButtonGui> successFunc,
			Consumer<SubmitButtonGui> failedFunc) {
		return buildSubmitButton(name, idPrefix, validateFunc, sendFunc,
				successFunc, failedFunc);
	}

	public SubmitButtonGui buildSubmitButton(String buttonName, String idPrefix,
			Function<SubmitButtonGui, Boolean> validateFunc,
			Function<SubmitButtonGui, Boolean> sendFunc,
			Consumer<SubmitButtonGui> successFunc,
			Consumer<SubmitButtonGui> failedFunc) {
		SubmitButtonFuncs sbf = new SubmitButtonFuncs(buttonName, idPrefix,
				validateFunc, sendFunc, successFunc, failedFunc);
		return buildSubmitButton(sbf);
	}

	/**
	 * 送信ボタンを作成し、gridに配置し、elapsedを進める。
	 *
	 * @param sbf	送信ボタン押下時の動作
	 * @return	作成された送信ボタン
	 */
	public SubmitButtonGui buildSubmitButton(SubmitButtonFuncs sbf) {
		//sbfで指定された方を優先して使用し、無ければ一連のGUI部品の値を使用する。
		//後者で済むのは1画面中に送信ボタンが1つしかないような場合だけ。
		String idPrefix;
		if (sbf.getIdPrefix() != null) {
			idPrefix = sbf.getIdPrefix();
		} else {
			idPrefix = this.idPrefix;
		}
		String buttonName;
		if (sbf.getButtonName() != null) {
			buttonName = sbf.getButtonName();
		} else {
			buttonName = this.name;
		}

		SubmitButtonGui r = new SubmitButtonGui();
		HBox submitPane = new HBox(10);
		submitPane.setId(idPrefix + "SubmitPane");
		submitPane.setAlignment(Pos.BOTTOM_RIGHT);

		final Text submitMessage = new Text();
		submitMessage.setId(idPrefix + "SubmitMessage");
		submitMessage.setFocusTraversable(true);
		submitPane.getChildren().add(submitMessage);
		r.setSubmitMessage(submitMessage);

		Button submit = new Button(buttonName);
		submit.setId(idPrefix + "SubmitButton");
		submit.setFocusTraversable(true);
		submit.setDefaultButton(true);
		submitPane.getChildren().add(submit);
		r.setSubmitButton(submit);

		grid.add(submitPane, 1, elapsed);
		addElapsed(1);

		submit.setOnAction((ev) -> {
			try {
				//連続送信防止
				if (submit.isDisable())
					return;
				//ボタンが押せなくなる
				submit.setDisable(true);

				r.message("", Color.BLACK);
				if (!sbf.getValidateFunc().apply(r)) {
					submit.setDisable(false);
					return;
				}
				if (sbf.getSendFunc() != null) {
					r.message(Lang.PROCESSING.toString(), Color.BLACK);
					Glb.getGui().executeAsync(() -> {
						boolean send = sbf.getSendFunc().apply(r);
						Glb.getGui().runByFXThread(() -> {
							//ボタンを押せるようにする
							submit.setDisable(false);
							if (send) {
								if (sbf.getSuccessFunc() != null)
									sbf.getSuccessFunc().accept(r);
								r.message(Lang.SUCCESS.toString(), Color.BLACK);
							} else {
								if (sbf.getFailedFunc() != null)
									sbf.getFailedFunc().accept(r);
								r.message(Lang.FAILED.toString());
							}
						});
					});
				} else {
					//いちいちこのように書かないといけない
					//finallyに書くとexecuteAsyncで戻すという事ができない
					submit.setDisable(false);
				}
			} catch (Exception e) {
				try {
					r.message(Lang.EXCEPTION.toString(), Color.FIREBRICK);
					submit.setDisable(false);
				} catch (Exception e2) {
					Glb.debug(e);
				}
				Glb.debug(e);
				return;
			}
		});

		addElapsed(2);

		return r;
	}

	/**
	 * @param exist	既にDBに記録されているオブジェクト。GUIにその状態が表示される
	 * @return
	 */
	public GridPane buildUpdate(V exist) {
		ctx = CRUDContext.UPDATE;
		return grid;
	}

	public GridPane buildUpdateBatch() {
		ctx = CRUDContext.UPDATE_BATCH;
		return grid;
	}

	public CRUDContext getCtx() {
		return ctx;
	}

	public int getElapsed() {
		return elapsed;
	}

	public SubmitButtonGui getExternalButton() {
		return externalButton;
	}

	public SubmitButtonFuncs getExternalButtonInfo() {
		return externalButtonInfo;
	}

	public GridPane getGrid() {
		return grid;
	}

	public String getId() {
		return idPrefix;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return	フォームの名前を表示するGUI部品
	 */
	public Text nameGui() {
		Text nameGui = new Text(name);
		nameGui.setId(idPrefix + "Name");
		nameGui.setFocusTraversable(true);
		nameGui.setFont(Font.font(20));
		return nameGui;
	}

	/**
	 * モデルをGUIに設定する
	 * @param o
	 */
	public void set(V o) {
		//setの前に必ずクリアが必要なので最も抽象的なクラスに書く
		clear();
	}

	abstract public void clear();

	public void setCtx(CRUDContext ctx) {
		this.ctx = ctx;
	}

	public void setId(String id) {
		this.idPrefix = id;
	}

	public void setName(String name) {
		this.name = name;
	}

}
