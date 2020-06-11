package bei7473p5254d69jcuat.tenyutalk.model.release1;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.other.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public class TenyutalkAppletPlaying implements TenyutalkAppletPlayingI {
	/**
	 * テキストチャット
	 */
	private Chat chat = new Chat();

	/**
	 * ペイントツール
	 */
	private Paint paint = null;

	/**
	 * チャット可能か
	 */
	private boolean chatable = true;
	/**
	 * 停止中か
	 * 停止したら新たなチャット発言が不可能になる。
	 */
	private boolean stop = false;

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
	public Long getTenyuRepositoryId() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public TenyuArtifactI getTenyuArtifact() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public void stop() {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public long getLaunchDate() {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	@Override
	public boolean isService() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean isStartup() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public void start() {
		// TODO 自動生成されたメソッド・スタブ

	}

}
