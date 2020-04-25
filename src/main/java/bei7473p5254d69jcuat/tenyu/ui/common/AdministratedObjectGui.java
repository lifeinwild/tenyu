package bei7473p5254d69jcuat.tenyu.ui.common;

import java.util.*;

import org.controlsfx.control.textfield.*;
import org.controlsfx.control.textfield.AutoCompletionBinding.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;

public abstract class AdministratedObjectGui<T1 extends AdministratedObjectI,
		T2 extends T1,
		T3 extends AdministratedObject,
		S extends AdministratedObjectStore<T1, T2>,
		G extends AdministratedObjectGui,
		TI extends AdministratedObjectTableItem<T1, T2>>
		extends IdObjectGui<T1, T2, T3, S, G, TI> {

	public AdministratedObjectGui(String name, String id) {
		super(name, id);
	}

	private Label registererLabel;
	private TextField registererNameInput;
	private Label mainAdministratorNameLabel;
	private TextField mainAdministratorNameInput;

	public void buildSearchResultAdministratedObject() {
		TableColumn<TI, String> registererHead = new TableColumn<>(
				Lang.ADMINISTRATEDOBJECT_REGISTERER.toString());
		registererHead.setCellValueFactory(
				new PropertyValueFactory<TI, String>("registerer"));

		searchResults.getColumns().add(registererHead);
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
	public GridPane buildSearch(SearchFuncs<T1, T2> sf) {
		super.buildSearch(sf);
		build();
		setSearchHandler(sf);
		return grid;
	}

	@Override
	public GridPane buildSearchSimple(SearchFuncs<T1, T2> sf) {
		super.buildSearchSimple(sf);
		build();
		setSearchHandler(sf);
		return grid;
	}

	private Collection<String> autoCompletionUserName(ISuggestionRequest t) {
		//ユーザー名補完
		try {
			String prefix = t.getUserText();
			if (prefix == null || prefix.length() == 0)
				return null;
			Map<String, Long> r = Glb.getObje()
					.getUser(us -> us.prefixSearchByNameRough(prefix, 20));
			if (r == null || r.size() == 0) {
				return null;
			}
			return r.keySet();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * AdministratedObjectに関するイベントハンドラを登録
	 * @param getStoreFunc
	 * @param clearFunc
	 * @param built
	 * @param singleFunc
	 * @param multiFunc
	 */
	public void setSearchHandler(SearchFuncs<T1, T2> arg) {
		SearchFuncs<T1, T2> sf = sf(arg);
		if (getMainAdministratorNameInput() != null) {
			TextFields.bindAutoCompletion(getMainAdministratorNameInput(),
					t -> autoCompletionUserName(t));
			getMainAdministratorNameInput().setOnKeyReleased((ev) -> {
				try {
					if (ev.getCharacter() == null
							|| ev.getCharacter().length() == 0)
						return;
					Glb.debug(getMainAdministratorNameInput().getText());
					Long userId = Glb.getObje().getUser(us -> us.getIdByName(
							getMainAdministratorNameInput().getText()));
					if (userId == null)
						return;
					List<Long> ids = Glb.getObje().readRet(
							txn -> getStore(txn).getIdsByAdministrator(userId));
					if (ids == null || ids.size() == 0) {
						sf.getClearFunc().run();
					}

					List<T1> objs = new ArrayList<>();
					for (Long id : ids) {
						T1 o = Glb.getObje()
								.readRet(txn -> getStore(txn).get(id));
						if (o != null)
							objs.add(o);
					}

					if (objs.size() == 0) {
						sf.getClearFunc().run();
					} else {
						sf.getSingleFunc().accept((T2) objs.get(0));
						sf.getMultiFunc().accept((List<T2>) objs);
					}
				} catch (NumberFormatException e1) {
					sf.getClearFunc().run();
				} catch (Exception e) {
					Glb.getLogger().error("", e);
					sf.getClearFunc().run();
				}
			});
		}

		if (getRegistererNameInput() != null) {
			TextFields.bindAutoCompletion(getRegistererNameInput(),
					t -> autoCompletionUserName(t));

			getRegistererNameInput().setOnKeyReleased((ev) -> {
				try {
					if (ev.getCharacter() == null
							|| ev.getCharacter().length() == 0)
						return;
					Glb.debug(getRegistererNameInput().getText());
					Long registererUserId = Glb.getObje().getUser(us -> us
							.getIdByName(getRegistererNameInput().getText()));
					if (registererUserId == null)
						return;
					List<Long> ids = Glb.getObje().readRet(txn -> getStore(txn)
							.getIdsByRegisterer(registererUserId));
					if (ids == null || ids.size() == 0) {
						sf.getClearFunc().run();
					}

					List<T1> objs = new ArrayList<>();
					for (Long id : ids) {
						T1 o = Glb.getObje()
								.readRet(txn -> getStore(txn).get(id));
						if (o != null)
							objs.add(o);
					}

					if (objs.size() == 0) {
						sf.getClearFunc().run();
					} else {
						sf.getSingleFunc().accept((T2) objs.get(0));
						sf.getMultiFunc().accept((List<T2>) objs);
					}
				} catch (NumberFormatException e1) {
					sf.getClearFunc().run();
				} catch (Exception e) {
					Glb.getLogger().error("", e);
					sf.getClearFunc().run();
				}
			});
		}
	}

	private void build() {
		boolean editable = CRUDContext.editableBase(ctx);
		registererLabel = new Label(
				Lang.ADMINISTRATEDOBJECT_REGISTERER_NAME.toString());
		registererLabel.setId(idPrefix + "RegistererName");
		grid.add(registererLabel, 0, elapsed);
		registererNameInput = new TextField();
		registererNameInput.setEditable(editable);
		registererNameInput.setId(idPrefix + "RegistererNameInput");
		grid.add(registererNameInput, 1, elapsed);
		elapsed += 1;

		mainAdministratorNameLabel = new Label(
				Lang.ADMINISTRATEDOBJECT_ADMINISTRATOR_NAME.toString());
		mainAdministratorNameLabel.setId(idPrefix + "AdministratorName");
		grid.add(mainAdministratorNameLabel, 0, elapsed);
		mainAdministratorNameInput = new TextField();
		mainAdministratorNameInput.setEditable(editable);
		mainAdministratorNameInput.setId(idPrefix + "AdministratorNameInput");
		grid.add(mainAdministratorNameInput, 1, elapsed);
		elapsed += 1;
	}

	public void clear() {
		super.clear();
		if (registererNameInput != null) {
			boolean editable = registererNameInput.isEditable();
			registererNameInput.setEditable(true);
			registererNameInput.clear();
			registererNameInput.setEditable(editable);
		}
	}

	public void setRegisterer(Long userId) {
		User registerer = Glb.getObje().getUser(us -> us.get(userId));
		String s;
		if (registerer == null) {
			s = "";
		} else {
			s = registerer.getName();
		}
		registererNameInput.setText(s);
	}

	public TextField getRegistererNameInput() {
		return registererNameInput;
	}

	@Override
	public void set(T3 n) {
		super.set(n);
		if (registererNameInput != null) {
			User u = Glb.getObje()
					.getUser(us -> us.get(n.getRegistererUserId()));
			if (u != null)
				registererNameInput.setText(u.getName());
		}
		if (mainAdministratorNameInput != null) {
			User u = Glb.getObje()
					.getUser(us -> us.get(n.getMainAdministratorUserId()));
			if (u != null)
				mainAdministratorNameInput.setText(u.getName());
		}

	}

	public void setGui(AdministratedObjectGui<T1, T2, T3, S, G, TI> gui) {
		super.setGui(gui);
		setRegistererNameInput(gui.getRegistererNameInput());
	}

	public void setRegistererNameInput(TextField registererInput) {
		this.registererNameInput = registererInput;
	}

	public T3 setupModelCreate() {
		super.setupModelCreate();

		//もし具象クラスによって登録者や管理者の決定方法が異なる場合、具象クラスのsetup系メソッドで再設定する
		Long myUserId = Glb.getMiddle().getMyUserId();
		modelCache.setRegistererUserId(myUserId);
		modelCache.setMainAdministratorUserId(myUserId);

		return modelCache;
	}

	public T3 setupModelUpdateOrDelete(T3 o) {
		super.setupModelUpdateOrDelete(o);
		Long mainAdmin = null;
		try {
			mainAdmin = Glb.getObje().getUser(
					us -> us.getIdByName(mainAdministratorNameInput.getText()));
		} catch (Exception e) {
		}
		modelCache.setMainAdministratorUserId(mainAdmin);
		return modelCache;
	}

	public TextField getMainAdministratorNameInput() {
		return mainAdministratorNameInput;
	}

	public void setMainAdministratorNameInput(
			TextField mainAdministratorNameInput) {
		this.mainAdministratorNameInput = mainAdministratorNameInput;
	}

}