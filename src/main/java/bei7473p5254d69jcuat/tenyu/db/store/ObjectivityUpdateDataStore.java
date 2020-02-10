package bei7473p5254d69jcuat.tenyu.db.store;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.satellite.*;
import glb.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

/**
 * 全客観更新情報ストア。
 * このストアはあまりにも肥大化するのでいちいち記録されない。
 * が、ノード毎に分散して記録していくことで
 * ネットワーク全体のログを収集すれば客観をネットワーク勃興時から
 * 再現する事ができる可能性がある。
 *
 * とはいえ、設計の方針として、再現できることはほとんどおまけであり、
 * 確実ではなくてもいいし、一応記録しているだけ。
 *
 * 再現のためには再現専用のプログラムを作る必要があるが、当面書くつもりが無い。
 * 一部の処理のために各リリースのプログラムを順に使用する必要があるかもしれない。
 * その検討をしていないし再現可能な配慮をしながら設計するのは難しすぎるので放棄している。
 * 古いクラス定義が残っているだけでは再現に不十分かもしれない。
 *
 * ユーザー数に応じて確率的に記録する。
 * P2Pネットワーク全体で復元可能であればいい。
 * 客観を作り上げた情報だが、客観の一部ではない。
 * 基本的に客観はログを含まず「最新状態を持つ」という方針なので。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ObjectivityUpdateDataStore
		extends LogStore<ObjectivityUpdateDataDBI> {
	private static final String name = ObjectivityUpdateData.class
			.getSimpleName();
	private final StoreInfo indexToHistory = new StoreInfo(
			name + "_indexToHistory");

	@Override
	public List<StoreInfo> getStoresObjectStoreConcrete() {
		return getStoresStatic();
	}

	public List<StoreInfo> getStoresStatic() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(indexToHistory);
		return r;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param domainName	Platform, GameA, GameBなど
	 * @param nodeCount		平均いくつのノードに記録されるか
	 */
	public ObjectivityUpdateDataStore(Transaction txn) {
		super(txn);
	}

	/**
	 * @param historyStep
	 * @return					書き込まれたか
	 */
	public boolean randomWrite(ObjectivityUpdateDataDBI historyStep) {
		try {
			boolean write = false;
			long count = ObjectivityUpdateDataStore.countSimple();

			if (Glb.getConf().isBigStorage()) {
				//ビッグストレージなら記録
				write = true;
			} else if (count < 1000L * 3) {
				//自分の保持件数が少なければ記録する
				write = true;
			} else {
				//途中から確率で記録する。
				//TODO:user数とネットワークのノード数の比は動的に変わっていく
				long userCount = DBUtil
						.countStatic(UserStore.getMainStoreInfoStatic());
				if (userCount > 0) {
					long d = userCount + count;
					long chance = 1000L;
					double rate = chance / d;
					if (rate > 1.0) {
						write = true;
					} else {
						//0-dの範囲の整数の乱数を作成したとして
						//それがchanceを超える確率はchance / d、つまりrateである。
						write = (chance < ThreadLocalRandom.current()
								.nextLong(d));
					}
				} else {
					write = true;
				}
			}

			//書き込み
			if (write) {
				Glb.getLogger().info("writing ObjectivityUpdateData log");
				if (create(historyStep.getHistoryIndex(), historyStep)) {
					Glb.getLogger().info("wrote ObjectivityUpdateData log");
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	@Override
	public StoreInfo getMainStoreInfo() {
		return indexToHistory;
	}

	@Override
	protected ObjectivityUpdateData chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof ObjectivityUpdateData)
				return (ObjectivityUpdateData) o;
			throw new InvalidTargetObjectTypeException(
					"not ObjectivityUpdateData object in ObjectivityUpdateDataStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	public static long countSimple() {
		return simple((s) -> s.count());
	}

	/*
		public static boolean createSimple(UserMessageList l) {
			return simple((s) -> {
				try {
					return s.create(l.getHistoryIndex(), l);
				} catch (Exception e) {
					Glb.getLogger().error("", e);
					return false;
				}
			});
		}
	*/
	private static <R> R simple(Function<ObjectivityUpdateDataStore, R> f) {
		return LogStore.simpleAccess(
				(txn) -> f.apply(new ObjectivityUpdateDataStore(txn)));
	}

}
