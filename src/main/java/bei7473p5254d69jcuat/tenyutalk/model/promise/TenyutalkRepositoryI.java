package bei7473p5254d69jcuat.tenyutalk.model.promise;

import java.util.*;

import com.github.zafarkhaja.semver.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.model.release1.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import glb.*;

/**
 * 客観の{@link TenyuRepositoryI}に対応するノード別管理となる情報。
 * 誰かが制作物をTenyutalk上で公開した時作成される。
 * 制作物毎に{@link TenyutalkRepositoryI}が１つある。
 *
 * 同様に誰かの制作物をDLした時も作成される。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyutalkRepositoryI extends ValidatableI {

	/**
	 * オブジェクトに電子署名を追加する。
	 * addとしているがListを持つか１件のみとするかは実装クラスの自由。
	 *
	 * @param sign	電子署名
	 * @return	追加に成功したか
	 */
	boolean addReadSign(NominalSignature sign);

	/**
	 * @return	最新のバージョン
	 */
	Version getLatestVersion();

	/**
	 * @return	バージョンリスト
	 */
	List<Version> getAllVersions();

	/**
	 * @return	これが対応する客観上のリポジトリメタデータのID
	 */
	Long getTenyuRepositoryId();

	/**
	 * @return	これが対応する客観上のリポジトリメタデータ
	 */
	TenyuRepositoryI getTenyuRepository();

	/**
	 * @return	このリポジトリのリリースフォルダのミラーノード一覧
	 */
	Map<NodeIdentifierUser, List<Version>> getReleaseMirrors();

	/**
	 * @return	このリポジトリのベースフォルダ
	 */
	default String getTenyuRepositoryDir() {
		return Glb.getFile().getTenyutalkRepositoryDir(getTenyuRepository());
	}

	/**
	 * @return	このリポジトリのリリースフォルダ
	 */
	default String getTenyuRepositoryReleaseDir() {
		return Glb.getFile()
				.getTenyutalkRepositoryReleaseDir(getTenyuRepository());
	}

	/**
	 * @return	このリポジトリの作業フォルダ
	 * 作業フォルダはgitローカルリポジトリが設置される。
	 */
	default String getTenyuRepositoryWorkingDir() {
		return Glb.getFile()
				.getTenyutalkRepositoryWorkingDir(getTenyuRepository());
	}
}
