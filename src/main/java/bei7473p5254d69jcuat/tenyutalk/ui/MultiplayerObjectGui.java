package bei7473p5254d69jcuat.tenyutalk.ui;

import bei7473p5254d69jcuat.tenyutalk.db.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;

public abstract class MultiplayerObjectGui<T1 extends MultiplayerObjectI,
		T2 extends T1,
		T3 extends MultiplayerObject,
		S extends MultiplayerObjectStore<T1, T2>,
		G extends MultiplayerObjectGui,
		TI extends MultiplayerObjectTableItem<T1, T2>>
		extends CreativeObjectGui<T1, T2, T3, S, G, TI> {

	public MultiplayerObjectGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

}
