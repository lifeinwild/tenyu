package bei7473p5254d69jcuat.tenyu.model.promise;

import bei7473p5254d69jcuat.tenyu.ui.*;

public interface HasGui {
	/**
	 * @return	このオブジェクトの内容を表すGUIを作成するオブジェクト
	 */
	ObjectGui<?> getGui(String guiName, String cssIdPrefix);

}
