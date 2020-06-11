package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.user;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import glb.*;
import glb.util.*;
import javafx.scene.image.*;
import jetbrains.exodus.env.*;

/**
 * {@link User}のアバター
 *
 * アバターのファイルはTenyutalkを通じてアップロード及び参照される。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Avatar implements ValidatableI {
	public static final int actionToPixelArtSpriteSheetMax = 2000;
	public static final int expressionToImgFileMax = 2000;
	/**
	 * 動作：ドット絵スプライトシート
	 * 2d
	 */
	private Map<AvatarAction,
			TenyuReferenceArtifactByVersionMajor> actionToPixelArtSpriteSheet = new HashMap<>();

	/**
	 * 表情　：　立ち絵画像ファイル
	 * サポートされる画像ファイルのフォーマットは{@link Image}に依存する。
	 * しかしBMPはTenyu側の仕様で拒否される。
	 *
	 * 3d or 2d
	 */
	private Map<FacialExpression,
			TenyuReferenceArtifactByVersionMajor> expressionToImgFile = new HashMap<>();

	/**
	 * 96x96 icon
	 * 3d or 2d
	 */
	private TenyuReferenceArtifactByVersionMajor icon96;

	/**
	 * VRM等
	 * 3d
	 */
	private TenyuReferenceArtifactByVersionMajor model3d;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Avatar other = (Avatar) obj;
		if (actionToPixelArtSpriteSheet == null) {
			if (other.actionToPixelArtSpriteSheet != null)
				return false;
		} else if (!actionToPixelArtSpriteSheet
				.equals(other.actionToPixelArtSpriteSheet))
			return false;
		if (expressionToImgFile == null) {
			if (other.expressionToImgFile != null)
				return false;
		} else if (!expressionToImgFile.equals(other.expressionToImgFile))
			return false;
		if (icon96 == null) {
			if (other.icon96 != null)
				return false;
		} else if (!icon96.equals(other.icon96))
			return false;
		if (model3d == null) {
			if (other.model3d != null)
				return false;
		} else if (!model3d.equals(other.model3d))
			return false;
		return true;
	}

	public Map<AvatarAction,
			TenyuReferenceArtifactByVersionMajor> getActionToPixelArtSpriteSheet() {
		return actionToPixelArtSpriteSheet;
	}

	public Map<FacialExpression,
			TenyuReferenceArtifactByVersionMajor> getExpressionToImgFile() {
		return expressionToImgFile;
	}

	public TenyuReferenceArtifactByVersionMajor getModel3d() {
		return model3d;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actionToPixelArtSpriteSheet == null) ? 0
				: actionToPixelArtSpriteSheet.hashCode());
		result = prime * result + ((expressionToImgFile == null) ? 0
				: expressionToImgFile.hashCode());
		result = prime * result + ((icon96 == null) ? 0 : icon96.hashCode());
		result = prime * result + ((model3d == null) ? 0 : model3d.hashCode());
		return result;
	}

	public void setActionToPixelArtSpriteSheet(Map<AvatarAction,
			TenyuReferenceArtifactByVersionMajor> actionToPixelArtSpriteSheet) {
		this.actionToPixelArtSpriteSheet = actionToPixelArtSpriteSheet;
	}

	public void setExpressionToImgFile(Map<FacialExpression,
			TenyuReferenceArtifactByVersionMajor> expressionToImgFile) {
		this.expressionToImgFile = expressionToImgFile;
	}

	public void setModel3d(TenyuReferenceArtifactByVersionMajor model3d) {
		this.model3d = model3d;
	}

	@Override
	public String toString() {
		return "Avatar [actionToPixelArtSpriteSheet="
				+ actionToPixelArtSpriteSheet + ", expressionToImgFile="
				+ expressionToImgFile + ", icon96=" + icon96 + ", model3d="
				+ model3d + "]";
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r))
			b = false;

		if (model3d != null) {
			if (!model3d.validateAtCreate(r))
				b = false;
		}
		if (icon96 != null) {
			if (!icon96.validateAtCreate(r))
				b = false;
		}
		if (expressionToImgFile != null) {
			for (TenyuReferenceArtifactByVersionMajor e : expressionToImgFile
					.values()) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}
		if (actionToPixelArtSpriteSheet != null) {
			for (TenyuReferenceArtifactByVersionMajor e : actionToPixelArtSpriteSheet
					.values()) {
				if (!e.validateAtCreate(r)) {
					b = false;
					break;
				}
			}
		}

		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		boolean b = true;
		if (!validateCommon(r))
			b = false;

		if (model3d != null) {
			if (!model3d.validateAtUpdate(r))
				b = false;
		}
		if (icon96 != null) {
			if (!icon96.validateAtUpdate(r))
				b = false;
		}
		if (expressionToImgFile != null) {
			for (TenyuReferenceArtifactByVersionMajor e : expressionToImgFile
					.values()) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}
		if (actionToPixelArtSpriteSheet != null) {
			for (TenyuReferenceArtifactByVersionMajor e : actionToPixelArtSpriteSheet
					.values()) {
				if (!e.validateAtUpdate(r)) {
					b = false;
					break;
				}
			}
		}

		return b;
	}

	private boolean validateCommon(ValidationResult r) {
		boolean b = true;
		if (actionToPixelArtSpriteSheet == null) {
			r.add(Lang.AVATAR, Lang.PIXEL_ART_SPRITE_SHEET, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (actionToPixelArtSpriteSheet
					.size() > actionToPixelArtSpriteSheetMax) {
				r.add(Lang.AVATAR, Lang.PIXEL_ART_SPRITE_SHEET,
						Lang.ERROR_TOO_MANY,
						"size=" + actionToPixelArtSpriteSheet.size());
				b = false;
			}
		}
		if (expressionToImgFile == null) {
			r.add(Lang.AVATAR, Lang.EXPLANATION_ARTICLES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (expressionToImgFile.size() > expressionToImgFileMax) {
				r.add(Lang.AVATAR, Lang.EXPLANATION_ARTICLES,
						Lang.ERROR_TOO_MANY,
						"size=" + expressionToImgFile.size());
				b = false;
			}
		}
		return b;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		boolean b = true;
		if (model3d != null && !model3d.validateReference(r, txn))
			b = false;
		if (model3d != null && !icon96.validateReference(r, txn))
			b = false;

		for (TenyuReferenceArtifactByVersionMajor e : actionToPixelArtSpriteSheet
				.values()) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}

		for (TenyuReferenceArtifactByVersionMajor e : expressionToImgFile.values()) {
			if (!e.validateReference(r, txn)) {
				b = false;
				break;
			}
		}

		return b;
	}

	public TenyuReferenceArtifactByVersionMajor getIcon96() {
		return icon96;
	}

	public void setIcon96(TenyuReferenceArtifactByVersionMajor icon96) {
		this.icon96 = icon96;
	}

	public static int getActiontopixelartspritesheetmax() {
		return actionToPixelArtSpriteSheetMax;
	}

	public static int getExpressiontoimgfilemax() {
		return expressionToImgFileMax;
	}

}