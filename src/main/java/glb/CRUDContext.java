package glb;

/**
 * GUIで使用しているが意味的にはもっと普遍的なのでここに置く。
 *
 * 呼び出し側の文脈
 *
 * Simpleは簡易表示
 * SUBはGUI末尾に送信ボタンがつき、その送信ボタンの動作を呼び出し元が与え、
 * 外側のGUI部品と連動するタイプ。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public enum CRUDContext {
	CREATE,
	CREATE_BATCH,
	READ,
	/**
	 * 簡易な表示がしたい場合
	 */
	READ_SIMPLE,
	UPDATE,
	UPDATE_BATCH,
	DELETE,
	DELETE_BATCH,
	SEARCH,
	/**
	 * 参照の設定等で名前検索だけ使いたい場合など
	 */
	SEARCH_SIMPLE;

	public static boolean editable(CRUDContext ctx) {
		boolean editable = true;
		if (ctx == CRUDContext.READ || ctx == CRUDContext.DELETE)
			editable = false;
		return editable;
	}

	/**
	 * 更新において修正不可
	 * IDなど更新不可能なメンバーについての判定
	 *
	 * @param ctx
	 * @return
	 */
	public static boolean editableBase(CRUDContext ctx) {
		boolean editable = false;
		if (ctx == CREATE || ctx == CRUDContext.SEARCH)
			editable = true;
		/*
		if (ctx == CRUDContext.READ || ctx == CRUDContext.DELETE
				|| ctx == UPDATE)
			editable = false;*/
		return editable;
	}

}