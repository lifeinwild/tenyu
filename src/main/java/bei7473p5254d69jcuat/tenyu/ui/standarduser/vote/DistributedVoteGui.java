package bei7473p5254d69jcuat.tenyu.ui.standarduser.vote;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.vote.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.vote.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.vote.DistributedVoteGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class DistributedVoteGui extends
		IndividualityObjectGui<DistributedVoteI,
				DistributedVote,
				DistributedVote,
				DistributedVoteStore,
				DistributedVoteGui,
				DistributedVoteTableItem> {
	public DistributedVoteGui(String name, String id) {
		super(name, id);
	}

	public static class DistributedVoteTableItem
			extends IndividualityObjectTableItem<DistributedVoteI, DistributedVote> {

		public DistributedVoteTableItem(DistributedVote src) {
			super(src);
		}

	}

	@Override
	protected DistributedVoteTableItem createTableItem(DistributedVote o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected DistributedVoteGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public DistributedVoteStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
