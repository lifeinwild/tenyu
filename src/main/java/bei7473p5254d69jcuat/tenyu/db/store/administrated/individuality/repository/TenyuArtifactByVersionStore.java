package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.repository;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import com.github.zafarkhaja.semver.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.DBUtil.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.repository.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class TenyuArtifactByVersionStore
		extends AdministratedObjectStore<TenyuArtifactByVersionI,
				TenyuArtifactByVersion> {
	public static final String modelName = TenyuArtifactByVersion.class
			.getSimpleName();

	private static final StoreInfo tenyuArtifactIdToId = new StoreInfo(
			modelName + "_tenyuArtifactIdToId_Dup",
			StoreConfig.WITH_DUPLICATES);

	public static StoreInfo getLatestMajortoid() {
		return latestMajorToId;
	}

	/**
	 * major version : そのメジャーバージョンの最新版のid
	 *
	 * これは一意制約ではないので通常のサブインデックスとかなり意味が異なる。
	 */
	private static final StoreInfo latestMajorToId = new StoreInfo(
			modelName + "_latestMajorToId", StoreConfig.WITHOUT_DUPLICATES,
			true);

	public Long getIdByMajor(int major) {
		return getId(latestMajorToId, cnvI(major));
	}

	public TenyuArtifactByVersionStore(Transaction txn) {
		super(txn);
	}

	public static StoreInfo getTenyuartifactidtoid() {
		return tenyuArtifactIdToId;
	}

	public Long getLatestId(int major) {
		return getId(getLatestMajortoid(), cnvI(major));
	}

	public TenyuArtifactByVersionI getLatest(int major) {
		return get(getLatestId(major));
	}

	public KVSRecord<Integer, Long> getLastVersionId() {
		return util.getLast(getLatestMajortoid(), k -> cnvI(k), v -> cnvL(v));
	}

	public TenyuArtifactByVersionI getLastVersion() {
		return get(getLastVersionId().getValue());
	}

	@Override
	protected boolean createAdministratedObjectConcrete(
			TenyuArtifactByVersionI o) throws Exception {
		if (!util.put(getTenyuartifactidtoid(), cnvL(o.getTenyuArtifactId()),
				cnvL(o.getId()))) {
			return false;
		}

		Version v = o.getSemVer();
		if (shouldReplaceLatestMajor(v)) {
			if (!util.put(getLatestMajortoid(), cnvI(v.getMajorVersion()),
					cnvL(o.getId()))) {
				return false;
			}
		}

		return true;
	}

	private boolean shouldReplaceLatestMajor(Version candidate) {
		Long latestId = getIdByMajor(candidate.getMajorVersion());
		if (latestId == null)
			return true;
		TenyuArtifactByVersionI latest = get(latestId);
		if (latest == null) {
			return true;
		}
		Version old = latest.getSemVer();
		return candidate.compareTo(old) > 0;
	}

	@Override
	protected boolean dbValidateAtUpdateAdministratedObjectConcrete(
			TenyuArtifactByVersionI updated, TenyuArtifactByVersionI old,
			ValidationResult r) {
		return true;
	}

	@Override
	protected boolean deleteAdministratedObjectConcrete(
			TenyuArtifactByVersionI o) throws Exception {
		if (!util.deleteDupSingle(getTenyuartifactidtoid(),
				cnvL(o.getTenyuArtifactId()), cnvL(o.getId())))
			return false;

		Version v = o.getSemVer();
		Long latestId = getIdByMajor(v.getMajorVersion());
		if (latestId != null && latestId.equals(o.getId())) {
			if (!util.delete(getLatestMajortoid(), cnvI(v.getMajorVersion())))
				return false;
		}

		return true;
	}

	@Override
	protected boolean existAdministratedObjectConcrete(
			TenyuArtifactByVersionI o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (!existByTenyuArtifactId(o.getTenyuArtifactId(), o.getId())) {
			vr.add(Lang.TENYU_ARTIFACT_BY_VERSION, Lang.TENYU_ARTIFACT_ID,
					Lang.ERROR_DB_NOTFOUND,
					"tenyuArtifactId=" + o.getTenyuArtifactId());
			b = false;
		}

		return b;
	}

	public boolean existByTenyuArtifactId(Long tenyuArtifactId, Long id) {
		if (tenyuArtifactId == null || id == null)
			return false;
		return util.getDupSingle(getTenyuartifactidtoid(),
				cnvL(tenyuArtifactId), cnvL(id), bi -> cnvL(bi)) != null;
	}

	@Override
	protected List<StoreInfo> getStoresAdministratedObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(tenyuArtifactIdToId);
		r.add(latestMajorToId);
		return r;
	}

	public List<Long> getIdsByTenyuArtifactId(Long tenyuArtifactId) {
		return util.getDup(tenyuArtifactIdToId, cnvL(tenyuArtifactId),
				v -> cnvL(v));
	}

	public List<TenyuArtifactByVersionI> getByTenyuArtifactId(
			Long tenyuArtifactId) {
		List<Long> ids = util.getDup(tenyuArtifactIdToId, cnvL(tenyuArtifactId),
				v -> cnvL(v));
		List<TenyuArtifactByVersionI> r = new ArrayList<>();
		if (ids == null || ids.size() == 0)
			return r;

		for (Long id : ids) {
			TenyuArtifactByVersionI e = get(id);
			if (e != null)
				r.add(e);
		}

		return r;
	}

	@Override
	protected boolean noExistAdministratedObjectConcrete(
			TenyuArtifactByVersionI o, ValidationResult vr) throws Exception {
		boolean b = true;
		if (existByTenyuArtifactId(o.getTenyuArtifactId(), o.getId())) {
			vr.add(Lang.TENYU_ARTIFACT_BY_VERSION, Lang.TENYU_ARTIFACT_ID,
					Lang.ERROR_DB_EXIST,
					"tenyuArtifactId=" + tenyuArtifactIdToId);
			b = false;
		}

		//existは判定しないがnoExistは判定する
		Long latestMajorId = getId(getLatestMajortoid(),
				cnvI(o.getSemVer().getMajorVersion()));
		if (latestMajorId != null) {
			vr.add(Lang.TENYU_ARTIFACT_BY_VERSION,
					Lang.TENYU_ARTIFACT_BY_VERSION_LATEST_MAJOR_ID,
					Lang.ERROR_DB_EXIST, "latestMajorId=" + latestMajorId);
			b = false;
		}

		return b;
	}

	@Override
	protected boolean updateAdministratedObjectConcrete(
			TenyuArtifactByVersionI updated, TenyuArtifactByVersionI old)
			throws Exception {
		if (Glb.getUtil().notEqual(updated.getTenyuArtifactId(),
				old.getTenyuArtifactId())) {
			if (!util.deleteDupSingle(tenyuArtifactIdToId,
					cnvL(old.getTenyuArtifactId()), cnvL(old.getId())))
				return false;
			if (!util.put(tenyuArtifactIdToId,
					cnvL(updated.getTenyuArtifactId()), cnvL(updated.getId())))
				return false;
		}

		Version v = updated.getSemVer();
		if (shouldReplaceLatestMajor(v)) {
			if (!util.put(getLatestMajortoid(), cnvI(v.getMajorVersion()),
					cnvL(updated.getId())))
				return false;
		}

		return true;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof TenyuArtifactByVersionI;
	}

	@Override
	protected TenyuArtifactByVersion chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof TenyuArtifactByVersion)
				return (TenyuArtifactByVersion) o;
			throw new InvalidTargetObjectTypeException(
					"not TenyuArtifactByVersion object in TenyuArtifactByVersionStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public String getName() {
		return modelName;
	}

}
