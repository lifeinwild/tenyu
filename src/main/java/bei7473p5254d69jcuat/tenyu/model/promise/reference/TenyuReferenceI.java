package bei7473p5254d69jcuat.tenyu.model.promise.reference;

import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.promise.rpc.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import glb.*;

/**
 * Tenyu基盤ソフトウェアが扱うモデルデータや機能への参照
 *
 * 機能への参照はRPCのようにメソッドの呼び出しを意味する。
 *
 * いくつかの客観系モデルのメンバー変数に使用される。
 * 他にURL化してMarkdown記事中で使用される。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyuReferenceI
		extends TenyuSingleObjectMessageI, ObjectI {
	/**
	 * @return	参照先についてのエンドユーザー向け簡易説明
	 */
	String getSimpleExplanationForUser();

	@Override
	default Object rpc() throws Exception {
		showReferenced();
		return null;
	}

	/**
	 * @return	参照先を表示するGUI
	 */
	ObjectGui<?> getGuiReferenced(String guiName, String cssIdPrefix);

	/**
	 * @return	参照自体を表示するGUI
	 */
	ObjectGui<? extends TenyuReferenceI> getGuiMyself(String guiName,
			String cssIdPrefix);

	/**
	 * 参照先をGUI表示する
	 */
	default void showReferenced() {
		ObjectGui<?> gui = getGuiReferenced("show", "show");
		if (gui == null)
			return;
		Glb.getGui().createTab(gui.buildCreate(),
				getSimpleExplanationForUser());
	}

	/**
	 * 参照自体をGUI表示する
	 */
	default void showMyself() {
		ObjectGui<? extends TenyuReferenceI> gui = getGuiMyself("show",
				"show");
		if (gui == null)
			return;
		Glb.getGui().createTab(gui.buildCreate(),
				Lang.REF + ":" + getSimpleExplanationForUser());
	}

}
