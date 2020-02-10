package bei7473p5254d69jcuat.tenyutalk;

import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyutalk.db.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 使用時にいちいちインスタンス化する必要がある。
 * Glbに常駐オブジェクトとして置けない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Tenyutalk implements DBObj {
	/**
	 * tenyutalkのDBはノード毎に存在し、
	 * 同じストア名で異なる内容のストアがノード毎にあるのでノードを指定する必要がある。
	 */
	private NodeIdentifierUser node;

	private Tenyutalk(NodeIdentifierUser node) {
		this.node = node;
	}

	@SuppressWarnings("unused")
	private Tenyutalk() {
	}

	/**
	 * @return	{@link Tenyutalk#construct(NodeIdentifierUser)}を呼び出すためだけのインスタンス
	 */
	public static Tenyutalk constructForGlbInstance() {
		return new Tenyutalk();
	}

	/**
	 * Glbにインスタンスを常駐させるため。
	 * そうするとテスト用インスタンスをセットできる。
	 * このメソッドは常駐用インスタンスにおいてのみ呼び出され、
	 * 返値のインスタンスは実際のDBアクセスに使用される。
	 *
	 * @param node
	 * @return	実際に使用するインスタンス
	 */
	public Tenyutalk construct(NodeIdentifierUser node) {
		return new Tenyutalk(node);
	}

	public Environment getEnv() {
		return Glb.getDb(Glb.getFile()
				.getTenyutalkDBPath(node.getUser().getName()
						+ Glb.getConst().getFileSeparator()
						+ node.getNodeNumber()));
	}

	public <T> T getFolder(Function<TenyutalkFolderStore, T> f) {
		return readTryW(txn -> f.apply(new TenyutalkFolderStore(txn)));
	}

	public <T> T getFile(Function<TenyutalkFileStore, T> f) {
		return readTryW(txn -> f.apply(new TenyutalkFileStore(txn)));
	}

}
