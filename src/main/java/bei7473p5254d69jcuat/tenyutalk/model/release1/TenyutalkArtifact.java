package bei7473p5254d69jcuat.tenyutalk.model.release1;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class TenyutalkArtifact implements TenyutalkArtifactI {
	private Long tenyuArtifactId;

	@Override
	public Long getTenyuArtifactId() {
		return tenyuArtifactId;
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
	public TenyuArtifact getTenyuArtifact() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public ObjectGui<?> getGuiReferenced(String guiName, String cssIdPrefix) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
