package bei7473p5254d69jcuat.tenyu.ui.standarduser.user;

import java.time.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.neighbor.*;
import glb.*;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import jetbrains.exodus.env.*;

public class UserGui extends
		IndividualityObjectGui<UserI, User, User, UserStore, UserGui, UserTableItem> {
	public UserGui(String name, String id) {
		super(name, id);
	}

	@Override
	protected UserTableItem createTableItem(User o) {
		return new UserTableItem(o);
	}

	@Override
	protected UserGui createDetailGui() {
		return new UserGui(Lang.DETAIL.toString(), idPrefix + detail);
	}

	@Override
	public UserStore getStore(Transaction txn) {
		try {
			return new UserStore(txn);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	private P2PEdge inviter;
	private User me;
	private TableView<P2PEdgeTableItem> introducerTable;

	//FQDN
	private Label fqdnLabel;

	private TextField fqdnInput;

	private Label introducerLabel;

	//紹介者
	private Label inviterLabel;
	private TextField inviterInput;

	//ポート
	private Label portLabel;

	private TextField p2pPortInput;
	private TextField gamePortInput;
	//セキュアユーザー
	private Label secureLabel;

	private TextField secureInput;
	//タイムゾーン
	private Label timezoneLabel;

	private TextField timezoneInput;

	private SubmitButtonGui registerButton;

	@Override
	public GridPane buildCreate() {
		super.buildCreate();

		//紹介者一覧を作成する。０ならダイアログを出す
		ObservableList<
				P2PEdgeTableItem> obs = FXCollections.observableArrayList();
		for (P2PEdge n : Glb.getSubje().getNeighborList().getNeighborsCopy()) {
			byte[] pubKey = n.getNode().getPubKey().getByteArray();
			if (Glb.getObje().getUser(us -> us.getIdByAny(pubKey)) != null)
				obs.add(new P2PEdgeTableItem(n));
		}
		if (obs.size() == 0) {
			Glb.getGui().alert(AlertType.ERROR, name,
					Lang.USER_NO_NEIGHBOR.toString());
		}
		//紹介者
		introducerLabel = new Label(
				Lang.NECESSARY.toString() + Lang.USER_INTRODUCER.toString()
						+ "   " + Lang.CHOICE_ONE.toString());
		introducerLabel.setId(idPrefix + "Introducer");
		introducerLabel.setFocusTraversable(true);
		grid.add(introducerLabel, 0, elapsed);

		//近傍のうちユーザー登録済みのノード一覧
		//近傍とユーザーの積集合なのでUserGuiで構築するとかではない
		setIntroducerTable(new TableView<>());
		getIntroducerTable().setId(idPrefix + "IntroducerInput");
		getIntroducerTable().setEditable(false);

		TableColumn<P2PEdgeTableItem,
				Long> userIdHead = new TableColumn<>(Lang.USER_ID.toString());
		userIdHead.setCellValueFactory(
				new PropertyValueFactory<P2PEdgeTableItem, Long>("userId"));

		TableColumn<P2PEdgeTableItem, String> nameHead = new TableColumn<>(
				Lang.INDIVIDUALITY_OBJECT_NAME.toString());
		nameHead.setCellValueFactory(
				new PropertyValueFactory<P2PEdgeTableItem, String>("name"));

		TableColumn<P2PEdgeTableItem,
				String> addrHead = new TableColumn<>(Lang.IPADDR.toString());
		addrHead.setCellValueFactory(
				new PropertyValueFactory<P2PEdgeTableItem, String>("addr"));

		TableColumn<P2PEdgeTableItem,
				String> createDateHead = new TableColumn<>(
						Lang.CREATE_DATE.toString());
		createDateHead.setCellValueFactory(
				new PropertyValueFactory<P2PEdgeTableItem, String>(
						"createDate"));

		getIntroducerTable().getColumns().addAll(nameHead, addrHead,
				createDateHead);
		final VBox introducerBox = new VBox();
		introducerBox.setId(idPrefix + "IntroducerBox");
		introducerBox.setSpacing(3);
		introducerBox.setPadding(new Insets(10, 0, 0, 10));
		introducerBox.getChildren().addAll(introducerLabel,
				getIntroducerTable());

		grid.add(introducerBox, 1, elapsed);
		addElapsed(1);

		getIntroducerTable().getItems().clear();
		getIntroducerTable().setItems(obs);

		Glb.getGui().polling(grid, () -> Glb.getGui().<P2PEdge,
				P2PEdgeTableItem> createPollingTaskTableView(() -> {
					List<P2PEdge> neighbors = Glb.getSubje().getNeighborList()
							.getNeighborsCopy();
					List<P2PEdge> users = new ArrayList<P2PEdge>();
					for (P2PEdge n : neighbors) {
						if (Glb.getObje()
								.getUser(us -> us.getIdByAny(n.getNode()
										.getPubKey().getByteArray())) != null
								&& n.getCommonKeyExchangeState().isSucceed())
							users.add(n);
					}
					return users;
				}, (model) -> new P2PEdgeTableItem(model),
						getIntroducerTable()));

		return grid;
	}

	@Override
	public GridPane buildRead() {
		super.buildRead();
		buildIndividualityObjectPrompt();
		buildFQDNPortSecureTimeZone();
		buildReadPolling(this);
		return grid;
	}

	@Override
	public GridPane buildUpdate(User exist) {
		super.buildUpdate(exist);
		buildIndividualityObjectPrompt();
		buildFQDNPortSecureTimeZone();
		buildSetTimeZoneCurrent();

		set(exist);

		buildSubmitButton(gui -> validateAtUpdate(gui, exist), gui -> true,
				null, null);

		return grid;
	}

	@Override
	public GridPane buildSearch(SearchFuncs<UserI, User> sf) {
		//総件数
		buildCount(DBUtil.countStatic(UserStore.getMainStoreInfoStatic()));

		super.buildSearch(sf);

		add(detailGui);

		return grid;
	}

	@Override
	public GridPane buildSearchSimple(SearchFuncs<UserI, User> sf) {
		super.buildSearchSimple(sf);
		buildIndividualityObjectPrompt();
		return grid;
	}

	private void buildReadPolling(UserGui gui) {
		Glb.getGui().polling(grid, () -> {
			return new Task<Void>() {
				public Void call() {
					try {
						String idStr = getIdInput().getText();
						if (idStr == null || idStr.isEmpty())
							return null;
						Long userId = Long.valueOf(idStr);
						User u = Glb.getObje().getUser(us -> us.get(userId));
						gui.set(u);
					} catch (NumberFormatException e1) {
					} catch (Exception e) {
						Glb.debug(e);
					}
					return null;
				}
			};
		});
	}

	private void buildFQDNPortSecureTimeZone() {
		//FQDN
		setFqdnLabel(new Label(Lang.FQDN.toString()));
		getFqdnLabel().setId(idPrefix + "Fqdn");
		grid.add(getFqdnLabel(), 0, elapsed);
		setFqdnInput(new TextField());
		if (ctx == CRUDContext.READ || ctx == CRUDContext.DELETE) {
			getFqdnInput().setEditable(false);
		} else {
			getFqdnInput().setEditable(true);
		}
		getFqdnInput().setId(idPrefix + "FqdnInput");
		grid.add(getFqdnInput(), 1, elapsed);
		elapsed += 1;

		//ポート
		setPortLabel(new Label(Lang.PORT.toString()));
		getPortLabel().setId(idPrefix + "Port");
		grid.add(getPortLabel(), 0, elapsed);
		setPortInput(new TextField());
		if (ctx == CRUDContext.READ || ctx == CRUDContext.DELETE) {
			getPortInput().setEditable(false);
		} else {
			getPortInput().setEditable(true);
		}
		getPortInput().setId(idPrefix + "PortInput");
		grid.add(getPortInput(), 1, elapsed);
		elapsed += 1;

		//セキュアユーザー
		setSecureLabel(new Label(Lang.USER_SECURE.toString()));
		getSecureLabel().setId(idPrefix + "Secure");
		grid.add(getSecureLabel(), 0, elapsed);
		setSecureInput(new TextField());
		getSecureInput().setEditable(false);
		getSecureInput().setId(idPrefix + "SecureInput");
		grid.add(getSecureInput(), 1, elapsed);
		elapsed += 1;

		//タイムゾーン
		setTimezoneLabel(new Label(Lang.TIMEZONE.toString()));
		getTimezoneLabel().setId(idPrefix + "TimeZone");
		grid.add(getTimezoneLabel(), 0, elapsed);
		setTimezoneInput(new TextField());
		getTimezoneInput().setEditable(false);
		getTimezoneInput().setId(idPrefix + "TimeZoneInput");
		grid.add(getTimezoneInput(), 1, elapsed);
		elapsed += 1;

	}

	private void buildSetTimeZoneCurrent() {
		buildSubmitButton(Lang.USER_TIMEZONE_SET_CURRENT.toString(),
				"SetTimeZoneCurrent", gui -> {
					getTimezoneInput().setText(ZoneId.systemDefault().getId());
					return true;
				}, null, null, null);
	}

	private GridPane buildDetail() {
		return grid;
	}

	private void buildIndividualityObjectPrompt() {
		//説明のプロンプト
		getExplanationInput()
				.setPromptText(Lang.INDIVIDUALITY_OBJECT_EXPLANATION.toString()
						+ " Twitter, Blog, etc.");
	}

	public User setupModelCreate() {
		modelCache = new User();
		super.setupModelCreate();
		P2PEdgeTableItem e = introducerTable.getSelectionModel()
				.getSelectedItem();
		if (e != null && e.getUserId() != null) {
			modelCache.setRegistererUserId(e.getUserId());
		}

		Conf cf = Glb.getConf();
		modelCache.setPcPublicKey(cf.getKeys().getMyPcPublicKey().getEncoded());
		modelCache.setMobilePublicKey(cf.getKeys().getMyMobilePublicKey().getEncoded());
		modelCache.setOfflinePublicKey(cf.getKeys().getMyOfflinePublicKey().getEncoded());

		return modelCache;
	}

	public User setupModelUpdateOrDelete(User exist) {
		super.setupModelUpdateOrDelete(exist);

		AddrInfo addr = new AddrInfo();
		addr.setFqdn(fqdnLabel.getText());

		try {
			int p2pport = Integer.valueOf(p2pPortInput.getText());
			addr.setP2pPort(p2pport);
			int gamePort = Integer.valueOf(gamePortInput.getText());
			addr.setGamePort(gamePort);
		} catch (Exception e) {
			Glb.debug(e);
		}
		//TODO GUIがノード番号別のアドレス情報設定に対応する必要がある
		exist.addNodeNumberToAddr(0, addr);

		exist.setTimezone(ZoneId.of(timezoneInput.getText()));
		return exist;
	}

	public void clear() {
		super.clear();
		if (fqdnInput != null)
			fqdnInput.clear();
		if (p2pPortInput != null)
			p2pPortInput.clear();
		if (gamePortInput != null)
			gamePortInput.clear();
		if (inviterInput != null)
			inviterInput.clear();
		if (secureInput != null)
			secureInput.clear();
		if (timezoneInput != null)
			timezoneInput.clear();

		if (introducerTable != null)
			introducerTable.getSelectionModel().clearSelection();
	}

	/**
	 * 検索結果をGUI部品に設定
	 * @param u
	 */
	@Override
	public void set(User u) {
		if (u == null) {
			clear();
			return;
		}
		super.set(u);

		//TODO ノード番号別アドレス情報に対応する。現在0番のみの想定で作られている
		AddrInfo addr = u.getAddr(0);
		if (addr != null) {
			if (fqdnInput != null)
				fqdnInput.setText(addr.getFqdn());
			if (p2pPortInput != null)
				p2pPortInput.setText(addr.getP2pPort() + "");
		}
		if (inviterInput != null)
			inviterInput.setText(u.getInviter() + "");
		if (secureInput != null)
			secureInput.setText(u.isSecure() + "");
		if (timezoneInput != null)
			timezoneInput.setText(u.getTimezone().getId());
	}

	@Override
	public Lang getClassNameLang() {
		return Lang.USER;
	}

	public P2PEdge getInviter() {
		return inviter;
	}

	public User getTmpMe() {
		return me;
	}

	public TableView<P2PEdgeTableItem> getIntroducerTable() {
		return introducerTable;
	}

	public void setInviter(P2PEdge inviter) {
		this.inviter = inviter;
	}

	public void setTmpMe(User me) {
		this.me = me;
	}

	public void setIntroducerTable(
			TableView<P2PEdgeTableItem> introducerTable) {
		this.introducerTable = introducerTable;
	}

	public Label getFqdnLabel() {
		return fqdnLabel;
	}

	public void setFqdnLabel(Label fqdnLabel) {
		this.fqdnLabel = fqdnLabel;
	}

	public TextField getFqdnInput() {
		return fqdnInput;
	}

	public void setFqdnInput(TextField fqdnInput) {
		this.fqdnInput = fqdnInput;
	}

	public Label getInviterLabel() {
		return inviterLabel;
	}

	public void setInviterLabel(Label inviterLabel) {
		this.inviterLabel = inviterLabel;
	}

	public TextField getInviterInput() {
		return inviterInput;
	}

	public void setInviterInput(TextField inviterInput) {
		this.inviterInput = inviterInput;
	}

	public Label getPortLabel() {
		return portLabel;
	}

	public void setPortLabel(Label portLabel) {
		this.portLabel = portLabel;
	}

	public TextField getPortInput() {
		return p2pPortInput;
	}

	public void setPortInput(TextField portInput) {
		this.p2pPortInput = portInput;
	}

	public Label getSecureLabel() {
		return secureLabel;
	}

	public void setSecureLabel(Label secureLabel) {
		this.secureLabel = secureLabel;
	}

	public TextField getSecureInput() {
		return secureInput;
	}

	public void setSecureInput(TextField secureInput) {
		this.secureInput = secureInput;
	}

	public Label getTimezoneLabel() {
		return timezoneLabel;
	}

	public void setTimezoneLabel(Label timezoneLabel) {
		this.timezoneLabel = timezoneLabel;
	}

	public TextField getTimezoneInput() {
		return timezoneInput;
	}

	public void setTimezoneInput(TextField timezoneInput) {
		this.timezoneInput = timezoneInput;
	}

	public void set(UserRegistrationIntroduceOfferTableItem newSelection) {
		getNameInput().setText(newSelection.getName());
		getExplanationInput().setText(newSelection.getExplanation().get());
	}

}