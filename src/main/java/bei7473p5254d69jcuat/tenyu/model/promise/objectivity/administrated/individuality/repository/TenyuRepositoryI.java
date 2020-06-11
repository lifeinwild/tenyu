package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import glb.*;

/**
 * 各ユーザーが自由に制作物をアップロードする場所を表す。
 * しかしこれ自体はその一部メタデータ（客観）で、
 * フォルダやファイルはノード別管理で非客観となる。
 *
 * リリースフォルダに置かれたファイルは各々のユーザーのPCでホスティングされ、
 * ミラー等を通じて他のノードにもホスティングされ、
 * P2Pネットワーク上で公開される。
 *
 * 設計の経緯としては、当初リポジトリを客観にするか各ノードがローカルで持つもの
 * にするか迷っていたが、様々な事を考慮した結果、
 * リポジトリという概念は客観オブジェクトやノード別管理オブジェクトやファイルフォルダ等、
 * 様々なものの連携で実現されるという事になった。
 * 検索のためにリポジトリの一部メタデータは客観とし、
 * リポジトリ内のフォルダやファイルやgitローカルリポジトリや
 * それらに伴うTenyu基盤ソフトウェア内のオブジェクトはノード別管理とした。
 *
 * リポジトリという言葉は様々な場面で使用されるので混同を避けるため接頭辞にTenyuと入れた。
 * このあたりの文脈ではgitローカルリポジトリや、
 * Git型の{@link TenyuRepositoryI}である{@link TenyuGitRepositoryI}や、
 * ノード別管理となる{@link TenyutalkRepositoryI}もあるので区別する必要がある。
 * 接頭辞がTenyuだったら客観、Tenyutalkだったらノード別管理。
 *
 * 参照：tenyutalk.md
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyuRepositoryI extends IndividualityObjectI, HasSocialityI {

	@Override
	default int getNameMax() {
		return 30;//ファイルパスに入るから短め
	}

	/**
	 * @return	このリポジトリを管理しているノード
	 */
	NodeIdentifierUser getNodeIdentifier();

	/**
	 * @return	このリポジトリに関連したリポジトリ
	 */
	List<Long> getRelatedIds();

	/**
	 * @return	このリポジトリの種類
	 */
	TenyuRepositoryType getType();

	/**
	 * @return	このリポジトリを管理している{@link UserI}
	 */
	default User getUploaderUser() {
		return Glb.getObje().getUser(us -> us.get(getUploaderUserId()));
	}

	/**
	 * @return	このリポジトリを管理している{@link UserI}のID
	 */
	default Long getUploaderUserId() {
		return getRegistererUserId();
	}
}
