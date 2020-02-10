package bei7473p5254d69jcuat.tenyu.reference;

import bei7473p5254d69jcuat.tenyu.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.release1.*;
import glb.*;

/**
 * 参照
 *
 * ObjectStore前提
 * 常にどこかのObjectStoreから取得する
 *
 * 具象クラスで返値の型を具体的なものにする。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyuReference<V> extends HasGui, Storable {

	/**
	 * @return	参照の文字列表現時の区切り文字
	 */
	default String getDelimiter() {
		return "&";
	}


	/**
	 * local://tenyu.p2p/?rt=javaKryo
	 *
	 * localプロトコルは勝手に書いているだけで実在しない。
	 * localhost内のプロセス間の連携を実現するためのプロトコル。
	 * 第二レベルドメインの文字列をuser.homeから探して
	 * 対象とするlocalhostのプロセスを特定し、URLそのものを送信する。
	 * rt=return type	デフォルトでjavaKryo
	 *
	 * このURLの場合、
	 * user.homeのtenyuフォルダのtenyu.txtを読みポート番号を取得し
	 * URLを送信、返り値としてKryoでシリアライズされたJavaオブジェクトを受け取る。
	 *
	 * @return	文字列表現の接頭辞
	 */
	default String getPrefix() {
		return "local://tenyu.p2p/?";
	}

	/**
	 * @return	参照先オブジェクト
	 */
	V getObj();

}
