package bei7473p5254d69jcuat.tenyu.release1.db.store;

import static bei7473p5254d69jcuat.tenyu.release1.db.DBUtil.*;

import java.io.*;
import java.security.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class AgendaStore extends NaturalityStore<AgendaDBI, Agenda> {
	public static final String modelName = Agenda.class.getSimpleName();
	private static final StoreInfo statusToId = new StoreInfo(
			modelName + "_statusToId_Dup", StoreConfig.WITH_DUPLICATES);

	public static StoreInfo getMainStoreInfoStatic() {
		return getMainStoreInfoStatic(modelName);
	}

	/*
	public static Agenda getSimple(Long id) {
		return simple((s) -> s.get(id));
	}

	private static <R> R simple(Function<AgendaStore, R> f) {
		return IdObjectStore
				.simpleReadAccess((txn) -> f.apply(new AgendaStore(txn)));
	}
	*/

	public AgendaStore(Transaction txn) throws NoSuchAlgorithmException {
		super(txn);
	}

	@Override
	protected Agenda chainversionup(ByteIterable bi) {
		//AgendaはRunnable委譲メンバー変数を持つが、ここでそれもchainversionupする
		//だからここのchainversionupは少し条件が複雑化しうる
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Agenda)
				return (Agenda) o;
			throw new InvalidTargetObjectTypeException(
					"not Agenda object in AgendaStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean createNaturalityConcrete(AgendaDBI o)
			throws Exception {
		if (!util.put(statusToId, cnvI(o.getStatus().getNum()),
				cnvL(o.getRecycleId()))) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateNaturalityConcrete(AgendaDBI updated,
			AgendaDBI old, ValidationResult r) {
		boolean b = true;
		Integer updatedStatus = updated.getStatus().getNum();
		Integer oldStatus = old.getStatus().getNum();
		if (Glb.getUtil().notEqual(updatedStatus, oldStatus)) {
			if (existByStatus(updated.getStatus(), updated.getRecycleId())) {
				r.add(Lang.AGENDA_STATUS, Lang.ERROR_DB_EXIST,
						"status=" + updated.getStatus());
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteNaturalityConcrete(AgendaDBI o)
			throws Exception {
		if (!util.deleteDupSingle(statusToId, cnvI(o.getStatus().getNum()),
				cnvL(o.getRecycleId())))
			return false;
		return true;
	}

	public boolean existByStatus(AgendaStatus status, Long rId) {
		if (status == null || rId == null)
			return false;
		return util.getDupSingle(statusToId, cnvI(status.getNum()), cnvL(rId),
				(bi) -> cnvL(bi)) != null;
	}

	@Override
	protected boolean existNaturalityConcrete(AgendaDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByStatus(o.getStatus(), o.getRecycleId())) {
			vr.add(Lang.AGENDA_STATUS, Lang.ERROR_DB_NOTFOUND);
			b = false;
		}
		return b;
	}

	public List<Long> getIdsByStatus(AgendaStatus status) {
		return util.getDup(statusToId, cnvI(status.getNum()), (bi) -> cnvL(bi));
	}

	@Override
	public String getName() {
		return modelName;
	}

	@Override
	public List<StoreInfo> getStoresNaturalityConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(statusToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof AgendaDBI;
	}

	@Override
	protected boolean noExistNaturalityConcrete(AgendaDBI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByStatus(o.getStatus(), o.getRecycleId())) {
			vr.add(Lang.AGENDA_STATUS, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateNaturalityConcrete(AgendaDBI updated,
			AgendaDBI old) throws Exception {
		Integer updatedStatus = updated.getStatus().getNum();
		Integer oldStatus = old.getStatus().getNum();
		Long rId = old.getRecycleId();
		if (Glb.getUtil().notEqual(updatedStatus, oldStatus)) {
			if (oldStatus != null) {
				if (!util.deleteDupSingle(statusToId, cnvI(oldStatus),
						cnvL(rId)))
					return false;
			}
			if (!util.put(statusToId, cnvI(updatedStatus), cnvL(rId)))
				return false;
		}

		return true;
	}
}
