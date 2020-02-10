package bei7473p5254d69jcuat.tenyutalk.ui;

import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyutalk.db.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;

public abstract class CreativeObjectGui<T1 extends CreativeObjectDBI,
		T2 extends T1,
		T3 extends CreativeObject,
		S extends CreativeObjectStore<T1, T2>,
		G extends CreativeObjectGui,
		TI extends CreativeObjectTableItem<T1, T2>>
		extends IndividualityObjectGui<T1, T2, T3, S, G, TI> {
	public CreativeObjectGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}


}
