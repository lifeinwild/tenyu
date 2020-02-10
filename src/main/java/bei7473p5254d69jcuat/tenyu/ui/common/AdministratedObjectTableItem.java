package bei7473p5254d69jcuat.tenyu.ui.common;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;
import javafx.beans.property.*;

public class AdministratedObjectTableItem<T1 extends AdministratedObjectDBI,
		T2 extends T1> extends IdObjectTableItem<T1, T2> {
	public AdministratedObjectTableItem(T2 src) {
		super(src);
		updateUpdateAdministratedObjectDBITableItem();
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
		updateUpdateAdministratedObjectDBITableItem();
	}

	public void updateUpdateAdministratedObjectDBITableItem() {
		if (src == null)
			return;
		setRegistererUserName(src.getRegistererUserId());
	}
}