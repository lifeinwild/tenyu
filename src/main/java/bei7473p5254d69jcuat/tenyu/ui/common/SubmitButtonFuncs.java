package bei7473p5254d69jcuat.tenyu.ui.common;

import java.util.function.*;

public class SubmitButtonFuncs {
	private Function<SubmitButtonGui, Boolean> validateFunc;
	private Function<SubmitButtonGui, Boolean> sendFunc;
	private Consumer<SubmitButtonGui> successFunc;
	private Consumer<SubmitButtonGui> failedFunc;
	private String buttonName;
	private String idPrefix;

	public SubmitButtonFuncs(Function<SubmitButtonGui, Boolean> validateFunc,
			Function<SubmitButtonGui, Boolean> sendFunc,
			Consumer<SubmitButtonGui> successFunc,
			Consumer<SubmitButtonGui> failedFunc) {
		this.validateFunc = validateFunc;
		this.sendFunc = sendFunc;
		this.successFunc = successFunc;
		this.failedFunc = failedFunc;
	}

	public SubmitButtonFuncs(String buttonName, String idPrefix,
			Function<SubmitButtonGui, Boolean> validateFunc,
			Function<SubmitButtonGui, Boolean> sendFunc,
			Consumer<SubmitButtonGui> successFunc,
			Consumer<SubmitButtonGui> failedFunc) {
		this(validateFunc, sendFunc, successFunc, failedFunc);
		this.buttonName = buttonName;
		this.idPrefix = idPrefix;
	}

	public String getButtonName() {
		return buttonName;
	}

	public Function<SubmitButtonGui, Boolean> getValidateFunc() {
		return validateFunc;
	}

	public void setValidateFunc(
			Function<SubmitButtonGui, Boolean> validateFunc) {
		this.validateFunc = validateFunc;
	}

	public Function<SubmitButtonGui, Boolean> getSendFunc() {
		return sendFunc;
	}

	public void setSendFunc(Function<SubmitButtonGui, Boolean> sendFunc) {
		this.sendFunc = sendFunc;
	}

	public Consumer<SubmitButtonGui> getSuccessFunc() {
		return successFunc;
	}

	public void setSuccessFunc(Consumer<SubmitButtonGui> successFunc) {
		this.successFunc = successFunc;
	}

	public Consumer<SubmitButtonGui> getFailedFunc() {
		return failedFunc;
	}

	public void setFailedFunc(Consumer<SubmitButtonGui> failedFunc) {
		this.failedFunc = failedFunc;
	}

	public String getIdPrefix() {
		return idPrefix;
	}

	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
	}

	public void setButtonName(String buttonName) {
		this.buttonName = buttonName;
	}
}