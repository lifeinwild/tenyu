package bei7473p5254d69jcuat.tenyu.ui.common;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import glb.*;
import javafx.beans.property.*;

public abstract class IdObjectTableItem<T1 extends IdObjectDBI, T2 extends T1>
		implements TableRow<T2> {
	private LongProperty id = new SimpleLongProperty();
	protected T2 src;

	public IdObjectTableItem(T2 src) {
		this.src = src;
		updateIdObjectDBITableItem();
	}

	public Long getId() {
		return id.get();
	}

	@Override
	public T2 getSrc() {
		return src;
	}

	public void setId(Long id) {
		if (id == null)
			id = IdObjectDBI.getExceptionalId();
		this.id.set(id);
	}

	public void update() {
		updateIdObjectDBITableItem();
	}

	public void updateIdObjectDBITableItem() {
		if (src == null)
			return;
		setId(src.getId());
	}

}