package bei7473p5254d69jcuat.tenyu.ui.standarduser.vote;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.vote.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.vote.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.vote.DistributedVoteResultGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class DistributedVoteResultGui extends
		AdministratedObjectGui<DistributedVoteResultI,
				DistributedVoteResult,
				DistributedVoteResult,
				DistributedVoteResultStore,
				DistributedVoteResultGui,
				DistributedVoteResultTableItem> {
	public DistributedVoteResultGui(String name, String id) {
		super(name, id);
	}

	public static class DistributedVoteResultTableItem
			extends AdministratedObjectTableItem<DistributedVoteResultI,
					DistributedVoteResult> {

		public DistributedVoteResultTableItem(DistributedVoteResult src) {
			super(src);
		}

	}

	@Override
	protected DistributedVoteResultTableItem createTableItem(
			DistributedVoteResult o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected DistributedVoteResultGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public DistributedVoteResultStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
