package bei7473p5254d69jcuat.tenyutalk.model.release1;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.release1.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import bei7473p5254d69jcuat.tenyutalk.file.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class TenyutalkArtifactByVersion implements TenyutalkArtifactByVersionI {
	public static int getHashSignMax(int activePublication) {
		return getReadSignMax(activePublication);
	}

	public static int getReadSignMax(int activePublication) {
		int r = 200;

		double rate = 18.0 * (activePublication / 100);
		r *= rate;
		if (r > 2000)
			r = 2000;

		return r;
	}

	private Long tenyuArtifactByVersionId;

	/**
	 * 成果物の公開直前にハッシュ値を近傍等に送信して電子署名してもらう。
	 */
	private HashSet<NominalSignature> hashSigns = new HashSet<>();

	/**
	 * 公開日時証明のための読み取った人達による電子署名一覧。
	 * ユーザーが直接成果物を見た場合に電子署名する。
	 */
	private HashSet<NominalSignature> readSigns = new HashSet<>();

	@Override
	public ObjectGui<?> getGuiReferenced(String guiName, String cssIdPrefix) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public HashSet<NominalSignature> getHashSigns() {
		return hashSigns;
	}

	public HashSet<NominalSignature> getReadSigns() {
		return readSigns;
	}

	private String getSignNominal(String nominal) {
		return Glb.getConf().getKeys().getSignNominal(
				TenyutalkArtifact.class.getSimpleName(), nominal,
				getTenyuArtifactByVersion().getNameByVersion());
	}

	/**
	 * @param artifactName
	 * @return	ハッシュ署名名目
	 */
	public String getSignNominalHash() {
		return getSignNominal("Hash");
	}

	/**
	 * @return	読み取り署名名目
	 */
	public String getSignNominalRead() {
		return getSignNominal("Read");
	}

	@Override
	public TenyutalkFileMetadataI getTenyutalkFileMetadataI() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public byte[] getUploaderSign() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public void setHashSigns(HashSet<NominalSignature> hashSigns) {
		this.hashSigns = hashSigns;
	}

	public void setReadSigns(HashSet<NominalSignature> readSigns) {
		this.readSigns = readSigns;
	}

	/**
	 * コンテンツ読み取り時に署名する
	 * @return	署名に成功したか
	 */
	public boolean signRead() {
		return Glb.getUtil().sign(sign -> readSigns.add(sign),
				() -> getSignNominalRead(),
				getTenyutalkFileMetadataI().getSignTargetHash());
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public Long getTenyuArtifactByVersionId() {
		return tenyuArtifactByVersionId;
	}

	public void setTenyuArtifactByVersionId(Long tenyuArtifactByVersionId) {
		this.tenyuArtifactByVersionId = tenyuArtifactByVersionId;
	}

}
