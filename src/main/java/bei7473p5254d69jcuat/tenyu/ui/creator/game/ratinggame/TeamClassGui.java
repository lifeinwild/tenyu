package bei7473p5254d69jcuat.tenyu.ui.creator.game.ratinggame;

import com.ibm.icu.text.*;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game.RatingGame.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import glb.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class TeamClassGui extends ObjectGui<TeamClass> {
	public TeamClassGui(String name, String id) {
		super(name, id);
	}

	@Override
	public void set(TeamClass o) {
		if (nameInput != null)
			nameInput.setText(o.getName());
		if (memberCountInput != null)
			memberCountInput.setText(o.getMemberCount() + "");
	}

	private TeamClass team;
	private TextField nameInput;
	private TextField memberCountInput;

	public TeamClass setupModel() {
		//GUIに設定された情報からTeamオブジェクトを作成
		TeamClass tc = new TeamClass();
		setTeam(tc);
		int memberCount = -1;
		try {
			memberCount = Integer.valueOf(getMemberCountInput().getText());
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		}
		tc.setMemberCount(memberCount);
		tc.setName(nameInput.getText());
		return tc;
	}

	@Override
	public GridPane buildRead() {
		// TODO 自動生成されたメソッド・スタブ
		return super.buildRead();
	}

	@Override
	public GridPane buildCreate() {
		super.buildCreate();

		Label teamName = new Label(Lang.RATINGGAME_TEAMCLASS_NAME.toString());
		teamName.setId(idPrefix + "TeamName");
		teamName.setFocusTraversable(true);
		TextField teamNameInput = new TextField() {
			@Override
			public void replaceText(int start, int end, String text) {
				text = text.trim();
				String normalized = Normalizer2.getNFKCInstance()
						.normalize(text);
				super.replaceText(start, end, normalized);
			}
		};
		grid.add(teamName, 0, elapsed);
		grid.add(teamNameInput, 1, elapsed);
		elapsed += 1;

		Label teamMemberCount = new Label(
				Lang.RATINGGAME_TEAMCLASS_MEMBER_COUNT.toString());
		teamMemberCount.setId(idPrefix + "TeamMemberCount");
		teamMemberCount.setFocusTraversable(true);
		TextField teamMemberCountInput = new TextField();
		setNameInput(teamNameInput);
		setMemberCountInput(teamMemberCountInput);
		grid.add(teamMemberCount, 0, elapsed);
		grid.add(teamMemberCountInput, 1, elapsed);
		elapsed += 1;
		return grid;
	}

	public void clear() {
		if (nameInput != null) {
			nameInput.clear();
		}
		if (memberCountInput != null) {
			memberCountInput.clear();
		}
	}

	public TextField getNameInput() {
		return nameInput;
	}

	public void setNameInput(TextField nameInput) {
		this.nameInput = nameInput;
	}

	public TextField getMemberCountInput() {
		return memberCountInput;
	}

	public void setMemberCountInput(TextField memberCountInput) {
		this.memberCountInput = memberCountInput;
	}

	public TeamClass getTeam() {
		return team;
	}

	public void setTeam(TeamClass team) {
		this.team = team;
	}

}