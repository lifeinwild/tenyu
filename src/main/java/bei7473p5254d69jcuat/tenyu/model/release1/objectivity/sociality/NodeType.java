package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.sociality;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.db.store.single.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.game.*;
import glb.*;
import jetbrains.exodus.env.*;

/**
 * 相互評価フローネットワークのノードの種類
 * @author exceptiontenyu@gmail.com
 *
 */
public enum NodeType {
	/**
	 * これら抽象ノードの詳細な名目はNodeTypeではないのでコメントアウト。
	 * "抽象ノード"がノードタイプ。
	 *
	 * ABSTRACT_SOFTWARE
	 * Tenyuのソフトウェア開発への貢献者にエッジを作成する抽象ノード。
	 * 全体運営者が指名したユーザーが管理者
	 *
	 * ABSTRACT_ADMINISTRATION
	 * Tenyuの運営への貢献者にエッジを作成する抽象ノード。
	 * 全体運営者が指名したユーザーが管理者
	 *
	 * ABSTRACT_ENVIRONMENT
	 * JVMやWEBブラウザ等幅広く利用されているソフトウェアにエッジを作成する抽象ノード。
	 * 全体運営者が指名したユーザーが管理者
	 *
	 * ABSTRACT_AVATAR
	 * アバターに対応するWEBページにエッジを作成する抽象ノード。
	 * 全体運営者が指名したユーザーが管理者
	 *
	 * ABSTRACT_INVENTION
	 * Tenyuに関連する発明者にエッジを作成する抽象ノード。
	 * 全体運営者が指名したユーザーが管理者
	 *
	 * ABSTRACT_STARTUP
	 * Tenyuの立ち上げへの貢献者にエッジを作成する抽象ノード。
	 * 全体運営者が指名したユーザーが管理者
	 *
	 * ABSTRACT_OPEN_SOURCE
	 * 有望なOSSにエッジを作成する抽象ノード。
	 * 全体運営者が指名したユーザーが管理者
	 */
	FLOWNETWORK_ABSTRACTNOMINAL(6),
	/**
	 * 共同主体と呼ぶ場合も。全体運営者が管理者
	 */
	COOPERATIVE_ACCOUNT(3),
	/**
	 * レーティングゲーム。ゲーム登録申請者が管理者
	 */
	RATINGGAME(5),
	/**
	 * 常駐空間。ゲーム登録申請者が管理者
	 */
	STATICGAME(4),
	/**
	 * ユーザー登録に伴って作成されそのユーザーが管理者
	 */
	USER(1),
	/**
	 * URL証明またはエッジ作成時の相手方にURLが指定された場合に作成され
	 * URL証明によって特定された公開鍵を持つユーザーが管理者
	 */
	WEB(2),
	/**
	 * アバターIDに対応するノード
	 * アバターの設定者数、設定者の仮想通貨使用ペース等から、
	 * 各アバターの貢献度が計算され、アバター抽象ノードから各アバターノードにエッジが作成される。
	 * アバターノードの管理者によってアバターノードから各素材ファイルへエッジが作成される。
	 * アバターノードの管理者はそのアバターの知財を持つ人がなるべきだが、
	 * 一時的にそうでなくても管理者を交代する機能が実装予定なので必ずしも重大な問題になるわけではない。
	 * 議決によって強制的に交代させる機能も実装予定。
	 */
	AVATAR(7),
	/**
	 * 例外的な場合
	 */
	UNKNOWN(0);

	/**
	 * @return	getId()で返されるidのバイト数
	 */
	public static int getIdSize() {
		return Byte.BYTES;
	}

	/**
	 * @param txn
	 * @return	ノードタイプに対応するストア
	 */
	public IdObjectStore<?, ?> getStore(Transaction txn) {
		try {
			switch (id) {
			case 1:
				return new UserStore(txn);
			case 2:
				return new WebStore(txn);
			case 3:
				return new ObjectivityCoreStore(txn);
			case 4:
				return new StaticGameStore(txn);
			case 5:
				return new RatingGameStore(txn);
			case 6:
				return new FlowNetworkAbstractNominalStore(txn);
			default:
				throw new Exception("unknown store");
			}
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	public static NodeType getNodeType(Object o) {
		return getNodeType(getNodeId(o));
	}

	public static byte getNodeId(Object o) {
		if (o instanceof User) {
			return 1;
		} else if (o instanceof Web) {
			return 2;
		} else if (o instanceof AdministratedObject) {
			return 3;
		} else if (o instanceof StaticGame) {
			return 4;
		} else if (o instanceof RatingGame) {
			return 5;
		} else if (o instanceof FlowNetworkAbstractNominal) {
			return 6;
		}
		Glb.debug("not supported class");
		return -1;
	}

	public static NodeType getNodeType(byte typeId) {
		for (NodeType type : NodeType.values()) {
			if (type.getId() == typeId)
				return type;
		}
		Glb.debug("undefined typeId");
		return null;
	}

	private transient byte id;

	private NodeType(int id) {
		this.id = (byte) id;
	}

	public byte getId() {
		return id;
	}
}