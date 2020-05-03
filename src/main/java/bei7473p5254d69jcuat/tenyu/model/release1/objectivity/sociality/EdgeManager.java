package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 社会性のエッジを管理する
 * エッジの重みは1日の最大増加量が1000、最大限少量が-1000なので、
 * ユーザーはそれを想定して何にどの程度の重みを設定するか考える必要がある。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class EdgeManager implements StorableI {
	/**
	 * エッジの数の最大値
	 * 全ノードタイプのうちの最大値であり、
	 * ノードタイプによってはこれより小さい最大値が別途適用される
	 */
	public static final int edgeMax = 1000 * 100;

	/**
	 * 最近のエッジ重み増減量と増減ペース制限機能
	 */
	private PaceLimitAmount changeAmount;
	/**
	 * socialityId : Edge
	 */
	protected Map<Long, Edge> edges = new ConcurrentHashMap<>();

	EdgeManager() {
	}

	public EdgeManager(long historyIndex) {
		setChangeAmount(new PaceLimitAmount(0, historyIndex));
	}

	/**
	 * エッジの重みを変化させる前に1回呼ぶ必要がある
	 * unitが動的に変化する可能性も考慮すると
	 * エッジの重みを変化させる直前に毎回呼ぶべき
	 */
	public void init(long increaseMax, long decreaseMax,
			long createHistoryIndex, long freePeriod) {
		changeAmount.init(increaseMax, decreaseMax, getHistoryIndexUnit(),
				createHistoryIndex, freePeriod);
	}

	public long getHistoryIndexUnit() {
		return Glb.getObje().getCore().getConfig().getHistoryIndexWeekRough();
	}

	/**
	 * 新規作成または既存のエッジの重みを修正する場合に使用する
	 * @param add	増分を表現したEdge
	 * @param historyIndex	更新された時のhistoryIndex
	 * @return	成功すればtrue。少しも更新が生じ無かった場合false
	 * @throws	中途半端に更新が生じて更新が完了しなかった場合
	 */
	public boolean add(Edge add, long historyIndex) throws Exception {
		//1つの社会性が短期間に過剰に重みを変化させる事ができない
		if (!changeAmount.add(add.getWeight(), historyIndex)) {
			return false;
		}
		Edge exist = edges.get(add.getDestSocialityId());
		if (exist == null) {
			exist = new Edge(add.getDestSocialityId(), add.getType());
			edges.put(exist.getDestSocialityId(), exist);
		}
		exist.add(add);
		return true;
	}

	public PaceLimitAmount getChangeAmount() {
		return changeAmount;
	}

	public Map<Long, Edge> getEdges() {
		return Collections.unmodifiableMap(edges);
	}

	public Edge remove(Long socialityId) {
		return edges.remove(socialityId);
	}

	public void setChangeAmount(PaceLimitAmount changeAmount) {
		this.changeAmount = changeAmount;
	}

	private final boolean validateAtCommon(ValidationResult vr) {
		boolean b = true;
		if (edges == null) {
			vr.add(Lang.EDGEMANAGER_EDGES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (edges.size() > edgeMax) {
				vr.add(Lang.EDGEMANAGER_EDGES, Lang.ERROR_TOO_MANY,
						"size=" + edges.size());
				b = false;
			} else {
				if (!Model.validateIdStandardNotSpecialId(edges.keySet())) {
					vr.add(Lang.EDGEMANAGER_EDGES, Lang.ERROR_INVALID,
							"edgesKeySet=" + edges.keySet());
					b = false;
				}
			}
		}

		if (changeAmount == null) {
			vr.add(Lang.EDGEMANAGER_EDGES, Lang.ERROR_EMPTY);
			b = false;
		}

		return b;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		} else {
			for (Edge e : edges.values()) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}
		if (changeAmount != null) {
			if (!changeAmount.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateAtCommon(r)) {
			b = false;
		} else {
			for (Edge e : edges.values()) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}
		if (changeAmount != null) {
			if (!changeAmount.validateAtUpdate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtUpdateChange(ValidationResult r, Object old) {
		if (old == null || !(old instanceof EdgeManager)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		EdgeManager old2 = (EdgeManager) old;

		boolean b = true;
		for (Entry<Long, Edge> e : edges.entrySet()) {
			if (!e.getValue().validateAtUpdateChange(r,
					old2.getEdges().get(e.getKey()))) {
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		boolean b = true;
		if (changeAmount != null) {
			if (!changeAmount.validateAtDelete(r)) {
				b = false;
			}
		}
		//edges検証処理しない。どうせtrue
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (changeAmount != null) {
			if (!changeAmount.validateReference(r, txn)) {
				b = false;
			}
		}
		if (edges != null) {
			for (Edge e : edges.values()) {
				if (!e.validateReference(r, txn)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((changeAmount == null) ? 0 : changeAmount.hashCode());
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EdgeManager other = (EdgeManager) obj;
		if (changeAmount == null) {
			if (other.changeAmount != null)
				return false;
		} else if (!changeAmount.equals(other.changeAmount))
			return false;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		return true;
	}

}