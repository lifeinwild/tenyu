package bei7473p5254d69jcuat.tenyutalk;

import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyutalk.db.*;
import bei7473p5254d69jcuat.tenyutalk.db.other.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * node毎に異なるインスタンスが必要になるので
 * 少し特殊な設計がある。
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

	private OnmemoryManagementMultiplayer multiplayer;

	/**
	 * @return	{@link MultiplayerObjectI}のオンメモリ管理
	 */
	public OnmemoryManagementMultiplayer getMultiplayerOnMemory() {
		return multiplayer;
	}

	/**
	 * @param f	ストアを使った処理
	 * @return	{@link MultiplayerObjectI}のストア
	 */
	public <T, LI extends CreativeObjectI, L extends LI> T getMultiplayer(
			Function<MultiplayerObjectStore<LI, L>, T> f) {
		return readTryW(txn -> f.apply(new MultiplayerObjectStore<LI, L>(txn)));
	}

	public Tenyutalk(NodeIdentifierUser node) {
		this.node = node;
	}

	@SuppressWarnings("unused")
	private Tenyutalk() {
	}

	/**
	 * {@link Tenyutalk}のインスタンスが{@link Tenyutalk}のインスタンスを作る。
	 * この特殊な設計は、システム全体でTenyutalkクラスの
	 * テストバージョンクラスを使用できるようにするために行っている。
	 *
	 * テストバージョンクラスはこのメソッドをオーバーライドする事で
	 * システム内の全Tenyutalkインスタンスをテスト用に変えれる。
	 *
	 * Tenyutalkインスタンスは{@link Glb#getTenyutalk(NodeIdentifierUser)}
	 * を通じて利用されるので、Glbの大本のインスタンスがテストバージョンクラスに変われば
	 * システム全体で変わる。
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

	public <T> T getComment(Function<CommentStore, T> f) {
		return readTryW(txn -> f.apply(new CommentStore(txn)));
	}

}
