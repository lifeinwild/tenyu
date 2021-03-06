package bei7473p5254d69jcuat.tenyu.communication.request;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.PlainPackage.*;

public class StandardResponse extends AbstractStandardResponse
		implements PlainPackageContent {

	@SuppressWarnings("unused")
	private StandardResponse() {
	}

	public StandardResponse(ResultCode code) {
		this.code = code;
	}

	@Override
	public boolean isValid(Request req) {
		return true;
	}

	@Override
	protected boolean validateAbstractStandardResponseConcrete(Message m) {
		return true;
	}

}