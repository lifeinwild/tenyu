package bei7473p5254d69jcuat.tenyu.release1.db.store.single;

import java.security.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import jetbrains.exodus.env.*;

/**
 * インスタンスが1個しかないクラスについて保存する。
 * 大抵、Glbのstaticメンバーの一部がこのタイプのストアを利用する。
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <T>
 */
public abstract class SingleObjectStore<T extends IdObjectDBI>
		extends IdObjectStore<T, T> {

	protected SingleObjectStore(Transaction txn)
			throws NoSuchAlgorithmException {
		super(txn);
	}

	public T get() throws Exception {
		return super.get(IdObjectDBI.getFirstRecycleId());
	}

	@Override
	public final List<StoreInfo> getStoresIdObjectConcrete() {
		//サブインデックス等が無い前提
		//もしあればSingleObjectConcrete系メソッドを定義する
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(getMainStoreInfo());
		return r;
	}

	@Override
	protected boolean createIdObjectConcrete(T o) throws Exception {
		return true;
	}

	@Override
	protected boolean updateIdObjectConcrete(T updated, T old) throws Exception {
		return true;
	}

	@Override
	protected boolean deleteIdObjectConcrete(T o) throws Exception {
		return true;
	}

	@Override
	public boolean noExistIdObjectConcrete(T o, ValidationResult vr) throws Exception {
		return true;
	}

	@Override
	public boolean existIdObjectConcrete(T o, ValidationResult vr) throws Exception {
		return true;
	}

	/**
	 * TODO 恐らくこのメソッドは不要。getでいい
	 * @return
	 */
	/*
	public T load() {
		try {
			//loadもsaveも警告が出ているが、消す方法が見つからない
			//動的にクラスを指定していて、総称型等を使っても消えてくれない
			//子クラスを通じて呼び出され、子クラスのコンストラクタが呼ばれる
			SingleObjectStore<
					T> s = this.getClass().getConstructor(Transaction.class)
							.newInstance(util.getTxn());
			return s.get();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		} finally {
		}
	}
	*/

	public boolean save(T o) {
		try {
//			SingleObjectStore<
//					T> s = this.getClass().getConstructor(Transaction.class)
//							.newInstance(util.getTxn());

			o.setRecycleId(IdObjectDBI.getFirstRecycleId());

			if (get() == null) {
				if (createSpecifiedId(o) == null)
					throw new Exception("Failed to createSpecifiedId");
			} else {
				if (!update(o))
					throw new Exception("Failed to update");
			}
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		} finally {

		}
	}

	@Override
	protected boolean dbValidateAtUpdateIdObjectConcrete(T updated, T old,
			ValidationResult r) {
		return true;
	}
}
