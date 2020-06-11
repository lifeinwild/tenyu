package bei7473p5254d69jcuat.tenyu.model.promise;

import bei7473p5254d69jcuat.tenyu.ui.*;

public interface HasGuiI {
	/**
	 * {@link Object}を改修できるならそこに記述するが、
	 * できないのでこのようなインターフェースで代用している。
	 *
	 * @return	このオブジェクトの内容を表すGUIを作成するオブジェクト
	 */
	ObjectGui<?> getGuiReferenced(String guiName, String cssIdPrefix);

}
