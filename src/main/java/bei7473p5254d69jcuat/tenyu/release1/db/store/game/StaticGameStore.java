package bei7473p5254d69jcuat.tenyu.release1.db.store.game;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class StaticGameStore
		extends AbstractGameStore<StaticGameDBI, StaticGame> {
	public static final String modelName = StaticGame.class.getSimpleName();
	/**
	 * ServerのuserIdからStaticGameのidへのサブインデックスストア
	 * 重複キー
	 *
	 */
	private static final StoreInfo serverToId = new StoreInfo(
			modelName + "_serverToId_Dup", StoreConfig.WITH_DUPLICATES, true);

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}
/*
	public static StaticGame getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	private static <R> R simple(Function<StaticGameStore, R> f) {
		return IdObjectStore
				.simpleReadAccess((txn) -> f.apply(new StaticGameStore(txn)));
	}
*/
	public StaticGameStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected StaticGame chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof StaticGame)
				return (StaticGame) o;
			throw new InvalidTargetObjectTypeException(
					"not StaticGame object in StaticGameStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createAbstractGameConcrete(StaticGameDBI game)
			throws Exception {
		for (NodeIdentifierUser server : game.getServerIdentifiers()) {
			if (server == null) {
				Glb.getLogger().error(
						"server userId is null. gameId=" + game.getRecycleId(),
						new Exception());
				continue;
			}
			if (!util.put(serverToId, cnvO(server), cnvL(game.getRecycleId())))
				return false;
		}
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateAbstractGameConcrete(
			StaticGameDBI updated, StaticGameDBI old, ValidationResult r) {
		boolean b = true;
		List<NodeIdentifierUser> newL = updated.getServerIdentifiers();
		List<NodeIdentifierUser> oldL = old.getServerIdentifiers();
		if (Glb.getUtil().notEqual(newL, oldL)) {
			//検証なので、追加分についてのみ扱う。
			List<NodeIdentifierUser> added = Glb.getUtil().getExtra(newL, oldL);
			for (NodeIdentifierUser identifer : added) {
				if (existByServerUserId(identifer, updated.getRecycleId())) {
					r.add(Lang.STATICGAME_SERVER, Lang.ERROR_DB_EXIST);
					Glb.getLogger().error("subindex already exists.",
							new Exception());
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	protected boolean deleteAbstractGameConcrete(StaticGameDBI game)
			throws Exception {
		for (NodeIdentifierUser server : game.getServerIdentifiers()) {
			if (!util.deleteDupSingle(serverToId, cnvO(server),
					cnvL(game.getRecycleId())))
				return false;
		}
		return true;
	}

	@Override
	public boolean existAbstractGameConcrete(StaticGameDBI game,
			ValidationResult vr) {
		boolean b = true;
		for (NodeIdentifierUser server : game.getServerIdentifiers()) {
			if (!existByServerUserId(server, game.getRecycleId())) {
				vr.add(Lang.STATICGAME_SERVER, Lang.ERROR_DB_NOTFOUND);
				b = false;
				break;
			}
		}
		return b;
	}

	public boolean existByServerUserId(NodeIdentifierUser server, Long id) {
		if (server == null || id == null)
			return false;
		try {
			return util.getDupSingle(serverToId, cnvO(server), cnvL(id),
					(bi) -> cnvL(bi)) != null;
		} catch (IOException e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	public List<Long> getIdsByServerUserId(Long serverUserId) {
		return util.getDup(serverToId, cnvL(serverUserId), v -> cnvL(v));
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresAbstractGameConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(serverToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.StaticGameDBI)
			return true;
		return false;
	}

	@Override
	public boolean noExistAbstractGameConcrete(StaticGameDBI game,
			ValidationResult vr) {
		boolean b = true;
		for (NodeIdentifierUser server : game.getServerIdentifiers()) {
			if (existByServerUserId(server, game.getRecycleId())) {
				vr.add(Lang.STATICGAME_SERVER, Lang.ERROR_DB_EXIST);
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	protected boolean updateAbstractGameConcrete(StaticGameDBI updated,
			StaticGameDBI old) throws Exception {
		List<NodeIdentifierUser> newL = updated.getServerIdentifiers();
		List<NodeIdentifierUser> oldL = old.getServerIdentifiers();
		if (Glb.getUtil().notEqual(newL, oldL)) {
			//newLとoldLの順番が違う場合もここに来てしまうが、
			//その場合サブインデックスの更新は必要ない。
			//その場合以下のロジックで処理が発生しないので問題無い。

			List<NodeIdentifierUser> removed = Glb.getUtil().getExtra(oldL,
					newL);
			List<NodeIdentifierUser> added = Glb.getUtil().getExtra(newL, oldL);
			for (NodeIdentifierUser identifier : removed) {
				if (!util.deleteDupSingle(serverToId, cnvO(identifier),
						cnvL(old.getRecycleId())))
					throw new IOException("Failed to updateSub");
			}
			for (NodeIdentifierUser identifier : added) {
				if (!util.put(serverToId, cnvO(identifier),
						cnvL(updated.getRecycleId())))
					throw new IOException("Failed to updateSub");
			}
		}

		return true;
	}
}
