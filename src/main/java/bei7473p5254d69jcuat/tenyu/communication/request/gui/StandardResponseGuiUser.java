package bei7473p5254d69jcuat.tenyu.communication.request.gui;

import bei7473p5254d69jcuat.tenyu.communication.packaging.UserCommonKeyPackage.*;

public class StandardResponseGuiUser extends StandardResponseGui
		implements UserCommonKeyPackageContent {
	@SuppressWarnings("unused")
	private StandardResponseGuiUser() {
	}

	public StandardResponseGuiUser(ResultCode code) {
		super(code);
	}
}
