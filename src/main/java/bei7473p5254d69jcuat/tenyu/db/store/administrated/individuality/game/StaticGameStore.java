package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.game;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class StaticGameStore
		extends IndividualityObjectStore<StaticGameI, StaticGame> {
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

	public StaticGameStore(Transaction txn) {
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
	protected boolean createIndividualityObjectConcrete(StaticGameI game)
			throws Exception {
		for (NodeIdentifierUser server : game.getServerIdentifiers()) {
			if (server == null) {
				Glb.getLogger().error(
						"server userId is null. gameId=" + game.getId(),
						new Exception());
				continue;
			}
			if (!util.put(serverToId, cnvO(server), cnvL(game.getId())))
				return false;
		}
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			StaticGameI updated, StaticGameI old, ValidationResult r) {
		boolean b = true;
		List<NodeIdentifierUser> newL = updated.getServerIdentifiers();
		List<NodeIdentifierUser> oldL = old.getServerIdentifiers();
		if (Glb.getUtil().notEqual(newL, oldL)) {
			//検証なので、追加分についてのみ扱う。
			Collection<NodeIdentifierUser> added = Glb.getUtil().getExtra(newL, oldL);
			for (NodeIdentifierUser identifer : added) {
				if (existByServerUserId(identifer, updated.getId())) {
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
	protected boolean deleteIndividualityObjectConcrete(StaticGameI game)
			throws Exception {
		for (NodeIdentifierUser server : game.getServerIdentifiers()) {
			if (!util.deleteDupSingle(serverToId, cnvO(server),
					cnvL(game.getId())))
				return false;
		}
		return true;
	}

	@Override
	public boolean existIndividualityObjectConcrete(StaticGameI game,
			ValidationResult vr) {
		boolean b = true;
		for (NodeIdentifierUser server : game.getServerIdentifiers()) {
			if (!existByServerUserId(server, game.getId())) {
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
	public List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<StoreInfo>();
		r.add(serverToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.game.StaticGameI)
			return true;
		return false;
	}

	@Override
	public boolean noExistIndividualityObjectConcrete(StaticGameI game,
			ValidationResult vr) {
		boolean b = true;
		for (NodeIdentifierUser server : game.getServerIdentifiers()) {
			if (existByServerUserId(server, game.getId())) {
				vr.add(Lang.STATICGAME_SERVER, Lang.ERROR_DB_EXIST);
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(StaticGameI updated,
			StaticGameI old) throws Exception {
		List<NodeIdentifierUser> newL = updated.getServerIdentifiers();
		List<NodeIdentifierUser> oldL = old.getServerIdentifiers();
		if (Glb.getUtil().notEqual(newL, oldL)) {
			//newLとoldLの順番が違う場合もここに来てしまうが、
			//その場合サブインデックスの更新は必要ない。
			//その場合以下のロジックで処理が発生しないので問題無い。

			Collection<NodeIdentifierUser> removed = Glb.getUtil().getExtra(oldL,
					newL);
			Collection<NodeIdentifierUser> added = Glb.getUtil().getExtra(newL, oldL);
			for (NodeIdentifierUser identifier : removed) {
				if (!util.deleteDupSingle(serverToId, cnvO(identifier),
						cnvL(old.getId())))
					throw new IOException("Failed to updateSub");
			}
			for (NodeIdentifierUser identifier : added) {
				if (!util.put(serverToId, cnvO(identifier),
						cnvL(updated.getId())))
					throw new IOException("Failed to updateSub");
			}
		}

		return true;
	}

}
