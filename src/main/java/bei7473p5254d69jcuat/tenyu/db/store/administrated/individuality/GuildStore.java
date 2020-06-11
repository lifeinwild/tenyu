package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class GuildStore extends IndividualityObjectStore<GuildI, Guild> {
	public static final String modelName = Guild.class.getSimpleName();
	private static final StoreInfo userIdToId = new StoreInfo(
			modelName + "_userIdToId");

	public GuildStore(Transaction txn) {
		super(txn);
	}

	public static StoreInfo getUseridtoid() {
		return userIdToId;
	}

	@Override
	protected boolean createIndividualityObjectConcrete(GuildI o)
			throws Exception {
		for (Long userId : o.getGuildMemberIds()) {
			if (!util.put(getUseridtoid(), cnvL(userId), cnvL(o.getId()))) {
				return false;
			}
		}

		return true;
	}

	public boolean existByUserId(Long userId) {
		if (userId == null)
			return false;
		return util.get(getUseridtoid(), cnvL(userId)) != null;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			GuildI updated, GuildI old, ValidationResult r) {
		boolean b = true;

		List<Long> updatedUserIds = updated.getGuildMemberIds();
		List<Long> oldUserIds = old.getGuildMemberIds();
		Collection<Long> added = Glb.getUtil().getExtra(updatedUserIds,
				oldUserIds);

		if (added.size() > 0) {
			for (Long userId : added) {
				//もし既に存在したら、このユーザーは既に他のギルドに所属しているということ。
				if (existByUserId(userId)) {
					r.add(Lang.GUILD_MEMBER, Lang.ERROR_DB_EXIST,
							"userId=" + userId);
					b = false;
					break;
				}
			}
		}

		return b;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(GuildI o)
			throws Exception {
		for (Long userId : o.getGuildMemberIds()) {
			if (!util.delete(getUseridtoid(), cnvL(userId))) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(GuildI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		for (Long userId : o.getGuildMemberIds()) {
			if (!existByUserId(userId)) {
				vr.add(Lang.GUILD_MEMBER, Lang.ERROR_DB_NOTFOUND,
						"userId=" + userId);
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	protected List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		r.add(userIdToId);
		return r;
	}

	/**
	 * @param userId
	 * @return	指定されたユーザーが所属するギルドのID
	 */
	public Long getGulidIdByUserId(Long userId) {
		try {
			ByteIterable bi = util.get(userIdToId, cnvL(userId));
			if (bi == null)
				return null;
			return cnvL(bi);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(GuildI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		for (Long userId : o.getGuildMemberIds()) {
			if (existByUserId(userId)) {
				vr.add(Lang.GUILD_MEMBER, Lang.ERROR_DB_EXIST,
						"userId=" + userId);
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(GuildI updated,
			GuildI old) throws Exception {
		if (!updateCollectionSubIndex(getUseridtoid(), old.getId(),
				updated.getId(), () -> updated.getGuildMemberIds(),
				() -> old.getGuildMemberIds(), (k) -> cnvL(k)))
			return false;
		return true;
	}

	@Override
	public boolean isSupport(Object o) {
		return o instanceof GuildI;
	}

	@Override
	protected Guild chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Guild)
				return (Guild) o;
			throw new InvalidTargetObjectTypeException(
					"not Guild object in GuildStore");
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
