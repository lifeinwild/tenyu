package bei7473p5254d69jcuat.tenyu.db.store;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public abstract class AbstractKVS<K, V> extends ObjectStore<K, V> {

	public AbstractKVS(Transaction txn) {
		super(txn);
	}

	public abstract StoreInfo getMainStoreInfo();

	@Override
	protected ByteIterable cnvKey(K bi) {
		try {
			return cnvO(bi);
		} catch (Exception e) {
			return null;
		}
	}

	/*
	public V get(K key) throws IOException {
		return getMain(cnvO(key));
	}
	*/

	public boolean delete(K key) throws IOException {
		return deleteDirect(cnvO(key));
	}

	public boolean update(K key, V val) throws IOException {
		return putDirect(cnvO(key), cnvO(val));
	}

	public boolean create(K key, V val) throws IOException {
		return putDirect(cnvO(key), cnvO(val));
	}

}
