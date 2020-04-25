package bei7473p5254d69jcuat.tenyu.ui;

import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import glb.util.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import jetbrains.exodus.env.*;

public abstract class ModelGui<T1 extends IdObjectI,
		T2 extends T1,
		T3 extends IdObject,
		S extends IdObjectStore<T1, T2>,
		G extends IdObjectGui,
		TI extends IdObjectTableItem<T1, T2>> extends ObjectGui<T3> {

	protected static final String detail = "Detail";

	public static TextFormatter<Long> getIdFormatter() {
		return new TextFormatter<Long>(change -> {
			if (change.getControlNewText().equals("-"))
				return change;
			if (change.getControlNewText().length() == 0)
				return change;
			try {
				Long id = Long.valueOf(change.getControlNewText());
				if (!IdObject.validateIdStandard(id))
					return null;
			} catch (Exception e) {
				return null;
			}
			return change;
		});
	}

	public static TextFormatter<String> getTextFormatterValidation(
			Function<Change, ValidationResult> validate) {
		return new TextFormatter<String>(change -> {
			if (change.getControlNewText().length() == 0)
				return change;
			try {
				ValidationResult vr = validate.apply(change);
				if (!vr.isNoError())
					return null;
			} catch (Exception e) {
				return null;
			}
			return change;
		});
	}

	private Label createDateLabel;
	private TextField createDateInput;
	private Label createHistoryIndexLabel;
	private TextField createHistoryIndexInput;
	private Label updateDateLabel;
	private TextField updateDateInput;

	private Label updateHistoryIndexLabel;

	private TextField updateHistoryIndexInput;

	/**
	 * 検索結果
	 */
	protected TableView<TI> searchResults;

	public ModelGui(String name, String cssIdPrefix) {
		super(name, cssIdPrefix);
	}

	private void build() {
		boolean editable = false;
		createDateLabel = new Label(Lang.MODEL_CREATE_DATE.toString());
		createDateLabel.setId(idPrefix + "CreateDateLabel");
		grid.add(createDateLabel, 0, elapsed);
		createDateInput = new TextField();
		createDateInput.setId(idPrefix + "CreateDateInput");
		createDateInput.setEditable(editable);
		grid.add(createDateInput, 1, elapsed);
		elapsed += 1;

		createHistoryIndexLabel = new Label(
				Lang.MODEL_CREATE_HISTORY_INDEX.toString());
		createHistoryIndexLabel.setId(idPrefix + "CreateHistoryIndexLabel");
		grid.add(createHistoryIndexLabel, 0, elapsed);
		createHistoryIndexInput = new TextField();
		createHistoryIndexInput.setId(idPrefix + "CreateHistoryIndexInput");
		createHistoryIndexInput.setEditable(editable);
		grid.add(createHistoryIndexInput, 1, elapsed);
		elapsed += 1;

		updateDateLabel = new Label(Lang.MODEL_UPDATE_DATE.toString());
		updateDateLabel.setId(idPrefix + "UpdateDateLabel");
		grid.add(updateDateLabel, 0, elapsed);
		updateDateInput = new TextField();
		updateDateInput.setId(idPrefix + "UpdateDateInput");
		updateDateInput.setEditable(editable);
		grid.add(updateDateInput, 1, elapsed);
		elapsed += 1;

		updateHistoryIndexLabel = new Label(
				Lang.MODEL_UPDATE_HISTORY_INDEX.toString());
		updateHistoryIndexLabel.setId(idPrefix + "UpdateHistoryIndexLabel");
		grid.add(updateHistoryIndexLabel, 0, elapsed);
		updateHistoryIndexInput = new TextField();
		updateHistoryIndexInput.setId(idPrefix + "UpdateHistoryIndexInput");
		updateHistoryIndexInput.setEditable(editable);
		grid.add(updateHistoryIndexInput, 1, elapsed);
		elapsed += 1;
	}

	/**
	 * 総件数を表示するGUI部品を構築
	 *
	 * @param grid
	 * @param elapsed
	 * @param guiId
	 * @param count
	 * @return
	 */
	public void buildCount(long count) {
		Label countLabelGui = new Label(Lang.COUNT.toString());
		countLabelGui.setId(idPrefix + "CountLabel");
		grid.add(countLabelGui, 0, elapsed);
		Label countGui = new Label("" + count);
		countGui.setId(idPrefix + "Count");
		grid.add(countGui, 1, elapsed);
		addElapsed(1);
	}

	@Override
	public GridPane buildRead() {
		super.buildRead();
		build();
		return grid;
	}

	@Override
	public GridPane buildReadSimple() {
		super.buildReadSimple();
		build();
		return grid;
	}

	/**
	 * このクラスのインスタンスが複数のラムダ式の中で使われた場合、
	 * 返値を輸送する手段が必要なので、内部にキャッシュする。
	 *
	 * setup系メソッドでセットアップされる。
	 * ～Gui系クラスはGUI部品を通じてモデルをセットアップすると捉えれる。
	 * そのセットアップされたモデルが設定される。
	 * ただしセットアップされるGUIの状態は作成や更新など文脈によって異なるので
	 * setupもvalidateも文脈毎にメソッドが用意される必要がある。
	 */
	protected T3 modelCache;

	/**
	 * 検索結果に{@link Model}関連の列を加える
	 * 各子クラス毎にこのようなメソッドが定義される
	 * 検索結果テーブルは画面によって表示すべき属性が異なるので
	 * このメソッドを呼ぶかは画面や具象クラスに委ねられ、
	 * どこかから勝手に呼ばれるということはない。
	 */
	public void buildSearchResultModel() {
		TableColumn<TI,
				Long> updateDateHead = new TableColumn<>(Lang.ID.toString());
		updateDateHead.setCellValueFactory(
				new PropertyValueFactory<TI, Long>("updateDate"));

		searchResults.getColumns().add(updateDateHead);
	}

	protected abstract TI createTableItem(T2 o);

	public TableView<TI> getSearchResults() {
		return searchResults;
	}

	/**
	 * 1行かつ横幅最大のテキストを表示。
	 *
	 * @param grid
	 * @param elapsed
	 * @param subTitle
	 * @param guiId
	 * @return
	 */
	public void buildSubTitle(String subTitle) {
		Label label = new Label(subTitle);

		HBox box = new HBox();
		box.setAlignment(Pos.CENTER);
		box.getChildren().add(label);

		label.setId(idPrefix + "SubTitle");
		label.setFocusTraversable(false);

		grid.add(box, 0, elapsed, 2, 1);
		elapsed += 1;
	}

	@Override
	public GridPane buildUpdate(T3 exist) {
		super.buildUpdate(exist);
		build();
		return grid;
	}

	@Override
	public synchronized void clear() {
		if (createDateInput != null) {
			boolean editable = createDateInput.isEditable();
			createDateInput.setEditable(true);
			createDateInput.clear();
			createDateInput.setEditable(editable);
		}
		if (updateDateInput != null) {
			boolean editable = updateDateInput.isEditable();
			updateDateInput.setEditable(true);
			updateDateInput.clear();
			updateDateInput.setEditable(editable);
		}
	}

	public T3 getModelCache() {
		return modelCache;
	}

	public void setGui(ModelGui<T1, T2, T3, S, G, TI> gui) {
		setCreateDateInput(gui.getCreateDateInput());
		setUpdateDateInput(gui.getUpdateDateInput());
	}

	public T3 setupModelCreate() {
		return modelCache;
	}

	public void setModelCache(T3 modelCache) {
		this.modelCache = modelCache;
	}

	public T3 setupModelUpdateOrDelete(T3 o) {
		modelCache = o;
		return modelCache;
	}

	@Override
	public void set(T3 o) {
		super.set(o);
		if (createDateInput != null && !o.isDefaultCreateDate())
			getCreateDateInput().setText("" + o.getCreateDate());
	}

	/**
	 * @return	このモデルのLangに定義された名前
	 */
	abstract public Lang getClassNameLang();

	/**
	 * モデルにも{@link Model#getStore(Transaction)}があるが、
	 * こちらにも用意した。
	 *
	 * @param txn
	 * @return
	 */
	public abstract S getStore(Transaction txn);

	public static String getDetail() {
		return detail;
	}

	public Label getCreateDateLabel() {
		return createDateLabel;
	}

	public TextField getCreateDateInput() {
		return createDateInput;
	}

	public Label getCreateHistoryIndexLabel() {
		return createHistoryIndexLabel;
	}

	public TextField getCreateHistoryIndexInput() {
		return createHistoryIndexInput;
	}

	public Label getUpdateDateLabel() {
		return updateDateLabel;
	}

	public TextField getUpdateDateInput() {
		return updateDateInput;
	}

	public Label getUpdateHistoryIndexLabel() {
		return updateHistoryIndexLabel;
	}

	public TextField getUpdateHistoryIndexInput() {
		return updateHistoryIndexInput;
	}

	public void setCreateDateLabel(Label createDateLabel) {
		this.createDateLabel = createDateLabel;
	}

	public void setCreateDateInput(TextField createDateInput) {
		this.createDateInput = createDateInput;
	}

	public void setCreateHistoryIndexLabel(Label createHistoryIndexLabel) {
		this.createHistoryIndexLabel = createHistoryIndexLabel;
	}

	public void setCreateHistoryIndexInput(TextField createHistoryIndexInput) {
		this.createHistoryIndexInput = createHistoryIndexInput;
	}

	public void setUpdateDateLabel(Label updateDateLabel) {
		this.updateDateLabel = updateDateLabel;
	}

	public void setUpdateDateInput(TextField updateDateInput) {
		this.updateDateInput = updateDateInput;
	}

	public void setUpdateHistoryIndexLabel(Label updateHistoryIndexLabel) {
		this.updateHistoryIndexLabel = updateHistoryIndexLabel;
	}

	public void setUpdateHistoryIndexInput(TextField updateHistoryIndexInput) {
		this.updateHistoryIndexInput = updateHistoryIndexInput;
	}

}
