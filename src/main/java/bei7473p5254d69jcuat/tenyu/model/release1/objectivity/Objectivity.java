package bei7473p5254d69jcuat.tenyu.model.release1.objectivity;

import java.io.*;
import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.right.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.game.item.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.HashStore.*;
import bei7473p5254d69jcuat.tenyu.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.role.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.Integrity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.usermessagelist.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.vote.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.core.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.role.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.vote.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.other.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import glb.*;
import glb.Glb.*;
import glb.util.*;
import glb.util.Util.*;
import jetbrains.exodus.env.*;

/**
 * 客観。統一値を網羅的に管理するクラス。
 *
 * 統一値全体は巨大すぎる事、トランザクション処理が必要な事から、
 * 各部はストアクラスを通じてDBのみで管理されオンメモリにならない。
 * つまり、このクラスは永続化される動的な内部状態を一切持たない。
 *
 * このクラスを通して取得できるストアクラスで統一値はほとんど網羅される。
 * 統一値という言葉の意味する範囲を客観かつモデル系のデータとすればすべて網羅できる。
 * それ以外は、ソフトウェアが予め持つ設定値のみである。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Objectivity implements GlbMemberDynamicState, DBObj {

	/**
	 * 1トランザクションで何件書き込むか
	 */
	public static final int maxWrite = 1000 * 30;

	/**
	 * 全ノードで一致する現在日時のミリ秒表現
	 */
	private transient long globalCurrentTime = Glb.getUtil()
			.getEpochMilliIgnoreSeconds(0, globalCurrentTimeInterval);
	/**
	 * ２分に１回更新される事を前提としている。
	 * それより早いペースを期待する事はできない。
	 */
	private static int globalCurrentTimeInterval = 60;

	public long getGlobalCurrentTime() {
		return globalCurrentTime;
	}

	/**
	 * {@link Objectivity#getGlobalCurrentTime()}の返値を更新する。
	 * このメソッドを呼ばないと更新されない。
	 * @return 最新の{@link Objectivity#getGlobalCurrentTime()}の返値
	 */
	public long updateGlobalCurrentTime() {
		this.globalCurrentTime = Glb.getUtil().getEpochMilliIgnoreSeconds(0,
				globalCurrentTimeInterval);
		return getGlobalCurrentTime();
	}

	public Environment getEnv() {
		return Glb.getDb(Glb.getFile().getObjectivityDBDir());
	}

	@Override
	public void start() {
		GlbMemberDynamicState.super.start();
		//客観コアは非オンメモリのシングルストアという点で唯一
		//ストア初期化のため書き込みトランザクション
		writeTryW(txn -> {
			ObjectivityCoreStore s = new ObjectivityCoreStore(txn);
			if (s.get(ModelI.getFirstId()) == null) {
				ObjectivityCore init = new ObjectivityCore();
				//ObjectivityCoreはSocialityと対応づけられWalletがある
				//Walletの検証処理でCoreに依存しているので
				//そこで再度newされる。つまりCoreのSave処理の中でCoreが必要になったら
				//getCoreを通じていちいち作成される。
				//一度作成されれば次からはDBから読み取られたものが使われるのでnewされない。
				//客観コアのセーブでは常に1つ前の客観コアの状態によって検証処理が行われる。
				//それ自体は仕様上問題無いように思う。
				s.save(init);
				return true;
			}
			return false;
		});
	}

	public long applyMessageList(UserMessageList l) {
		long nextHistoryIndex = Glb.getObje().getCore().getHistoryIndex() + 1;
		return applyMessageList(l, nextHistoryIndex);
	}

	/**
	 * 他モジュールから登録された反映処理を実行する。
	 * １件でも失敗すれば全件失敗する。
	 * @return	反映された処理一覧
	 */
	public List<ObjectivityUpdateDataElement> applyOtherModuleProc(
			List<ObjectivityUpdateDataElement> procFromOtherModules,
			long nextHistoryIndex) {
		List<ObjectivityUpdateDataElement> r = new ArrayList<>();
		compute(txn -> {
			try {
				long count = 0;
				for (ObjectivityUpdateDataElement e : procFromOtherModules) {
					if (e.apply(txn, nextHistoryIndex)) {
						r.add(e);
						count++;
					}
				}
				return count;
			} catch (Exception e) {
				txn.abort();
				r.clear();
				return r;
			}
		});
		return r;
	}

	/**
	 * メッセージを客観に反映する。内部で検証は行われない。
	 * 1メッセージでも例外を投げれば全件失敗、
	 * falseを返すだけなら他のメッセージは反映続行。
	 * 全件成功した場合、ヒストリーインデックスが1加算される。
	 * @return 処理されたサイズ
	 */
	public synchronized long applyMessageList(UserMessageList l,
			long nextHistoryIndex) {
		Glb.getLogger().info("called");
		if (l == null || l.getMessages() == null || l.getMessages().size() == 0)
			return 0L;
		return Glb.getObje().compute(txn -> {
			try {
				//メッセージリストの最大処理サイズ
				long applySizeMax = Glb.getObje().getCore().getConfig()
						.getLoadSetting().getUserMessageListApplySizeMax();
				//これまで処理したサイズ
				long size = 0;
				for (Message m : l.getMessages()) {
					if (size >= applySizeMax)
						break;
					//				if (!m.validateAndSetup())
					//					continue;
					if (!(m.getContent() instanceof UserRightRequest))
						continue;
					UserRightRequest c = (UserRightRequest) m.getContent();
					//１メッセージでも例外を出せばそのメッセージリスト全体が否定される
					if (c.apply(txn, nextHistoryIndex)) {
						size += c.getApplySize();
					} else {
						Glb.getLogger().warn("Failed to apply "
								+ c.getClass().getSimpleName() + " " + c);
					}
				}

				//メッセージリストのハッシュ値を書き込む
				UserMessageListHash historyStep = new UserMessageListHash(l,
						nextHistoryIndex);
				UserMessageListHashStore history = new UserMessageListHashStore(
						txn);
				if (history.create(historyStep) == null) {
					//トランザクションを破棄する
					throw new Exception(
							"Failed to create UserMessageListHash in history log ");
				}

				//客観コアを検証し、失敗すれば全トランザクションを破棄
				ValidationResult vr = new ValidationResult();
				if (!Glb.getObje().getCore().validateAtUpdate(vr)) {
					throw new IllegalStateException(
							"Failed to validate ObjectivityCore "
									+ vr.toString());
				}
				return size;
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				txn.abort();
				return 0L;
			}
		});
	}

	/**
	 * 非同期
	 * ヒストリーインデックスを1進め、
	 * ヒストリーインデックスが進んだ時にしなければならない処理を非同期に実行する。
	 * @return	新しいヒストリーインデックス
	 */
	public void fireHistoryIndexIncremented(long historyIndex) {
		Glb.debug("fireHistoryIndexIncremented called");

		//非同期処理
		Glb.getExecutor().execute(() -> {
			//この処理は失敗したとしても致命的ではない。客観の一部ではないので
			execute(txn -> {
				boolean r = CatchUpUpdatedIDListStore
						.commitUpdated(historyIndex, txn);
				Glb.debug("CatchUpUpdatedIDListStore.commitUpdated " + r);
				CatchUpUpdatedIDListStore.clearOldRecordAll(txn);
			});

			Glb.getMiddle().startRoleServers();
		});
	}

	/**
	 * 客観にオブジェクトがあれば更新、無ければ作成。
	 * 一部が失敗しても処理が続行され、可能な限り反映される。
	 *
	 * 同調処理から使われているが、同調処理は本来の処理の流れを無視して
	 * DBを同調させるので、参照系の検証ができない。
	 * 本来の処理の流れなら「データAがある場合のみデータBが存在する」
	 * という前提が成立するとしても、
	 * 同調処理ではデータAが無いのにデータBを書き込む場合がある。
	 *
	 * この問題は、レアケースではあるがdbValidate等でも発生する可能性がある。
	 * つまり現在のコードでもまれに発生する可能性があり、
	 * 取得したオブジェクトを書き込めない可能性がある。
	 * しかしそれは書き込む順番の問題なので、何度か同調処理が行われる中で解決されるはず。
	 * データA→Bの順番で書き込む必要があるとき、B、Aの順番で書き込みを試行すると、
	 * Bが失敗しAが成功し、次のBの書き込みは成功するようになる。
	 *
	 * @param objs
	 */
	public synchronized void applySparseObjectList(
			StoreNameObjectivity storeName, List<? extends ModelI> objs) {
		getEnv().executeInTransaction((txn) -> {
			try {
				@SuppressWarnings("unchecked")
				ModelStore<ModelI,
						?> s = (ModelStore<ModelI, ?>) storeName.getStore(txn);
				for (ModelI o : objs) {
					try {
						s.catchUp(o);
					} catch (Exception e) {
						Glb.getLogger().error("", e);
					}
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		});
	}

	/**
	 * 指定したオブジェクトを削除する
	 * @param removeIds	削除するID一覧
	 * @param storeName		対象ストア
	 */
	public synchronized void delete(HashSet<Long> removeIds,
			StoreNameObjectivity storeName) {
		if (removeIds == null || removeIds.size() == 0)
			return;
		getEnv().executeInTransaction(txn -> {
			for (Long remove : removeIds) {
				ModelStore<?, ?> s = storeName.getStore(txn);
				try {
					s.delete(remove);
				} catch (Exception e) {
					Glb.getLogger().error("", e);
				}
			}
		});
	}

	/**
	 * @param removeHids	削除されるHID一覧
	 * @param storeName		対象ストア
	 */
	public synchronized void deleteByHids(HashSet<Long> removeHids,
			StoreNameObjectivity storeName) {
		if (removeHids == null || removeHids.size() == 0)
			return;
		getEnv().executeInTransaction(txn -> {
			for (Long hid : removeHids) {
				ModelStore<?, ?> s = storeName.getStore(txn);
				try {
					Long id = s.getIdByHid(hid);
					if (id == null)
						continue;
					s.delete(id);
				} catch (Exception e) {
					Glb.getLogger().error("", e);
				}
			}
		});
	}

	public ObjectivityCore getCore() {
		return readRet(txn -> getCore(txn));
	}

	/**
	 * @param txn
	 * @return
	 */
	public ObjectivityCore getCore(Transaction txn) {
		ObjectivityCoreStore s = new ObjectivityCoreStore(txn);
		return getCore(s, txn);
	}

	public ObjectivityCore getCore(ObjectivityCoreStore s, Transaction txn) {
		try {
			ObjectivityCore r = s.get(s.getDefaultId());
			if (r == null) {
				r = new ObjectivityCore();
			}
			return r;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	public Boolean updateCore(Transaction txn, Consumer<ObjectivityCore> f) {
		try {
			ObjectivityCoreStore s = new ObjectivityCoreStore(txn);
			ObjectivityCore c = s.get(s.getDefaultId());
			if (c == null) {
				c = new ObjectivityCore();
			}
			f.accept(c);
			return s.save(c);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	/**
	 * 客観コアをセーブする
	 * @param core	セーブ対象
	 * @return	セーブに成功したか
	 */
	public boolean save(ObjectivityCore core) {
		return compute(txn -> new ObjectivityCoreStore(txn).save(core));
	}

	/**
	 * 読み取り専用ストア簡易アクセス
	 * むかしgetSimple()等のstaticインターフェースをストアに用意していたがそれを代替するもの。
	 * トランザクションを閉じることを保証するため、
	 * UserStoreを返せないのでラムダを使う。
	 *
	 * @return
	 */
	public <T> T getUser(Function<UserStore, T> f) {
		return readTryW(txn -> f.apply(new UserStore(txn)));
	}

	public <T> T getCertification(Function<CertificationStore, T> f) {
		return readTryW(txn -> f.apply(new CertificationStore(txn)));
	}

	public <T> T getModelCondition(Function<ModelConditionStore, T> f) {
		return readTryW(txn -> f.apply(new ModelConditionStore(txn)));
	}

	public <T> T getWeb(Function<WebStore, T> f) {
		return readTryW(txn -> f.apply(new WebStore(txn)));
	}

	public <T> T getRole(Function<RoleStore, T> f) {
		return readTryW(txn -> f.apply(new RoleStore(txn)));
	}

	public <T> T getStaticGame(Function<StaticGameStore, T> f) {
		return readTryW(txn -> f.apply(new StaticGameStore(txn)));
	}

	public <T> T getRatingGame(Function<RatingGameStore, T> f) {
		return readTryW(txn -> f.apply(new RatingGameStore(txn)));
	}

	public <T> T getRatingGameMatchStore(Function<RatingGameMatchStore, T> f) {
		return readTryW(txn -> f.apply(new RatingGameMatchStore(txn)));
	}

	public <T> T getRatingGameStateByUser(
			Function<RatingGameStateByUserStore, T> f) {
		return readTryW(txn -> f.apply(new RatingGameStateByUserStore(txn)));
	}

	public <T> T getStaticGameStateByUser(
			Function<StaticGameStateByUserStore, T> f) {
		return readTryW(txn -> f.apply(new StaticGameStateByUserStore(txn)));
	}

	public <T> T getStaticGameEquipmentClass(
			Function<StaticGameEquipmentClassStore, T> f) {
		return readTryW(txn -> f.apply(new StaticGameEquipmentClassStore(txn)));
	}

	public <T> T getRatingGameEquipmentClass(
			Function<RatingGameEquipmentClassStore, T> f) {
		return readTryW(txn -> f.apply(new RatingGameEquipmentClassStore(txn)));
	}

	public <T> T getStaticGameMaterialClass(
			Function<StaticGameMaterialClassStore, T> f) {
		return readTryW(txn -> f.apply(new StaticGameMaterialClassStore(txn)));
	}

	public <T> T getFlowNetworkAbstractNominal(
			Function<FlowNetworkAbstractNominalStore, T> f) {
		return readTryW(
				txn -> f.apply(new FlowNetworkAbstractNominalStore(txn)));
	}

	public <T> T getSociality(Function<SocialityStore, T> f) {
		return readTryW(txn -> f.apply(new SocialityStore(txn)));
	}

	public <T> T getSocialityIncomeSharing(
			Function<SocialityIncomeSharingStore, T> f) {
		return readTryW(txn -> f.apply(new SocialityIncomeSharingStore(txn)));
	}

	public <T> T getUserMessageListHash(
			Function<UserMessageListHashStore, T> f) {
		return readTryW(txn -> f.apply(new UserMessageListHashStore(txn)));
	}

	public <T> T getFreeKVPair(Function<FreeKVPairStore, T> f) {
		return readTryW(txn -> f.apply(new FreeKVPairStore(txn)));
	}

	public <T> T getDistributedTradable(
			Function<DistributedTradableStore, T> f) {
		return readTryW(txn -> f.apply(new DistributedTradableStore(txn)));
	}

	public <T> T getAgenda(Function<AgendaStore, T> f) {
		return readTryW(txn -> f.apply(new AgendaStore(txn)));
	}

	public <T> T getDistributedVote(Function<DistributedVoteStore, T> f) {
		return readTryW(txn -> f.apply(new DistributedVoteStore(txn)));
	}

	public <T> T getTenyuRepository(Function<TenyuRepositoryStore, T> f) {
		return readTryW(txn -> f.apply(new TenyuRepositoryStore(txn)));
	}

	public <T> T getTenyuArtifact(Function<TenyuArtifactStore, T> f) {
		return readTryW(txn -> f.apply(new TenyuArtifactStore(txn)));
	}

	public <T> T getTenyuArtifactByVersion(
			Function<TenyuArtifactByVersionStore, T> f) {
		return readTryW(txn -> f.apply(new TenyuArtifactByVersionStore(txn)));
	}

	/**
	 * @return	現在の自分の客観の整合性情報
	 */
	public Integrity getIntegrity() {
		Integrity r = new Integrity();

		ObjectivityCore core = getCore();
		r.setHistoryIndex(core.getHistoryIndex());

		//客観コアハッシュ値
		r.setCoreHash(core.hash());

		//ストア毎の整合性情報を設定
		for (StoreNameObjectivity storeName : StoreNameObjectivity.values()) {
			IntegrityByStore byStore = readRet(txn -> {
				//tmpを外側に出してbyStoreに一元化するとたまに例外が出る
				IntegrityByStore tmp = new IntegrityByStore();
				ModelStore<?, ?> s = storeName.getStore(txn);
				HashStore hs = s.getHashStore();
				RecycleHidStore rs = s.getRecycleHidStore();

				//リサイクルHIDの件数
				long count = rs.count();
				tmp.setRecycleHidCount(count);

				//リサイクルHIDリスト
				long loop = (count / RecycleHidStore.unitIDList)
						+ (count % RecycleHidStore.unitIDList > 0 ? 1 : 0);
				for (int i = 0; i < loop; i++) {
					List<IDList> l = rs.getIDList(i);
					if (l == null) {
						Glb.getLogger().error("", new Exception(
								"invalid loop or bug of getIDList()"));
						break;
					}
					byte[] h = HashStore.hash(l);
					if (h == null) {
						Glb.getLogger().error("",
								new Exception("hash is null"));
						break;
					}
					tmp.getRecycleHidListHash().add(i, new ByteArrayWrapper(h));
				}

				//最後のID
				Long lastIdOfHashStore = hs.getLastHidOfHashStore();
				if (lastIdOfHashStore == null) {
					Glb.getLogger()
							.warn("lastId is null. bug or just no record");
				}
				tmp.setLastHidOfHashStore(lastIdOfHashStore);

				//最上位ハッシュ配列
				HashStoreRecordPositioned top = hs.getTopHash();
				if (top == null) {
					Glb.getLogger().warn("top is null. bug or just no record");
				}
				tmp.setTop(top);
				return tmp;
			});

			Glb.debug(storeName + " lastIdOfHashStore="
					+ byStore.getLastHidOfHashStore());

			//登録
			r.getByStore().put(storeName, byStore);
		}

		return r;
	}

	/**
	 * ネットワークが勃興するために必要な客観の初期値のセットアップ
	 * 初回起動時だけでなく、毎回起動直後呼ばれるので、
	 * 客観を破壊しないように作る必要がある。
	 */
	public void initForRise() {
		writeAuthorToDB();
		setupTenyuManager();
		setupRole();
	}

	private void setupTenyuManager() {
		writeTryW(txn -> {
			ObjectivityCoreStore s = new ObjectivityCoreStore(txn);
			ObjectivityCore core = Glb.getObje().getCore(s, txn);
			//作者が最初の全体運営者になる。全体運営者無しでは本構想を開始できない
			boolean exist = false;
			User author = Glb.getConst().getAuthor();

			Long authorId = author.getId();
			for (TenyuManager m : core.getManagerList().getManagers()) {
				if (m.getUserId().equals(authorId))
					exist = true;
			}
			if (!exist) {
				TenyuManager authorM = new TenyuManager();
				authorM.setUserId(authorId);
				//最初0.6で設定されてもその後弱まる可能性はある
				authorM.setPower(0.6D);
				core.getManagerList().add(authorM);
			}
			return s.save(core);
		});
	}

	/**
	 * 開発者のユーザー情報をDBに登録
	 */
	public void writeAuthorToDB() {
		//作者
		User author = Glb.getConst().getAuthor();
		ValidationResult vr = new ValidationResult();
		author.validateAtCreate(vr);
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			Glb.getLogger().error("作者ユーザーの設定値が不正");
		}

		//作者IDであるべきIDでDBからロードされたUser
		User loaded = Glb.getObje().getUser(us -> us.get(author.getId()));

		//作者をDBに書き込むか
		boolean write = false;

		if (loaded == null) {
			//初回起動時ここに来る
			write = true;
		} else if (loaded != null && !loaded.equals(author)) {
			//異常ケース	ユーザーDBをクリアして作者情報を書き込む
			Glb.getLogger().error("作者IDで読み出されたUserが作者ではない");
			// バグでここに来る事が恐ろしいのでコメントアウト。検証処理でバグが生じたら来てしまう
			/*
			Environment e = Glb.getDb(Glb.getFile().getObjectivityDBPath());
			Transaction txn = e.beginTransaction();
			for (StoreInfo s : UserStore.getStores()) {
				e.removeStore(s.getStoreName(), txn);
			}
			txn.commit();
			write = true;
			*/
		}

		//作者をDBに書き込む
		/*
		 * 作者のユーザー情報は、初回起動時にDBに書き込まれるが、最新ノードにおいて更新されている可能性がある。
		 * その特殊さはどんな問題になるか？
		 * 不整合が生じるのは非最新ノードにおいてのみ。
		 * そのようなノードは他にも様々な不整合を抱えている。
		 * 作者ノードに関して存在する特殊さは、定数として記録されていて、異なる手段で書き込まれる事であるが、
		 * 最新値が上書きされる事は無いので、基本的に問題は生じないように思う。
		 * ＞一応、作者ノードが頻繁に更新されると、オフラインノードのDB状況も考慮すると、リスクが高まる。
		 * ＞作者ノードの更新頻度を抑える必要性について留意する必要がある。
		 */
		if (write) {
			writeTryW(txn -> {
				if (!User.createSequence(txn, author, true,
						Glb.getObje().getCore().getHistoryIndex()))
					throw new Exception("UserRegistration.register()で失敗");
				return txn.commit();
			});

			//確認
			loaded = Glb.getObje().getUser(us -> us.get(author.getId()));
			if (loaded == null || !loaded.equals(author)) {
				Glb.debug(new Exception("作者のユーザーIDでロードされたユーザーがnullまたは作者じゃない"));
				System.exit(1);
			}
		}
	}

	public boolean writeSystemVoteToDB() {
		//システム用分散合意がストアに無ければ書き込む
		//システム用分散合意は全体運営者投票等
		Long electionId = DistributedVoteManager.managerElectionId;
		return writeTryW(txn -> {
			DistributedVoteStore s = new DistributedVoteStore(txn);
			//全体運営者選出投票について書き込む必要があるか
			boolean writeZero = false;
			//electionIdで取得される投票オブジェクトは、全体運営者投票でなければならないし、
			//システムのどの時点でも必ず存在しなければならない。
			DistributedVote zero = s.get(electionId);
			String managerVoteName = Glb.getConst()
					.getDistributedVoteManagerVoteName();
			if (zero == null) {
				writeZero = true;
			} else {
				if (!managerVoteName.equals(zero.getName())) {
					if (!s.delete(electionId)) {
						Glb.getLogger().error(
								"Failed to delete id=0 DistributedVote",
								new IOException());
						return false;
					}
					writeZero = true;
				}
			}
			if (writeZero) {
				//書き込む
				DistributedVote managerVote = new DistributedVote();
				managerVote.setRegistererUserId(ModelI.getSystemId());
				managerVote.setMainAdministratorUserId(ModelI.getSystemId());
				managerVote.setId(electionId);
				//HIDも0(electionId)想定でいい
				managerVote.setHid(electionId);
				managerVote.setName(managerVoteName);
				managerVote.setSystem(true);
				managerVote.setEnable(true);
				//managerVote.setName("全体運営者選出投票TenyuManagerElection");
				//managerVote.setId(electionId);
				managerVote.setSchedule("0 0 */8 * * ?");
				managerVote.setSustainable(true);
				managerVote.setExplanation(
						"全体運営者を選出する投票。Vote to elect an TenyuManager.");
				User author = Glb.getConst().getAuthor();
				DistributedVoteChoice authorC = new DistributedVoteChoice();
				authorC.setName(author.getName());
				authorC.setOptionLong(author.getId());
				managerVote.getChoices().add(authorC);

				if (s.createSpecifiedId(managerVote) == null) {
					Glb.getLogger().error(
							"Failed to write System DistributedVote",
							new Exception());
				}
			}
			return txn.commit();
		});
	}

	public void setupRole() {
		//各種権限をDBに書き込む
		execute(txn -> {
			try {
				RoleStore rs = new RoleStore(txn);
				for (String roleName : RoleI.roleInitialNames.getNames()) {
					Role exist = rs.getByName(roleName);
					if (exist == null) {
						Role r = new Role();
						r.setName(roleName);
						r.setExplanation(roleName + " Administrators");
						r.setRegistererUserId(ModelI.getSystemId());
						r.setMainAdministratorUserId(ModelI.getVoteId());
						r.setLocale(Locale.ENGLISH);
						Long createdId = rs.create(r);
						if (createdId == null) {
							Glb.getLogger().error("Failed to create role",
									new Exception());
						}
					}
				}

				//メッセージ受付サーバが０か
				//この条件で判定すると、作者がメッセージ受付サーバを辞めれる可能性がある
				Role r = rs
						.getByName(UserMessageListServer.class.getSimpleName());
				if (r == null) {
					Glb.getLogger().error(
							"No role object of UserMessageListServer",
							new Exception());
				} else {
					if (r.getAdminUserIds().size() == 0) {
						//作者が最初のメッセージ受付サーバになる。
						r.addAdmin(Glb.getConst().getAuthor().getId());
						rs.update(r);
					}
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		});
	}

}
