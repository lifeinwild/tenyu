package bei7473p5254d69jcuat.tenyu.ui.common;

import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import glb.*;
import glb.util.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import jetbrains.exodus.env.*;

/**
 * 作成処理か更新処理かなど場合によってidInputがnullか異なる。
 * validateやclear処理はGUI部品が設置されているかに応じて動作を分ける。
 *
 * ・GUIの包括的設計
 * 納得できる設計に到達できていない。{@link ModelGui}など
 * ＜モデル＞Gui系クラスは、GUIの状態からそのモデルを作ったり、モデルを受け取ってGUIに設定したり、
 * 送信ボタン押下時の複数のラムダ式の間で情報を共有するために使われる。
 * 良くbuiltという変数が使われるが、1つの画面を通して構築する対象という意味。
 * GUI部品やその設定値、画面内の処理における中間情報など。
 * 中間情報はメソッド名にtmpが入っている
 *
 * T1	I
 * T2	Iの最新実装
 * T3	GUIで主に扱うクラス。ほぼT2と一致
 * S	T1,T2に対応するストア
 * T1-3など冗長な感じが非常にあるが、修正方法が無い。
 * 総称型は伝染する。T2とT3は互いに代替にならないから。
 * DBに少しでも関わるとT1,T2,Sの宣言が必要。
 * 本来ならT3一つで済ましたいが、できなかった。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class ModelGui<T1 extends ModelI,
		T2 extends T1,
		T3 extends Model,
		S extends ModelStore<T1, T2>,
		G extends ModelGui,
		TI extends ModelTableItem<T1, T2>> extends ObjectGui<T3> {

	protected static final String detail = "Detail";

	public static String getDetail() {
		return detail;
	}

	public static TextFormatter<Long> getIdFormatter() {
		return new TextFormatter<Long>(change -> {
			if (change.getControlNewText().equals("-"))
				return change;
			if (change.getControlNewText().length() == 0)
				return change;
			try {
				Long id = Long.valueOf(change.getControlNewText());
				if (!Model.validateIdStandard(id))
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

	private TextField idInput;

	private Label idLabel;

	/**
	 * 検索結果から１つ選択した時に詳細表示するGUI
	 */
	protected G detailGui;

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

	public ModelGui(String name, String id) {
		super(name, id);
	}

	private void build() {
		boolean editable = CRUDContext.editableBase(ctx);
		//ID
		idLabel = new Label(Lang.ID.toString());
		idLabel.setId(idPrefix + "Id");
		grid.add(idLabel, 0, elapsed);
		idInput = new TextField();
		idInput.setEditable(editable);
		idInput.setId(idPrefix + "IdInput");
		idInput.setTextFormatter(getIdFormatter());
		grid.add(idInput, 1, elapsed);
		elapsed += 1;

		//時間系
		createDateLabel = new Label(Lang.CREATE_DATE.toString());
		createDateLabel.setId(idPrefix + "CreateDateLabel");
		grid.add(createDateLabel, 0, elapsed);
		createDateInput = new TextField();
		createDateInput.setId(idPrefix + "CreateDateInput");
		createDateInput.setEditable(editable);
		grid.add(createDateInput, 1, elapsed);
		elapsed += 1;

		createHistoryIndexLabel = new Label(
				Lang.CREATE_HISTORY_INDEX.toString());
		createHistoryIndexLabel.setId(idPrefix + "CreateHistoryIndexLabel");
		grid.add(createHistoryIndexLabel, 0, elapsed);
		createHistoryIndexInput = new TextField();
		createHistoryIndexInput.setId(idPrefix + "CreateHistoryIndexInput");
		createHistoryIndexInput.setEditable(editable);
		grid.add(createHistoryIndexInput, 1, elapsed);
		elapsed += 1;

		updateDateLabel = new Label(Lang.UPDATE_DATE.toString());
		updateDateLabel.setId(idPrefix + "UpdateDateLabel");
		grid.add(updateDateLabel, 0, elapsed);
		updateDateInput = new TextField();
		updateDateInput.setId(idPrefix + "UpdateDateInput");
		updateDateInput.setEditable(editable);
		grid.add(updateDateInput, 1, elapsed);
		elapsed += 1;

		updateHistoryIndexLabel = new Label(
				Lang.UPDATE_HISTORY_INDEX.toString());
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
	public GridPane buildDelete(T3 exist) {
		super.buildDelete(exist);
		return grid;
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

	public GridPane buildSearch(SearchFuncs<T1, T2> arg) {
		ctx = CRUDContext.SEARCH;
		detailGui = createDetailGui();
		detailGui.buildRead();
		searchResults = new TableView<>();
		searchResults.setId(idPrefix + "SearchResults");
		searchResults.setFocusTraversable(true);

		//1つ選択した時に詳細表示するハンドラ
		searchResults.getSelectionModel().selectedItemProperty()
				.addListener((obs, oldSelection, newSelection) -> {
					SearchFuncs<T1, T2> sf = sf(arg);
					if (newSelection == null)
						return;
					if (sf.getSingleFunc() != null)
						sf.getSingleFunc().accept(newSelection.getSrc());
				});

		build();
		getIdInput().setOnKeyReleased((ev) -> {
			SearchFuncs<T1, T2> sf = sf(arg);
			try {
				Long id = Long.valueOf(getIdInput().getText());
				T2 o = Glb.getObje().readRet(txn2 -> getStore(txn2).get(id));
				if (o == null) {
					sf.getClearFunc().run();
				} else {
					sf.getSingleFunc().accept((T2) o);
					List<T2> l = new ArrayList<>();
					l.add(o);
					sf.getMultiFunc().accept((List<T2>) l);
				}
			} catch (NumberFormatException e1) {
				sf.getClearFunc().run();
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				sf.getClearFunc().run();
			}
		});

		return grid;
	}

	/**
	 * 検索結果に{@link Model}関連の列を加える
	 * 各子クラス毎にこのようなメソッドが定義される
	 * 検索結果テーブルは画面によって表示すべき属性が異なるので
	 * このメソッドを呼ぶかは画面や具象クラスに委ねられ、
	 * どこかから勝手に呼ばれるということはない。
	 */
	public void buildSearchResultModel() {
		TableColumn<TI, Long> idHead = new TableColumn<>(Lang.ID.toString());
		idHead.setCellValueFactory(new PropertyValueFactory<TI, Long>("id"));

		searchResults.getColumns().add(idHead);

		TableColumn<TI,
				Long> updateDateHead = new TableColumn<>(Lang.ID.toString());
		updateDateHead.setCellValueFactory(
				new PropertyValueFactory<TI, Long>("updateDate"));

		searchResults.getColumns().add(updateDateHead);

	}

	/**
	 * 簡易検索は他のモデルで参照を設定するために使われる。
	 *
	 * @param sbf	引数に検索結果をどう処理するかのラムダ式。
	 * sbfに想定している処理は呼び出し元で他のGUI部品と連携させる処理。
	 * @param sf
	 * @return
	 */
	public GridPane buildSearchSimple(SearchFuncs<T1, T2> sf) {
		ctx = CRUDContext.SEARCH_SIMPLE;
		searchResults = getSearchResults();
		//単純検索の場合ID検索は無い

		return grid;
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

	/**
	 * @param exist	既にDBに記録されているオブジェクト。GUIにその状態が表示される
	 * @return
	 */
	@Override
	public GridPane buildUpdate(T3 exist) {
		super.buildUpdate(exist);
		build();
		return grid;
	}

	@Override
	public GridPane buildUpdateBatch() {
		super.buildUpdateBatch();
		return grid;
	}

	public synchronized void clear() {
		if (idInput != null) {
			boolean editable = idInput.isEditable();
			idInput.setEditable(true);
			idInput.clear();
			idInput.setEditable(editable);
		}
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

	protected abstract G createDetailGui();

	protected abstract TI createTableItem(T2 o);

	/**
	 * @return	このモデルのLangに定義された名前
	 */
	abstract public Lang getClassNameLang();

	public TextField getCreateDateInput() {
		return createDateInput;
	}

	public Label getCreateDateLabel() {
		return createDateLabel;
	}

	public TextField getCreateHistoryIndexInput() {
		return createHistoryIndexInput;
	}

	public Label getCreateHistoryIndexLabel() {
		return createHistoryIndexLabel;
	}

	public SearchFuncs<T1, T2> getDefaultSearchFuncs() {
		return new SearchFuncs<>(
				() -> Glb.getGui()
						.runByFXThread(() -> searchResults.getItems().clear()),
				r -> {
					if (detailGui != null) {
						Glb.getGui().runByFXThread(() -> detailGui.set((T3) r));
					}
				}, r -> {
					Glb.getGui().runByFXThread(() -> {
						if (searchResults == null)
							return;
						if (r == null || r.size() == 0) {
							searchResults.getItems().clear();
							return;
						}
						Glb.getGui().<T2, TI> updateTable(r,
								src -> createTableItem(src), searchResults);
					});
				});
	}

	public G getDetailGui() {
		return detailGui;
	}

	public TextField getIdInput() {
		synchronized (idInput) {
			return idInput;
		}
	}

	public T3 getModelCache() {
		return modelCache;
	}

	public TableView<TI> getSearchResults() {
		return searchResults;
	}

	/**
	 * モデルにも{@link Model#getStore(Transaction)}があるが、
	 * こちらにも用意した。
	 *
	 * @param txn
	 * @return
	 */
	public abstract S getStore(Transaction txn);

	public TextField getUpdateDateInput() {
		return updateDateInput;
	}

	public Label getUpdateDateLabel() {
		return updateDateLabel;
	}

	public TextField getUpdateHistoryIndexInput() {
		return updateHistoryIndexInput;
	}

	public Label getUpdateHistoryIndexLabel() {
		return updateHistoryIndexLabel;
	}

	@Override
	public void set(T3 o) {
		super.set(o);
		if (idInput != null && o.getId() != null)
			getIdInput().setText("" + o.getId());
		if (createDateInput != null && !o.isDefaultCreateDate())
			getCreateDateInput().setText("" + o.getCreateDate());
	}

	public void setCreateDateInput(TextField createDateInput) {
		this.createDateInput = createDateInput;
	}

	public void setCreateDateLabel(Label createDateLabel) {
		this.createDateLabel = createDateLabel;
	}

	public void setCreateHistoryIndexInput(TextField createHistoryIndexInput) {
		this.createHistoryIndexInput = createHistoryIndexInput;
	}

	public void setCreateHistoryIndexLabel(Label createHistoryIndexLabel) {
		this.createHistoryIndexLabel = createHistoryIndexLabel;
	}

	public void setGui(ModelGui<T1, T2, T3, S, G, TI> gui) {
		setIdInput(gui.getIdInput());
		setCreateDateInput(gui.getCreateDateInput());
		setUpdateDateInput(gui.getUpdateDateInput());
	}

	public void setIdInput(TextField idInput) {
		this.idInput = idInput;
	}

	public void setModelCache(T3 modelCache) {
		this.modelCache = modelCache;
	}

	public void setUpdateDateInput(TextField updateDateInput) {
		this.updateDateInput = updateDateInput;
	}

	public void setUpdateDateLabel(Label updateDateLabel) {
		this.updateDateLabel = updateDateLabel;
	}

	public void setUpdateHistoryIndexInput(TextField updateHistoryIndexInput) {
		this.updateHistoryIndexInput = updateHistoryIndexInput;
	}

	public void setUpdateHistoryIndexLabel(Label updateHistoryIndexLabel) {
		this.updateHistoryIndexLabel = updateHistoryIndexLabel;
	}

	public T3 setupModelCreate() {
		return modelCache;
	}

	public T3 setupModelUpdateOrDelete(T3 o) {
		modelCache = o;
		return modelCache;
	}

	protected SearchFuncs<T1, T2> sf(SearchFuncs<T1, T2> arg) {
		SearchFuncs<T1, T2> sf;
		if (arg == null) {
			return getDefaultSearchFuncs();
		} else {
			return arg;
		}
	}

	/**
	 * 検証に成功した場合、モデルをキャッシュする
	 * @param gui
	 * @param o
	 * @param dbvalidate
	 * @return
	 */
	private boolean validate(SubmitButtonGui gui, T3 o,
			Function<ValidationResult, ValidationResult> validate,
			BiFunction<ValidationResult,
					Transaction,
					ValidationResult> dbvalidate) {
		ValidationResult r = new ValidationResult();
		Glb.getObje().read(txn -> dbvalidate.apply(r, txn));
		if (r.isNoError()) {
			setModelCache(o);
			return true;
		} else {
			gui.message(r);
			setModelCache(null);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public boolean validateAtCreate(SubmitButtonGui gui) {
		T3 o = setupModelCreate();
		return validate(gui, o, r -> o.validateAtCreate2(r), Glb
				.tryW2((vr, txn) -> getStore(txn).noExist2((T1) o, vr), null));
	}

	@SuppressWarnings("unchecked")
	public boolean validateAtDelete(SubmitButtonGui gui, T3 exist) {
		T3 o = setupModelUpdateOrDelete(exist);
		return validate(gui, o, r -> o.validateAtDelete2(r), Glb.tryW2(
				(vr, txn) -> getStore(txn).exist2((T1) exist, vr), null));
	}

	@SuppressWarnings("unchecked")
	public boolean validateAtUpdate(SubmitButtonGui gui, T3 exist) {
		T3 updated = setupModelUpdateOrDelete(exist);
		return validate(gui, updated, r -> updated.validateAtUpdate2(r), (vr,
				txn) -> getStore(txn).validateAtUpdateChange2((T1) exist, vr));
	}

}