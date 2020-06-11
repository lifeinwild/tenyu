package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.game;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.game.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;
import bei7473p5254d69jcuat.tenyu.ui.creator.game.staticgame.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 常駐空間ゲーム
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class StaticGame extends IndividualityObject implements StaticGameI {
	public static boolean createSequence(Transaction txn, StaticGame sg,
			boolean specifiedId, long historyIndex) throws Exception {
		return ObjectivitySequence.createSequence(txn, sg, specifiedId,
				historyIndex, sg.getRegistererUserId(),
				sg.getRegistererUserId(), StoreNameObjectivity.STATIC_GAME);
	}

	public static boolean deleteSequence(Transaction txn, StaticGame u)
			throws Exception {
		return ObjectivitySequence.deleteSequence(txn, u,
				StoreNameObjectivity.STATIC_GAME);
	}

	public static List<Long> getAdministratorUserIdCreateStatic() {
		return Glb.getObje().getRole(rs -> rs
				.getByName(StaticGame.class.getSimpleName()).getAdminUserIds());
	}

	@Override
	public boolean isMainAdministratorChangable() {
		return true;
	}

	/**
	 * サーバー一覧
	 * ユーザーがサーバーをやるという構想
	 * ユーザーIDからそのユーザーのFQDNやタイムゾーンを取得できる
	 * このゲームに関して特別な承認を行う権限がサーバーに与えられる。
	 * これをロビーサーバとして他のサーバに誘導することも可能。
	 */
	private ServerList serverList = new ServerList();

	public List<Long> getAdministratorUserIdCreate() {
		return getAdministratorUserIdCreateStatic();
	}

	@Override
	public List<Long> getAdministratorUserIdDelete() {
		return Glb.getObje().getRole(rs -> rs
				.getByName(StaticGame.class.getSimpleName()).getAdminUserIds());
	}

	@Override
	public List<Long> getAdministratorUserIdUpdate() {
		List<Long> r = new ArrayList<>();
		r.add(mainAdministratorUserId);
		return r;
	}

	public ServerList getServerList() {
		return serverList;
	}

	@Override
	public List<NodeIdentifierUser> getServerIdentifiers() {
		return serverList.getServerIdentifiers();
	}

	private final boolean validateAtCommonIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (serverList == null) {
			r.add(Lang.STATICGAME_SERVERS, Lang.ERROR_EMPTY);
			b = false;
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonIndividualityObjectConcrete(r)) {
			b = false;
		} else {
			if (!serverList.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	protected final boolean validateAtUpdateIndividualityObjectConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonIndividualityObjectConcrete(r)) {
			b = false;
		} else {
			if (!serverList.validateAtUpdate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeIndividualityObjectConcrete(
			ValidationResult r, Object old) {
		if (!(old instanceof StaticGame)) {
			r.add(Lang.OLD_OBJECT_AT_UPDATE, Lang.ERROR_INVALID,
					"old.class=" + old.getClass().getSimpleName());
			return false;
		}
		//StaticGame old2 = (StaticGame) old;

		boolean b = true;
		return b;
	}

	@Override
	public boolean validateReferenceIndividualityObjectConcrete(
			ValidationResult r, Transaction txn) throws Exception {
		boolean b = true;
		if (!serverList.validateReference(r, txn)) {
			b = false;
		}
		return b;
	}

	@Override
	public StaticGameGui getGuiReferenced(String guiName, String cssIdPrefix) {
		return new StaticGameGui(guiName, cssIdPrefix);
	}

	@Override
	public StaticGameStore getStore(Transaction txn) {
		return new StaticGameStore(txn);
	}

	@Override
	public boolean validateNameSub(ValidationResult r) {
		return true;
	}

	@Override
	public StoreNameEnum getStoreName() {
		return StoreNameObjectivity.STATIC_GAME;
	}

}
