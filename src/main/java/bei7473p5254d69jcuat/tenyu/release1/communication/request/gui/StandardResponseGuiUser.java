package bei7473p5254d69jcuat.tenyu.release1.communication.request.gui;

import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.UserCommonKeyPackage.*;

public class StandardResponseGuiUser extends StandardResponseGui
		implements UserCommonKeyPackageContent {
	@SuppressWarnings("unused")
	private StandardResponseGuiUser() {
	}

	public StandardResponseGuiUser(ResultCode code) {
		super(code);
	}
}
