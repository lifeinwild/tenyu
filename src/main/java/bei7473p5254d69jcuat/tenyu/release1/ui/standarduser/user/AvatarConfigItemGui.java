package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.AvatarConfig.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.beans.property.*;

public class AvatarConfigItemGui implements TableRow<AvatarConfigItem> {
	private AvatarConfigItem src;
	private IntegerProperty priority = new SimpleIntegerProperty();
	private StringProperty avatarName = new SimpleStringProperty();

	public AvatarConfigItemGui(AvatarConfigItem src) {
		this.src = src;
		this.priority.set(src.getPriority());
		this.avatarName.set(src.getAvatar().getName());
	}

	public String getAvatarName() {
		return avatarName.get();
	}

	public Integer getPriority() {
		return priority.get();
	}

	@Override
	public AvatarConfigItem getSrc() {
		return src;
	}

	@Override
	public void update() {
		// TODO 自動生成されたメソッド・スタブ

	}

}