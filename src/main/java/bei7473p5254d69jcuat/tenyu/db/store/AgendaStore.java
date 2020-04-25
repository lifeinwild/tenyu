package bei7473p5254d69jcuat.tenyu.db.store;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.communication.mutual.right.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.agenda.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class AgendaStore extends IndividualityObjectStore<AgendaI, Agenda> {
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

	public AgendaStore(Transaction txn) {
		super(txn);
	}

	/**
	 * 現在のHIを用いて
	 * 可決された議題のうち執行日時を過ぎたものを実行する。
	 * @param maxApplySize		処理件数。件数が多すぎた場合に
	 * 一部が次回へ送られる。客観更新の反映段階の処理時間を一定時間以下に抑えるため。
	 * @return	1件でも処理したか
	 */
	public List<ObjectivityUpdateDataElement> applyDelayRuns(long maxApplySize)
			throws Exception {
		return applyDelayRuns(maxApplySize,
				Glb.getObje().getCore().getHistoryIndex());
	}

	public List<ObjectivityUpdateDataElement> applyDelayRuns(long maxApplySize,
			long nextHistoryIndex) throws Exception {
		List<ObjectivityUpdateDataElement> r = new ArrayList<>();
		//可決済みかつ未処理なオブジェクト一覧
		List<Long> acceptedIds = getIdsByStatus(AgendaStatus.ACCEPTED);
		if (acceptedIds == null || acceptedIds.size() == 0)
			return r;
		//昇順
		Collections.sort(acceptedIds);
		//処理した量。各タスクの処理の重さに依存
		long count = 0;
		for (Long acceptedId : acceptedIds) {
			//最大件数に到達したら終了
			if (count >= maxApplySize)
				return r;
			Agenda o = get(acceptedId);
			if (o == null) {
				Glb.getLogger().error(
						"Fatal error occured in AgendaStore. ID not found acceptedId="
								+ acceptedId,
						new IllegalStateException());
				continue;
			}

			//実行
			count += o.getApplySize();
			if (!o.apply(util.getTxn(), nextHistoryIndex)) {
				Glb.getLogger()
						.error("Fatal error occured. Failed to run. acceptedId="
								+ acceptedId, new RuntimeException());
				continue;
			}

			//ステータスを処理済みに
			r.add(o);
			o.next();
			update(o);
		}
		return r;
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
	protected boolean createIndividualityObjectConcrete(AgendaI o)
			throws Exception {
		if (!util.put(statusToId, cnvI(o.getStatus().getNum()),
				cnvL(o.getId()))) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			AgendaI updated, AgendaI old, ValidationResult r) {
		boolean b = true;
		Integer updatedStatus = updated.getStatus().getNum();
		Integer oldStatus = old.getStatus().getNum();
		if (Glb.getUtil().notEqual(updatedStatus, oldStatus)) {
			if (existByStatus(updated.getStatus(), updated.getId())) {
				r.add(Lang.AGENDA_STATUS, Lang.ERROR_DB_EXIST,
						"status=" + updated.getStatus());
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(AgendaI o)
			throws Exception {
		if (!util.deleteDupSingle(statusToId, cnvI(o.getStatus().getNum()),
				cnvL(o.getId())))
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
	protected boolean existIndividualityObjectConcrete(AgendaI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByStatus(o.getStatus(), o.getId())) {
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
	public List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(statusToId);
		return r;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof AgendaI;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(AgendaI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByStatus(o.getStatus(), o.getId())) {
			vr.add(Lang.AGENDA_STATUS, Lang.ERROR_DB_EXIST);
			b = false;
		}
		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(AgendaI updated,
			AgendaI old) throws Exception {
		Integer updatedStatus = updated.getStatus().getNum();
		Integer oldStatus = old.getStatus().getNum();
		Long rId = old.getId();
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
