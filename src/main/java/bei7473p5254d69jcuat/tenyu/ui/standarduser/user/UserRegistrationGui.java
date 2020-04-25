package bei7473p5254d69jcuat.tenyu.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.right.user.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import glb.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;

public class UserRegistrationGui extends ObjectGui<UserRegistration> {

	private VBox introducerBox;
	private TableView<UserRegistrationIntroduceOfferTableItem> offerTable;
	private TableView.TableViewSelectionModel<
			UserRegistrationIntroduceOfferTableItem> offerTableSelect;

	public UserRegistrationGui(String name, String id) {
		super(name, id);
	}

	private UserGui detailUser;

	public UserGui getDetailUser() {
		return detailUser;
	}

	@Override
	public GridPane buildCreate() {
		super.buildCreate();

		//紹介依頼一覧
		offerTable = new TableView<>();
		offerTable.setId(idPrefix + "IntroduceOfferTable");
		offerTable.setEditable(false);
		offerTableSelect = offerTable.getSelectionModel();

		introducerBox = new VBox();
		introducerBox.setId(idPrefix + "IntroduceOfferBox");
		introducerBox.setSpacing(3);
		introducerBox.setPadding(new Insets(10, 0, 0, 0));
		introducerBox.getChildren().addAll(offerTable);

		grid.add(introducerBox, 1, elapsed);

		elapsed += 1;

		//自己紹介
		detailUser = new UserGui(name, idPrefix);
		detailUser.buildReadSimple();
		add(detailUser);

		TableColumn<UserRegistrationIntroduceOfferTableItem,
				String> nameHead = new TableColumn<>(
						Lang.INDIVIDUALITY_OBJECT_NAME.toString());
		nameHead.setCellValueFactory(new PropertyValueFactory<
				UserRegistrationIntroduceOfferTableItem,
				String>("name"));

		TableColumn<UserRegistrationIntroduceOfferTableItem,
				String> addrHead = new TableColumn<>(Lang.IPADDR.toString());
		addrHead.setCellValueFactory(new PropertyValueFactory<
				UserRegistrationIntroduceOfferTableItem,
				String>("addr"));

		TableColumn<UserRegistrationIntroduceOfferTableItem,
				String> createDateHead = new TableColumn<>(
						Lang.CREATE_DATE.toString());
		createDateHead.setCellValueFactory(new PropertyValueFactory<
				UserRegistrationIntroduceOfferTableItem,
				String>("createDate"));

		offerTable.getColumns().addAll(nameHead, addrHead, createDateHead);

		//登録申請の詳細を表示するためのハンドラ登録
		offerTableSelect.selectedItemProperty()
				.addListener((obs, oldSelection, newSelection) -> {
					if (newSelection == null)
						return;
					detailUser.set(newSelection);
				});

		addElapsed(2);

		Glb.getGui().polling(grid, () -> Glb.getGui().<Message,
				UserRegistrationIntroduceOfferTableItem> createPollingTaskTableView(
						() -> Glb.getMiddle().getUserRegistrationIntroduceList()
								.getUserRegistrations(),
						(model) -> new UserRegistrationIntroduceOfferTableItem(
								model),
						offerTable));
		return grid;
	}

	public VBox getIntroducerBox() {
		return introducerBox;
	}

	public TableView<UserRegistrationIntroduceOfferTableItem> getOfferTable() {
		return offerTable;
	}

	public TableView.TableViewSelectionModel<
			UserRegistrationIntroduceOfferTableItem> getOfferTableSelect() {
		return offerTableSelect;
	}

	@Override
	public void set(UserRegistration o) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void setIntroducerBox(VBox introducerBox) {
		this.introducerBox = introducerBox;
	}

	public void setOfferTable(
			TableView<UserRegistrationIntroduceOfferTableItem> offerTable) {
		this.offerTable = offerTable;
	}

	public void setOfferTableSelect(TableView.TableViewSelectionModel<
			UserRegistrationIntroduceOfferTableItem> offerTableSelect) {
		this.offerTableSelect = offerTableSelect;
	}

	@Override
	public void clear() {
		// TODO 自動生成されたメソッド・スタブ

	}
}
