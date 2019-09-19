package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.middle.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.sociality.*;
import jetbrains.exodus.env.*;

/**
 * 常駐空間ゲーム
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class StaticGame extends AbstractGame implements StaticGameDBI {
	public static boolean createSequence(Transaction txn, StaticGame u,
			boolean specifiedId, long historyIndex) throws Exception {
		return ObjectivitySequence.createSequence(txn, u, specifiedId,
				historyIndex, new StaticGameStore(txn),
				u.getMainAdministratorUserId(), u.getRegistererUserId(),
				NodeType.STATICGAME);
	}

	public static boolean deleteSequence(Transaction txn, StaticGame u)
			throws Exception {
		return ObjectivitySequence.deleteSequence(txn, u,
				new StaticGameStore(txn), NodeType.STATICGAME);
	}

	public static List<Long> getAdministratorUserIdCreateStatic() {
		return Glb.getObje().getRole(rs -> rs
				.getByName(StaticGame.class.getSimpleName()).getAdminUserIds());
	}

	/**
	 * サーバー一覧
	 * ユーザーがサーバーをやるという構想
	 * ユーザーIDからそのユーザーのFQDNやタイムゾーンを取得できる
	 * このゲームに関して特別な承認を行う権限がサーバーに与えられる。
	 * これをロビーサーバとして他のサーバに誘導することも可能。
	 */
	private ServerList serverList = new ServerList();

	@Override
	public String getDir() {
		return Glb.getFile().getStaticGameDirSingle() + getName() + "/";
	}

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

	private final boolean validateAtCommonAbstractGameConcrete(
			ValidationResult r) {
		boolean b = true;
		if (serverList == null) {
			r.add(Lang.STATICGAME_SERVERS, Lang.ERROR_EMPTY);
			b = false;
		}
		return b;
	}

	@Override
	protected final boolean validateAtCreateAbstractGameConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonAbstractGameConcrete(r)) {
			b = false;
		} else {
			if (!serverList.validateAtCreate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	protected final boolean validateAtUpdateAbstractGameConcrete(
			ValidationResult r) {
		boolean b = true;
		if (!validateAtCommonAbstractGameConcrete(r)) {
			b = false;
		} else {
			if (!serverList.validateAtUpdate(r)) {
				b = false;
			}
		}
		return b;
	}

	@Override
	protected boolean validateAtUpdateChangeAbstractGameConcrete(
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
	public boolean validateReferenceAbstractGameConcrete(ValidationResult r,
			Transaction txn) throws Exception {
		boolean b = true;
		if (!serverList.validateReference(r, txn)) {
			b = false;
		}
		return b;
	}

}
