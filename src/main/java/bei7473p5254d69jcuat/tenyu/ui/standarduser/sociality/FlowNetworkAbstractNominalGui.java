package bei7473p5254d69jcuat.tenyu.ui.standarduser.sociality;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.sociality.FlowNetworkAbstractNominalGui.*;
import glb.*;
import jetbrains.exodus.env.*;

public class FlowNetworkAbstractNominalGui extends
		IndividualityObjectGui<FlowNetworkAbstractNominalI,
				FlowNetworkAbstractNominal,
				FlowNetworkAbstractNominal,
				FlowNetworkAbstractNominalStore,
				FlowNetworkAbstractNominalGui,
				FlowNetworkAbstractNominalTableItem> {
	public FlowNetworkAbstractNominalGui(String name, String id) {
		super(name, id);
	}

	public static class FlowNetworkAbstractNominalTableItem
			extends IndividualityObjectTableItem<FlowNetworkAbstractNominalI,
					FlowNetworkAbstractNominal> {

		public FlowNetworkAbstractNominalTableItem(
				FlowNetworkAbstractNominal src) {
			super(src);
		}

	}

	@Override
	protected FlowNetworkAbstractNominalTableItem createTableItem(
			FlowNetworkAbstractNominal o) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public Lang getClassNameLang() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	protected FlowNetworkAbstractNominalGui createDetailGui() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public FlowNetworkAbstractNominalStore getStore(Transaction txn) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

}
