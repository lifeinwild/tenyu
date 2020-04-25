package bei7473p5254d69jcuat.tenyu.ui.standarduser.neighbor;

import bei7473p5254d69jcuat.tenyu.communication.request.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import glb.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.util.converter.*;

public class NeighborListGui extends ObjectGui<ReadonlyNeighborList> {

	public NeighborListGui(String name, String id) {
		super(name, id);
	}

	@Override
	public void set(ReadonlyNeighborList o) {
		// TODO 自動生成されたメソッド・スタブ

	}

	private TableView<P2PEdgeTableItem> neighborListGui;
	private P2PEdgeTableItem selected;
	private SubmitButtonGui deleteButton;

	public GridPane buildDelete() {
		super.buildCreate();
		setNeighborListGui(new TableView<>());

		getNeighborListGui().setId(idPrefix + "Table");
		getNeighborListGui().setEditable(true);
		getNeighborListGui().setMinWidth(640);
		//getNeighborListGui().prefWidthProperty().bind(grid.widthProperty());

		TableColumn<P2PEdgeTableItem,
				Long> userIdHead = new TableColumn<>(Lang.USER_ID.toString());
		userIdHead.setCellValueFactory(
				new PropertyValueFactory<P2PEdgeTableItem, Long>("userId"));

		TableColumn<P2PEdgeTableItem, String> nameHead = new TableColumn<>(
				Lang.INDIVIDUALITY_OBJECT_NAME.toString());
		nameHead.setCellValueFactory(
				new PropertyValueFactory<P2PEdgeTableItem, String>("name"));

		TableColumn<P2PEdgeTableItem,
				Long> latencyHead = new TableColumn<>(Lang.LATENCY.toString());
		latencyHead.setCellValueFactory(
				new PropertyValueFactory<P2PEdgeTableItem, Long>("latency"));

		TableColumn<P2PEdgeTableItem,
				String> addrHead = new TableColumn<>(Lang.IPADDR.toString());
		addrHead.setCellValueFactory(
				new PropertyValueFactory<P2PEdgeTableItem, String>("addr"));

		TableColumn<P2PEdgeTableItem,
				Integer> impressionHead = new TableColumn<>(
						Lang.IMPRESSION.toString());
		impressionHead.setCellValueFactory(
				new PropertyValueFactory<P2PEdgeTableItem, Integer>(
						"impression"));
		impressionHead.setMinWidth(impressionHead.getMinWidth() + 40);

		TableColumn<P2PEdgeTableItem,
				Boolean> dontRemoveHead = new TableColumn<>(
						Lang.NEIGHBOR_DONT_REMOVE_AUTOMATICALLY.toString());
		dontRemoveHead.setCellValueFactory(
				new PropertyValueFactory<P2PEdgeTableItem, Boolean>(
						"dontRemove"));
		dontRemoveHead.setEditable(true);
		dontRemoveHead.setCellFactory(TextFieldTableCell
				.forTableColumn(new BooleanStringConverter()));
		//TODO falseからtrueに編集後再び編集しようとするとfalseになる。最初にセルに設定された値になる。
		dontRemoveHead.setOnEditCommit(
				new EventHandler<CellEditEvent<P2PEdgeTableItem, Boolean>>() {
					@Override
					public void handle(
							CellEditEvent<P2PEdgeTableItem, Boolean> ev) {
						boolean dontRemove = ev.getNewValue();
						ev.getRowValue().getSrc()
								.setDontRemoveAutomatically(dontRemove);
					}
				});

		TableColumn<P2PEdgeTableItem,
				String> createDateHead = new TableColumn<>(
						Lang.CREATE_DATE.toString());
		createDateHead.setCellValueFactory(
				new PropertyValueFactory<P2PEdgeTableItem, String>(
						"createDate"));

		getNeighborListGui().getColumns().addAll(userIdHead, nameHead,
				latencyHead, addrHead, impressionHead, dontRemoveHead,
				createDateHead);

		grid.add(getNeighborListGui(), 0, elapsed, 2, 4);
		addElapsed(5);
		Glb.getGui().polling(grid,
				() -> Glb.getGui().<P2PEdge,
						P2PEdgeTableItem> createPollingTaskTableView(
								() -> Glb.getSubje().getNeighborList()
										.getNeighborsCopy(),
								(model -> new P2PEdgeTableItem(model)),
								getNeighborListGui()));

		//削除
		deleteButton = buildSubmitButton(Lang.NEIGHBOR_MANUAL_DELETE.toString(),
				idPrefix + "ManualDelete", submit -> {
					P2PEdgeTableItem selected = getNeighborListGui()
							.getSelectionModel().getSelectedItem();
					if (selected == null || selected.getSrc() == null) {
						submit.message(
								Lang.NEIGHBOR_MANUAL_DELETE_EMPTY.toString());
						return false;
					}
					setSelected(selected);
					return true;
				}, submit -> DeleteEdge.send(getSelected().getSrc()), null,
				null);

		return grid;
	}

	public SubmitButtonGui getDeleteButton() {
		return deleteButton;
	}

	public TableView<P2PEdgeTableItem> getNeighborListGui() {
		return neighborListGui;
	}

	public void setNeighborListGui(
			TableView<P2PEdgeTableItem> neighborListGui) {
		this.neighborListGui = neighborListGui;
	}

	public P2PEdgeTableItem getSelected() {
		return selected;
	}

	public void setSelected(P2PEdgeTableItem selected) {
		this.selected = selected;
	}

	@Override
	public void clear() {
		// TODO 自動生成されたメソッド・スタブ

	}
}