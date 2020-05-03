package bei7473p5254d69jcuat.tenyu.ui.other;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.other.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.other.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.other.UserMessageListHashGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class UserMessageListHashGui extends
		AdministratedObjectGui<UserMessageListHashI,
				UserMessageListHash,
				UserMessageListHash,
				UserMessageListHashStore,
				UserMessageListHashGui,
				UserMessageListHashTableItem> {
	public UserMessageListHashGui(String name, String id) {
		super(name, id);
	}

	public static class UserMessageListHashTableItem
			extends AdministratedObjectTableItem<UserMessageListHashI,
					UserMessageListHash> {

		public UserMessageListHashTableItem(UserMessageListHash src) {
			super(src);
		}

	}

	@Override
	protected UserMessageListHashTableItem createTableItem(
			UserMessageListHash o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected UserMessageListHashGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public UserMessageListHashStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
