package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.materialrelation;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.file.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.TableRow;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.materialrelation.MaterialRelationGui.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import jetbrains.exodus.env.*;

public class MaterialRelationGui extends
		ObjectivityObjectGui<MaterialRelationDBI,
				MaterialRelation,
				MaterialRelation,
				MaterialRelationStore,
				MaterialRelationGui,
				MaterialRelationTableItem> {
	public static class MaterialRelationTableItem extends
			ObjectivityObjectTableItem<MaterialRelationDBI, MaterialRelation> {

		public MaterialRelationTableItem(MaterialRelation src) {
			super(src);
		}

	}

	@Override
	protected MaterialRelationGui createDetailGui() {
		return new MaterialRelationGui(Lang.DETAIL.toString(),
				idPrefix + detail);
	}

	@Override
	protected MaterialRelationTableItem createTableItem(MaterialRelation o) {
		return new MaterialRelationTableItem(o);
	}

	public MaterialRelationGui(String name, String id) {
		super(name, id);
	}

	private Label styleLabel;
	private TextField styleName;

	private Label avatarLabel;
	private TextField avatarName;

	private TableView<RefToMaterialTableItem> refToMaterialId;

	@Override
	public GridPane buildUpdate(MaterialRelation exist) {
		super.buildUpdate(exist);

		set(exist);

		buildMaterialRelationHalfPart();

		buildReference();

		return grid;
	}

	@Override
	public GridPane buildSearch(
			SearchFuncs<MaterialRelationDBI, MaterialRelation> sf) {
		//総件数
		buildCount(DBUtil
				.countStatic(MaterialRelationStore.getMainStoreInfoStatic()));

		super.buildSearch(null);

		return grid;
	}

	@Override
	public GridPane buildDelete(MaterialRelation exist) {
		super.buildDelete(exist);

		buildMaterialRelationHalfPart();
		buildReference();

		set(exist);
		return grid;
	}

	public void buildMaterialRelationHalfPart() {
		Label styleLabel = new Label(Lang.STYLE.toString());
		setStyleLabel(styleLabel);
		TextField styleName = new TextField();
		styleName.setPromptText(Lang.NATURALITY_NAME.toString());
		setStyleName(styleName);
		grid.add(styleLabel, 0, elapsed);
		grid.add(styleName, 1, elapsed);
		elapsed += 1;

		Label avatarLabel = new Label(Lang.AVATAR.toString());
		setAvatarLabel(avatarLabel);
		TextField avatarName = new TextField();
		avatarName.setPromptText(Lang.NATURALITY_NAME.toString());
		setAvatarName(avatarName);
		grid.add(avatarLabel, 0, elapsed);
		grid.add(avatarName, 1, elapsed);
		elapsed += 1;
	}

	private MaterialGui material;
	private Label refLabel;
	private TableView<RefToMaterialTableItem> refToMaterial;

	private void buildReference() {
		//呼び出し名
		refLabel = new Label(
				Lang.MATERIALRELATION_REFTOMATERIALID_REFNAME.toString());
		refLabel.setFocusTraversable(false);
		TextField refInput = new TextField();

		//対応する素材
		material = new MaterialGui(name, idPrefix + "Material");
		material.buildSearchSimple(null);
		add(material);

		buildSubmitButton(Lang.MATERIALRELATION_REFTOMATERIALID_ADD.toString(),
				idPrefix + "RefToMaterialIdAdd", gui -> true, gui -> true, null,
				null);

		//対応関係一覧
		refToMaterial = new TableView<>();
		grid.add(refToMaterial, 0, elapsed);
		addElapsed(5);

		//選択した対応関係を削除
		buildSubmitButton(Lang.TABLEVIEW_REMOVE_SELECTED.toString(),
				idPrefix + "RefToMaterialIdRemove", gui -> true, gui -> {
					Object selected = refToMaterial.getSelectionModel()
							.getSelectedItems();
					return refToMaterial.getItems().remove(selected);
				}, null, null);
	}

	@Override
	public GridPane buildCreate() {
		super.buildCreate();

		buildMaterialRelationHalfPart();
		buildReference();

		return grid;
	}

	public static class RefToMaterialTableItem
			implements TableRow<RefToMaterial> {
		private RefToMaterial src;

		public RefToMaterialTableItem(RefToMaterial src) {
			this.src = src;
		}

		@Override
		public RefToMaterial getSrc() {
			return src;
		}

		@Override
		public void update() {
			// TODO 自動生成されたメソッド・スタブ

		}

	}

	public MaterialRelation setupModelCreate() {
		modelCache = new MaterialRelation();

		Long styleId = Glb.getObje()
				.getStyle(ss -> ss.getIdByName(styleName.getText()));
		if (styleId != null) {
			modelCache.setStyleId(styleId);
		}

		Long avatarId = Glb.getObje()
				.getAvatar(as -> as.getIdByName(avatarName.getText()));
		if (avatarId != null) {
			modelCache.setAvatarId(avatarId);
		}

		for (RefToMaterialTableItem e : refToMaterialId.getItems()) {
			modelCache.getRefToMaterialId().put(e.getSrc().getRef(),
					e.getSrc().getMaterial().getRecycleId());
		}

		return super.setupModelCreate();
	}

	@Override
	public void set(MaterialRelation o) {
		super.set(o);

		Style s = Glb.getObje().getStyle(ss -> ss.get(o.getStyleId()));
		if (s != null)
			styleName.setText(s.getName());

		Avatar a = Glb.getObje().getAvatar(as -> as.get(o.getAvatarId()));
		if (a != null)
			avatarName.setText(a.getName());

		for (RefToMaterial e : o.getRefToMaterialList()) {
			refToMaterialId.getItems().add(new RefToMaterialTableItem(e));
		}
	}

	@Override
	public MaterialRelation setupModelUpdateOrDelete(MaterialRelation o) {
		return super.setupModelUpdateOrDelete(o);
	}

	@Override
	public Lang getClassNameLang() {
		return Lang.MATERIALRELATION;
	}

	public Label getStyleLabel() {
		return styleLabel;
	}

	public void setStyleLabel(Label styleLabel) {
		this.styleLabel = styleLabel;
	}

	public TextField getStyleName() {
		return styleName;
	}

	public void setStyleName(TextField styleName) {
		this.styleName = styleName;
	}

	public Label getAvatarLabel() {
		return avatarLabel;
	}

	public void setAvatarLabel(Label avatarLabel) {
		this.avatarLabel = avatarLabel;
	}

	public TextField getAvatarName() {
		return avatarName;
	}

	public void setAvatarName(TextField avatarName) {
		this.avatarName = avatarName;
	}

	public TableView getRefToMaterialId() {
		return refToMaterialId;
	}

	public void setRefToMaterialId(TableView refToMaterialId) {
		this.refToMaterialId = refToMaterialId;
	}

	@Override
	public MaterialRelationStore getStore(Transaction txn) {
		try {
			return new MaterialRelationStore(txn);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

}
