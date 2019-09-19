package bei7473p5254d69jcuat.tenyu.release1.global.middle.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;

public class CatchUpContext {
	private Integrity majorityAtStart;

	private Integrity myAtStart;

	/**
	 * 更新されたID一覧やリサイクルID一覧から特定された取得すべきID一覧
	 * storeName : ids
	 */
	private Map<String, HashSet<Long>> storeNameToGetIds = new HashMap<>();
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

	public Map<String, HashSet<Long>> getStoreNameToGetIds() {
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

	public void setStoreNameToGetIds(
			Map<String, HashSet<Long>> storeNameToGetIds) {
		this.storeNameToGetIds = storeNameToGetIds;
	}

	public void setTeachers(ReadonlyNeighborList teachers) {
		this.teachers = teachers;
	}

}
