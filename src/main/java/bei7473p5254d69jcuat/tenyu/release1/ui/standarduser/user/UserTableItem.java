package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;

public class UserTableItem extends NaturalityTableItem<UserDBI, User> {
	private User src;

	public UserTableItem(User src) {
		super(src);
		this.src = src;
	}

	@Override
	public User getSrc() {
		return src;
	}

	@Override
	public void update() {
		super.update();
	}

}
