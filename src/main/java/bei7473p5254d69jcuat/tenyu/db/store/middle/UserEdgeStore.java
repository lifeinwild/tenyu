package bei7473p5254d69jcuat.tenyu.db.store.middle;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.IdObjectStore.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import glb.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * {@link UserEdge}を保存するストア
 *
 * nodeNumberに対応しているので1ユーザーが多数のノードを立ち上げれる。
 * NodeIdentifierUserがKeyになっているが、実際byte[]がKeyになっている。
 * cnvKeyでその変換に対応している。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserEdgeStore
		extends SatelliteStore<NodeIdentifierUser, UserEdge> {
	/**
	 * userIdとnodeNumberでキーを作る。一意
	 * userIdだけで前方一致検索ができる。全nodeNumberの値が検索される。
	 */
	private final StoreInfo userIdNodeNumberToUserEdge = new StoreInfo(
			getNameStatic() + "_userIdNodeNumberToUserEdge");

	/**
	 * @param userId	このユーザーの全ノード番号のUserEdgeを取得する
	 * @return
	 */
	public static List<UserEdge> getSimplePrefix(Long userId) {
		return simple(s -> s.getValuesByUserId(userId));
	}

	public List<UserEdge> getValuesByUserId(Long userId) {
		return util.getValuesByKeyPrefix(userIdNodeNumberToUserEdge,
				cnvKey(new NodeIdentifierUser(userId)), bi -> {
					Object r = null;
					r = chainversionup(bi);
					if (!(r instanceof UserEdge))
						return null;
					return (UserEdge) r;
				}, 1000 * 100);
	}

	public UserEdge get(Long userId, int nodeNumber) {
		return get(new NodeIdentifierUser(userId, nodeNumber));
	}

	public final boolean create(Long userId, int nodeNumber, UserEdge e)
			throws Exception {
		return create(new NodeIdentifierUser(userId, nodeNumber), e);
	}

	public final boolean update(Long userId, int nodeNumber, UserEdge e)
			throws Exception {
		return update(new NodeIdentifierUser(userId, nodeNumber), e);
	}

	public final boolean delete(Long userId, int nodeNumber) throws Exception {
		return delete(new NodeIdentifierUser(userId, nodeNumber));
	}

	public static UserEdge getSimple(Long userId, int nodeNumber) {
		return simple((s) -> s.get(new NodeIdentifierUser(userId, nodeNumber)));
	}

	public static UserEdge getSimple(NodeIdentifierUser identifier) {
		return simple((s) -> s.get(identifier));
	}

	private static <R> R simple(Function<UserEdgeStore, R> f) {
		return simpleReadAccess((txn) -> f.apply(new UserEdgeStore(txn)));
	}

	protected static <R> R simpleReadAccess(StoreFunction<Transaction, R> f) {
		return simpleReadAccess(Glb.getFile().getMiddleDBPath(), f);
	}

	@Override
	protected ByteIterable cnvKey(NodeIdentifierUser key) {
		return cnvBA(key.getIdentifier());
	}

	public UserEdgeStore(Transaction txn) {
		super(txn);
	}

	@Override
	protected UserEdge chainversionup(ByteIterable bi) {
		try {
			return (UserEdge) cnvO(bi);
		} catch (IOException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected NodeIdentifierUser cnvKey(ByteIterable bi) {
		return new NodeIdentifierUser(cnvBA(bi));
	}

	@Override
	public StoreInfo getMainStoreInfo() {
		return userIdNodeNumberToUserEdge;
	}

	@Override
	public String getName() {
		return getNameStatic();
	}

	public String getNameStatic() {
		return AddrInfo.class.getSimpleName();//通信クラスの名前になっている
	}

	@Override
	public List<StoreInfo> getStoresObjectStoreConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

}
