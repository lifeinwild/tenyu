package bei7473p5254d69jcuat.tenyutalk.db;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class MultiplayerObjectStore<T1 extends CreativeObjectI,
		T2 extends T1> extends CreativeObjectStore<T1, T2> {

	public MultiplayerObjectStore(Transaction txn) {
		super(txn);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	@Override
	protected boolean createVersionedConcrete(T1 o) throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean dbValidateAtUpdateVersionedConcrete(T1 updated, T1 old,
			ValidationResult r) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean deleteVersionedConcrete(T1 o) throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean existVersionedConcrete(T1 o, ValidationResult vr)
			throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public List<StoreInfo> getStoresVersionedConcrete() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected boolean noExistVersionedConcrete(T1 o, ValidationResult vr)
			throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected boolean updateVersionedConcrete(T1 updated, T1 old)
			throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean isSupport(Object o) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	protected T2 chainversionup(ByteIterable bi) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
