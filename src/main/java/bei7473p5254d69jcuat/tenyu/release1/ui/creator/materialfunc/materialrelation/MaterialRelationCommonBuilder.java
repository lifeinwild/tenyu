package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.materialrelation;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.materialrelation.MaterialRelationGui.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MaterialRelationCommonBuilder {

	public static int buildMaterialRelationHalfPart(GuiBuilder builder,
			int elapsed, GridPane grid, CRUDContext ctx,
			MaterialRelationGui built) {
		Label styleLabel = new Label(Lang.STYLE.toString());
		built.setStyleLabel(styleLabel);
		TextField styleName = new TextField();
		styleName.setPromptText(Lang.NATURALITY_NAME.toString());
		built.setStyleName(styleName);
		grid.add(styleLabel, 0, elapsed);
		grid.add(styleName, 1, elapsed);
		elapsed += 1;

		Label avatarLabel = new Label(Lang.AVATAR.toString());
		built.setAvatarLabel(avatarLabel);
		TextField avatarName = new TextField();
		avatarName.setPromptText(Lang.NATURALITY_NAME.toString());
		built.setAvatarName(avatarName);
		grid.add(avatarLabel, 0, elapsed);
		grid.add(avatarName, 1, elapsed);
		elapsed += 1;

		return elapsed;
	}

	public static int buildReference(GuiBuilder builder, int elapsed,
			GridPane grid, CRUDContext ctx, MaterialRelationGui built) {
		int boxElapsed = 0;
		VBox box = new VBox();
		box.setId(builder.id() + "RefToMaterialPane");
		if (ctx != CRUDContext.DELETE) {
			HBox refPart = new HBox(10);
			refPart.setAlignment(Pos.CENTER_RIGHT);
			refPart.setId(builder.id() + "RefPart");
			Label refLabel = new Label(
					Lang.MATERIALRELATION_REFTOMATERIALID_REFNAME.toString());
			refLabel.setFocusTraversable(false);
			refPart.getChildren().add(refLabel);
			TextField refInput = new TextField();
			refPart.getChildren().add(refInput);
			box.getChildren().add(refPart);
			boxElapsed += 2;

			HBox materialPart = new HBox(10);
			refPart.setId(builder.id() + "MaterialPart");
			materialPart.setAlignment(Pos.CENTER_RIGHT);
			Label materialLabel = new Label(Lang.MATERIAL.toString());
			materialPart.getChildren().add(materialLabel);
			TextField materialNameInput = new TextField();
			materialNameInput.setPromptText(Lang.NATURALITY_NAME.toString());
			materialPart.getChildren().add(materialNameInput);
			box.getChildren().add(materialPart);
			boxElapsed += 2;

			GuiCommon.buildSubmitButton(box, boxElapsed,
					Lang.MATERIALRELATION_REFTOMATERIALID_ADD.toString(),
					builder.id() + "RefToMaterialIdAdd", gui -> true,
					gui -> true, null, null);
			boxElapsed += 2;
		}
		TableView<RefToMaterialTableItem> refToMaterial = new TableView<>();
		box.getChildren().add(refToMaterial);
		boxElapsed += 5;

		if (ctx != CRUDContext.DELETE) {
			GuiCommon.buildSubmitButton(box, boxElapsed,
					Lang.TABLEVIEW_REMOVE_SELECTED.toString(),
					builder.id() + "RefToMaterialIdRemove", gui -> true,
					gui -> {
						Object selected = refToMaterial.getSelectionModel()
								.getSelectedItems();
						return refToMaterial.getItems().remove(selected);
					}, null, null);
			boxElapsed += 2;
		}

		grid.add(box, 0, elapsed, 2, boxElapsed);

		return boxElapsed + elapsed;
	}

}
