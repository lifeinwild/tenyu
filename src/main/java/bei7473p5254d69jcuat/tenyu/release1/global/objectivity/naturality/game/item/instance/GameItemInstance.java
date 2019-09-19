package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item.instance;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.item.*;
import jetbrains.exodus.env.*;

/**
 * GameItem系クラスで作られるアイテムの種類に対して、
 * GameItemInstance系クラスはそれら種類の実体を表現する。
 * 誰が何を持っているか。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class GameItemInstance implements Storable {
	/**
	 * @return
	 */
	public abstract GameItemClass getItemClass();

	/**
	 * アイテムの種類、そのアイテムの種類のゲーム、そのゲームの管理者へと辿り返す。
	 * @return
	 */
	public Long getGameAdministratorUserId() {
		try {
			return getItemClass().getGameRef().getGameAdministratorUserId();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	protected abstract boolean validateAtCreateGameInstanceConcrete(
			ValidationResult r);

	protected abstract boolean validateAtUpdateGameInstanceConcrete(
			ValidationResult r);

	protected abstract boolean validateReferenceGameInstanceConcrete(
			ValidationResult r, Transaction txn) throws Exception;

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return validateReferenceGameInstanceConcrete(r, txn);
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		return validateAtCreateGameInstanceConcrete(r);
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtUpdateGameInstanceConcrete(r);
	}

}
