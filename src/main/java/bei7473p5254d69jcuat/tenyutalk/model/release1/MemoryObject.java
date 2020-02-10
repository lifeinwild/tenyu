package bei7473p5254d69jcuat.tenyutalk.model.release1;

import bei7473p5254d69jcuat.tenyutalk.db.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.ui.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public abstract class MemoryObject extends CreativeObject implements MemoryObjectDBI {

	@Override
	public CreativeObjectGui<?, ?, ?, ?, ?, ?> getGui(String guiName,
			String cssIdPrefix) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public CreativeObjectStore<? extends MemoryObjectDBI,
			? extends MemoryObjectDBI> getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected boolean validateAtCreateCreativeObjectConcrete(ValidationResult r) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean validateAtUpdateChangeCreativeObjectConcrete(
			ValidationResult r, Object old) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean validateAtUpdateCreativeObjectConcrete(ValidationResult r) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean validateReferenceCreativeObjectConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

}
