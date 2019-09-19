package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file;

import java.util.*;
import java.util.Map.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.file.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import jetbrains.exodus.env.*;

/**
 * P2Pネットワークに登録されたファイルを外部アプリから呼び出すための仕組み。
 * あるアバターがあるスタイルにおいてどの素材ファイルを使うか。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class MaterialRelation extends ObjectivityObject
		implements MaterialRelationDBI {
	/**
	 * refToFileIdの最大件数
	 */
	public static final int refMax = 1000 * 50;

	public static String generateUnrecycleId(Long avatarId, Long styleId) {
		return avatarId + "_" + styleId;
	}

	/**
	 * アバターID
	 */
	private Long avatarId;

	/**
	 * 呼び出し名:素材ID
	 *
	 * 呼び出し名は外部アプリが素材を呼び出す際に用いる文字列。
	 * 「大ダメージを与えた時の音声」
	 * 「中ダメージを与えた時の音声」
	 * 「小ダメージを与えた時の音声」
	 * 「大ダメージを受けた時の音声」
	 * 「中ダメージを受けた時の音声」
	 * 「小ダメージを受けた時の音声」
	 * 「攻撃を回避した時の音声」
	 * 「補助効果を自分が自分にかけた時の音声」
	 * 「補助効果を他人が自分にかけた時の音声」
	 * 「補助効果を自分が他人にかけた時の音声」
	 * などなど、多くのゲームに共通と思われる事項について呼び出し名にして、
	 * 各アバターができるだけそれら呼び出し名に対応する素材を登録する。
	 * そして外部アプリ（ゲーム）から対応する素材が実行時にロードされ利用される。
	 * 呼び出し名に対応する素材は音声やエフェクトやモーション等が想定される。
	 *
	 * 一方で、「スキルA発動時音声」のようなゲーム固有の呼び出し名は
	 * ゲームにその素材ファイルが同梱されるべきで、
	 * MaterialRelationで扱わない。
	 *
	 */
	private Map<String, Long> refToMaterialId = new HashMap<>();

	/**
	 * スタイルID
	 * NullRecycleIdなら全スタイル共通の設定
	 */
	private Long styleId;

	public List<Long> getAdministratorUserIdCreate() {
		List<Long> r = new ArrayList<Long>();
		r.addAll(Glb.getObje().getRole(
				rs -> rs.getByName(MaterialRelation.class.getSimpleName()))
				.getAdminUserIds());
		if (avatarId != null) {
			Avatar a = Glb.getObje().getAvatar(as -> as.get(avatarId));
			r.add(a.getRegistererUserId());
		}
		return r;
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return getAdministratorUserIdCreate();
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		return getAdministratorUserIdCreate();
	}

	public Long getAvatarId() {
		return avatarId;
	}

	public Map<String, Long> getRefToMaterialId() {
		return refToMaterialId;
	}

	/**
	 * @return getRefToMaterialId()の別形式。materialIdをMaterialにしたもの。
	 */
	public List<RefToMaterial> getRefToMaterialList() {
		List<RefToMaterial> r = new ArrayList<>();
		for (Entry<String, Long> e : getRefToMaterialId().entrySet()) {
			Material m = Glb.getObje().getMaterial(ms -> ms.get(e.getValue()));
			r.add(new RefToMaterial(e.getKey(), m));
		}
		return r;
	}

	public Long getStyleId() {
		return styleId;
	}

	public void setAvatarId(Long avatarId) {
		this.avatarId = avatarId;
	}

	public void setRefToMaterialId(Map<String, Long> refToMaterialId) {
		this.refToMaterialId = refToMaterialId;
	}

	public void setStyleId(Long styleId) {
		this.styleId = styleId;
	}

	private final boolean validateAtCommonObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!IdObject.validateIdStandardNotSpecialId(avatarId)) {
			r.add(Lang.MATERIALRELATION_AVATAR_ID, Lang.ERROR_INVALID);
			b = false;
		} else {
		}
		if (!IdObject.validateIdStandardNotSpecialId(styleId)) {
			r.add(Lang.MATERIALRELATION_STYLE_ID, Lang.ERROR_INVALID);
			b = false;
		} else {
		}
		if (refToMaterialId == null) {
			r.add(Lang.MATERIALRELATION_REFTOMATERIALID, Lang.ERROR_EMPTY);
			b = false;
		} else if (refToMaterialId.size() > refMax) {
			r.add(Lang.MATERIALRELATION_REFTOMATERIALID, Lang.ERROR_TOO_MANY,
					refToMaterialId.size() + " / " + refMax);
			b = false;
		} else {
			for (Entry<String, Long> e : refToMaterialId.entrySet()) {
				if (!Naturality.validateTextAllCtrlChar(
						Lang.MATERIALRELATION_REFTOMATERIALID_REFNAME,
						e.getKey(), r))
					b = false;
				if (!IdObject.validateIdStandardNotSpecialId(e.getValue())) {
					r.add(Lang.MATERIALRELATION_REFTOMATERIALID_MATERIALID,
							Lang.ERROR_INVALID, e.getKey());
					b = false;
				} else {
				}
			}
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonObjectivityObjectConcrete(r))
			b = false;
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeObjectivityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof MaterialRelation)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//MaterialRelation old2 = (MaterialRelation) old;

		boolean b = true;
		return b;
	}

	@Override
	protected final boolean validateAtUpdateObjectivityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonObjectivityObjectConcrete(r))
			b = false;
		return b;
	}

	@Override
	public boolean validateReferenceObjectivityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		if (new AvatarStore(txn).get(avatarId) == null) {
			r.add(Lang.MATERIALRELATION_AVATAR_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}
		if (new StyleStore(txn).get(styleId) == null) {
			r.add(Lang.MATERIALRELATION_STYLE_ID,
					Lang.ERROR_DB_NOTFOUND_REFERENCE);
			b = false;
		}
		MaterialStore ms = new MaterialStore(txn);
		for (Entry<String, Long> e : refToMaterialId.entrySet()) {
			if (ms.get(e.getValue()) == null) {
				r.add(Lang.MATERIALRELATION_REFTOMATERIALID_MATERIALID,
						Lang.ERROR_DB_NOTFOUND_REFERENCE, e.getKey());
				b = false;
				break;
			}
		}
		return b;
	}
}
