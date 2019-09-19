package bei7473p5254d69jcuat.tenyu.release1.ui;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.admin.other.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.game.ratinggame.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.game.staticgame.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.avatar.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.materialrelation.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.style.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.gameplay.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.neighbor.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.other.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.sociality.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.user.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

/**
 * 機能一覧のTreeViewを構築する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class TreeViewBuilder extends GuiBuilder {

	@Override
	public TreeView<GuiBuilder> build() {
		//TreeView root item
		TreeItem<GuiBuilder> outlineItem = new TreeItem<>(
				new NullBuilder(Lang.OUTLINE.toString()));
		outlineItem.setExpanded(true);

		//TreeView
		TreeView<GuiBuilder> outlinePane = new TreeView<>(outlineItem);
		outlinePane.setId("outlinePane");
		outlinePane.setOnKeyPressed((ev) -> {
			Glb.debug("tree pressed : " + KeyCode.valueOf(ev.getCode().name()));
			if (ev.getCode() == KeyCode.SPACE) {
				itemExpand();
				itemContentAction();
			}
		});
		outlinePane.setOnMouseReleased((ev) -> {
			Glb.debug("clicked");
			if (ev.getClickCount() == 1) {
				itemContentAction();
			}
		});
		outlinePane.focusedProperty()
				.addListener((arg0, oldPropertyValue, newPropertyValue) -> {
					outlinePane.getSelectionModel().select(
							outlinePane.getFocusModel().getFocusedIndex());
					if (newPropertyValue) {
						System.out.println("treePane on focus");
					} else {
						System.out.println("treePane out focus");
					}
				});

		//一般、制作、運営
		TreeItem<GuiBuilder> standardUser = new TreeItem<>(
				new NullBuilder(Lang.STANDARD_USER));
		standardUser.setExpanded(true);
		outlineItem.getChildren().add(standardUser);
		TreeItem<GuiBuilder> creatorUser = new TreeItem<>(
				new NullBuilder(Lang.CREATOR_USER));
		creatorUser.setExpanded(true);
		outlineItem.getChildren().add(creatorUser);
		TreeItem<GuiBuilder> adminUser = new TreeItem<>(
				new NullBuilder(Lang.ADMIN_USER));
		adminUser.setExpanded(true);
		outlineItem.getChildren().add(adminUser);

		//客観、主観
		TreeItem<GuiBuilder> subje = new TreeItem<>(
				new NullBuilder(Lang.SUBJECTIVITY));
		subje.setExpanded(true);
		standardUser.getChildren().add(subje);
		TreeItem<GuiBuilder> obje = new TreeItem<>(
				new NullBuilder(Lang.OBJECTIVITY));
		obje.setExpanded(true);
		standardUser.getChildren().add(obje);

		//近傍
		TreeItem<GuiBuilder> neighbor = new TreeItem<>(
				new NullBuilder(Lang.NEIGHBOR));
		neighbor.setExpanded(true);
		subje.getChildren().add(neighbor);
		//近傍手動追加
		TreeItem<GuiBuilder> neighborManualAdd = new TreeItem<>(
				new NeighborManualAddBuilder());
		neighbor.getChildren().add(neighborManualAdd);
		//近傍一覧
		TreeItem<GuiBuilder> neighborList = new TreeItem<>(
				new NeighborListBuilder());
		neighbor.getChildren().add(neighborList);

		//User
		TreeItem<GuiBuilder> user = new TreeItem<>(new NullBuilder(Lang.USER));
		user.setExpanded(true);
		obje.getChildren().add(user);
		TreeItem<GuiBuilder> userRegistration = new TreeItem<>(
				new UserRegistrationBuilder());
		user.getChildren().add(userRegistration);
		TreeItem<GuiBuilder> userRegistrationIntroduce = new TreeItem<>(
				new UserRegistrationIntroduceBuilder());
		user.getChildren().add(userRegistrationIntroduce);
		TreeItem<GuiBuilder> userUpdate = new TreeItem<>(
				new UserUpdateBuilder());
		user.getChildren().add(userUpdate);
		TreeItem<GuiBuilder> userSearch = new TreeItem<>(
				new UserSearchBuilder());
		user.getChildren().add(userSearch);

		//全体運営者を決定する分散合意の投票
		TreeItem<GuiBuilder> managerVote = new TreeItem<>(
				new ManagerVoteBuilder());
		user.getChildren().add(managerVote);

		//ゲームプレイ
		TreeItem<GuiBuilder> play = new TreeItem<>(
				new NullBuilder(Lang.GAMEPLAY));
		play.setExpanded(true);
		obje.getChildren().add(play);
		TreeItem<GuiBuilder> ratingGamePlaySearchSingle = new TreeItem<>(
				new RatingGamePlaySearchSingleBuilder());
		play.getChildren().add(ratingGamePlaySearchSingle);
		TreeItem<GuiBuilder> ratingGamePlaySearchTeam = new TreeItem<>(
				new RatingGamePlaySearchTeamBuilder());
		play.getChildren().add(ratingGamePlaySearchTeam);
		TreeItem<GuiBuilder> ratingGameMatchResultSearchSingle = new TreeItem<>(
				new RatingGameMatchResultSearchSingleBuilder());
		play.getChildren().add(ratingGameMatchResultSearchSingle);
		TreeItem<GuiBuilder> ratingGameMatchResultSearchTeam = new TreeItem<>(
				new RatingGameMatchResultSearchTeamBuilder());
		play.getChildren().add(ratingGameMatchResultSearchTeam);
		TreeItem<GuiBuilder> staticGamePlaySearch = new TreeItem<>(
				new StaticGamePlaySearchBuilder());
		play.getChildren().add(staticGamePlaySearch);

		//社会性
		TreeItem<GuiBuilder> sociality = new TreeItem<>(
				new NullBuilder(Lang.SOCIALITY_GUI_NAME));
		sociality.setExpanded(true);
		obje.getChildren().add(sociality);
		TreeItem<GuiBuilder> edge = new TreeItem<>(new EdgeBuilder());
		sociality.getChildren().add(edge);
		TreeItem<GuiBuilder> edgeSearch = new TreeItem<>(
				new EdgeSearchBuilder());
		sociality.getChildren().add(edgeSearch);
		TreeItem<GuiBuilder> sendMoney = new TreeItem<>(new SendMoneyBuilder());
		sociality.getChildren().add(sendMoney);

		//その他
		TreeItem<GuiBuilder> subjeOther = new TreeItem<>(
				new NullBuilder(Lang.OTHER));
		subjeOther.setExpanded(false);
		subje.getChildren().add(subjeOther);
		TreeItem<GuiBuilder> secretKeyPasswordChange = new TreeItem<>(
				new SecretKeyPasswordChangeBuilder());
		subjeOther.getChildren().add(secretKeyPasswordChange);
		TreeItem<GuiBuilder> inventorProvementPassword = new TreeItem<>(
				new InventorProvementPasswordBuilder());

		//ファイル関連
		TreeItem<GuiBuilder> fileFuncs = new TreeItem<>(
				new NullBuilder(Lang.MATERIAL_FUNCTIONS));
		fileFuncs.setExpanded(true);
		creatorUser.getChildren().add(fileFuncs);

		//スタイル
		TreeItem<
				GuiBuilder> style = new TreeItem<>(new NullBuilder(Lang.STYLE));
		style.setExpanded(true);
		fileFuncs.getChildren().add(style);
		TreeItem<GuiBuilder> styleRegistration = new TreeItem<>(
				new StyleRegisterBuilder());
		style.getChildren().add(styleRegistration);
		TreeItem<GuiBuilder> styleSearch = new TreeItem<>(
				new StyleSearchBuilder());
		style.getChildren().add(styleSearch);

		//アバター
		TreeItem<GuiBuilder> avatar = new TreeItem<>(
				new NullBuilder(Lang.AVATAR));
		avatar.setExpanded(true);
		fileFuncs.getChildren().add(avatar);
		TreeItem<GuiBuilder> avatarRegistration = new TreeItem<>(
				new AvatarRegisterBuilder());
		avatar.getChildren().add(avatarRegistration);
		TreeItem<GuiBuilder> avatarSearch = new TreeItem<>(
				new AvatarSearchBuilder());
		avatar.getChildren().add(avatarSearch);

		//素材
		TreeItem<GuiBuilder> material = new TreeItem<>(
				new NullBuilder(Lang.MATERIAL));
		material.setExpanded(true);
		fileFuncs.getChildren().add(material);
		TreeItem<GuiBuilder> materialRegistration = new TreeItem<>(
				new MaterialRegisterBatchBuilder());
		material.getChildren().add(materialRegistration);
		TreeItem<GuiBuilder> materialUpdateBatch = new TreeItem<>(
				new MaterialUpdateBatchBuilder());
		material.getChildren().add(materialUpdateBatch);
		TreeItem<GuiBuilder> materialSearch = new TreeItem<>(
				new MaterialSearchBuilder());
		material.getChildren().add(materialSearch);

		//素材対応関係
		TreeItem<GuiBuilder> materialRelation = new TreeItem<>(
				new NullBuilder(Lang.MATERIALRELATION));
		materialRelation.setExpanded(true);
		fileFuncs.getChildren().add(materialRelation);
		TreeItem<GuiBuilder> materialRelationRegistration = new TreeItem<>(
				new MaterialRelationRegisterBuilder());
		materialRelation.getChildren().add(materialRelationRegistration);
		TreeItem<GuiBuilder> materialRelationSearch = new TreeItem<>(
				new MaterialRelationSearchBuilder());
		materialRelation.getChildren().add(materialRelationSearch);

		//ゲーム
		TreeItem<GuiBuilder> gameFuncs = new TreeItem<>(
				new NullBuilder(Lang.GAME));
		gameFuncs.setExpanded(true);
		creatorUser.getChildren().add(gameFuncs);

		//レーティングゲーム
		TreeItem<GuiBuilder> ratingGame = new TreeItem<>(
				new NullBuilder(Lang.RATINGGAME));
		ratingGame.setExpanded(true);
		gameFuncs.getChildren().add(ratingGame);
		TreeItem<GuiBuilder> ratingGameRegistration = new TreeItem<>(
				new RatingGameRegisterBuilder());
		ratingGame.getChildren().add(ratingGameRegistration);
		TreeItem<GuiBuilder> ratingGameSearch = new TreeItem<>(
				new RatingGameSearchBuilder());
		ratingGame.getChildren().add(ratingGameSearch);
		TreeItem<GuiBuilder> ratingGameConflictMatchList = new TreeItem<>(
				new RatingGameConflictMatchList());
		ratingGame.getChildren().add(ratingGameConflictMatchList);

		//常駐空間ゲーム
		TreeItem<GuiBuilder> staticGame = new TreeItem<>(
				new NullBuilder(Lang.STATICGAME));
		staticGame.setExpanded(true);
		gameFuncs.getChildren().add(staticGame);
		TreeItem<GuiBuilder> staticGameRegistration = new TreeItem<>(
				new StaticGameRegisterBuilder());
		staticGame.getChildren().add(staticGameRegistration);
		TreeItem<GuiBuilder> staticGameUpdateBatch = new TreeItem<>(
				new StaticGameUpdateBatchBuilder());
		staticGame.getChildren().add(staticGameUpdateBatch);
		TreeItem<GuiBuilder> staticGameSearch = new TreeItem<>(
				new StaticGameSearchBuilder());
		staticGame.getChildren().add(staticGameSearch);

		//運営のその他
		TreeItem<GuiBuilder> adminOther = new TreeItem<>(
				new NullBuilder(Lang.OTHER));
		adminOther.setExpanded(false);
		adminUser.getChildren().add(adminOther);
		adminOther.getChildren().add(inventorProvementPassword);

		return outlinePane;
	}

	/**
	 * 機能一覧の各カテゴリーを選択した時の子一覧を表示またはしまう動作
	 */
	private void itemExpand() {
		TreeView<GuiBuilder> treePane = Glb.getGui().getOutlinePane();
		FocusModel<TreeItem<GuiBuilder>> focus = treePane.getFocusModel();
		TreeItem<GuiBuilder> focusedItem = focus.getFocusedItem();
		if (focusedItem != null) {
			if (focusedItem.getChildren() != null
					&& focusedItem.getChildren().size() > 0) {
				boolean ex = focusedItem.isExpanded();
				focusedItem.setExpanded(!ex);
			}
		}
	}

	/**
	 * 機能一覧の各項目を選択した時の動作
	 */
	private void itemContentAction() {
		TreeView<GuiBuilder> treePane = Glb.getGui().getOutlinePane();

		FocusModel<TreeItem<GuiBuilder>> focus = treePane.getFocusModel();
		TreeItem<GuiBuilder> focusedItem = focus.getFocusedItem();
		if (focusedItem != null) {
			GuiBuilder itemContent = focusedItem.getValue();
			if (itemContent != null) {
				//内容作成
				Node p = itemContent.build();

				//gridpaneの伸縮に合わせてサイズが変わるようにする
				//縦幅は変更していない。基本的に縦幅はスクロール前提であるべき。
				//gridは最大横幅2を前提としていて、横幅2のコンテンツのみ横幅の自動伸縮をする
				if (p instanceof GridPane) {
					GridPane g = (GridPane) p;
					for (Node o : g.getChildren()) {
						Integer span = GridPane.getColumnSpan(o);
						if (!(o instanceof Region))
							continue;
						Region reg = (Region) o;
						if (span == null && GridPane.getColumnIndex(o) == 1) {
							//reg.prefWidthProperty().bind(g.widthProperty());	見栄え的に微妙になるだけだった
						} else if (span != null && span.equals(2)) {
							reg.prefWidthProperty().bind(g.widthProperty());
						}
					}
				}

				Glb.getGui().createTab(p, itemContent.name());
			}
		}
	}

	@Override
	public String name() {
		return Lang.OUTLINE.toString();
	}

	@Override
	public String id() {
		return "outline";
	}

}
