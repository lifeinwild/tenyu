package bei7473p5254d69jcuat.tenyu.release1.ui.common;

import java.util.*;

import org.apache.commons.lang.*;
import org.controlsfx.control.textfield.*;

import com.ibm.icu.text.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;

public abstract class NaturalityGui<T1 extends NaturalityDBI,
		T2 extends T1,
		T3 extends Naturality,
		S extends NaturalityStore<T1, T2>,
		G extends NaturalityGui,
		TI extends NaturalityTableItem<T1, T2>>
		extends ObjectivityObjectGui<T1, T2, T3, S, G, TI> {

	public NaturalityGui(String name, String id) {
		super(name, id);
	}

	public Label getNameLabel() {
		return nameLabel;
	}

	private TextField nameInput;
	private Label nameLabel;
	private Label explanationLabel;
	private TextArea explanationInput;

	private void buildNaturalityName() {
		boolean editableBase = CRUDContext.editableBase(ctx);
		//名前
		nameLabel = new Label(getNecessary() + Lang.NATURALITY_NAME.toString());
		nameLabel.setId(idPrefix + "Name");
		nameLabel.setFocusTraversable(true);

		nameInput = new TextField() {
			@Override
			public void replaceText(int start, int end, String text) {
				text = text.trim();
				String normalized = Normalizer2.getNFKCInstance()
						.normalize(text);
				super.replaceText(start, end, normalized);
			}
		};

		nameInput.setEditable(editableBase);
		nameInput.setId(idPrefix + "NameInput");
		nameInput.setTextFormatter(new TextFormatter<String>(change -> {
			String s = change.getControlNewText();
			if (s.length() <= Naturality.nameMin) {
				//この処理を入れないと最小文字数未満であると判定され一切入力できない
				//仮の文字列を設定。この値は判定のためだけに使われる
				s += RandomStringUtils.randomAlphabetic(Naturality.nameMin);
			}
			if (!Naturality.validateName(s, new ValidationResult())) {
				return null;
			}
			return change;
		}));

		grid.add(nameLabel, 0, elapsed);
		grid.add(nameInput, 1, elapsed);
		addElapsed(1);
		//getNameInput().setPromptText(name);
	}

	private void buildNameSearchHandler(SearchFuncs<T1, T2> arg) {
		SearchFuncs<T1, T2> sf = sf(arg);
		TextFields.bindAutoCompletion(nameInput, (t) -> {
			try {
				//名前の前方一致検索
				//大文字小文字の違いを無視出来ない。
				//高機能検索を内蔵しないという方針のため、基盤ソフトウェアでは解決不能
				//しかし外部ツールを使えば良いだけ
				String prefix = t.getUserText();
				if (prefix == null || prefix.length() == 0)
					return null;

				Map<String, Long> r = Glb.getObje().readRet(txn -> getStore(txn)
						.prefixSearchByNameRough(prefix, 20));
				List<T1> objs = new ArrayList<>();
				for (Long id : r.values()) {
					try {
						T1 o = Glb.getObje()
								.readRet(txn -> getStore(txn).get(id));
						objs.add(o);
					} catch (Exception e) {
						Glb.debug(e);
					}
				}
				if (sf.getClearFunc() != null) {
					sf.getClearFunc().run();
				}
				if (objs.size() == 0) {
				} else {
					if (sf.getSingleFunc() != null)
						sf.getSingleFunc().accept((T2) objs.get(0));
					if (sf.getMultiFunc() != null)
						sf.getMultiFunc().accept((List<T2>) objs);
				}
				if (r == null || r.size() == 0) {
					return null;
				}

				return r.keySet();
			} catch (Exception e) {
				return null;
			}
		});

		nameInput.setOnKeyReleased((ev) -> {
			try {
				if (ev.getCharacter() == null
						|| ev.getCharacter().length() == 0)
					return;

				String name = nameInput.getText();
				if (!Naturality.validateName(name, new ValidationResult()))
					return;
				T1 o = Glb.getObje()
						.readRet(txn -> getStore(txn).getByName(name));
				//古い検索結果をクリア
				if (sf.getClearFunc() != null)
					sf.getClearFunc().run();
				if (o == null) {
				} else {
					if (sf.getSingleFunc() != null)
						sf.getSingleFunc().accept((T2) o);
					if (sf.getMultiFunc() != null) {
						List<T1> l = new ArrayList<>();
						l.add(o);
						sf.getMultiFunc().accept((List<T2>) l);
					}
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				sf.getClearFunc().run();
			}
			return;
		});
	}

	public void buildSearchResultNaturality() {
		TableColumn<TI, Long> idHead = new TableColumn<>(Lang.ID.toString());
		idHead.setCellValueFactory(new PropertyValueFactory<TI, Long>("id"));
		TableColumn<TI, String> nameHead = new TableColumn<>(
				Lang.NATURALITY_NAME.toString());
		nameHead.setCellValueFactory(
				new PropertyValueFactory<TI, String>("name"));
		TableColumn<TI, String> explanationHead = new TableColumn<>(
				Lang.NATURALITY_EXPLANATION.toString());
		explanationHead.setCellValueFactory(
				new PropertyValueFactory<TI, String>("explanation"));
		TableColumn<TI, String> registererHead = new TableColumn<>(
				Lang.OBJECTIVITYOBJECT_REGISTERER.toString());
		registererHead.setCellValueFactory(
				new PropertyValueFactory<TI, String>("registerer"));

		searchResults.getColumns().addAll(idHead, nameHead, explanationHead,
				registererHead);
		searchResults.setId(idPrefix + "NaturalityTable");
	}

	/**
	 * 自然性検索GUI部品を構築
	 * WithHeadは頭のラベルや検索結果TableViewを表示するという意味
	 */
	public void buildNaturalitySearch(SearchFuncs<T1, T2> sf) {
		//検索結果
		buildSearchResultNaturality();

		buildNaturalityName();

		buildNameSearchHandler(sf);

		addElapsed(3);

		grid.add(searchResults, 0, elapsed, 2, 4);
		addElapsed(5);
	}

	@Override
	public GridPane buildSearch(SearchFuncs<T1, T2> sf) {
		super.buildSearch(sf);
		buildNaturalitySearch(sf);
		buildNameSearchHandler(sf);
		return grid;
	}

	@Override
	public GridPane buildSearchSimple(SearchFuncs<T1, T2> sf) {
		super.buildSearchSimple(sf);
		buildNaturalityName();
		buildNameSearchHandler(sf);
		return grid;
	}

	@Override
	public GridPane buildCreate() {
		super.buildCreate();
		build();
		return grid;
	}

	@Override
	public GridPane buildUpdate(T3 exist) {
		super.buildUpdate(exist);
		build();
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

	private void build() {
		buildNaturalityName();
		buildExplanation();
	}

	private void buildExplanation() {
		boolean editable = CRUDContext.editable(ctx);
		//説明
		explanationLabel = new Label(
				getNecessary() + Lang.NATURALITY_EXPLANATION.toString());
		explanationLabel.setId(idPrefix + "ExplanationLabel");
		explanationLabel.setFocusTraversable(true);
		grid.add(explanationLabel, 0, elapsed);
		explanationInput = new TextArea() {
			@Override
			public void replaceText(int start, int end, String text) {
				text = text.trim();
				String normalized = Normalizer2.getNFKCInstance()
						.normalize(text);
				super.replaceText(start, end, normalized);
			}
		};
		explanationInput.setTextFormatter(new TextFormatter<String>(change -> {
			if (!Naturality.validateExplanation(change.getControlNewText(),
					new ValidationResult())) {
				return null;
			}
			return change;
		}));
		explanationInput.setEditable(editable);
		explanationInput
				.setPromptText(Lang.NATURALITY_EXPLANATION_PROMPT.toString());
		explanationInput.setId(idPrefix + "ExplanationInput");
		grid.add(explanationInput, 1, elapsed);
		elapsed += 4;

		getExplanationInput()
				.setPromptText(Lang.NATURALITY_EXPLANATION_PROMPT.toString());
	}

	public void clear() {
		super.clear();
		if (nameInput != null) {
			boolean editable = nameInput.isEditable();
			nameInput.setEditable(true);
			nameInput.clear();
			nameInput.setEditable(editable);
		}
		if (explanationInput != null) {
			boolean editable = explanationInput.isEditable();
			explanationInput.setEditable(true);
			explanationInput.clear();
			explanationInput.setEditable(editable);
		}
	}

	public TextArea getExplanationInput() {
		return explanationInput;
	}

	public TextField getNameInput() {
		return nameInput;
	}

	@Override
	public void set(T3 n) {
		if (n == null)
			return;
		super.set(n);

		if (explanationInput != null)
			explanationInput.setText(n.getExplanation());
		if (nameInput != null)
			nameInput.setText(n.getName());
	}

	public void setExplanationInput(TextArea explanationInput) {
		this.explanationInput = explanationInput;
	}

	public void setGui(NaturalityGui<T1, T2, T3, S, G, TI> gui) {
		super.setGui(gui);
		setNameInput(gui.getNameInput());
		setExplanationInput(gui.getExplanationInput());
	}

	public void setNameInput(TextField nameInput) {
		this.nameInput = nameInput;
	}

	public T3 setupModelCreate() {
		super.setupModelCreate();
		modelCache.setName(nameInput.getText());
		modelCache.setExplanation(explanationInput.getText());
		return modelCache;
	}

	public T3 setupModelUpdateOrDelete(T3 o) {
		super.setupModelUpdateOrDelete(o);
		modelCache.setExplanation(explanationInput.getText());
		return modelCache;
	}

}