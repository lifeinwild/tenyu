package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.game;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;

/**
 * 常駐空間ゲーム
 * @author exceptiontenyu@gmail.com
 *
 */
public interface StaticGameI
		extends IndividualityObjectI, HasSocialityI, HasTenyutalkAppletI {
	/**
	 * 現実を意味する常駐空間
	 */
	public static final Long real = 0L;

	/**
	 * ユーザーが常駐空間ゲームに対して何らかの操作をした時、
	 * サーバーとして適切な権限があるかの判定に使われるかもしれない。
	 * ゲームは管理者ユーザーを持つが、それは管理者としての操作のため。
	 * それとは別にサーバーとしての操作もあり得る。
	 *
	 * DBをバイナリレベルで一致させるため、順序保証があるListを使う。
	 * 本質的に重複してはいけないデータだが順序保証のためHashSetを使えない。
	 *
	 * @return
	 */
	List<NodeIdentifierUser> getServerIdentifiers();
}
