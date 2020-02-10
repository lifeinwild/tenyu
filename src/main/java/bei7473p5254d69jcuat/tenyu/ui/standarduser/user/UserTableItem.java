package bei7473p5254d69jcuat.tenyu.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;

public class UserTableItem extends IndividualityObjectTableItem<UserDBI, User> {
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
