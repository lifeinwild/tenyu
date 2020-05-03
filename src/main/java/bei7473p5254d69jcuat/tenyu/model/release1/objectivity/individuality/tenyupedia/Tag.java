package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.tenyupedia;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.model.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;
import bei7473p5254d69jcuat.tenyutalk.reference.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class Tag extends IndividualityObject implements TagI {
	/**
	 * 説明記事が増えすぎるとこのオブジェクトのサイズがでかくなりすぎるので
	 * 低めに制限する。
	 * もし限界に達したらいくつか削除する事になる。
	 */
	private static final int expMax = 200;

	/**
	 * このタグの説明記事一覧
	 */
	private List<TenyutalkReferenceFlexible<
			TenyutalkFile>> explanationArticles = new ArrayList<>();

	public boolean addArticle(TenyutalkReferenceFlexible<TenyutalkFile> a) {
		if (a == null || explanationArticles.size() > expMax
				|| explanationArticles.contains(a))
			return false;
		explanationArticles.add(a);
		return true;
	}

	public boolean removeArticle(TenyutalkReferenceFlexible<TenyutalkFile> a) {
		return explanationArticles.remove(a);
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.TAG;
	}

	@Override
	public boolean isMainAdministratorChangable() {
		return true;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (explanationArticles == null) {
			r.add(Lang.TAG, Lang.EXPLANATION_ARTICLES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (explanationArticles.size() > expMax) {
				r.add(Lang.TAG, Lang.EXPLANATION_ARTICLES, Lang.ERROR_TOO_MANY,
						"size=" + explanationArticles.size());
				b = false;
			}
		}

		return b;
	}

	@Override
	protected boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		} else {
			for (TenyutalkReferenceFlexible<
					TenyutalkFile> e : explanationArticles) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof Tag)) {
			return false;
		}
		boolean b = true;
		Tag o = (Tag) old;

		return b;
	}

	@Override
	protected boolean validateAtUpdateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r)) {
			b = false;
		} else {
			for (TenyutalkReferenceFlexible<
					TenyutalkFile> e : explanationArticles) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
			//このオブジェクトのIDと同じIDが関連タグIDリストに含まれていてはいけない
			//自分を関連タグに含めれないという事
			for (Long tagId : tagIds) {
				if (getId().equals(tagId)) {
					r.add(Lang.TAG, Lang.TAG_IDS, Lang.ERROR_INVALID,
							"tagId=" + tagId);
					b = false;
				}
			}
		}
		return b;
	}

	@Override
	protected boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	protected boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		for (TenyutalkReferenceFlexible<
				TenyutalkFile> e : explanationArticles) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}
		return b;
	}

	@Override
	public TagStore getStore(Transaction txn) {
		return new TagStore(txn);
	}

	@Override
	public TagGui getGui(String guiName, String cssIdPrefix) {
		return new TagGui(guiName, cssIdPrefix);
	}

	@Override
	public List<Long> getAdministratorUserIdCreate() {
		return null;
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return null;
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return null;
	}

	public static int getExpmax() {
		return expMax;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((explanationArticles == null) ? 0
				: explanationArticles.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tag other = (Tag) obj;
		if (explanationArticles == null) {
			if (other.explanationArticles != null)
				return false;
		} else if (!explanationArticles.equals(other.explanationArticles))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Tag [explanationArticles=" + explanationArticles + "]";
	}

	public void setExplanationArticles(List<
			TenyutalkReferenceFlexible<TenyutalkFile>> explanationArticles) {
		this.explanationArticles = explanationArticles;
	}

}
