package bei7473p5254d69jcuat.tenyu.release1.ui.common;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import javafx.beans.property.*;

public class ObjectivityObjectTableItem<T1 extends ObjectivityObjectDBI,
		T2 extends T1> extends IdObjectTableItem<T1, T2> {
	public ObjectivityObjectTableItem(T2 src) {
		super(src);
		updateUpdateObjectivityObjectDBITableItem();
	}

	private StringProperty registererUserName = new SimpleStringProperty();

	public String getRegistererUserName() {
		return registererUserName.get();
	}

	@Override
	public T2 getSrc() {
		return src;
	}

	public void setRegistererUserName(Long registererUserId) {
		if (registererUserId == null) {
			return;
		}

		User registerer = Glb.getObje().getUser(us->us.get(registererUserId));
		String registererName = null;
		if (registerer == null) {
			registererName = "";
		} else {
			registererName = registerer.getName();
		}
		this.registererUserName.set(registererName);
	}

	@Override
	public void update() {
		super.update();
		updateUpdateObjectivityObjectDBITableItem();
	}

	public void updateUpdateObjectivityObjectDBITableItem() {
		if (src == null)
			return;
		setRegistererUserName(src.getRegistererUserId());
	}
}