package bei7473p5254d69jcuat.tenyu.model.release1;

import java.net.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.neighbor.*;
import glb.*;
import glb.Glb.*;
import glb.util.*;
import glb.util.Util.*;
import jetbrains.exodus.env.*;

/**
* P2P通信にストレージ消費攻撃の防止策などを施す。
*
* メモ
* ・DDoS対策
* アプリケーションレベルでは対策不可能だと思った。
* ISPが何か対策を用意すべきか。
* アドレスAがアドレスBからの通信を届けないでほしいとISPに通知したら、
* ISP側でブロックする。
* さらに他のISPにも伝達されてできるだけその通信の出元でブロックするとか。
* ブロックは時間制限できるようにする。
* しかし、アドレスとユーザーの対応関係はたまに変化するから難しいか。
*
* @author exceptiontenyu@gmail.com
*
*/
public class P2PDefense extends Model
		implements GlbMemberDynamicState, P2PDefenseI {

	/**
	 * 受信側ノードの処理性能を送信側1ノードが最大どれくらい消費して良いか
	 */
	private static final double rate = 0.25;
	/**
	 * 蓄積された情報をリセットする周期
	 */
	private static final long resetPeriod = 1000L * 60 * 5;
	/**
	 * 1分当たりに1ノードが受信させる事ができるサイズ
	 */
	private static final int countPerMinute = (int) (100 * rate);
	private static final long resetPeriodMinute = resetPeriod / (1000L * 60);
	/**
	 * 一定時間あたり１IPアドレスあたりの受信可能な最大メッセージ件数
	 */
	private static final int countMax = (int) (countPerMinute
			* resetPeriodMinute);

	/**
	 * dupCheckの最大件数
	 * dupCheckをThroughputLimitというクラスでやる事にしたので
	 * 最大件数という概念が無くなった。
	 */
	//private static final int dupCheckMax = 1000 * 500;

	/**
	 * 1分間当たりのSimpleMessageの最大回数
	 */
	private static final int simpleMessageCountMax = (int) (3
			* resetPeriodMinute);

	/**
	 * 1分当たりに１ノードが受信させる事ができる件数
	 */
	private static final long sizePerMinute = (long) (2500L * 1000 * rate);

	/**
	 * 一定時間あたり１IPアドレスあたりの受信可能な最大サイズ
	 */
	private static final long sizeMax = (int) (sizePerMinute
			* resetPeriodMinute);

	/**
	 * ユーザーメッセージの一定時間ごとの最大サイズ。
	 */
	private static final long sizeMaxUser = sizeMax;

	public static P2PDefense loadOrCreate() {
		return Glb.getDb(Glb.getFile().getDefenseDBDir())
				.computeInTransaction((txn) -> {
					P2PDefense r = null;
					try {
						P2PDefenseStore s = new P2PDefenseStore(txn);
						r = s.get(s.getDefaultId());
					} catch (Exception e) {
						Glb.getLogger().error("", e);
					}
					return r == null ? new P2PDefense() : r;
				});
	}

	public P2PDefense() {
		SingleObjectStoreI.setup(this);
	}

	/**
	 * 重複判定用
	 * メッセージと対応づくバイト配列：受信した時点のヒストリーインデックス
	 */
	private ThroughputLimit<
			Message> dupCheck = new ThroughputLimit<>(resetPeriod, 1L);

	/**
	 * IPアドレスごとの登録件数
	 * これはメモリ可視性にシビアではない
	 * 僅かにオーバーする程度なら問題無いので
	 */
	private ThroughputLimit<
			ByteArrayWrapper> isaToCount = new ThroughputLimit<>(resetPeriod,
					countMax);

	/**
	 * IPアドレスごとのサイズ計測
	 */
	private ThroughputLimit<ByteArrayWrapper> isaToSize = new ThroughputLimit<>(
			resetPeriod, sizeMax);

	/**
	 * P2PEdgeId : count
	 * SimpleMessageの時間当たり回数制限のため
	 */
	private ThroughputLimit<
			Long> p2pEdgeToCountSimpleMessage = new ThroughputLimit<>(
					resetPeriod, simpleMessageCountMax);
	/**
	 * UserId : Size
	 * ユーザーごとのサイズ制限
	 */
	private ThroughputLimit<Long> userIdToSize = new ThroughputLimit<>(
			resetPeriod, sizeMaxUser);

	/**
	 * userId : count
	 * SimpleMessageの時間当たり回数制限のため
	 */
	private ThroughputLimit<
			Long> userToCountSimpleMessage = new ThroughputLimit<>(resetPeriod,
					simpleMessageCountMax);

	/**
	 * 同じメッセージを重複して受信しないようにする。
	 *
	 * @param c		重複判定対象
	 * @return		重複したメッセージ、または重複判定用マップの件数が最大に達して判定不可
	 */
	public boolean isDup(Message m) {
		synchronized (dupCheck) {
			/*
			 * この方法はダメだった。
			 * kryoのシリアライズはデシリアライズに影響しない無駄なデータを付加できるので
			 * ハッシュ値を簡単に変化させられる。その場合復元後オブジェクトに影響しないので、
			 * 署名にも影響しない。
			 *
			//最も内側の梱包のcontentBinaryのハッシュ値で重複判定をする。
			//メッセージを複製して無駄に流し込む攻撃を防げる。
			//メッセージの一部を改ざんしてハッシュ値を変化させて流し込む攻撃も
			//多くの場合このcontentBinaryは何らかの認証が必要なデータだから防げる。
			//唯一防げないのはあらゆる認証が無い認識の前の段階だが、
			//それは仮近傍一覧というアイデアによって不正が防止されている。
			byte[] contentBinary = m.getInnermostPack().getContentBinary();
			MessageDigest md = Glb.getUtil().getMD();
			ByteArrayWrapper contentBinaryHash = new ByteArrayWrapper(
					md.digest(contentBinary));
			return dupCheck.isOverCount(contentBinaryHash);
			*/

			//equals,hashcodeに頼って重複チェックを行う
			return dupCheck.isOverCount(m);
		}
	}

	/**
	 * @param isa
	 * @return	この送信アドレスによるメッセージ件数は許容範囲内か、あるいは不正な引数ならtrue
	 */
	public boolean isOverCount(InetSocketAddress isa) {
		if (isa == null || isa.getAddress() == null
				|| isa.getAddress().getAddress() == null)
			return true;
		ByteArrayWrapper addr = new ByteArrayWrapper(
				isa.getAddress().getAddress());

		return isaToCount.isOverCount(addr);
	}

	public boolean isOverCountSimpleMessage(Long userId) {
		if (userId == null)
			return true;
		return userToCountSimpleMessage.isOverCount(userId);
	}

	public boolean isOverCountSimpleMessage(P2PEdge e) {
		if (e == null)
			return true;
		return p2pEdgeToCountSimpleMessage.isOverCount(e.getEdgeId());
	}

	public boolean isOverSize(InetSocketAddress isa, long dataLength) {
		if (isa == null || isa.getAddress() == null
				|| isa.getAddress().getAddress() == null || dataLength < 0)
			return true;

		ByteArrayWrapper addr = new ByteArrayWrapper(
				isa.getAddress().getAddress());
		return isaToSize.isOverCount(addr, dataLength);
	}

	/**
	 * ユーザーごとのサイズ制限
	 * @param userId		送信者
	 * @param received		受信したデータ
	 * @return				サイズが許容範囲内か
	 */
	public boolean isOverSize(Long userId, Message received) {
		if (userId == null || received == null)
			return true;
		long size = received.getSize();
		if (size < 0)
			return true;

		//作者か
		boolean author = Glb.getConst().getAuthor().getId().equals(userId);

		//ゲームサーバーか
		boolean server = Glb.getObje().getCore()
				.isHeavyLoadServerToUserMessageListServer(userId);

		//一部の者は最大サイズが引き上げられる
		double multiplier = 1D;
		if (author || server) {
			multiplier = 25;
		}

		return userIdToSize.isOverCount(userId, size, multiplier);
	}

	public boolean save() {
		return Glb.getDb(Glb.getFile().getDefenseDBDir())
				.computeInTransaction((txn) -> {
					try {
						return new P2PDefenseStore(txn).save(this);
					} catch (Exception e) {
						Glb.getLogger().error("", e);
						return false;
					}
				});
	}

	public void start() {
	}

	public void stop() {
		save();
	}

	private final boolean validateAtCommonModelConcrete(ValidationResult r) {
		boolean b = true;
		boolean v = dupCheck != null && isaToCount != null
				&& userIdToSize != null && p2pEdgeToCountSimpleMessage != null
				&& userToCountSimpleMessage != null && isaToSize != null;
		if (!v) {
			r.add(Lang.P2PDEFENSE, Lang.ERROR_INVALID);
			b = false;
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateModelConcrete(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonModelConcrete(r))
			b = false;
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeModelConcrete(ValidationResult r,
			Object old) {
		return true;
	}

	@Override
	protected final boolean validateAtUpdateModelConcrete(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonModelConcrete(r))
			b = false;
		return b;
	}

	@Override
	public boolean validateReferenceModelConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		return true;
	}

	@Override
	public P2PDefenseGui getGuiReferenced(String guiName, String cssIdPrefix) {
		return new P2PDefenseGui(guiName, cssIdPrefix);
	}

	@Override
	public P2PDefenseStore getStore(Transaction txn) {
		return new P2PDefenseStore(txn);
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameSingle.P2P_DEFENSE;
	}

	@Override
	public TenyuReferenceModelSingle<P2PDefense> getReference() {
		return new TenyuReferenceModelSingle<>(StoreNameSingle.P2P_DEFENSE);
	}
}
