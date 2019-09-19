package bei7473p5254d69jcuat.tenyu.release1.communication.request.gui;

import bei7473p5254d69jcuat.tenyu.release1.communication.packaging.P2PEdgeCommonKeyPackage.*;

public class StandardResponseGuiP2PEdge extends StandardResponseGui
		implements P2PEdgeCommonKeyPackageContent {
	@SuppressWarnings("unused")
	private StandardResponseGuiP2PEdge() {
	}

	public StandardResponseGuiP2PEdge(ResultCode code) {
		super(code);
	}
}
