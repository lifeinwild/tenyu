package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.content;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda.*;
import jetbrains.exodus.env.*;

/**
 * 客観のデータを物理削除する。
 * 物理削除によってそれまで成立していた参照が成立しなくなる可能性があり、
 * 参照検証処理がエラーを返すようになる可能性がある。
 * どこからも参照されないようなゴミデータの削除を想定している。
 * あるいは、外部ツールによって入念に検討すべき。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class AgendaPhysicalDelete implements AgendaContentI {

	public static final int deleteMax = 1000;

	private List<TenyuPhysicalDeleteByStore> deletes = new ArrayList<>();

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgendaPhysicalDelete other = (AgendaPhysicalDelete) obj;
		if (deletes == null) {
			if (other.deletes != null)
				return false;
		} else if (!deletes.equals(other.deletes))
			return false;
		return true;
	}

	public List<TenyuPhysicalDeleteByStore> getDeletes() {
		return deletes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deletes == null) ? 0 : deletes.hashCode());
		return result;
	}

	@Override
	public boolean run(Agenda a) {
		boolean r = Glb.getObje().compute(txn -> {
			try {
				for (TenyuPhysicalDeleteByStore byStore : deletes) {
					IdObjectStore<?, ?> s = Glb.getObje()
							.getStore(byStore.getStoreName(), txn);
					for (Long id : byStore.getIds()) {
						s.delete(id);
					}
				}
				return txn.commit();
			} catch (Exception e) {
				Glb.getLogger().error("", e);
				txn.abort();
				return false;
			}
		});
		String result;
		if (r) {
			result = Lang.SUCCESS.toString();
		} else {
			result = Lang.FAILED.toString();
		}
		Glb.getLogger().info("", Lang.AGENDA_PHYSICALDELETE + " " + result);
		return r;
	}

	public void setDeletes(List<TenyuPhysicalDeleteByStore> deletes) {
		this.deletes = deletes;
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (deletes == null || deletes.size() == 0) {
			r.add(Lang.AGENDA_PHYSICALDELETE_DELETES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (deletes.size() > deleteMax) {
				r.add(Lang.AGENDA_PHYSICALDELETE_DELETES, Lang.ERROR_TOO_MANY);
				b = false;
			} else {
				for (TenyuPhysicalDeleteByStore byStore : deletes) {
					if (!byStore.validateAtCreate(r)) {
						b = false;
						break;
					}
				}
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (deletes != null) {
			for (TenyuPhysicalDeleteByStore e : deletes) {
				if (!e.validateReference(r, txn)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	public static class TenyuPhysicalDeleteByStore implements Storable {
		public static final int deleteIdMax = 1000 * 1000 * 10;
		private List<Long> ids = new ArrayList<>();
		private String storeName;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TenyuPhysicalDeleteByStore other = (TenyuPhysicalDeleteByStore) obj;
			if (ids == null) {
				if (other.ids != null)
					return false;
			} else if (!ids.equals(other.ids))
				return false;
			if (storeName == null) {
				if (other.storeName != null)
					return false;
			} else if (!storeName.equals(other.storeName))
				return false;
			return true;
		}

		public List<Long> getIds() {
			return ids;
		}

		public String getStoreName() {
			return storeName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((ids == null) ? 0 : ids.hashCode());
			result = prime * result
					+ ((storeName == null) ? 0 : storeName.hashCode());
			return result;
		}

		public void setIds(List<Long> ids) {
			this.ids = ids;
		}

		public void setStoreName(String storeName) {
			this.storeName = storeName;
		}

		private boolean validateAtCommon(ValidationResult vr) {
			boolean b = true;
			if (storeName == null || storeName.length() == 0) {
				vr.add(Lang.AGENDA_PHYSICALDELETE_BYSTORE_STORENAME,
						Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (!Naturality.validateText(
						Lang.AGENDA_PHYSICALDELETE_BYSTORE_STORENAME, storeName,
						vr))
					b = false;
			}

			if (ids == null || ids.size() == 0) {
				vr.add(Lang.AGENDA_PHYSICALDELETE_BYSTORE_IDS,
						Lang.ERROR_EMPTY);
				b = false;
			} else {
				if (!IdObject.validateIdStandardNotSpecialId(ids)) {
					vr.add(Lang.AGENDA_PHYSICALDELETE_BYSTORE_IDS,
							Lang.ERROR_INVALID);
					b = false;
				}
			}

			return b;
		}

		@Override
		public boolean validateAtCreate(ValidationResult r) {
			return validateAtCommon(r);
		}

		@Override
		public boolean validateAtDelete(ValidationResult r) {
			return true;
		}

		@Override
		public boolean validateAtUpdate(ValidationResult r) {
			return validateAtCommon(r);
		}

		@Override
		public boolean validateReference(ValidationResult vr, Transaction txn)
				throws Exception {
			boolean b = true;
			IdObjectStore<?, ?> s = Glb.getObje().getStore(storeName, txn);
			if (s == null) {
				vr.add(Lang.AGENDA_PHYSICALDELETE_BYSTORE_STORENAME,
						Lang.ERROR_INVALID);
				b = false;
			} else {
				if (ids != null) {
					for (Long id : ids) {
						if (s.get(id) == null) {
							vr.add(Lang.AGENDA_PHYSICALDELETE_BYSTORE_ID,
									Lang.ERROR_DB_NOTFOUND_REFERENCE,
									"id=" + id + " storeName=" + storeName);
							b = false;
							break;
						}
					}
				}
			}
			return b;
		}

	}

}
