package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;

public class CatchUpContext {
	private Integrity majorityAtStart;

	private Integrity myAtStart;

	/**
	 * 更新されたID一覧等から特定された取得すべきオブジェクトのID一覧
	 * storeName : ids
	 */
	private Map<StoreNameObjectivity, HashSet<Long>> storeNameToGetIds = new HashMap<>();

	/**
	 * リサイクルHID一覧の差から特定された取得すべきオブジェクトのHID一覧
	 * storeName : hids
	 */
	private Map<StoreNameObjectivity, HashSet<Long>> storeNameToGetHids = new HashMap<>();

	/**
	 * 正しい整合性情報を知っていた近傍一覧
	 */
	private ReadonlyNeighborList teachers;

	public Integrity getMajorityAtStart() {
		return majorityAtStart;
	}

	public Integrity getMyAtStart() {
		return myAtStart;
	}

	public Map<StoreNameObjectivity, HashSet<Long>> getStoreNameToGetHids() {
		return storeNameToGetHids;
	}

	public Map<StoreNameObjectivity, HashSet<Long>> getStoreNameToGetIds() {
		return storeNameToGetIds;
	}

	public ReadonlyNeighborList getTeachers() {
		return teachers;
	}

	public void setMajorityAtStart(Integrity majorityAtStart) {
		this.majorityAtStart = majorityAtStart;
	}

	public void setMyAtStart(Integrity myAtStart) {
		this.myAtStart = myAtStart;
	}

	public void setStoreNameToGetHids(
			Map<StoreNameObjectivity, HashSet<Long>> storeNameToGetHids) {
		this.storeNameToGetHids = storeNameToGetHids;
	}

	public void setStoreNameToGetIds(
			Map<StoreNameObjectivity, HashSet<Long>> storeNameToGetIds) {
		this.storeNameToGetIds = storeNameToGetIds;
	}

	public void setTeachers(ReadonlyNeighborList teachers) {
		this.teachers = teachers;
	}

}
