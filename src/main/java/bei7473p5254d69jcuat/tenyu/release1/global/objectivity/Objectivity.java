package bei7473p5254d69jcuat.tenyu.release1.global.objectivity;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.right.*;
import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.HashStore.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.file.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.game.item.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.sociality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.Glb.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup.Integrity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.usermessagelist.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.vote.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.dtradable.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.vote.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.role.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;
import jetbrains.exodus.env.*;

/**
 * 客観。統一値を網羅的に管理するクラス。
 * 統一値全体は巨大すぎる事、トランザクション処理が必要な事から、
 * 各部はストアクラスを通じてDBのみで管理されオンメモリにならない。
 * つまり、本クラスは永続化される動的な内部状態を一切持たない。
 * 本クラスを通して取得できるストアクラスで統一値はほとんど網羅される。
 * それ以外は、ソフトウェアが予め持つ設定値のみである。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Objectivity implements GlbMemberDynamicState {
	/**
	 * 1トランザクションで何件書き込むか
	 */
	public static final int maxWrite = 1000 * 30;

	@Override
	public void start() {
		GlbMemberDynamicState.super.start();
		//客観コアは非オンメモリのシングルストアという点で唯一
		//ストア初期化のため書き込みトランザクション
		writeTryW(txn -> {
			ObjectivityCoreStore s = new ObjectivityCoreStore(txn);
			if (s.count() == 0) {
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

	/**
	 * 遅延実行を実行する
	 * @param drs	遅延実行ストア
	 * @param txn
	 * @param maxApplySize		処理件数
	 * @param nextHistoryIndex
	 * @return	最大件数まで処理したか、または
	 * 現在のヒストリーインデックスまでの全件を処理したらtrue
	 */
	public <T1 extends DelayRunDBI,
			T2 extends T1> List<DelayRunDBI> applyDelayRuns(
					DelayRunStore<T1, T2> drs, Transaction txn,
					long maxApplySize, long nextHistoryIndex) throws Exception {
		//処理した件数
		long count = 0;
		List<DelayRunDBI> r = new ArrayList<>();

		//DBに登録されている遅延実行のヒストリーインデックス一覧
		List<Long> his = drs.getAllHistoryIndex();
		//昇順
		Collections.sort(his);
		for (Long hi : his) {
			if (hi == null || hi > nextHistoryIndex)
				return r;
			//このヒストリーインデックスの全遅延実行IDを取得
			List<Long> ids = drs.getIdsByRunHistoryIndex(hi);
			for (Long id : ids) {
				//最大件数に到達したら終了
				if (count >= maxApplySize)
					return r;
				T1 proc = drs.get(id);
				if (proc == null) {
					Glb.getLogger()
							.error("Not found. id=" + id + " store="
									+ drs.getClass().getSimpleName(),
									new IllegalStateException());
					continue;
				}

				//削除に失敗する事は2回以上実行される潜在的可能性を意味するので削除が先
				if (!drs.delete(id)) {
					//失敗したら実行しない
					//削除に失敗するオブジェクトが増え過ぎると性能が悪化するので
					//早急に問題を究明しアップデートする必要があるだろう。
					Glb.getLogger().warn("",
							new Exception("Failed to delete proc. id=" + id
									+ " store="
									+ drs.getClass().getSimpleName()));
					continue;
				}
				count += proc.getApplySize();
				r.add(proc);

				//実行
				proc.run(txn);
			}
		}
		return r;
	}

	public long applyMessageList(UserMessageList l) {
		long nextHistoryIndex = Glb.getObje().getCore().getHistoryIndex() + 1;
		return applyMessageList(l, nextHistoryIndex);
	}

	/**
	 * 他モジュールから登録された反映処理を実行する。
	 * @return	反映された件数
	 */
	public long applyOtherModuleProc(
			List<ObjectivityUpdateDataElement> procFromOtherModules,
			long nextHistoryIndex) {
		return Glb.getObje().writeTryW(txn -> {
			long count = 0;
			for (ObjectivityUpdateDataElement e : procFromOtherModules) {
				if (e.apply(txn, nextHistoryIndex)) {
					count++;
				}
			}
			return count;
		});
	}

	/**
	 * メッセージを客観に反映する。内部で検証は行われない。
	 * 1メッセージでも例外を投げれば全件失敗、
	 * falseを返すだけなら他のメッセージは反映続行。
	 * 全件成功した場合、ヒストリーインデックスが1加算される。
	 * @return 反映された件数
	 */
	public synchronized long applyMessageList(UserMessageList l,
			long nextHistoryIndex) {
		Glb.getLogger().info("called");
		if (l == null || l.getMessages() == null)
			return 0;
		Transaction txn = null;
		long count = 0;
		try {
			txn = getEnv().beginTransaction();

			for (Message m : l.getMessages()) {
				//				if (!m.validateAndSetup())
				//					continue;
				if (!(m.getContent() instanceof UserRightRequest))
					continue;
				UserRightRequest c = (UserRightRequest) m.getContent();
				try {
					if (c.apply(txn, nextHistoryIndex)) {
						count++;
					} else {
						Glb.getLogger().warn("Failed to apply "
								+ c.getClass().getSimpleName() + " " + c);
					}
				} catch (Exception e) {
					throw e;
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
				Glb.getLogger().error(
						"Failed to validate ObjectivityCore " + vr.toString(),
						new Exception());
				return 0;
			}

			if (txn.commit()) {
				return count;
			} else {
				return 0;
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			txn.abort();
			return 0;
		} finally {
			if (txn != null && !txn.isFinished()) {
				txn.abort();
			}
		}
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
			Glb.getObje().execute(txn -> {
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
	public synchronized void applySparseObjectList(String storeName,
			List<? extends IdObjectDBI> objs) {
		getEnv().executeInTransaction((txn) -> {
			try {
				@SuppressWarnings("unchecked")
				IdObjectStore<IdObjectDBI, ?> s = (IdObjectStore<IdObjectDBI,
						?>) Glb.getObje().getStore(storeName, txn);
				for (IdObjectDBI o : objs) {
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
	public synchronized void delete(HashSet<Long> removeIds, String storeName) {
		if (removeIds == null || removeIds.size() == 0)
			return;
		getEnv().executeInTransaction(txn -> {
			for (Long remove : removeIds) {
				IdObjectStore<?, ?> s = Glb.getObje().getStore(storeName, txn);
				try {
					s.delete(remove);
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
		try {
			ObjectivityCoreStore s = new ObjectivityCoreStore(txn);
			ObjectivityCore r = s.get();
			if (r == null) {
				r = new ObjectivityCore();
			}
			return r;
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
		return compute(txn -> {
			try {
				return new ObjectivityCoreStore(txn).save(core);
			} catch (NoSuchAlgorithmException e) {
				Glb.getLogger().error("", e);
				return false;
			}
		});
	}

	public Environment getEnv() {
		return Glb.getDb(Glb.getFile().getObjectivityDBPath());
	}

	/**
	 * 客観DBに指定された処理を行う。
	 *
	 * @param dbProc	処理内容
	 * @return	処理内容によって規定される返値
	 */
	public <T> T compute(Function<Transaction, T> dbProc) {
		return getEnv().computeInTransaction(txn -> dbProc.apply(txn));
	}

	public void execute(Consumer<Transaction> dbProc) {
		getEnv().executeInTransaction(txn -> dbProc.accept(txn));
	}

	/**
	 * 読み取り専用トランザクションで客観DBから読み取る
	 * @param dbProc
	 * @return
	 */
	public <T> T readRet(Function<Transaction, T> dbProc) {
		return getEnv().computeInReadonlyTransaction(txn -> dbProc.apply(txn));
	}

	public void read(Consumer<Transaction> dbProc) {
		getEnv().executeInReadonlyTransaction(txn -> dbProc.accept(txn));
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

	public <T> T getGameEquipmentClass(Function<GameEquipmentClassStore, T> f) {
		return readTryW(txn -> f.apply(new GameEquipmentClassStore(txn)));
	}

	public <T> T getGameMaterialClass(Function<GameMaterialClassStore, T> f) {
		return readTryW(txn -> f.apply(new GameMaterialClassStore(txn)));
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

	public <T> T getMaterial(Function<MaterialStore, T> f) {
		return readTryW(txn -> f.apply(new MaterialStore(txn)));
	}

	public <T> T getStyle(Function<StyleStore, T> f) {
		return readTryW(txn -> f.apply(new StyleStore(txn)));
	}

	public <T> T getAvatar(Function<AvatarStore, T> f) {
		return readTryW(txn -> f.apply(new AvatarStore(txn)));
	}

	public <T> T getMaterialRelation(Function<MaterialRelationStore, T> f) {
		return readTryW(txn -> f.apply(new MaterialRelationStore(txn)));
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

	public <T> T readTryW(ThrowableFunction<Transaction, T> getStore) {
		return readRet(txn -> {
			try {
				return getStore.apply(txn);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		});
	}

	public <T> T writeTryW(ThrowableFunction<Transaction, T> getStore) {
		return compute(txn -> {
			try {
				return getStore.apply(txn);
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				return null;
			}
		});
	}

	/**
	 * 同調処理の対象となる客観コア以外の永続化されるデータを記録するストアの名前の一覧を返す
	 *
	 * このストア名一覧にObjectivityCoreを加えれば客観のすべてである。
	 * @return	客観の一部であるDB上のストアの名前一覧。順序はいつも同じ
	 * GetIntegrityInfoResponse#getHash()のため順序を保証する必要がある
	 */
	public List<String> getIdObjectStoreNames() {
		List<String> names = new ArrayList<>();
		names.add(User.class.getSimpleName());
		names.add(Web.class.getSimpleName());

		names.add(StaticGame.class.getSimpleName());
		names.add(RatingGame.class.getSimpleName());

		names.add(RatingGameStateByUser.class.getSimpleName());
		names.add(StaticGameStateByUser.class.getSimpleName());
		names.add(GameEquipmentClass.class.getSimpleName());
		names.add(GameMaterialClass.class.getSimpleName());

		names.add(FlowNetworkAbstractNominal.class.getSimpleName());

		names.add(Sociality.class.getSimpleName());
		names.add(SocialityIncomeSharing.class.getSimpleName());

		names.add(Material.class.getSimpleName());
		names.add(Style.class.getSimpleName());
		names.add(Avatar.class.getSimpleName());
		names.add(MaterialRelation.class.getSimpleName());

		names.add(UserMessageListHash.class.getSimpleName());
		names.add(FreeKVPair.class.getSimpleName());
		names.add(DistributedTradable.class.getSimpleName());
		names.add(Agenda.class.getSimpleName());

		names.add(DistributedVote.class.getSimpleName());

		return names;
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
		for (String storeName : Glb.getObje().getIdObjectStoreNames()) {
			IntegrityByStore byStore = readRet(txn -> {
				//tmpを外側に出してbyStoreに一元化するとたまに例外が出る
				IntegrityByStore tmp = new IntegrityByStore();
				IdObjectStore<?, ?> s = Glb.getObje().getStore(storeName, txn);
				HashStore hs = s.getHashStore();
				RecycleIdStore rs = s.getRecycleIdStore();

				//リサイクルIDの件数
				long count = rs.count();
				tmp.setRecycleIdCount(count);

				//リサイクルIDリスト
				long loop = (count / RecycleIdStore.unitIDList)
						+ (count % RecycleIdStore.unitIDList > 0 ? 1 : 0);
				for (int i = 0; i < loop; i++) {
					List<IDList> l = rs.getIDList(i);
					if (l == null) {
						Glb.getLogger().error("", new Exception(
								"invalid loop or bug of getIDList()"));
						break;
					}
					byte[] h = Glb.getUtil().hash(l);
					if (h == null) {
						Glb.getLogger().error("",
								new Exception("hash is null"));
						break;
					}
					tmp.getRecycleIdListHash().add(i, h);
				}

				//最後のID
				Long lastIdOfHashStore = hs.getLastIdOfHashStore();
				if (lastIdOfHashStore == null) {
					Glb.getLogger()
							.warn("lastId is null. bug or just no record");
				}
				tmp.setLastIdOfHashStore(lastIdOfHashStore);

				//最上位ハッシュ配列
				HashStoreRecordPositioned top = hs.getTopHash();
				if (top == null) {
					Glb.getLogger().warn("top is null. bug or just no record");
				}
				tmp.setTop(top);
				return tmp;
			});

			Glb.debug(storeName + " lastIdOfHashStore="
					+ byStore.getLastIdOfHashStore());

			//登録
			r.getByStore().put(storeName, byStore);
		}

		return r;
	}

	/**
	 * @param storeName		取得するストアのストア名
	 * @return				ストア名に対応するストア
	 */
	public IdObjectStore<? extends IdObjectDBI, ?> getStore(String storeName,
			Transaction txn) {
		IdObjectStore<?, ?> r = null;
		try {
			if (UserStore.modelName.equals(storeName)) {
				r = new UserStore(txn);
			} else if (WebStore.modelName.equals(storeName)) {
				r = new WebStore(txn);
			} else if (StaticGameStore.modelName.equals(storeName)) {
				r = new StaticGameStore(txn);
			} else if (RatingGameStore.modelName.equals(storeName)) {
				r = new RatingGameStore(txn);
			} else if (RatingGameStateByUserStore.modelName.equals(storeName)) {
				r = new RatingGameStateByUserStore(txn);
			} else if (StaticGameStateByUserStore.modelName.equals(storeName)) {
				r = new StaticGameStateByUserStore(txn);
			} else if (GameEquipmentClassStore.modelName.equals(storeName)) {
				r = new GameEquipmentClassStore(txn);
			} else if (GameMaterialClassStore.modelName.equals(storeName)) {
				r = new GameMaterialClassStore(txn);
			} else if (FlowNetworkAbstractNominalStore.modelName
					.equals(storeName)) {
				r = new FlowNetworkAbstractNominalStore(txn);
			} else if (UserMessageListHashStore.modelName.equals(storeName)) {
				r = new UserMessageListHashStore(txn);
			} else if (FreeKVPairStore.modelName.equals(storeName)) {
				r = new FreeKVPairStore(txn);
			} else if (SocialityStore.modelName.equals(storeName)) {
				r = new SocialityStore(txn);
			} else if (DistributedTradableStore.modelName.equals(storeName)) {
				r = new DistributedTradableStore(txn);
			} else if (MaterialStore.modelName.equals(storeName)) {
				r = new MaterialStore(txn);
			} else if (StyleStore.modelName.equals(storeName)) {
				r = new StyleStore(txn);
			} else if (AvatarStore.modelName.equals(storeName)) {
				r = new AvatarStore(txn);
			} else if (MaterialRelationStore.modelName.equals(storeName)) {
				r = new MaterialRelationStore(txn);
			} else if (AgendaStore.modelName.equals(storeName)) {
				r = new AgendaStore(txn);
			} else if (DistributedVoteStore.modelName.equals(storeName)) {
				r = new DistributedVoteStore(txn);
			} else if (SocialityIncomeSharingStore.modelName
					.equals(storeName)) {
				r = new SocialityIncomeSharingStore(txn);
			} else if (AgendaProcStore.modelName.equals(storeName)) {
				r = new AgendaProcStore(txn);
			} else if (RatingGameMatchProcStore.modelName.equals(storeName)) {
				r = new RatingGameMatchProcStore(txn);
			} else if (RatingGameMatchStore.modelName.equals(storeName)) {
				r = new RatingGameMatchStore(txn);
			} else if (EdgeLogStore.modelName.equals(storeName)) {
				r = new EdgeLogStore(txn);
			} else if (SocialityIncomeSharingStore.modelName
					.equals(storeName)) {
				r = new SocialityIncomeSharingStore(txn);
			} else if (URLProvementRegexStore.modelName.equals(storeName)) {
				r = new URLProvementRegexStore(txn);
			} else {
				throw new Exception("存在しないストア");
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
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
			ObjectivityCore core = s.get();
			//作者が最初の全体運営者になる。全体運営者無しでは本構想を開始できない
			boolean exist = false;
			User author = Glb.getConst().getAuthor();

			Long authorId = author.getRecycleId();
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
		//定数として設定されている作者ユーザーの情報が、ID指定型作成処理に入力するのに妥当か
		ValidationResult vr = new ValidationResult();
		author.validateAtCreateSpecifiedId(vr);
		if (!vr.isNoError()) {
			Glb.debug(vr.toString());
			Glb.getLogger().error("作者ユーザーの設定値が不正");
		}

		//作者IDであるべきIDでDBからロードされたUser
		User loaded = Glb.getObje()
				.getUser(us -> us.get(author.getRecycleId()));

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
			loaded = Glb.getObje().getUser(us -> us.get(author.getRecycleId()));
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
		return Glb.getObje().writeTryW(txn -> {
			DistributedVoteStore s = new DistributedVoteStore(txn);
			//0番に全体運営者選出投票が設定されているか
			boolean writeZero = false;
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
				managerVote.setRegistererUserId(IdObjectDBI.getSystemId());
				managerVote
						.setMainAdministratorUserId(IdObjectDBI.getSystemId());
				managerVote.setRecycleId(electionId);
				managerVote.setName(managerVoteName);
				managerVote.setSystem(true);
				managerVote.setEnable(true);
				managerVote.setName("全体運営者選出投票TenyuManagerElection");
				managerVote.setRecycleId(electionId);
				managerVote.setSchedule("0 0 */8 * * ?");
				managerVote.setSustainable(true);
				managerVote.setExplanation(
						"全体運営者を選出する投票。Vote to elect an TenyuManager.");
				User author = Glb.getConst().getAuthor();
				DistributedVoteChoice authorC = new DistributedVoteChoice();
				authorC.setName(author.getName());
				authorC.setOptionLong(author.getRecycleId());
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
		Glb.getObje().execute(txn -> {
			try {
				RoleStore rs = new RoleStore(txn);
				for (String roleName : RoleDBI.roleInitialNames.getNames()) {
					Role exist = rs.getByName(roleName);
					if (exist == null) {
						Role r = new Role();
						r.setName(roleName);
						r.setExplanation(roleName + " Administrators");
						r.setRegistererUserId(IdObjectDBI.getSystemId());
						r.setMainAdministratorUserId(IdObjectDBI.getVoteId());
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
						r.addAdmin(Glb.getConst().getAuthor().getRecycleId());
						rs.update(r);
					}
				}
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		});
	}

}
