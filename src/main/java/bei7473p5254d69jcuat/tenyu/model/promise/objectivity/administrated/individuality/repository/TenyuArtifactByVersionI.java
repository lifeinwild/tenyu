package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository;

import java.nio.*;

import com.github.zafarkhaja.semver.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyutalk.file.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import glb.*;
import jetbrains.exodus.env.*;

/**
 * 成果物バージョン別情報
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyuArtifactByVersionI
		extends AdministratedObjectI, NominalSignatureI {
	public static String createName(String filename, String semver) {
		return filename + "-" + semver;
	}

	/**
	 * @return	署名した日時
	 */
	long getSignDate();

	/**
	 * @return	この成果物のファイルまたはフォルダのハッシュ値
	 * フォルダの場合それ以下の全ファイルに依存する。ただしシンボリックリンクや隠しファイルを除く
	 */
	byte[] getFileHash();

	default TenyutalkFileMetadataI getFileMetadata() {
		return getFileMetadata(getTenyuArtifact());
	}

	default TenyutalkFileMetadataI getFileMetadata(TenyuArtifactI a) {
		return new TenyutalkArtifactByVersionFile(getId(), a.getName(),
				getFileHash(), getFileSize());
	}

	default TenyutalkFileMetadataI getFileMetadata(Transaction txn) {
		return getFileMetadata(getTenyuArtifact(txn));
	}

	/**
	 * @return	この成果物のファイルまたはフォルダのサイズ
	 * フォルダの場合それ以下の合計サイズただしシンボリックリンクや隠しファイルを除く
	 */
	long getFileSize();

	/**
	 * @return	semantec versioning
	 */
	String getSemVerStr();

	default Version getSemVer() {
		return Version.valueOf(getSemVerStr());
	}

	default String getNameByVersion() {
		return createName(getTenyuArtifact().getName(), getSemVerStr());
	}

	static String getSignNominalStatic() {
		return Glb.getConf().getKeys()
				.getSignNominal(TenyuArtifactByVersion.class.getSimpleName());

	}

	default String getSignNominal() {
		return getSignNominalStatic();
	}

	/**
	 * この署名は成果物とアップロード者の対応関係の堅牢性を{@link UserI}と同等にする。
	 *
	 * 客観は多数の攻撃者が結託して少数派ノードを攻撃すれば
	 * その少数派ノード限定で改ざんされるリスクがあり、
	 * 成果物のハッシュ値だけでは安全性が不十分と判断し、
	 * 作者署名をつける事にした。
	 *
	 * とはいえ、{@link UserI}の公開鍵情報を改ざんされたらこの署名情報は無意味なので、
	 * 現状少数派ノードの客観が改ざんされるリスクに対してこの署名情報はセキュリティを改善しないが、
	 * 潜在的には、Userの公開鍵情報をさらに堅牢にする新たなアイデアが見つかれば
	 * この署名は重要な意味を持つ。
	 * Userの情報は重要なのでそのようなアイデアをさらに模索する可能性が高い。
	 *
	 * @return	この成果物のファイルまたはフォルダのハッシュ値への電子署名
	 */
	byte[] getSign();

	/**
	 * override禁止
	 * 将来のバージョンに渡って署名対象データは同じであるべき
	 *
	 * @return	署名対象
	 */
	default byte[] getSignTargetOrig() {
		return getSignTargetOrig(getFileMetadata());
	}

	default byte[] getSignTargetOrig(TenyutalkFileMetadataI meta) {
		//filesizeを入れているのは、
		//客観を改ざんされてDL時に過剰に大きなサイズをDLさせられるリスクがあり、
		//確かにアップロード者（BANの責任を負う者）がそのサイズを指定したことを証明するため。
		ByteBuffer buf = ByteBuffer
				.allocate(meta.getFileHash().length + Long.BYTES * 2);
		buf.put(meta.getFileHash()).putLong(getTenyuArtifactId())
				.putLong(getFileSize());
		return buf.array();
	}

	default byte[] getSignTargetOrig(Transaction txn) {
		return getSignTargetOrig(getFileMetadata(txn));
	}

	default KeyType getKeyType() {
		return KeyType.PC;
	}

	default String getNominal() {
		return getSignNominal();
	}

	default Long getSignerUserId() {
		return getRegistererUserId();
	}

	default void setKeyType(KeyType type) {

	}

	default void setNominal(String nominal) {

	}

	default void setSignerUserId(Long signerUserId) {

	}

	/**
	 * @return	成果物
	 */
	TenyuArtifactI getTenyuArtifact();

	/**
	 * @return	成果物
	 */
	TenyuArtifactI getTenyuArtifact(Transaction txn);

	/**
	 * @return	成果物のID
	 */
	Long getTenyuArtifactId();

	default TenyuReferenceModelI<TenyutalkArtifactI> getTenyutalkReference() {
		return null;//TODO
	}
}
