package bei7473p5254d69jcuat.tenyu.db.store.satellite;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.*;
import glb.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * キーはGUIの完全修飾名
 * 値はLinkedHashMap
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class CookieStore extends ByNodeStore<String, Cookie> {
	private String className;
	private String userName;

	public CookieStore(String className,  Transaction txn) {
		super(txn);
		this.className = className;
		this.userName = Glb.getMiddle().getMe().getName();
	}

	@Override
	protected Cookie chainversionup(ByteIterable bi) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected String cnvKey(ByteIterable bi) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected ByteIterable cnvKey(String key) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public StoreInfo getMainStoreInfo() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public String getName() {
		return className + "_" + userName + "_"
				+ CookieStore.class.getSimpleName();
	}

	@Override
	public List<StoreInfo> getStoresObjectStoreConcrete() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
