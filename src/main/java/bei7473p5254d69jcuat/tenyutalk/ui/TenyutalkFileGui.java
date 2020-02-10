package bei7473p5254d69jcuat.tenyutalk.ui;

import bei7473p5254d69jcuat.tenyutalk.db.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;
import glb.*;
import jetbrains.exodus.env.*;

public class TenyutalkFileGui extends
		CreativeObjectGui<TenyutalkFileDBI,
				TenyutalkFile,
				TenyutalkFile,
				TenyutalkFileStore,
				TenyutalkFileGui,
				TenyutalkFileTableItem> {
	public TenyutalkFileGui(String name, String id) {
		super(name, id);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	protected TenyutalkFileTableItem createTableItem(TenyutalkFile o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected TenyutalkFileGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public TenyutalkFileStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
