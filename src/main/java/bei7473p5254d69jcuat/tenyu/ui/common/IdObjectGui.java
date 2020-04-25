package bei7473p5254d69jcuat.tenyu.ui.common;

import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import glb.*;
import glb.util.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import jetbrains.exodus.env.*;

/**
 * 作成処理か更新処理かなど場合によってidInputがnullか異なる。
 * validateやclear処理はGUI部品が設置されているかに応じて動作を分ける。
 *
 * ・GUIの包括的設計
 * 納得できる設計に到達できていない。IdObjectGuiなど
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
public abstract class IdObjectGui<T1 extends IdObjectI,
		T2 extends T1,
		T3 extends IdObject,
		S extends IdObjectStore<T1, T2>,
		G extends IdObjectGui,
		TI extends IdObjectTableItem<T1, T2>>
		extends ModelGui<T1, T2, T3, S, G, TI> {

	/**
	 * 検索結果にIdObject関連の列を加える
	 * IdObjectを継承した各クラス毎にこのようなメソッドが定義される
	 * 検索結果テーブルは画面によって表示すべき属性が異なるので
	 * このメソッドを呼ぶかは画面や具象クラスに委ねられ、
	 * どこかから勝手に呼ばれるということはない。
	 */
	public void buildSearchResultIdObject() {
		TableColumn<TI, Long> idHead = new TableColumn<>(Lang.ID.toString());
		idHead.setCellValueFactory(new PropertyValueFactory<TI, Long>("id"));

		searchResults.getColumns().add(idHead);
	}

	private TextField idInput;

	private Label idLabel;

	public IdObjectGui(String name, String id) {
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
		idInput.setTextFormatter(ModelGui.getIdFormatter());
		grid.add(idInput, 1, elapsed);
		elapsed += 1;
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

	/**
	 * 検索結果から１つ選択した時に詳細表示するGUI
	 */
	protected G detailGui;

	protected SearchFuncs<T1, T2> sf(SearchFuncs<T1, T2> arg) {
		SearchFuncs<T1, T2> sf;
		if (arg == null) {
			return getDefaultSearchFuncs();
		} else {
			return arg;
		}
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
		super.clear();
		if (idInput != null) {
			boolean editable = idInput.isEditable();
			idInput.setEditable(true);
			idInput.clear();
			idInput.setEditable(editable);
		}
	}

	protected abstract G createDetailGui();

	public G getDetailGui() {
		return detailGui;
	}

	public TextField getIdInput() {
		synchronized (idInput) {
			return idInput;
		}
	}

	@Override
	public void set(T3 o) {
		super.set(o);
		if (idInput != null && o.getId() != null)
			getIdInput().setText("" + o.getId());
	}

	public void setGui(IdObjectGui<T1, T2, T3, S, G, TI> gui) {
		super.setGui(gui);
		setIdInput(gui.getIdInput());
	}

	public void setIdInput(TextField idInput) {
		this.idInput = idInput;
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

	public boolean validateAtCreate(SubmitButtonGui gui) {
		T3 o = setupModelCreate();
		return validate(gui, o, r -> o.validateAtCreate2(r), Glb
				.tryW2((vr, txn) -> getStore(txn).noExist2((T1) o, vr), null));
	}

	public boolean validateAtDelete(SubmitButtonGui gui, T3 exist) {
		T3 o = setupModelUpdateOrDelete(exist);
		return validate(gui, o, r -> o.validateAtDelete2(r), Glb.tryW2(
				(vr, txn) -> getStore(txn).exist2((T1) exist, vr), null));
	}

	public boolean validateAtUpdate(SubmitButtonGui gui, T3 exist) {
		T3 updated = setupModelUpdateOrDelete(exist);
		return validate(gui, updated, r -> updated.validateAtUpdate2(r),
				(vr, txn) -> getStore(txn).validateAtUpdate2((T1) exist, vr));
	}

}