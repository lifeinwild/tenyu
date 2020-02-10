package bei7473p5254d69jcuat.tenyu.communication.mutual.right;

import java.util.*;
import java.util.concurrent.*;

/**
 * 他モジュールからの客観更新処理一覧
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ObjectivityUpdateElementList {
	private List<
			ObjectivityUpdateDataElement> elements = new CopyOnWriteArrayList<>();

	/**
	 * @return	反映可能になったものの一覧
	 */
	public List<ObjectivityUpdateDataElement> pickup() {
		List<ObjectivityUpdateDataElement> r = new ArrayList<>();
		List<ObjectivityUpdateDataElement> tmp = elements;
		elements = new CopyOnWriteArrayList<>();
		tmp.sort(null);
		int max = 2000;
		int count = 0;
		for (ObjectivityUpdateDataElement e : tmp) {
			if (e.isDiffused() && !e.isOld()) {
				r.add(e);
				count++;
			}
			if (count >= max)
				break;
		}
		return r;
	}

	/**
	 * 任意のモジュールが例外的な客観更新処理を登録する。
	 * @param proc	客観更新処理
	 * @return	登録されたか
	 */
	public boolean addProcFromOtherModules(ObjectivityUpdateDataElement proc) {
		return elements.add(proc);
	}

	public List<ObjectivityUpdateDataElement> getElemnts() {
		return elements;
	}

	public void setElemnts(List<ObjectivityUpdateDataElement> elemnts) {
		this.elements = elemnts;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((elements == null) ? 0 : elements.hashCode());
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
		ObjectivityUpdateElementList other = (ObjectivityUpdateElementList) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		return true;
	}

}
