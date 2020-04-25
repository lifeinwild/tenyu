package bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup;

import java.util.*;

import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 同調処理のため、更新されたオブジェクトのID一覧を記録、通信可能にする。
 * このクラスは同調を高速化するために導入され、このクラスの値は同調が保証されない。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class CatchUpUpdatedIDList implements CatchUpUpdatedIDListI {
	/**
	 * この回に更新されたオブジェクト一覧を対象とする
	 */
	private long historyIndex;
	/**
	 * 更新されたオブジェクトのID一覧
	 * 作成、削除は含まない
	 */
	private List<IDList> updated;

	public static final int updatedMax = 1000 * 10;

	@SuppressWarnings("unused")
	private CatchUpUpdatedIDList() {
	}

	public HashSet<Long> getIds() {
		return IDList.uncompress(updated);
	}

	public CatchUpUpdatedIDList(long historyIndex, List<IDList> updated) {
		this.historyIndex = historyIndex;
		this.updated = updated;
	}

	public long getHistoryIndex() {
		return historyIndex;
	}

	public List<IDList> getUpdated() {
		return updated;
	}

	public void setHistoryIndex(long historyIndex) {
		this.historyIndex = historyIndex;
	}

	public void setUpdated(List<IDList> updated) {
		this.updated = updated;
	}

	private final boolean validateAtCommon(ValidationResult vr) {
		boolean b = true;
		if (historyIndex < 0) {
			b = false;
		}
		if (updated == null) {
			b = false;
		} else {
			if (updated.size() > updatedMax) {
				b = false;
			} else {
			}
		}

		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		} else {
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCreate(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return true;//検証は重たい可能性があるし不正な値が入っていても重大な問題ではない
	}

}
